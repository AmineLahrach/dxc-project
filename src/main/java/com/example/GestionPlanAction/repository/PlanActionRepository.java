package com.example.GestionPlanAction.repository;

import com.example.GestionPlanAction.enums.StatutPlanAction;
import com.example.GestionPlanAction.model.PlanAction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanActionRepository extends JpaRepository<PlanAction, Long> {
    long countByStatut(StatutPlanAction statut);
    
    List<PlanAction> findByStatut(StatutPlanAction statut);
    
    @Query("SELECT p FROM PlanAction p JOIN p.variableActions va WHERE va.responsable.id = :userId")
    List<PlanAction> findByVariableActionsResponsableId(Long userId);
    
    @Query("SELECT sl.nom, COUNT(p), AVG(CASE WHEN p.statut = 'VERROUILLE' THEN 100 ELSE 0 END) " +
           "FROM PlanAction p JOIN p.variableActions va JOIN va.responsable.serviceLine sl " +
           "GROUP BY sl.nom")
    List<Object[]> getProgressByServiceLine();
    
    @Query("SELECT p.statut, COUNT(p) FROM PlanAction p GROUP BY p.statut")
    List<Object[]> getStatusDistribution();

    
    @Query(value = "SELECT p.* , v.* ,e.* ,u.* FROM plan_action p left join variable_action v on p.id=v.plan_action_id left join exercice e on e.id=p.id left join user u on u.id=v.responsable_id",nativeQuery = true)
	List<Object[]> findAllplans();
}