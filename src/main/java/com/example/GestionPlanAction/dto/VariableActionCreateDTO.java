package com.example.GestionPlanAction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariableActionCreateDTO {
    private String description;
    private Float poids;
    private Boolean fige;
    private Long responsableId;
    private Long planActionId;
    private Long vaMereId;  // Parent ID (null if this is a root variable)
    
    // Optional fields
    private Integer ordre;  // Will be auto-generated if null
}
