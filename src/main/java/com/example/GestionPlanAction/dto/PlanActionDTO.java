package com.example.GestionPlanAction.dto;

import java.util.List;

import com.example.GestionPlanAction.model.Exercice;
import com.example.GestionPlanAction.model.VariableAction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanActionDTO {
	

	    private Long id;
	    private String titre;
	    private String description;
	    private String statut;
	    private ExerciseDTO exercice;
	    private List<VariableActionDTO> variableActions;
	    

	    // Getters and Setters
	}



