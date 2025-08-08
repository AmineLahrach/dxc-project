package com.example.GestionPlanAction.dto;

import com.example.GestionPlanAction.enums.StatutPlanAction;

public class PlanActionResponse {
    private Long id;
    private String titre;
    private String description;
    private boolean verrouille;
    private StatutPlanAction statut;
    private Long createdBy;
    private String createdByName;
    // Add other fields as needed (e.g., exercice, variableActions)

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isVerrouille() {
        return verrouille;
    }

    public void setVerrouille(boolean verrouille) {
        this.verrouille = verrouille;
    }

    public StatutPlanAction getStatut() {
        return statut;
    }

    public void setStatut(StatutPlanAction statut) {
        this.statut = statut;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
}