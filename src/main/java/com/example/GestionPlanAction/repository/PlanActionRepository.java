package com.example.GestionPlanAction.repository;

import com.example.GestionPlanAction.model.PlanAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanActionRepository extends JpaRepository<PlanAction, Long> {
}