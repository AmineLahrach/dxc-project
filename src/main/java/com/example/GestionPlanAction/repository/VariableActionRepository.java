package com.example.GestionPlanAction.repository;

import com.example.GestionPlanAction.enums.StatutPlanAction;
import com.example.GestionPlanAction.model.VariableAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VariableActionRepository extends JpaRepository<VariableAction, Long> {
    long countByResponsableId(Long responsableId);
    
    @Query("SELECT COUNT(va) FROM VariableAction va WHERE va.responsable.id = :userId AND va.planAction.statut = :statut")
    long countByResponsableIdAndPlanActionStatut(Long userId, StatutPlanAction statut);
}
