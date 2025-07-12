package com.example.GestionPlanAction.repository;

import com.example.GestionPlanAction.model.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {
    long countByVerrouilleIsFalse();
}