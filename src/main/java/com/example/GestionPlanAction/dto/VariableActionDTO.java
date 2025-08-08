package com.example.GestionPlanAction.dto;

import lombok.Data;

@Data
public class VariableActionDTO {
    private Long id;
    private String description;
    private String code;
    private Integer ordre;
    private float poids;
    private boolean fige;
    private int niveau;
    private String planActionNom;
    private Long planActionId;
    private Long responsableId;
    private String responsableNom;
    private String responsablePrenom;
    private String responsableServiceLineNom;
}