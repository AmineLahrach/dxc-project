package com.example.GestionPlanAction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectorDashboardStats {
    private long pendingValidations;
    private double completionRate;
    private long activePlans;
    private long totalExercises;
    private long totalUsers;
}