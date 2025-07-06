package com.example.GestionPlanAction.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@Entity
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private String username;
    private String motDePasse;

    @ManyToMany
    @JoinTable(name = "profil_utilisateur_association",
            joinColumns = @JoinColumn(name = "idUtilisateur"),
            inverseJoinColumns = @JoinColumn(name = "idProfil"))
    private Set<Profil> profils = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "service_line_id")
    private ServiceLine serviceLine;

    @OneToMany(mappedBy = "responsable")
    private List<VariableAction> variableActions;
}
