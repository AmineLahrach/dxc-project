package com.example.GestionPlanAction.dto;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDTO {
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50)
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire") 
    @Size(min = 2, max = 50)
    private String prenom;
    
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 30)
    private String username;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 120)
    private String motDePasse;
    
    private Set<String> roles;
    
    private Long serviceLineId;
}

