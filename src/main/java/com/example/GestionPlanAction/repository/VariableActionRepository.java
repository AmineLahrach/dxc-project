package com.example.GestionPlanAction.repository;

import com.example.GestionPlanAction.enums.StatutPlanAction;
import com.example.GestionPlanAction.model.PlanAction;
import com.example.GestionPlanAction.model.VariableAction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VariableActionRepository extends JpaRepository<VariableAction, Long> {
    long countByResponsableId(Long responsableId);
    
    @Query("SELECT COUNT(va) FROM VariableAction va WHERE va.responsable.id = :userId AND va.planAction.statut = :statut")
    long countByResponsableIdAndPlanActionStatut(Long userId, StatutPlanAction statut);
    
    long countByPlanActionStatut(StatutPlanAction statut);

    List<VariableAction> findByPlanActionIdAndVaMereIsNull(Long planActionId);
    List<VariableAction> findByPlanActionAndVaMereIsNull(PlanAction planAction);
    List<VariableAction> findByVaMereIsNull();
    List<VariableAction> findByPlanActionId(Long planActionId);

    @Query("SELECT COALESCE(MAX(va.ordre), 0) FROM VariableAction va WHERE va.vaMere.id = :parentId")
    Integer getMaxOrderForParent(@Param("parentId") Long parentId);

    @Query("SELECT COALESCE(MAX(va.ordre), 0) FROM VariableAction va WHERE va.planAction.id = :planActionId AND va.vaMere IS NULL")
    Integer getMaxOrderForRootLevel(@Param("planActionId") Long planActionId);

    boolean existsByCode(String code);
}
