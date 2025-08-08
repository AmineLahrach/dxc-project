package com.example.GestionPlanAction.dto;

import lombok.Data;
import java.util.List;

@Data
public class VariableActionHierarchyDTO {
    private Long id;
    private String code;
    private String description;
    private float poids;
    private int niveau;
    private boolean fige;
    private Integer ordre;
    private String responsableNom;
    private List<VariableActionHierarchyDTO> children;
}