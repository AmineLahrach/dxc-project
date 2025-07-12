package com.example.GestionPlanAction.controller;

import com.example.GestionPlanAction.dto.PlanStatusUpdateRequest;
import com.example.GestionPlanAction.model.PlanAction;
import com.example.GestionPlanAction.service.PlanActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanActionController {

    @Autowired
    private PlanActionService planActionService;

    @GetMapping
    public List<PlanAction> getAll() {
        return planActionService.getAll();
    }

    @GetMapping("/{id}")
    public PlanAction getById(@PathVariable Long id) {
        return planActionService.getById(id);
    }

    @PostMapping
    public PlanAction create(@RequestBody PlanAction planAction) {
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
