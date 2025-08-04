package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.PlanActionByIdDto;
import com.example.GestionPlanAction.dto.PlanActionResponse;
import com.example.GestionPlanAction.enums.StatutPlanAction;
import com.example.GestionPlanAction.model.Audit;
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

    @Autowired
    private ExerciceServiceImpl exerciceService;

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
        
        // Set created by name
        if (plan.getCreatedBy() != null) {
            User user = userRepository.findById(plan.getCreatedBy()).orElse(null);
            resp.setCreatedByName(user != null ? user.getNom() : null);
        }
        
        // Add audit logs for this plan action
        List<Audit> audits = auditService.getAuditsForPlan(id);
        resp.setAuditLogs(auditService.formatAuditsForDisplay(audits));
        
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
    public PlanActionByIdDto update(Long id, PlanAction updated) {
        PlanAction existing = getById(id);

        // Store original values for audit logging
        String oldTitle = existing.getTitre();
        String oldDescription = existing.getDescription();
        StatutPlanAction oldStatus = existing.getStatut();
        Long oldExerciceId = existing.getExercice() != null ? existing.getExercice().getId() : null;

        // --- Track VariableAction changes ---
        List<VariableAction> oldVarActions = new ArrayList<>(existing.getVariableActions());
        List<VariableAction> newVarActions = updated.getVariableActions() != null ? updated.getVariableActions() : new ArrayList<>();

        // Build maps for easy lookup
        java.util.Map<Long, VariableAction> oldMap = new java.util.HashMap<>();
        for (VariableAction va : oldVarActions) {
            if (va.getId() != null) oldMap.put(va.getId(), va);
        }
        java.util.Map<Long, VariableAction> newMap = new java.util.HashMap<>();
        for (VariableAction va : newVarActions) {
            if (va.getId() != null) newMap.put(va.getId(), va);
        }

        // Detect removed
        List<VariableAction> removed = new ArrayList<>();
        for (VariableAction va : oldVarActions) {
            if (va.getId() != null && !newMap.containsKey(va.getId())) {
                removed.add(va);
            }
        }
        // Detect added
        List<VariableAction> added = new ArrayList<>();
        for (VariableAction va : newVarActions) {
            if (va.getId() == null || !oldMap.containsKey(va.getId())) {
                added.add(va);
            }
        }
        // Detect updated
        List<VariableAction> updatedVars = new ArrayList<>();
        for (VariableAction va : newVarActions) {
            if (va.getId() != null && oldMap.containsKey(va.getId())) {
                VariableAction oldVa = oldMap.get(va.getId());
                if (!java.util.Objects.equals(va.getDescription(), oldVa.getDescription()) ||
                    va.getPoids() != oldVa.getPoids() ||
                    va.isFige() != oldVa.isFige() ||
                    va.getNiveau() != oldVa.getNiveau() ||
                    !java.util.Objects.equals(va.getResponsable(), oldVa.getResponsable()) ||
                    !java.util.Objects.equals(va.getVaMere(), oldVa.getVaMere())) {
                    updatedVars.add(va);
                }
            }
        }

        // Update fields
        existing.setTitre(updated.getTitre());
        existing.setDescription(updated.getDescription());
        existing.setStatut(updated.getStatut());
        existing.setExercice(updated.getExercice());

        // Collect incoming IDs
        List<Long> incomingIds = new ArrayList<>();
        for (VariableAction va : newVarActions) {
            if (va.getId() != null) {
                incomingIds.add(va.getId());
            }
        }

        // Remove VariableActions not present in the update
        existing.getVariableActions().removeIf(eva -> eva.getId() != null && !incomingIds.contains(eva.getId()));

        // Update or add VariableActions
        for (VariableAction va : newVarActions) {
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

        PlanAction savedPlan = repository.save(existing);

        // Log the update action
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            StringBuilder changeDetails = new StringBuilder("Updated plan: ");

            if (!oldTitle.equals(updated.getTitre())) {
                changeDetails.append("title changed from \"")
                    .append(oldTitle).append("\" to \"")
                    .append(updated.getTitre()).append("\"; ");
            }

            if (!oldDescription.equals(updated.getDescription())) {
                changeDetails.append("description changed from \"")
                    .append(oldDescription).append("\" to \"")
                    .append(updated.getDescription()).append("\"; ");
            }

            if (oldStatus != updated.getStatut()) {
                changeDetails.append("status changed from '")
                    .append(oldStatus).append("' to '")
                    .append(updated.getStatut()).append("'; ");
            }

            // Log exercice year change using ExerciceServiceImpl
            // Long oldExerciceId = _oldExerciceId.getExercice() != null ? existing.getExercice().getId() : null;
            Long newExerciceId = updated.getExercice() != null ? updated.getExercice().getId() : null;
            if (!java.util.Objects.equals(oldExerciceId, newExerciceId)) {
                if (oldExerciceId != null && newExerciceId != null) {
                    Integer oldYear = exerciceService.getExerciceYearById(oldExerciceId);
                    Integer newYear = exerciceService.getExerciceYearById(newExerciceId);
                    changeDetails.append("exercice changed from \"")
                        .append(oldYear != null ? oldYear : "N/A").append("\" to \"")
                        .append(newYear != null ? newYear : "N/A").append("\"; ");
                } else if (oldExerciceId == null && newExerciceId != null) {
                    Integer newYear = exerciceService.getExerciceYearById(newExerciceId);
                    changeDetails.append("exercice set to \"")
                        .append(newYear != null ? newYear : "N/A").append("\"; ");
                } else if (oldExerciceId != null && newExerciceId == null) {
                    Integer oldYear = exerciceService.getExerciceYearById(oldExerciceId);
                    changeDetails.append("exercice removed (was \"")
                        .append(oldYear != null ? oldYear : "N/A").append("\"); ");
                }
            }

            // Log VariableAction changes only if there are any
            if (!added.isEmpty()) {
                changeDetails.append("Added VariableActions: ");
                for (VariableAction va : added) {
                    changeDetails.append("\"").append(va.getDescription()).append("\"; ");
                    // Create separate audit entry for each added variable
                    auditService.logAction("variableaction_added", currentUser, 
                        "Added new variable action: \"" + va.getDescription() + "\"", 
                        "VariableAction", va.getId());
                }
            }

            if (!removed.isEmpty()) {
                changeDetails.append("Removed VariableActions: ");
                for (VariableAction va : removed) {
                    changeDetails.append("\"").append(va.getDescription()).append("\"; ");
                    // Create separate audit entry for each removed variable
                    auditService.logAction("variableaction_removed", currentUser, 
                        "Removed variable action: \"" + va.getDescription() + "\"", 
                        "VariableAction", va.getId());
                }
            }

            // if (!updatedVars.isEmpty()) {
            //     changeDetails.append("Updated VariableActions: ");
            //     for (VariableAction va : updatedVars) {
            //         changeDetails.append("\"").append(va.getDescription()).append("\": ")
            //        .append(va.getChangeLog()).append("; ");
            //         // Create separate audit entry for each updated variable
            //         auditService.logAction("variableaction_updated", currentUser, 
            //             "Updated variable action \"" + va.getDescription() + "\": " + va.getChangeLog(), 
            //             "VariableAction", va.getId());
            //     }
            // }

            auditService.logPlanAction("updated", currentUser, savedPlan, changeDetails.toString());
        }

        // Convert to DTO with additional details
        PlanActionByIdDto resp = new PlanActionByIdDto();
        BeanUtils.copyProperties(savedPlan, resp);

        // Set created by name
        if (savedPlan.getCreatedBy() != null) {
            User user = userRepository.findById(savedPlan.getCreatedBy()).orElse(null);
            resp.setCreatedByName(user != null ? user.getNom() : null);
        }

        // Add audit logs for this plan action
        List<Audit> audits = auditService.getAuditsForPlan(id);
        resp.setAuditLogs(auditService.formatAuditsForDisplay(audits));

        return resp;
    }

    @Override
    public PlanActionByIdDto updateStatus(Long id, String status) {
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
        
        // Convert to DTO with additional details
        PlanActionByIdDto resp = new PlanActionByIdDto();
        BeanUtils.copyProperties(updatedPlan, resp);
        
        // Set created by name
        if (updatedPlan.getCreatedBy() != null) {
            User user = userRepository.findById(updatedPlan.getCreatedBy()).orElse(null);
            resp.setCreatedByName(user != null ? user.getNom() : null);
        }
        
        // Add audit logs for this plan action
        List<Audit> audits = auditService.getAuditsForPlan(id);
        resp.setAuditLogs(auditService.formatAuditsForDisplay(audits));
        
        return resp;
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

    @Override
    public String getPlanActionTitleById(Long id) {
        PlanAction plan = getById(id);
        return plan != null ? plan.getTitre() : "unassigned";
    }
}
