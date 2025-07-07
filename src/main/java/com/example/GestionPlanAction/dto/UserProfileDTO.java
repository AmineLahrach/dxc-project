package com.example.GestionPlanAction.dto;

import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String username;
    private Set<String> roles;
    private String serviceLine;
    private Boolean actif;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
