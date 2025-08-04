package com.example.GestionPlanAction.dto;

import com.example.GestionPlanAction.model.VariableAction;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariableActionResponseDTO {
    private Long id;
    private String description;
    private float poids;
    private boolean fige;
    private int niveau;
    private Long responsableId;
    private String responsableNom;
    private String responsablePrenom;
    private Long planActionId;
    private String planActionNom;
    private Long vaMereId;
    private List<Map<String, Object>> auditLogs;
    
    // Constructor from entity
    public VariableActionResponseDTO(VariableAction va) {
        this.id = va.getId();
        this.description = va.getDescription();
        this.poids = va.getPoids();
        this.fige = va.isFige();
        this.niveau = va.getNiveau();
        
        if (va.getResponsable() != null) {
            this.responsableId = va.getResponsable().getId();
            this.responsableNom = va.getResponsable().getNom();
            this.responsablePrenom = va.getResponsable().getPrenom();
        }
        
        if (va.getPlanAction() != null) {
            this.planActionId = va.getPlanAction().getId();
            this.planActionNom = va.getPlanAction().getTitre();
        }
        
        if (va.getVaMere() != null) {
            this.vaMereId = va.getVaMere().getId();
        }
    }
}
