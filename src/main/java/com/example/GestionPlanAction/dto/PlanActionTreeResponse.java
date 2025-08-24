package com.example.GestionPlanAction.dto;

import java.util.List;

public class PlanActionTreeResponse {
    private String name;
    private Long id; 
    private Long planActionId;
    private String description;
    private String nodeType;
    private float poids;
    private boolean fige;
    private boolean isOwner;
    private List<PlanActionTreeResponse> children;

    public PlanActionTreeResponse() {}

    public PlanActionTreeResponse(String name, Long id, Long planActionId, String description, float poids,
                                  boolean fige, String nodeType, boolean isOwner, List<PlanActionTreeResponse> children) {
        this.name = name;
        this.id = id;
        this.planActionId = planActionId;
        this.description = description;
        this.poids = poids;
        this.fige = fige;
        this.nodeType = nodeType;
        this.children = children;
        this.isOwner = isOwner;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlanActionId() { return planActionId; }
    public void setPlanActionId(Long planActionId) { this.planActionId = planActionId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public float getPoids() { return poids; }
    public void setPoids(float poids) { this.poids = poids; }

    public boolean isFige() { return fige; }
    public void setFige(boolean fige) { this.fige = fige; }

    public boolean isOwner() { return isOwner; }
    public void setOwner(boolean isOwner) { this.isOwner = isOwner; }

    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public List<PlanActionTreeResponse> getChildren() { return children; }
    public void setChildren(List<PlanActionTreeResponse> children) { this.children = children; }
}