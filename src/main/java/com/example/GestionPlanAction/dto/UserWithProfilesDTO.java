package com.example.GestionPlanAction.dto;

import java.util.HashSet;
import java.util.Set;

public class UserWithProfilesDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String username;
    private Boolean actif;
    private Long serviceLineId;
    private Set<ProfilDTO> profils = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
    public Long getServiceLineId() { return serviceLineId; }
    public void setServiceLineId(Long serviceLineId) { this.serviceLineId = serviceLineId; }
    public Set<ProfilDTO> getProfils() { return profils; }
    public void setProfils(Set<ProfilDTO> profils) { this.profils = profils; }
}