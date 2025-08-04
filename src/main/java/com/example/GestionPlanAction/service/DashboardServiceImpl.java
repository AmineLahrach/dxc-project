package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.*;
import com.example.GestionPlanAction.enums.StatutPlanAction;
import com.example.GestionPlanAction.model.Audit;
import com.example.GestionPlanAction.model.PlanAction;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.repository.*;
import com.example.GestionPlanAction.security.SecurityUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PlanActionRepository planActionRepository;
    
    @Autowired
    private ServiceLineRepository serviceLineRepository;
    
    @Autowired
    private ExerciceRepository exerciceRepository;
    
    @Autowired
    private ProfilRepository profilRepository;
    
    @Autowired
    private VariableActionRepository variableActionRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private AuditService auditService;

    @Override
    public AdminDashboardStats getAdminStats() {
        List<Audit> recentAudits = auditRepository.findTop10ByOrderByDateDesc();
        List<Map<String, Object>> formattedAudits = auditService.formatAuditsForDisplay(recentAudits);
        
        return new AdminDashboardStats(
            userRepository.count(),
            planActionRepository.count(),
            serviceLineRepository.count(),
            exerciceRepository.countByVerrouilleIsFalse(),
            profilRepository.count(),
            formattedAudits
        );
    }

    @Override
    public CollaboratorDashboardStats getCollaboratorStats() {
        
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("No authenticated user found");
        }
        List<Audit> userAudits = auditRepository.findTop10ByUtilisateurIdOrderByDateDesc(userId);
        List<Map<String, Object>> formattedAudits = auditService.formatAuditsForDisplay(userAudits);
        
        return new CollaboratorDashboardStats(
            variableActionRepository.countByResponsableIdAndPlanActionStatut(
                userId, StatutPlanAction.EN_COURS_PLANIFICATION),
            variableActionRepository.countByResponsableIdAndPlanActionStatut(
                userId, StatutPlanAction.VERROUILLE),
            variableActionRepository.countByResponsableIdAndPlanActionStatut(
                userId, StatutPlanAction.SUIVI_REALISATION),
            variableActionRepository.countByResponsableId(userId),
            planActionRepository.countByVariableActionsResponsableId(userId), // Total plans
            variableActionRepository.count(), // Total variables in the system
            formattedAudits
        );
    }

    @Override
    public DirectorDashboardStats getDirectorStats() {
        // Get current user ID
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("No authenticated user found");
        }
        
        // Get recent audits for the director
        List<Audit> directorAudits = auditRepository.findTop10ByUtilisateurIdOrderByDateDesc(userId);
        List<Map<String, Object>> formattedAudits = auditService.formatAuditsForDisplay(directorAudits);
        
        // Get variable action counts by status for ALL variables (not filtered by user)
        long planningVariables = variableActionRepository.countByPlanActionStatut(StatutPlanAction.EN_COURS_PLANIFICATION);
        long lockedVariables = variableActionRepository.countByPlanActionStatut(StatutPlanAction.VERROUILLE);
        long monitoringVariables = variableActionRepository.countByPlanActionStatut(StatutPlanAction.SUIVI_REALISATION);
        long totalVariables = variableActionRepository.count();
        long totalPlans = planActionRepository.count();
        
        // Calculate completion rate
        long completedPlans = planActionRepository.countByStatut(StatutPlanAction.VERROUILLE);
        double completionRate = totalPlans > 0 ? (completedPlans * 100.0) / totalPlans : 0;
        
        return new DirectorDashboardStats(
            planningVariables,
            lockedVariables,
            monitoringVariables,
            totalVariables,
            totalPlans,
            completionRate,
            formattedAudits
        );
    }

    @Override
    public List<PlanAction> getPendingValidations() {
        return planActionRepository.findByStatut(StatutPlanAction.EN_COURS_PLANIFICATION);
    }

    @Override
    public List<PlanAction> getUserPlans(Long userId) {
        return planActionRepository.findByVariableActionsResponsableId(userId);
    }

    @Override
    public List<Object[]> getServiceLineProgressData() {
        return planActionRepository.getProgressByServiceLine();
    }

    @Override
    public List<Object[]> getStatusDistributionData() {
        return planActionRepository.getStatusDistribution();
    }
}
