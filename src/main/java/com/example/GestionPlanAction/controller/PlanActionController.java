package com.example.GestionPlanAction.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.GestionPlanAction.dto.PlanActionDTO;
import com.example.GestionPlanAction.dto.PlanStatusUpdateRequest;
import com.example.GestionPlanAction.model.PlanAction;
import com.example.GestionPlanAction.service.PlanActionService;

@RestController
@RequestMapping("/api/plans")
public class PlanActionController {

    @Autowired
    private PlanActionService planActionService;

    @GetMapping
    public List<PlanActionDTO> getAll() {
    	
        return planActionService.getAll();
    }

    @GetMapping("/{id}")
    public PlanAction getById(@PathVariable Long id) {
        return planActionService.getById(id);
    }

    @PostMapping
    public PlanAction create(@RequestBody PlanAction planAction) {
    	System.out.println("Création d'un nouveau PlanAction: " + planAction.toString());
    	
        return planActionService.create(planAction);
    }

    @PutMapping("/{id}")
    public PlanAction update(@PathVariable Long id, @RequestBody PlanAction planAction) {
        return planActionService.update(id, planAction);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        planActionService.delete(id);
    }

    @PatchMapping("/{id}/status")
    public PlanAction updateStatus(@PathVariable Long id, @RequestBody PlanStatusUpdateRequest request) {
        return planActionService.updateStatus(id, request.getStatus());
    }
}
