package com.example.GestionPlanAction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariableActionDropdownDTO {
    private Long id;
    private String code;
    private String description;
    private boolean fige;
    // Optional: Include a display field that combines code and description for UI convenience
    public String getDisplayLabel() {
        return code + " - " + description;
    }
}