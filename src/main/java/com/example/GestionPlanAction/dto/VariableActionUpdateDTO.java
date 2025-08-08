package com.example.GestionPlanAction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariableActionUpdateDTO {
    private String description;
    private Float poids;
    private Boolean fige;
    private Long responsableId;
    private Long planActionId;
    private Long vaMereId;  // Can be null to make this a root variable
}