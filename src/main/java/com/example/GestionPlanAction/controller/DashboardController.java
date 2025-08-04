package com.example.GestionPlanAction.controller;

import com.example.GestionPlanAction.dto.*;
import com.example.GestionPlanAction.model.PlanAction;
import com.example.GestionPlanAction.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public AdminDashboardStats PlanActionByIdDto() {
        return dashboardService.getAdminStats();
    }

    @GetMapping("/collaborator/stats")
    @PreAuthorize("hasRole('ROLE_COLLABORATEUR')")
    public CollaboratorDashboardStats getCollaboratorStats() {
        return dashboardService.getCollaboratorStats();
    }

    @GetMapping("/director/stats")
    @PreAuthorize("hasRole('DIRECTEUR_GENERAL')")
    public DirectorDashboardStats getDirectorStats() {
        return dashboardService.getDirectorStats();
    }

    @GetMapping("/director/pending-validations")
    @PreAuthorize("hasRole('DIRECTEUR_GENERAL')")
    public List<PlanAction> getPendingValidations() {
        return dashboardService.getPendingValidations();
    }

    @GetMapping("/collaborator/my-plans/{userId}")
    @PreAuthorize("hasRole('COLLABORATOR') or hasRole('ADMINISTRATOR')")
    public List<PlanAction> getUserPlans(@PathVariable Long userId) {
        return dashboardService.getUserPlans(userId);
    }

    @GetMapping("/director/service-line-progress")
    @PreAuthorize("hasRole('GENERAL_DIRECTOR')")
    public List<Object[]> getServiceLineProgress() {
        return dashboardService.getServiceLineProgressData();
    }

    @GetMapping("/director/status-distribution")
    @PreAuthorize("hasRole('GENERAL_DIRECTOR')")
    public List<Object[]> getStatusDistribution() {
        return dashboardService.getStatusDistributionData();
    }

    @PostMapping("/director/validate-plan/{planId}")
    @PreAuthorize("hasRole('GENERAL_DIRECTOR')")
    public PlanAction validatePlan(@PathVariable Long planId) {
        // Implementation for plan validation
        return null;
    }
}