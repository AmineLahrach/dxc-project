package com.example.GestionPlanAction.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDTO {
    
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String nom;
    private String prenom;
    private Set<String> roles;
    private String serviceLine;
    
    public JwtResponseDTO(String accessToken, Long id, String username, String email, 
                      String nom, String prenom, Set<String> roles, String serviceLine) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.roles = roles;
        this.serviceLine = serviceLine;
    }
}

