package com.example.GestionPlanAction.model;

import com.example.GestionPlanAction.enums.StatutPlanAction;
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

    @Enumerated(EnumType.STRING)
    private StatutPlanAction statut;

    @ManyToOne
    @JoinColumn(name = "exercice_id")
    private Exercice exercice;

    @OneToMany(mappedBy = "planAction", cascade = CascadeType.ALL)
    private List<VariableAction> variableActions;
}
