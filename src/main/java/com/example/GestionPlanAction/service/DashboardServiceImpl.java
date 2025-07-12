package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.*;
import com.example.GestionPlanAction.enums.StatutPlanAction;
import com.example.GestionPlanAction.model.PlanAction;
import com.example.GestionPlanAction.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public AdminDashboardStats getAdminStats() {
        return new AdminDashboardStats(
            userRepository.count(),
            planActionRepository.count(),
            serviceLineRepository.count(),
            exerciceRepository.countByVerrouilleIsFalse(),
            profilRepository.count()
        );
    }

    @Override
    public CollaboratorDashboardStats getCollaboratorStats(Long userId) {
        return new CollaboratorDashboardStats(
            variableActionRepository.countByResponsableIdAndPlanActionStatut(
                userId, StatutPlanAction.EN_COURS_PLANIFICATION),
            variableActionRepository.countByResponsableIdAndPlanActionStatut(
                userId, StatutPlanAction.VERROUILLE),
            variableActionRepository.countByResponsableIdAndPlanActionStatut(
                userId, StatutPlanAction.SUIVI_REALISATION),
            variableActionRepository.countByResponsableId(userId)
        );
    }

    @Override
    public DirectorDashboardStats getDirectorStats() {
        long totalPlans = planActionRepository.count();
        long completedPlans = planActionRepository.countByStatut(StatutPlanAction.VERROUILLE);
        double completionRate = totalPlans > 0 ? (completedPlans * 100.0) / totalPlans : 0;
        
        return new DirectorDashboardStats(
            planActionRepository.countByStatut(StatutPlanAction.EN_COURS_PLANIFICATION),
            completionRate,
            planActionRepository.countByStatut(StatutPlanAction.EN_COURS_PLANIFICATION),
            exerciceRepository.count(),
            userRepository.count()
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
