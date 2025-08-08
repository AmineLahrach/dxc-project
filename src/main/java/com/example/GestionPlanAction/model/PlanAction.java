package com.example.GestionPlanAction.model;

import com.example.GestionPlanAction.enums.StatutPlanAction;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private boolean verrouille;
    @Enumerated(EnumType.STRING)
    private StatutPlanAction statut;

    @ManyToOne
    @JoinColumn(name = "exercice_id")
    private Exercice exercice;

    @JsonManagedReference("plan-variables")
    @OneToMany(mappedBy = "planAction", cascade = CascadeType.ALL)
    private List<VariableAction> variableActions;

    

    private Long createdBy; // User ID who created the plan
}
