package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.PlanActionDTO;
import com.example.GestionPlanAction.model.PlanAction;

import java.util.List;

public interface PlanActionService {
    List<PlanActionDTO> getAll();
    PlanAction getById(Long id);
    PlanAction create(PlanAction planAction);
    PlanAction update(Long id, PlanAction updated);
    PlanAction updateStatus(Long id, String status);
    void delete(Long id);
   
}