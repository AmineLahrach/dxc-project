package com.example.GestionPlanAction.dto;

import com.example.GestionPlanAction.model.Profil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilResponseDTO {
    private Long id;
    private String nom;
    private List<Map<String, Object>> auditLogs;
    
    // Constructor from Profil entity
    public ProfilResponseDTO(Profil profil) {
        this.id = profil.getId();
        this.nom = profil.getNom();
    }
}
