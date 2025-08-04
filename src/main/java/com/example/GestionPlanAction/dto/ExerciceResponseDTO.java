package com.example.GestionPlanAction.dto;

import com.example.GestionPlanAction.model.Exercice;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciceResponseDTO {
    private Long id;
    private Integer annee;
    private boolean verrouille;
    private List<Map<String, Object>> auditLogs;
    
    // Constructor to convert from Exercice entity
    public ExerciceResponseDTO(Exercice exercice) {
        this.id = exercice.getId();
        this.annee = exercice.getAnnee();
        this.verrouille = exercice.isVerrouille();
    }
}
