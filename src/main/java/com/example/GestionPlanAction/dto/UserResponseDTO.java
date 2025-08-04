package com.example.GestionPlanAction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String username;
    private Boolean actif; 
    private String serviceLineName; 
    private Long serviceLineId; 
    private Set<ProfilDTO> roles;
    private ServiceLineDTO serviceLine;
    private List<Map<String, Object>> auditLogs;
}