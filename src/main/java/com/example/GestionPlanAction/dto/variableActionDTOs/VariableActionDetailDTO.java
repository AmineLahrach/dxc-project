package com.example.GestionPlanAction.dto.variableActionDTOs;

import java.util.ArrayList;
import java.util.List;

public class VariableActionDetailDTO {
    private Long id;
    private String description;
    private String code;
    private float poids;
    private boolean fige;
    private int niveau;
    private Integer ordre;
    private Long responsableId;
    private String responsableNom;
    private String responsablePrenom;
    private Long vaMereId;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public float getPoids() { return poids; }
    public void setPoids(float poids) { this.poids = poids; }

    public boolean isFige() { return fige; }
    public void setFige(boolean fige) { this.fige = fige; }

    public int getNiveau() { return niveau; }
    public void setNiveau(int niveau) { this.niveau = niveau; }

    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }

    public Long getResponsableId() { return responsableId; }
    public void setResponsableId(Long responsableId) { this.responsableId = responsableId; }

    public String getResponsableNom() { return responsableNom; }
    public void setResponsableNom(String responsableNom) { this.responsableNom = responsableNom; }

    public String getResponsablePrenom() { return responsablePrenom; }
    public void setResponsablePrenom(String responsablePrenom) { this.responsablePrenom = responsablePrenom; }

    public Long getVaMereId() { return vaMereId; }
    public void setVaMereId(Long vaMereId) { this.vaMereId = vaMereId; }
}
