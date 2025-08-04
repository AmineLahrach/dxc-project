package com.example.GestionPlanAction.dto;

import com.example.GestionPlanAction.enums.StatutPlanAction;
import com.example.GestionPlanAction.model.Exercice;
import com.example.GestionPlanAction.model.VariableAction;

import java.util.List;
import java.util.Map;

public class PlanActionByIdDto {
    private Long id;
    private String titre;
    private String description;
    private StatutPlanAction statut;
    private Exercice exercice;
    private Long createdBy;
    private String createdByName; // Optional, if you want to include the name
    private List<VariableAction> variableActions;
    private List<Map<String, Object>> auditLogs; // Added field for audit logs

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public StatutPlanAction getStatut() { return statut; }
    public void setStatut(StatutPlanAction statut) { this.statut = statut; }

    public Exercice getExercice() { return exercice; }
    public void setExercice(Exercice exercice) { this.exercice = exercice; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public List<VariableAction> getVariableActions() { return variableActions; }
    public void setVariableActions(List<VariableAction> variableActions) { this.variableActions = variableActions; }

    public List<Map<String, Object>> getAuditLogs() { return auditLogs; }
    public void setAuditLogs(List<Map<String, Object>> auditLogs) { this.auditLogs = auditLogs; }
}
