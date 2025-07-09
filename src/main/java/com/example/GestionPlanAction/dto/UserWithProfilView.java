package com.example.GestionPlanAction.dto;

public interface UserWithProfilView {
    Long getUserId();
    String getNom();
    String getPrenom();
    String getEmail();
    String getUsername();
    Boolean getActif();
    Long getServiceLineId();
    Long getProfilId();
    String getProfilNom();
}
