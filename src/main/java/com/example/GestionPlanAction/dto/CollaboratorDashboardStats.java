package com.example.GestionPlanAction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollaboratorDashboardStats {
    private long myPlansInProgress;
    private long myPlansCompleted;
    private long myPlansPending;
    private long myVariableActions;
}