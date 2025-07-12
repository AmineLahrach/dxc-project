package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.enums.StatutPlanAction;
import com.example.GestionPlanAction.model.PlanAction;
import com.example.GestionPlanAction.repository.PlanActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanActionServiceImpl implements PlanActionService {

    @Autowired
    private PlanActionRepository repository;

    @Override
    public List<PlanAction> getAll() {
        return repository.findAll();
    }

    @Override
    public PlanAction getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PlanAction non trouvé"));
    }

    @Override
    public PlanAction create(PlanAction planAction) {
        if (planAction.getStatut() == null) {
            planAction.setStatut(StatutPlanAction.EN_COURS_PLANIFICATION);
        }
        return repository.save(planAction);
    }

    @Override
    public PlanAction update(Long id, PlanAction updated) {
        PlanAction existing = getById(id);
        existing.setTitre(updated.getTitre());
        existing.setDescription(updated.getDescription());
        existing.setStatut(updated.getStatut());
        existing.setExercice(updated.getExercice());
        return repository.save(existing);
    }

    public PlanAction updateStatus(Long id, String status) {
        PlanAction plan = getById(id);
        plan.setStatut(StatutPlanAction.valueOf(status));
        return repository.save(plan);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
