package com.example.GestionPlanAction.dto;

import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileDTO {
    public Long id;
    public String nom;
    public String prenom;
    public String email;
    public String username;
    public String motDePasse;
    public Set<Long> roles;
    public Long serviceLine;
    public Boolean actif;
    public java.time.LocalDateTime createdAt;
    public java.time.LocalDateTime updatedAt;
}
