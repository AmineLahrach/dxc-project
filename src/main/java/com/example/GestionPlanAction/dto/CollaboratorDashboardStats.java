package com.example.GestionPlanAction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class CollaboratorDashboardStats {
    private long myPlansInProgress;
    private long myPlansCompleted;
    private long myPlansPending;
    private long myVariableActions;
    private long totalPlans;
    private long totalVariables;
    private List<Map<String, Object>> recentAudits;
    
    public CollaboratorDashboardStats(long myPlansInProgress, long myPlansCompleted, long myPlansPending, 
                                    long myVariableActions) {
        this.myPlansInProgress = myPlansInProgress;
        this.myPlansCompleted = myPlansCompleted;
        this.myPlansPending = myPlansPending;
        this.myVariableActions = myVariableActions;
    }
    
    public CollaboratorDashboardStats(long myPlansInProgress, long myPlansCompleted, long myPlansPending, 
                                    long myVariableActions, long totalPlans, long totalVariables,
                                    List<Map<String, Object>> recentAudits) {
        this.myPlansInProgress = myPlansInProgress;
        this.myPlansCompleted = myPlansCompleted;
        this.myPlansPending = myPlansPending;
        this.myVariableActions = myVariableActions;
        this.totalPlans = totalPlans;
        this.totalVariables = totalVariables;
        this.recentAudits = recentAudits;
    }
}