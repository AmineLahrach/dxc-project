package com.example.GestionPlanAction.model;

import com.example.GestionPlanAction.security.SecurityUtils;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariableAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;
    
    @Column(name = "code")
    private String code; // VA1, VA11, VA111, etc.
    
    private float poids;
    private boolean fige;
    private int niveau; // Level derived from code depth
    
    @Column(name = "ordre")
    private Integer ordre; // Order within siblings

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "variableActions", "serviceLine", "profils"})
    private User responsable;

    @JsonBackReference("plan-variables")
    @ManyToOne
    @JoinColumn(name = "plan_action_id")
    private PlanAction planAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "va_mere_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "sousVAs", "planAction", "responsable"})
    private VariableAction vaMere;

    @OneToMany(mappedBy = "vaMere", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"vaMere", "planAction"}) // Prevent circular reference
    @OrderBy("ordre ASC")
    private List<VariableAction> sousVAs = new ArrayList<>();

    private Long createdBy; // User ID who created the plan


    // Helper methods
    
    /**
     * Get the root level VariableActions (those without parent)
     */
    public boolean isRoot() {
        return this.vaMere == null;
    }
    
    /**
     * Get all children at any level below this node
     */
    public List<VariableAction> getAllDescendants() {
        List<VariableAction> descendants = new ArrayList<>();
        for (VariableAction child : sousVAs) {
            descendants.add(child);
            descendants.addAll(child.getAllDescendants());
        }
        return descendants;
    }
    
    /**
     * Calculate level based on parent hierarchy
     */
    public int calculateLevel() {
        if (vaMere == null) {
            return 1;
        }
        return vaMere.calculateLevel() + 1;
    }
    
    /**
     * Get the path from root to this node
     */
    public List<VariableAction> getPath() {
        List<VariableAction> path = new ArrayList<>();
        VariableAction current = this;
        while (current != null) {
            path.add(0, current);
            current = current.getVaMere();
        }
        return path;
    }
    
    /**
     * Check if this node can have children based on business rules
     */
    public boolean canHaveChildren() {
        // Business rule: Level 1 nodes can have children up to level 15
        return this.niveau < 15;
    }
    
    /**
     * Get display name with level indicator
     */
    public String getDisplayName() {
        String levelIndicator = "•".repeat(Math.max(0, niveau - 1));
        return levelIndicator + " " + (code != null ? code : "VA") + " - " + description;
    }

    @PrePersist
    protected void onCreate() {
        createdBy = SecurityUtils.getCurrentUserId();
    }
}