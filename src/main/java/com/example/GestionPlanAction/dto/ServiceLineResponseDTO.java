package com.example.GestionPlanAction.dto;

import com.example.GestionPlanAction.model.ServiceLine;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceLineResponseDTO {
    private Long id;
    private String nom;
    private List<Map<String, Object>> auditLogs;
    
    // Constructor to convert from ServiceLine entity
    public ServiceLineResponseDTO(ServiceLine serviceLine) {
        this.id = serviceLine.getId();
        this.nom = serviceLine.getNom();
    }
}
