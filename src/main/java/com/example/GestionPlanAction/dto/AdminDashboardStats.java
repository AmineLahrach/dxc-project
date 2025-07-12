package com.example.GestionPlanAction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardStats {
    private long totalUsers;
    private long totalPlans;
    private long totalServiceLines;
    private long activeExercises;
    private long totalProfils;
}
