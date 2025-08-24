package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.PlanActionByIdDto;
import com.example.GestionPlanAction.dto.PlanActionResponse;
import com.example.GestionPlanAction.dto.PlanActionTreeResponse;
import com.example.GestionPlanAction.dto.PlanActionDTOs.PlanActionCreateDTO;
import com.example.GestionPlanAction.dto.variableActionDTOs.VariableActionDetailDTO;
import com.example.GestionPlanAction.enums.StatutPlanAction;
import com.example.GestionPlanAction.model.Audit;
import com.example.GestionPlanAction.model.Exercice;
import com.example.GestionPlanAction.model.PlanAction;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.model.VariableAction;
import com.example.GestionPlanAction.repository.ExerciceRepository;
import com.example.GestionPlanAction.repository.PlanActionRepository;
import com.example.GestionPlanAction.repository.UserRepository;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.GestionPlanAction.security.SecurityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PlanActionServiceImpl implements PlanActionService {

    @Autowired
    private PlanActionRepository repository;

    @Autowired
    private ExerciceRepository exerciceRepository;
    
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
        
        // Copy basic properties
        resp.setId(plan.getId());
        resp.setTitre(plan.getTitre());
        resp.setDescription(plan.getDescription());
        resp.setStatut(plan.getStatut());
        resp.setVerrouille(plan.isVerrouille());
        resp.setExercice(plan.getExercice());
        resp.setCreatedBy(plan.getCreatedBy());
        
        // Set created by name
        if (plan.getCreatedBy() != null) {
            User user = userRepository.findById(plan.getCreatedBy()).orElse(null);
            resp.setCreatedByName(user != null ? user.getNom() : null);
        }
        
        // Convert VariableActions to DTOs
        if (plan.getVariableActions() != null) {
            List<VariableActionDetailDTO> vaDetailDTOs = new ArrayList<>();
            
            // First, find only root-level variable actions
            List<VariableAction> rootVAs = plan.getVariableActions().stream()
                    .collect(java.util.stream.Collectors.toList());
            
            // Convert each root VA and its children recursively
            for (VariableAction rootVA : rootVAs) {
                vaDetailDTOs.add(convertToVariableActionDTO(rootVA));
            }
            
            resp.setVariableActions(vaDetailDTOs);
        }
        
        // Add audit logs for this plan action
        List<Audit> audits = auditService.getAuditsForPlan(id);
        resp.setAuditLogs(auditService.formatAuditsForDisplay(audits));
        
        return resp;
    }

    // Helper method to convert VariableAction to DTO recursively
    private VariableActionDetailDTO convertToVariableActionDTO(VariableAction va) {
        if (va == null) return null;
        
        VariableActionDetailDTO dto = new VariableActionDetailDTO();
        dto.setId(va.getId());
        dto.setDescription(va.getDescription());
        dto.setCode(va.getCode());
        dto.setPoids(va.getPoids());
        dto.setFige(va.isFige());
        dto.setNiveau(va.getNiveau());
        dto.setOrdre(va.getOrdre());
        
        // Set responsable details
        if (va.getResponsable() != null) {
            dto.setResponsableId(va.getResponsable().getId());
            dto.setResponsableNom(va.getResponsable().getNom());
            dto.setResponsablePrenom(va.getResponsable().getPrenom());
        }
        
        // Set parent VA ID
        if (va.getVaMere() != null) {
            dto.setVaMereId(va.getVaMere().getId());
        }
        
        return dto;
    }

    @Override
    public PlanAction create(PlanActionCreateDTO planActionDTO) {
        PlanAction planAction = new PlanAction();
        
        // Copy basic properties
        planAction.setTitre(planActionDTO.getTitre());
        planAction.setDescription(planActionDTO.getDescription());
        planAction.setVerrouille(planActionDTO.isVerrouille());
        
        // Set status with fallback
        if (planActionDTO.getStatut() != null) {
            planAction.setStatut(planActionDTO.getStatut());
        } else {
            planAction.setStatut(StatutPlanAction.EN_COURS_PLANIFICATION);
        }
        
        // Set exercice by ID
        if (planActionDTO.getExerciceId() != null) {
            Exercice exercice = exerciceRepository.findById(planActionDTO.getExerciceId())
                    .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));
            planAction.setExercice(exercice);
            
            // Generate automatic title based on exercise year if not provided
            // if (planAction.getTitre() == null || planAction.getTitre().isEmpty()) {
            Integer exerciceYear = exerciceService.getExerciceYearById(exercice.getId());
            if (exerciceYear != null) {
                planAction.setTitre("PA-" + exerciceYear);
            }
            // }
        }

        // Set createdBy from security context
        Long currentUserId = SecurityUtils.getCurrentUserId();
        planAction.setCreatedBy(currentUserId);

        // Initialize empty variable actions list
        planAction.setVariableActions(new ArrayList<>());
        
        // Save the plan first without variable actions
        PlanAction savedPlan = repository.save(planAction);
        
        // Log the creation action
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            auditService.logPlanAction("created", currentUser, savedPlan, "Initial plan creation");
        }
        
        return savedPlan;
    }

    @Override
    public PlanActionByIdDto update(Long id, PlanActionCreateDTO updated) {
        PlanAction existing = getById(id);
        
        String oldTitle = existing.getTitre();
        String oldDescription = existing.getDescription();
        StatutPlanAction oldStatus = existing.getStatut();
        boolean oldVerrouille = existing.isVerrouille();
        Long oldExerciceId = existing.getExercice() != null ? existing.getExercice().getId() : null;

        existing.setTitre(updated.getTitre());
        existing.setDescription(updated.getDescription());
        existing.setStatut(updated.getStatut());
        existing.setVerrouille(updated.isVerrouille());
        // Set exercice by ID
        if (updated.getExerciceId() != null) {
            Exercice exercice = exerciceRepository.findById(updated.getExerciceId())
                    .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));
            existing.setExercice(exercice);

            // Generate automatic title based on exercise year if not provided
            // if (planAction.getTitre() == null || planAction.getTitre().isEmpty()) {
            Integer exerciceYear = exerciceService.getExerciceYearById(exercice.getId());
            if (exerciceYear != null) {
                existing.setTitre("PA-" + exerciceYear);
            }
            // }
        }

        PlanAction savedPlan = repository.save(existing);

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

            if (oldVerrouille != updated.isVerrouille()) {
                changeDetails.append("verrouille changed from '")
                    .append(oldVerrouille).append("' to '")
                    .append(updated.isVerrouille()).append("'; ");
            }

            // Log exercice year change using ExerciceServiceImpl
            // Long oldExerciceId = _oldExerciceId.getExercice() != null ? existing.getExercice().getId() : null;
            Long newExerciceId = updated.getExerciceId() != null ? updated.getExerciceId() : null;
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

    public List<PlanActionTreeResponse> getPlanActionsTree() {
        List<PlanAction> plans = repository.findAll();
        List<PlanActionTreeResponse> result = new ArrayList<>();
        for (PlanAction plan : plans) {
            PlanActionTreeResponse planNode = new PlanActionTreeResponse();
            planNode.setName(plan.getTitre());
            planNode.setId(plan.getId());
            planNode.setPlanActionId(plan.getId());
            planNode.setNodeType("PLAN_ACTION"); 
            planNode.setDescription(plan.getDescription());
            // Only pass top-level VariableActions (vaMere == null)
            planNode.setChildren(buildVariableTree(
                plan.getVariableActions() == null ? null :
                plan.getVariableActions().stream()
                    .filter(va -> va.getVaMere() == null)
                    .toList()
            ));
            result.add(planNode);
        }
        return result;
    }

    private List<PlanActionTreeResponse> buildVariableTree(List<VariableAction> variables) {
        if (variables == null) return new ArrayList<>();
        List<PlanActionTreeResponse> children = new ArrayList<>();
        for (VariableAction va : variables) {
            PlanActionTreeResponse node = new PlanActionTreeResponse();
            node.setName(va.getCode()); // or va.getCode() if you want code
            node.setNodeType("VARIABLE_ACTION");
            node.setPoids(va.getPoids());
            node.setFige(va.isFige());
            node.setId(va.getId());
            node.setPlanActionId(va.getPlanAction() != null ? va.getPlanAction().getId() : null);
            node.setDescription(va.getDescription());
            node.setChildren(buildVariableTree(va.getSousVAs()));
            node.setOwner(Objects.equals(SecurityUtils.getCurrentUserId(), va.getCreatedBy()));
            children.add(node);
        }
        return children;
    }

    /**
     * Recursively assigns hierarchical codes and levels to VariableActions.
     * Each code follows the pattern: VA1, VA11, VA12, VA111, etc.
     * Level is derived from the number of digits after "VA".
     * @param variableActions List of VariableActions (top-level or children)
     * @param parentCode The code of the parent VA (null for top-level)
     */
    public void assignCodesAndLevels(List<VariableAction> variableActions, String parentCode) {
        if (variableActions == null) return;
        int counter = 1;
        for (VariableAction va : variableActions) {
            String code = (parentCode == null) ? "VA" + counter : parentCode + counter;
            va.setDescription(code); // If you want a dedicated code field, add it to VariableAction
            va.setNiveau(code.substring(2).length()); // Level = number of digits after "VA"
            counter++;
            // Recursively assign for children
            assignCodesAndLevels(va.getSousVAs(), code);
        }
    }

    @Override
    public String getPlanActionTitleById(Long id) {
        PlanAction plan = getById(id);
        return plan != null ? plan.getTitre() : "unassigned";
    }
}
