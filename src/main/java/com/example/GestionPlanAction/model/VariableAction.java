package com.example.GestionPlanAction.model;

import jakarta.persistence.*;
import lombok.*;

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

    private String description;
    private float poids;
    private boolean fige;
    private int niveau;

    @ManyToOne
    @JoinColumn(name = "va_mere_id")
    private VariableAction vaMere;

    @OneToMany(mappedBy = "vaMere", cascade = CascadeType.ALL)
    private List<VariableAction> sousVAs;

    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private User responsable;

    @ManyToOne
    @JoinColumn(name = "plan_action_id")
    private PlanAction planAction;
}
