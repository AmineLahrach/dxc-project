package com.example.GestionPlanAction.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Table(name = "user")
@EqualsAndHashCode(exclude = {"profils", "serviceLine", "variableActions", "notifications"}) // ✅ CRITICAL
@ToString(exclude = {"profils", "serviceLine", "variableActions", "notifications"})          // ✅ CRITICAL
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private String username;
    private String motDePasse;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinTable(
        name = "profil_utilisateur_association",
        joinColumns = @JoinColumn(name = "id_utilisateur", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "id_profil", referencedColumnName = "id")
    )
    @JsonIgnore
    private Set<Profil> profils = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_line_id")
    @JsonIgnore
    private ServiceLine serviceLine;

    @OneToMany(mappedBy = "responsable", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<VariableAction> variableActions = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Notification> notifications = new ArrayList<>();

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
        if (actif == null) {
            actif = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}