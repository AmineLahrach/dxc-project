package com.example.GestionPlanAction.dto.PlanActionDTOs;

import com.example.GestionPlanAction.enums.StatutPlanAction;
import lombok.Data;

@Data
public class PlanActionCreateDTO {
    private String titre;
    private String description;
    private boolean verrouille;
    private Long exerciceId;
    private StatutPlanAction statut;
    // No variableActions here - they'll be added later if needed
}