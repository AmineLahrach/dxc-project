package com.example.GestionPlanAction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardStats {
    private long totalUsers;
    private long totalPlans;
    private long totalServiceLines;
    private long activeExercises;
    private long totalProfils;
    private List<Map<String, Object>> recentAudits;
    
    // Constructor without recentAudits for backward compatibility
    public AdminDashboardStats(long totalUsers, long totalPlans, long totalServiceLines, 
                              long activeExercises, long totalProfils) {
        this.totalUsers = totalUsers;
        this.totalPlans = totalPlans;
        this.totalServiceLines = totalServiceLines;
        this.activeExercises = activeExercises;
        this.totalProfils = totalProfils;
    }
}
