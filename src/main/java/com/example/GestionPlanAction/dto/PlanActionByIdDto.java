package com.example.GestionPlanAction.dto;

import com.example.GestionPlanAction.dto.variableActionDTOs.VariableActionDetailDTO;
import com.example.GestionPlanAction.enums.StatutPlanAction;
import com.example.GestionPlanAction.model.Exercice;

import java.util.List;
import java.util.Map;

public class PlanActionByIdDto {
    private Long id;
    private String titre;
    private String description;
    private boolean verrouille;
    private StatutPlanAction statut;
    private Exercice exercice;
    private Long createdBy;
    private String createdByName; 
    private List<VariableActionDetailDTO> variableActions;
    private List<Map<String, Object>> auditLogs;

    // Update getter and setter for variableActions
    public List<VariableActionDetailDTO> getVariableActions() { return variableActions; }
    public void setVariableActions(List<VariableActionDetailDTO> variableActions) { this.variableActions = variableActions; }

    // Keep other getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isVerrouille() { return verrouille; }
    public void setVerrouille(boolean verrouille) { this.verrouille = verrouille; }

    public StatutPlanAction getStatut() { return statut; }
    public void setStatut(StatutPlanAction statut) { this.statut = statut; }

    public Exercice getExercice() { return exercice; }
    public void setExercice(Exercice exercice) { this.exercice = exercice; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public List<Map<String, Object>> getAuditLogs() { return auditLogs; }
    public void setAuditLogs(List<Map<String, Object>> auditLogs) { this.auditLogs = auditLogs; }
}
