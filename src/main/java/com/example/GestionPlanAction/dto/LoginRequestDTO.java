package com.example.GestionPlanAction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Login Request DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    
    @NotBlank(message = "Le nom d'utilisateur ou email est obligatoire")
    private String usernameOrEmail;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;
}
    