package com.example.GestionPlanAction.dto;

import java.util.HashSet;
import java.util.Set;

public class UserWithProfilesDTO {
    private Long id;
    private String nom;
    private String prenom;


	public String getServiceLineName() {
		return serviceLineName;
	}
	public void setServiceLineName(String serviceLineName) {
		this.serviceLineName = serviceLineName;
	}
	public void setServiceLineId(Long serviceLineId) {
		this.serviceLineId = serviceLineId;
	}
	private String email;
    private String username;
    private Boolean actif;
    private String serviceLineName;
    private Long serviceLineId;
    
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
				+ ", username=" + username + ", actif=" + actif + ", serviceLineName=" + serviceLineName
				+ ", serviceLineId=" + serviceLineId + ", profils=" + profils + "]";
	}
	public Long getServiceLineId() {
		return serviceLineId;
	}
	public UserWithProfilesDTO(Long id, String nom, String prenom, String email, String username, Boolean actif,
			String serviceLineName, Long serviceLineId, Set<ProfilDTO> profils) {
		super();
		this.id = id;
		this.nom = nom;
		this.prenom = prenom;
		this.email = email;
		this.username = username;
		this.actif = actif;
		this.serviceLineName = serviceLineName;
		this.serviceLineId = serviceLineId;
		this.profils = profils;
	}
	public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
   // public String getServiceLineId() { return serviceLineId; }
   // public void setServiceLineId(String serviceLineId) { this.serviceLineId = serviceLineId; }
    public Set<ProfilDTO> getProfils() { return profils; }
    public void setProfils(Set<ProfilDTO> profils) { this.profils = profils; }
}