package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.model.VariableAction;
import com.example.GestionPlanAction.repository.VariableActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VariableActionService {

    @Autowired
    private VariableActionRepository variableActionRepository;

    // ✅ Récupérer toutes les variables d'action
    public List<VariableAction> getAllVariableActions() {
        return variableActionRepository.findAll();
    }

    // ✅ Récupérer une variable d'action par ID
    public VariableAction getVariableActionById(Long id) {
        return variableActionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variable d'action non trouvée avec l'ID : " + id));
    }

    // ✅ Créer une nouvelle variable d'action
    public VariableAction createVariableAction(VariableAction variableAction) {
        return variableActionRepository.save(variableAction);
    }

    // ✅ Mettre à jour une variable d'action existante
    public VariableAction updateVariableAction(Long id, VariableAction updated) {
        VariableAction existing = getVariableActionById(id);

        existing.setDescription(updated.getDescription());
        existing.setPoids(updated.getPoids());
        existing.setFige(updated.isFige());
        existing.setNiveau(updated.getNiveau());
        existing.setVaMere(updated.getVaMere());
        existing.setResponsable(updated.getResponsable());
        existing.setPlanAction(updated.getPlanAction());

        return variableActionRepository.save(existing);
    }

    // ✅ Supprimer une variable d'action par ID
    public void deleteVariableAction(Long id) {
        variableActionRepository.deleteById(id);
    }
}
