package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.VariableActionDTO;
import com.example.GestionPlanAction.dto.VariableActionResponseDTO;
import com.example.GestionPlanAction.dto.VariableReponseDTO;
import com.example.GestionPlanAction.model.Audit;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.model.VariableAction;
import com.example.GestionPlanAction.repository.UserRepository;
import com.example.GestionPlanAction.repository.VariableActionRepository;
import com.example.GestionPlanAction.security.SecurityUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class VariableActionService {

    @Autowired
    private VariableActionRepository variableActionRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlanActionServiceImpl planActionService;

    @Autowired
    private UserServiceImpl userService;

    // ✅ Récupérer toutes les variables d'action
    public List<VariableAction> getAllVariableActions() {
        return variableActionRepository.findAll();
    }
    
    public VariableAction getVariableActionById(Long id) {
        return variableActionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variable d'action non trouvée avec l'ID : " + id));
    }
    
    public VariableActionResponseDTO getVariableActionWithAudits(Long id) {
        VariableAction variableAction = getVariableActionById(id);
        VariableActionResponseDTO responseDTO = new VariableActionResponseDTO(variableAction);
        
        // Add audit logs for this variable action
        List<Audit> audits = auditService.getAuditsForEntity("VariableAction", id);
        responseDTO.setAuditLogs(auditService.formatAuditsForDisplay(audits));
        
        return responseDTO;
    }
    
    public VariableReponseDTO getVariableActionEditById(Long id) {
        VariableAction dto =  variableActionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variable d'action non trouvée avec l'ID : " + id));
        VariableReponseDTO response = new VariableReponseDTO();
        response.setId(dto.getId());
        response.setDescription(dto.getDescription());
        response.setPoids(dto.getPoids());
        response.setFige(dto.isFige());
        response.setNiveau(dto.getNiveau());
        response.setPlanActionNom(dto.getPlanAction() != null ? dto.getPlanAction().getTitre() : null);
        response.setPlan_action_id(dto.getPlanAction() != null ? dto.getPlanAction().getId() : null);
        response.setResponsableNom(dto.getResponsable() != null ? dto.getResponsable().getNom() : null);
        response.setResponsablePrenom(dto.getResponsable() != null ? dto.getResponsable().getPrenom() : null);
        response.setResponsable_id(dto.getResponsable() != null ? dto.getResponsable().getId() : null);
        return response;
    }
    
    public List<VariableActionDTO> getAllVariableActionDTOs() {
        List<VariableAction> entities = variableActionRepository.findAll();
        List<VariableActionDTO> dtos = new ArrayList<>();
        for (VariableAction va : entities) {
            VariableActionDTO dto = new VariableActionDTO();
            dto.setId(va.getId());
            dto.setDescription(va.getDescription());
            dto.setPoids(va.getPoids());
            dto.setFige(va.isFige());
            dto.setNiveau(va.getNiveau());
            dto.setPlanActionNom(va.getPlanAction() != null ? va.getPlanAction().getTitre() : null);
            dto.setPlanActionId(va.getPlanAction() != null ? va.getPlanAction().getId() : null);
            dto.setResponsableNom(va.getResponsable() != null ? va.getResponsable().getNom() : null);
            dto.setResponsablePrenom(va.getResponsable() != null ? va.getResponsable().getPrenom() : null);
            dto.setResponsableId(va.getResponsable() != null ? va.getResponsable().getId() : null);
            dtos.add(dto);
        }
        return dtos;
    }

    // ✅ Créer une nouvelle variable d'action
    public VariableAction createVariableAction(VariableAction variableAction) {
        VariableAction savedVA = variableActionRepository.save(variableAction);
        
        // Log the creation action
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            String details = String.format("Created variable action: \"%s\"", savedVA.getDescription());
            if (savedVA.getPlanAction() != null) {
                details += String.format(" for plan \"%s\"", savedVA.getPlanAction().getTitre());
            }
            if (savedVA.getResponsable() != null) {
                details += String.format(" with responsible: %s %s", 
                    savedVA.getResponsable().getNom(),
                    savedVA.getResponsable().getPrenom());
            }
            
            auditService.logAction("variableaction_created", currentUser, details, "VariableAction", savedVA.getId());
        }
        
        return savedVA;
    }

    // ✅ Mettre à jour une variable d'action existante
    public VariableAction updateVariableAction(Long id, VariableAction updated) {
        VariableAction existing = getVariableActionById(id);

        // Store old values for audit log
        String oldDescription = existing.getDescription();
        float oldPoids = existing.getPoids();
        boolean oldFige = existing.isFige();
        int oldNiveau = existing.getNiveau();

        // Store old responsible info using UserServiceImpl
        String oldResponsableNom = existing.getResponsable() != null && existing.getResponsable().getId() != null ?
            userService.getUserFullNameById(existing.getResponsable().getId()) : "unassigned";

        // Store old plan action info using PlanActionServiceImpl
        String oldPlanActionTitle = existing.getPlanAction() != null && existing.getPlanAction().getId() != null ?
            planActionService.getPlanActionTitleById(existing.getPlanAction().getId()) : "unassigned";

        Long oldVaMereId = existing.getVaMere() != null ? existing.getVaMere().getId() : null;

        // Create a copy of existing for comparison
        VariableAction beforeUpdate = new VariableAction();
        beforeUpdate.setResponsable(existing.getResponsable());
        beforeUpdate.setPlanAction(existing.getPlanAction());

        // Update fields while maintaining existing references if not changed
        existing.setDescription(updated.getDescription());
        existing.setPoids(updated.getPoids());
        existing.setFige(updated.isFige());
        existing.setNiveau(updated.getNiveau());
        existing.setVaMere(updated.getVaMere());

        // Only update if explicitly changed
        if (updated.getResponsable() != null || updated.getResponsable() != beforeUpdate.getResponsable()) {
            existing.setResponsable(updated.getResponsable());
        }
        if (updated.getPlanAction() != null || updated.getPlanAction() != beforeUpdate.getPlanAction()) {
            existing.setPlanAction(updated.getPlanAction());
        }

        VariableAction savedVA = variableActionRepository.save(existing);

        // Log the update action
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            StringBuilder details = new StringBuilder("Updated variable action: ");

            if (!oldDescription.equals(savedVA.getDescription())) {
                details.append("description changed from \"")
                      .append(oldDescription)
                      .append("\" to \"")
                      .append(savedVA.getDescription())
                      .append("\"; ");
            }

            if (oldPoids != savedVA.getPoids()) {
                details.append("weight changed from ")
                      .append(oldPoids)
                      .append(" to ")
                      .append(savedVA.getPoids())
                      .append("; ");
            }

            if (oldFige != savedVA.isFige()) {
                details.append("fixed status changed from ")
                      .append(oldFige ? "fixed" : "not fixed")
                      .append(" to ")
                      .append(savedVA.isFige() ? "fixed" : "not fixed")
                      .append("; ");
            }

            if (oldNiveau != savedVA.getNiveau()) {
                details.append("level changed from ")
                      .append(oldNiveau)
                      .append(" to ")
                      .append(savedVA.getNiveau())
                      .append("; ");
            }

            // Check for responsible changes using UserServiceImpl
            String newResponsableNom = savedVA.getResponsable() != null && savedVA.getResponsable().getId() != null ?
                userService.getUserFullNameById(savedVA.getResponsable().getId()) : "unassigned";
            if (!Objects.equals(oldResponsableNom, newResponsableNom) &&
                (beforeUpdate.getResponsable() != savedVA.getResponsable())) {
                details.append("responsible changed from \"")
                      .append(oldResponsableNom)
                      .append("\" to \"")
                      .append(newResponsableNom)
                      .append("\"; ");
            }

            // Check for plan action changes using PlanActionServiceImpl
            String newPlanActionTitle = savedVA.getPlanAction() != null && savedVA.getPlanAction().getId() != null ?
                planActionService.getPlanActionTitleById(savedVA.getPlanAction().getId()) : "unassigned";
            if (!Objects.equals(oldPlanActionTitle, newPlanActionTitle) &&
                (beforeUpdate.getPlanAction() != savedVA.getPlanAction())) {
                details.append("plan action changed from \"")
                      .append(oldPlanActionTitle)
                      .append("\" to \"")
                      .append(newPlanActionTitle)
                      .append("\"; ");
            }

            auditService.logAction("variableaction_updated", currentUser, details.toString(), "VariableAction", savedVA.getId());
        }

        return savedVA;
    }

    // ✅ Supprimer une variable d'action par ID
    public void deleteVariableAction(Long id) {
        // Get the variable action before deleting for audit log
        VariableAction variableAction = getVariableActionById(id);
        
        // Delete the variable action
        variableActionRepository.deleteById(id);
        
        // Log the deletion
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            String details = String.format("Deleted variable action: \"%s\"", variableAction.getDescription());
            auditService.logAction("variableaction_deleted", currentUser, details, "VariableAction", id);
        }
    }

    // ✅ Mettre à jour l'état "fige" d'une variable d'action
    public VariableAction updateFige(Long id, boolean fige) {
        VariableAction existing = getVariableActionById(id);
        boolean oldFige = existing.isFige();
        existing.setFige(fige);
        
        VariableAction savedVA = variableActionRepository.save(existing);
        
        // Log the status change
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            String details = String.format("Changed fixed status for \"%s\" from %s to %s", 
                existing.getDescription(),
                oldFige ? "fixed" : "not fixed",
                fige ? "fixed" : "not fixed");
            
            auditService.logAction("variableaction_fige_updated", currentUser, details, "VariableAction", savedVA.getId());
        }
        
        return savedVA;
    }
}
