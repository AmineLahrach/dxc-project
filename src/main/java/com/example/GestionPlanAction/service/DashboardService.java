package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.*;
import com.example.GestionPlanAction.model.PlanAction;
import java.util.List;

public interface DashboardService {
    AdminDashboardStats getAdminStats();
    CollaboratorDashboardStats getCollaboratorStats(Long userId);
    DirectorDashboardStats getDirectorStats();
    List<PlanAction> getPendingValidations();
    List<PlanAction> getUserPlans(Long userId);
    List<Object[]> getServiceLineProgressData();
    List<Object[]> getStatusDistributionData();
}