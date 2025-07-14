package com.example.GestionPlanAction.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariableActionDTO {
	
	
	
    private Long id;
    private String description;
    private float poids;
    private Boolean fige;
    private int niveau;
    private String vaMere;
    private List<String> sousVAs;
  //  private Responsable responsable;
    private ResponsableDTO resonableid;
    private String planAction;
	    

}
