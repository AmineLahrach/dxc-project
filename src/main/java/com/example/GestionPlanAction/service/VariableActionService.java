package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.VariableActionCreateDTO;
import com.example.GestionPlanAction.dto.VariableActionDTO;
import com.example.GestionPlanAction.dto.VariableActionDropdownDTO;
import com.example.GestionPlanAction.dto.VariableActionHierarchyDTO;
import com.example.GestionPlanAction.dto.VariableActionResponseDTO;
import com.example.GestionPlanAction.dto.VariableActionUpdateDTO;
import com.example.GestionPlanAction.dto.VariableReponseDTO;
import com.example.GestionPlanAction.model.Audit;
import com.example.GestionPlanAction.model.PlanAction;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.model.VariableAction;
import com.example.GestionPlanAction.repository.UserRepository;
import com.example.GestionPlanAction.repository.VariableActionRepository;
import com.example.GestionPlanAction.security.SecurityUtils;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Autowired
    private NotificationService notificationService;

    // ✅ Récupérer toutes les variables d'action avec poids calculé dynamiquement
    public List<VariableAction> getAllVariableActions() {
        List<VariableAction> allVariableActions = variableActionRepository.findAll();
        
        // Group variable actions by parent (or root level) to calculate weights
        Map<Long, List<VariableAction>> childrenByParentId = new HashMap<>();
        List<VariableAction> rootItems = new ArrayList<>();
        
        // Organize items by their parent
        for (VariableAction va : allVariableActions) {
            if (va.getVaMere() == null) {
                rootItems.add(va);
            } else {
                Long parentId = va.getVaMere().getId();
                childrenByParentId.computeIfAbsent(parentId, k -> new ArrayList<>()).add(va);
            }
        }
        
        // Calculate weights for root items
        if (!rootItems.isEmpty()) {
            float rootWeightPerItem = 100.0f / rootItems.size();
            for (VariableAction rootItem : rootItems) {
                rootItem.setPoids(rootWeightPerItem);
            }
        }
        
        // Calculate weights for each group of siblings
        for (Map.Entry<Long, List<VariableAction>> entry : childrenByParentId.entrySet()) {
            List<VariableAction> siblings = entry.getValue();
            if (!siblings.isEmpty()) {
                float weightPerItem = 100.0f / siblings.size();
                for (VariableAction sibling : siblings) {
                    sibling.setPoids(weightPerItem);
                }
            }
        }
        
        return allVariableActions;
    }
    
    public VariableAction getVariableActionById(Long id) {
        VariableAction variableAction = variableActionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variable d'action non trouvée avec l'ID : " + id));
                
        // Calculate and set the weight based on siblings
        if (variableAction.getVaMere() != null) {
            // Get siblings (including self) for this parent
            List<VariableAction> siblings = variableAction.getVaMere().getSousVAs();
            if (siblings != null && !siblings.isEmpty()) {
                float weightPerItem = 100.0f / siblings.size();
                variableAction.setPoids(weightPerItem);
            }
        } else {
            // This is a root level item - find other root items for the same plan
            List<VariableAction> rootItems = variableActionRepository.findByPlanActionAndVaMereIsNull(
                    variableAction.getPlanAction());
            if (rootItems != null && !rootItems.isEmpty()) {
                float weightPerItem = 100.0f / rootItems.size();
                variableAction.setPoids(weightPerItem);
            }
        }
        
        return variableAction;
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
            dto.setCode(va.getCode());
            dto.setOrdre(va.getOrdre());
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

    private void logCreationAction(VariableAction savedVA){
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
    }

    // Original method signature for backward compatibility
    @Transactional
    public VariableAction createVariableAction(VariableAction variableAction) {
        // Convert to DTO and use the new method
        VariableActionCreateDTO dto = new VariableActionCreateDTO();
        dto.setDescription(variableAction.getDescription());
        dto.setPoids(variableAction.getPoids());
        dto.setFige(variableAction.isFige());
        dto.setOrdre(variableAction.getOrdre());
        
        // Set IDs for related entities
        if (variableAction.getResponsable() != null) {
            dto.setResponsableId(variableAction.getResponsable().getId());
        }
        
        if (variableAction.getPlanAction() != null) {
            dto.setPlanActionId(variableAction.getPlanAction().getId());
        }
        
        if (variableAction.getVaMere() != null) {
            dto.setVaMereId(variableAction.getVaMere().getId());
        }
        
        // Create the variable action using the DTO method
        VariableActionResponseDTO responseDto = createVariableAction(dto);
        // Get the entity by ID since the DTO doesn't have a getVariableAction() method
        return getVariableActionById(responseDto.getId());
    }

    // Original method signature for backward compatibility
    public VariableAction updateVariableAction(Long id, VariableAction updated) {
        // Convert to DTO and use the new method
        VariableActionUpdateDTO dto = new VariableActionUpdateDTO();
        dto.setDescription(updated.getDescription());
        dto.setPoids(updated.getPoids());
        dto.setFige(updated.isFige());
        
        // Set IDs for related entities
        if (updated.getResponsable() != null) {
            dto.setResponsableId(updated.getResponsable().getId());
        }
        
        if (updated.getPlanAction() != null) {
            dto.setPlanActionId(updated.getPlanAction().getId());
        }
        
        if (updated.getVaMere() != null) {
            dto.setVaMereId(updated.getVaMere().getId());
        } else {
            // Explicitly set to null to indicate moving to root level
            dto.setVaMereId(null);
        }
        
        // Update the variable action using the DTO method
        VariableActionResponseDTO responseDto = updateVariableAction(id, dto);
        // Get the entity by ID since the DTO doesn't have a getVariableAction() method
        return getVariableActionById(responseDto.getId());
    }


    // ✅ Créer une nouvelle variable d'action
    @Transactional
    public VariableActionResponseDTO createVariableAction(VariableActionCreateDTO dto) {
        // Create new variable action from DTO
        VariableAction variableAction = new VariableAction();
        variableAction.setDescription(dto.getDescription());
        variableAction.setFige(dto.getFige() != null ? dto.getFige() : false);
        
        // Set related entities
        if (dto.getResponsableId() != null) {
            User responsable = userRepository.findById(dto.getResponsableId())
                    .orElseThrow(() -> new RuntimeException("Responsable non trouvé avec l'ID : " + dto.getResponsableId()));
            variableAction.setResponsable(responsable);
        }
        
        if (dto.getPlanActionId() != null) {
            PlanAction planAction = planActionService.getById(dto.getPlanActionId());
            variableAction.setPlanAction(planAction);
        } else {
            throw new RuntimeException("Un plan d'action est requis pour créer une variable d'action");
        }
        
        // Set parent if provided
        if (dto.getVaMereId() != null) {
            VariableAction parent = getVariableActionById(dto.getVaMereId());
            
            // Validate that parent can accept children
            if (!parent.canHaveChildren()) {
                throw new RuntimeException("Le parent sélectionné ne peut plus avoir d'enfants (niveau maximum atteint).");
            }
            
            variableAction.setVaMere(parent);
            
            // Auto-calculate weight based on siblings
            List<VariableAction> siblings = parent.getSousVAs();
            int totalItems = siblings.size() + 1; // Include the new item
            float weightPerItem = 100.0f / totalItems;
            
            // Set weight for the new item
            variableAction.setPoids(weightPerItem);
            
            // Update weights for all siblings
            for (VariableAction sibling : siblings) {
                sibling.setPoids(weightPerItem);
                variableActionRepository.save(sibling);
            }
        } else {
            // Root level - calculate weight based on existing root items
            List<VariableAction> rootItems = variableActionRepository.findByPlanActionAndVaMereIsNull(
                    variableAction.getPlanAction());
            int totalItems = rootItems.size() + 1; // Include the new item
            float weightPerItem = 100.0f / totalItems;
            
            // Set weight for the new item
            variableAction.setPoids(weightPerItem);
            
            // Update weights for all root items
            for (VariableAction rootItem : rootItems) {
                rootItem.setPoids(weightPerItem);
                variableActionRepository.save(rootItem);
            }
        }
        
        // Set ordre if provided, otherwise it will be generated
        if (dto.getOrdre() != null) {
            variableAction.setOrdre(dto.getOrdre());
        }
        
        // IMPORTANT: Generate code and level BEFORE saving
        generateCodeAndLevel(variableAction);
        
        // Set order if not provided
        if (variableAction.getOrdre() == null) {
            variableAction.setOrdre(getNextOrderForParent(variableAction.getVaMere(), variableAction.getPlanAction()));
        }
        // Save the variable action with generated code and order
        VariableAction savedVA = variableActionRepository.save(variableAction);
        
        // Log the creation action
        logCreationAction(savedVA);
        createNotification(savedVA);
        
        // Create and return response DTO with audit logs
        VariableActionResponseDTO responseDTO = new VariableActionResponseDTO(savedVA);
        List<Audit> audits = auditService.getAuditsForEntity("VariableAction", savedVA.getId());
        responseDTO.setAuditLogs(auditService.formatAuditsForDisplay(audits));
        
        return responseDTO;
    }

    private void createNotification(VariableAction savedVA) {
        if (savedVA.getResponsable() == null) return;
        notificationService.notifyActionVariableAssigned(savedVA.getResponsable(), savedVA.getDisplayName());
    }

    // ✅ Mettre à jour une variable d'action existante
    @Transactional
    public VariableActionResponseDTO updateVariableAction(Long id, VariableActionUpdateDTO dto) {
        VariableAction existing = getVariableActionById(id);

        // Store old values for audit log
        String oldDescription = existing.getDescription();
        float oldPoids = existing.getPoids();
        boolean oldFige = existing.isFige();
        
        // Store old parent ID for detecting moves
        Long oldVaMereId = existing.getVaMere() != null ? existing.getVaMere().getId() : null;
        VariableAction oldParent = existing.getVaMere();

        // Store old responsible info using UserServiceImpl
        String oldResponsableNom = existing.getResponsable() != null && existing.getResponsable().getId() != null ?
            userService.getUserFullNameById(existing.getResponsable().getId()) : "unassigned";

        // Store old plan action info using PlanActionServiceImpl
        String oldPlanActionTitle = existing.getPlanAction() != null && existing.getPlanAction().getId() != null ?
            planActionService.getPlanActionTitleById(existing.getPlanAction().getId()) : "unassigned";

        // Create a copy of existing for comparison
        VariableAction beforeUpdate = new VariableAction();
        beforeUpdate.setResponsable(existing.getResponsable());
        beforeUpdate.setPlanAction(existing.getPlanAction());
        beforeUpdate.setVaMere(existing.getVaMere());

        // Update basic fields
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        
        if (dto.getPoids() != null) {
            existing.setPoids(dto.getPoids());
        }
        
        if (dto.getFige() != null) {
            existing.setFige(dto.getFige());
        }

        // Update related entities
        if (dto.getResponsableId() != null) {
            User responsable = userRepository.findById(dto.getResponsableId())
                    .orElseThrow(() -> new RuntimeException("Responsable non trouvé avec l'ID : " + dto.getResponsableId()));
            existing.setResponsable(responsable);
        }
        
        if (dto.getPlanActionId() != null) {
            PlanAction planAction = planActionService.getById(dto.getPlanActionId());
            existing.setPlanAction(planAction);
        }

        // Handle parent change (moving the variable action)
        VariableAction newParent = null;
        boolean parentChanged = false;
        
        if (dto.getVaMereId() != null && !dto.getVaMereId().equals(oldVaMereId)) {
            newParent = getVariableActionById(dto.getVaMereId());
            
            // Validate that new parent can accept children
            if (!newParent.canHaveChildren()) {
                throw new RuntimeException("Le parent sélectionné ne peut plus avoir d'enfants (niveau maximum atteint).");
            }
            
            // Prevent circular references
            if (isCircularReference(existing, newParent)) {
                throw new RuntimeException("Impossible de déplacer: cela créerait une référence circulaire.");
            }
            
            existing.setVaMere(newParent);
            parentChanged = true;
        } else if (dto.getVaMereId() == null && oldVaMereId != null) {
            // Moving to root level (no parent)
            existing.setVaMere(null);
            parentChanged = true;
        }
        
        // If parent changed, regenerate code, level and order
        if (parentChanged) {
            generateCodeAndLevel(existing);
            existing.setOrdre(getNextOrderForParent(existing.getVaMere(), existing.getPlanAction()));
        }
        
        VariableAction savedVA = variableActionRepository.save(existing);
        
        // Recalculate weights for both old and new parents if parent changed
        if (parentChanged) {
            if (oldParent != null) {
                recalculateParentWeights(oldParent);
            }
            if (newParent != null) {
                recalculateParentWeights(newParent);
            }
        }

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

            if (parentChanged) {
                String oldParentDesc = oldParent != null ? oldParent.getDescription() : "root";
                String newParentDesc = savedVA.getVaMere() != null ? savedVA.getVaMere().getDescription() : "root";
                
                details.append("parent changed from \"")
                      .append(oldParentDesc)
                      .append("\" to \"")
                      .append(newParentDesc)
                      .append("\"; ");
                      
                details.append("code changed from \"")
                      .append(oldParent != null ? oldParent.getCode() : "none")
                      .append("\" to \"")
                      .append(savedVA.getCode())
                      .append("\"; ");
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

        // Create and return response DTO with audit logs
        VariableActionResponseDTO responseDTO = new VariableActionResponseDTO(savedVA);
        List<Audit> audits = auditService.getAuditsForEntity("VariableAction", savedVA.getId());
        responseDTO.setAuditLogs(auditService.formatAuditsForDisplay(audits));
        
        return responseDTO;
    }

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
    
    // @Transactional
    // public VariableAction createVariableAction(VariableAction variableAction) {
    //     // Generate code and set level
    //     generateCodeAndLevel(variableAction);
        
    //     // Set order if not provided
    //     if (variableAction.getOrdre() == null) {
    //         variableAction.setOrdre(getNextOrderForParent(variableAction.getVaMere(), variableAction.getPlanAction()));
    //     }
        
    //     // Validate weight distribution
    //     if (variableAction.getVaMere() != null) {
    //         validateWeightDistribution(variableAction.getVaMere(), variableAction.getPoids());
    //     }
        
    //     VariableAction savedVA = variableActionRepository.save(variableAction);
        
    //     // Update parent weights
    //     if (savedVA.getVaMere() != null) {
    //         recalculateParentWeights(savedVA.getVaMere());
    //     }
        
    //     logCreationAction(savedVA);
    //     return savedVA;
    // }
    
    @Transactional
    public VariableAction moveVariableAction(Long id, Long newParentId) {
        VariableAction va = getVariableActionById(id);
        VariableAction oldParent = va.getVaMere();
        
        VariableAction newParent = null;
        if (newParentId != null) {
            newParent = getVariableActionById(newParentId);
            
            // Validate that new parent can accept children
            if (!newParent.canHaveChildren()) {
                throw new RuntimeException("Le parent sélectionné ne peut plus avoir d'enfants (niveau maximum atteint).");
            }
            
            // Prevent circular references
            if (isCircularReference(va, newParent)) {
                throw new RuntimeException("Impossible de déplacer: cela créerait une référence circulaire.");
            }
        }
        
        va.setVaMere(newParent);
        
        // Regenerate code and level
        generateCodeAndLevel(va);
        
        // Update order
        va.setOrdre(getNextOrderForParent(newParent, va.getPlanAction()));
        
        VariableAction savedVA = variableActionRepository.save(va);
        
        // Recalculate weights for both old and new parents
        if (oldParent != null) {
            recalculateParentWeights(oldParent);
        }
        if (newParent != null) {
            recalculateParentWeights(newParent);
        }
        
        return savedVA;
    }
    
    private boolean isCircularReference(VariableAction node, VariableAction potentialParent) {
        VariableAction current = potentialParent;
        while (current != null) {
            if (current.getId().equals(node.getId())) {
                return true;
            }
            current = current.getVaMere();
        }
        return false;
    }

    private void generateCodeAndLevel(VariableAction variableAction) {
        if (variableAction.getVaMere() == null) {
            // Root level: VA1, VA2, VA3, ...
            List<VariableAction> siblings = variableActionRepository.findByPlanActionAndVaMereIsNull(variableAction.getPlanAction());
            int nextNumber = getNextSiblingNumber(siblings, "VA");
            variableAction.setCode("VA" + nextNumber);
        } else {
            // Child: parent's code + next number (e.g. VA1 -> VA11, VA12, ...)
            VariableAction parent = variableAction.getVaMere();
            String parentCode = parent.getCode();
            List<VariableAction> siblings = parent.getSousVAs();
            int nextNumber = getNextSiblingNumber(siblings, parentCode);
            variableAction.setCode(parentCode + nextNumber);
        }
        // Set level: number of digits after "VA"
        String code = variableAction.getCode();
        int level = code.substring(2).length();  // Changed from substring(1) to substring(2) to account for "VA" prefix
        variableAction.setNiveau(level + 1);    // Add 1 because first level is 1, not 0
    }

    private int getNextSiblingNumber(List<VariableAction> siblings, String prefix) {
        // Find all numbers used by siblings with the same prefix
        java.util.Set<Integer> used = new java.util.HashSet<>();
        for (VariableAction va : siblings) {
            String code = va.getCode();
            if (code != null && code.startsWith(prefix)) {
                String numberPart = code.substring(prefix.length());
                try {
                    used.add(Integer.parseInt(numberPart));
                } catch (NumberFormatException ignored) {}
            }
        }
        // Find the smallest unused number starting from 1
        int n = 1;
        while (used.contains(n)) n++;
        return n;
    }

    private int getNextRootNumber(PlanAction planAction) {
        List<VariableAction> rootVAs = variableActionRepository.findByPlanActionAndVaMereIsNull(planAction);
        if (rootVAs.isEmpty()) {
            return 1;
        }
        return rootVAs.stream()
                .map(va -> extractNumberFromCode(va.getCode(), "VA"))
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private int getNextChildNumber(VariableAction parent) {
        if (parent.getSousVAs().isEmpty()) {
            return 1;
        }
        return parent.getSousVAs().stream()
                .map(va -> extractNumberFromCode(va.getCode(), parent.getCode()))
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private int extractNumberFromCode(String code, String prefix) {
        if (code == null || !code.startsWith(prefix)) {
            return 0;
        }
        String numberPart = code.substring(prefix.length());
        if (numberPart.isEmpty()) {
            return 0;
        }
        
        StringBuilder firstNumber = new StringBuilder();
        for (char c : numberPart.toCharArray()) {
            if (Character.isDigit(c)) {
                firstNumber.append(c);
            } else {
                break;
            }
        }
        
        try {
            return firstNumber.length() > 0 ? Integer.parseInt(firstNumber.toString()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void validateWeightDistribution(VariableAction parent, float newWeight) {
        float totalWeight = parent.getSousVAs().stream()
                .map(VariableAction::getPoids)
                .reduce(0f, Float::sum);
        
        if (totalWeight + newWeight > 100) {
            throw new RuntimeException("La somme des poids ne peut pas dépasser 100%. " +
                    "Poids actuel: " + totalWeight + "%, nouveau poids: " + newWeight + "%");
        }
    }

    public void recalculateParentWeights(VariableAction parent) {
        List<VariableAction> children = parent.getSousVAs();
        if (children.isEmpty()) {
            return;
        }
        
        float weightPerChild = 100f / children.size();
        for (VariableAction child : children) {
            child.setPoids(weightPerChild);
            variableActionRepository.save(child);
        }
    }

    private int getNextOrderForParent(VariableAction parent, PlanAction planAction) {
        if (parent != null) {
            Integer maxOrder = variableActionRepository.getMaxOrderForParent(parent.getId());
            return (maxOrder != null ? maxOrder : 0) + 1;
        } else {
            Integer maxOrder = variableActionRepository.getMaxOrderForRootLevel(planAction.getId());
            return (maxOrder != null ? maxOrder : 0) + 1;
        }
    }

    // Method to get hierarchical tree structure
    public List<VariableActionHierarchyDTO> getVariableActionHierarchy(Long planActionId) {
        List<VariableAction> rootActions = variableActionRepository.findByPlanActionIdAndVaMereIsNull(planActionId);
        
        return rootActions.stream()
                .sorted(Comparator.comparing(va -> va.getOrdre() != null ? va.getOrdre() : 0))
                .map(this::buildHierarchyDTO)
                .collect(Collectors.toList());
    }

    private VariableActionHierarchyDTO buildHierarchyDTO(VariableAction va) {
        VariableActionHierarchyDTO dto = new VariableActionHierarchyDTO();
        dto.setId(va.getId());
        dto.setCode(va.getCode());
        dto.setDescription(va.getDescription());
        dto.setPoids(va.getPoids());
        dto.setNiveau(va.getNiveau());
        dto.setFige(va.isFige());
        dto.setOrdre(va.getOrdre());
        dto.setResponsableNom(va.getResponsable() != null ? 
            va.getResponsable().getNom() + " " + va.getResponsable().getPrenom() : null);
        
        // Recursively build children
        dto.setChildren(va.getSousVAs().stream()
                .sorted(Comparator.comparing(child -> child.getOrdre() != null ? child.getOrdre() : 0))
                .map(this::buildHierarchyDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }

    /**
     * Get variable actions for dropdown selection by plan action ID
     * @param planActionId The ID of the plan action to filter by
     * @return List of simplified variable action DTOs suitable for dropdown
     */
    public List<VariableActionDropdownDTO> getVariableActionsByPlanForDropdown(Long planActionId) {
        // Find all variable actions for this plan
        List<VariableAction> variableActions;
        if (planActionId != null) {
            variableActions = variableActionRepository.findByPlanActionId(planActionId);
        } else {
            variableActions = variableActionRepository.findAll();
        }
        
        // Convert to dropdown DTOs
        return variableActions.stream()
            .map(va -> {
                VariableActionDropdownDTO dto = new VariableActionDropdownDTO();
                dto.setId(va.getId());
                dto.setCode(va.getCode());
                dto.setFige(va.isFige());
                dto.setDescription(va.getDescription());
                return dto;
            })
            .sorted(Comparator.comparing(VariableActionDropdownDTO::getCode)) // Sort by code for hierarchical display
            .collect(Collectors.toList());
    }
}
