package com.example.GestionPlanAction.repository;

import com.example.GestionPlanAction.model.ServiceLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceLineRepository extends JpaRepository<ServiceLine, Long> {
}