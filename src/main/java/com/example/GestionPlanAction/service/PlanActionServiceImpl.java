package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.PlanActionByIdDto;
import com.example.GestionPlanAction.dto.PlanActionResponse;
import com.example.GestionPlanAction.enums.StatutPlanAction;
import com.example.GestionPlanAction.model.PlanAction;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.model.VariableAction;
import com.example.GestionPlanAction.repository.PlanActionRepository;
import com.example.GestionPlanAction.repository.UserRepository;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.GestionPlanAction.security.SecurityUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlanActionServiceImpl implements PlanActionService {

    @Autowired
    private PlanActionRepository repository;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<PlanAction> getAll() {
        return repository.findAll();
    }

    @Override
    public PlanAction getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PlanAction non trouvé"));
    }

    @Override
    public PlanActionByIdDto getByIdWithDetails(Long id) {
        PlanAction plan = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PlanAction non trouvé"));
        PlanActionByIdDto resp = new PlanActionByIdDto();
        BeanUtils.copyProperties(plan, resp);
        if (plan.getCreatedBy() != null) {
            User user = userRepository.findById(plan.getCreatedBy()).orElse(null);
            resp.setCreatedByName(user != null ? user.getNom() : null);
        }
        return resp;
    }

    @Override
    public PlanAction create(PlanAction planAction) {
        if (planAction.getStatut() == null) {
            planAction.setStatut(StatutPlanAction.EN_COURS_PLANIFICATION);
        }
        // Set createdBy from security context
        Long currentUserId = SecurityUtils.getCurrentUserId();
        planAction.setCreatedBy(currentUserId);

        if (planAction.getVariableActions() != null) {
            for (var va : planAction.getVariableActions()) {
                va.setPlanAction(planAction); // Set parent reference
            }
        }
        
        PlanAction savedPlan = repository.save(planAction);
        
        // Log the creation action
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            auditService.logPlanAction("created", currentUser, savedPlan, "Initial plan creation");
        }
        
        return savedPlan;
    }

    @Override
    public PlanAction update(Long id, PlanAction updated) {
        PlanAction existing = getById(id);
        existing.setTitre(updated.getTitre());
        existing.setDescription(updated.getDescription());
        existing.setStatut(updated.getStatut());
        existing.setExercice(updated.getExercice());

        // Collect incoming IDs
        List<Long> incomingIds = new ArrayList<>();
        if (updated.getVariableActions() != null) {
            for (VariableAction va : updated.getVariableActions()) {
                if (va.getId() != null) {
                    incomingIds.add(va.getId());
                }
            }
        }

        // Remove VariableActions not present in the update
        existing.getVariableActions().removeIf(eva -> eva.getId() != null && !incomingIds.contains(eva.getId()));

        // Update or add VariableActions
        if (updated.getVariableActions() != null) {
            for (VariableAction va : updated.getVariableActions()) {
                va.setPlanAction(existing);
                if (va.getId() != null) {
                    VariableAction existingVa = existing.getVariableActions().stream()
                        .filter(eva -> eva.getId().equals(va.getId()))
                        .findFirst()
                        .orElse(null);
                    if (existingVa != null) {
                        existingVa.setDescription(va.getDescription());
                        existingVa.setPoids(va.getPoids());
                        existingVa.setFige(va.isFige());
                        existingVa.setNiveau(va.getNiveau());
                        existingVa.setResponsable(va.getResponsable());
                        existingVa.setVaMere(va.getVaMere());
                    } else {
                        existing.getVariableActions().add(va);
                    }
                } else {
                    existing.getVariableActions().add(va);
                }
            }
        }

        return repository.save(existing);
    }

    public PlanAction updateStatus(Long id, String status) {
        PlanAction plan = getById(id);
        String oldStatus = plan.getStatut().toString();
        plan.setStatut(StatutPlanAction.valueOf(status));
        PlanAction updatedPlan = repository.save(plan);
        
        // Log the status change
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            auditService.logStatusChange(currentUser, updatedPlan, oldStatus, status);
        }
        
        return updatedPlan;
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<PlanActionResponse> getAllWithCreatedByName() {
        List<PlanAction> plans = repository.findAll();
        List<PlanActionResponse> responses = new ArrayList<>();
        for (PlanAction plan : plans) {
            PlanActionResponse resp = new PlanActionResponse();
            BeanUtils.copyProperties(plan, resp);
            if (plan.getCreatedBy() != null) {
                User user = userRepository.findById(plan.getCreatedBy()).orElse(null);
                resp.setCreatedByName(user != null ? user.getNom() : null);
            }
            responses.add(resp);
        }
        return responses;
    }
}
