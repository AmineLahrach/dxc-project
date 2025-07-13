package com.example.GestionPlanAction.dto;

import java.util.HashSet;
import java.util.Set;

public class UserWithProfilesDTO {
    private Long id;
    private String nom;
    private String prenom;
    public UserWithProfilesDTO(Long id, String nom, String prenom, String email, String username, Boolean actif,
			String serviceLineId, Set<ProfilDTO> profils) {
		super();
		this.id = id;
		this.nom = nom;
		this.prenom = prenom;
		this.email = email;
		this.username = username;
		this.actif = actif;
		this.serviceLineId = serviceLineId;
		this.profils = profils;
	}
	private String email;
    private String username;
    private Boolean actif;
    private String serviceLineId;
    
    private Set<ProfilDTO> profils = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public UserWithProfilesDTO(){}
	public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    @Override
	public String toString() {
		return "UserWithProfilesDTO [id=" + id + ", nom=" + nom + ", prenom=" + prenom + ", email=" + email
				+ ", username=" + username + ", actif=" + actif + ", serviceLineId=" + serviceLineId + ", profils="
				+ profils + "]";
	}
	public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
    public String getServiceLineId() { return serviceLineId; }
    public void setServiceLineId(String serviceLineId) { this.serviceLineId = serviceLineId; }
    public Set<ProfilDTO> getProfils() { return profils; }
    public void setProfils(Set<ProfilDTO> profils) { this.profils = profils; }
}