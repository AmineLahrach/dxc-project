package com.example.GestionPlanAction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectorDashboardStats {
    private long planningVariables;
    private long lockedVariables;
    private long monitoringVariables;
    private long totalVariables;
    private long totalPlans;
    private double completionRate;
    private List<Map<String, Object>> auditLogs;
}