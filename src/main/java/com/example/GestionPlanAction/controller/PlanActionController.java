package com.example.GestionPlanAction.controller;

import com.example.GestionPlanAction.dto.PlanActionByIdDto;
import com.example.GestionPlanAction.dto.PlanActionResponse;
import com.example.GestionPlanAction.dto.PlanActionTreeResponse;
import com.example.GestionPlanAction.dto.PlanStatusUpdateRequest;
import com.example.GestionPlanAction.dto.PlanActionDTOs.PlanActionCreateDTO;
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
    public List<PlanActionResponse> getAll() {
        return planActionService.getAllWithCreatedByName();
    }

    @GetMapping("/{id}")
    public PlanActionByIdDto getById(@PathVariable Long id) {
        return planActionService.getByIdWithDetails(id);
    }

    @PostMapping
    public PlanAction create(@RequestBody PlanActionCreateDTO planActionDTO) {
        return planActionService.create(planActionDTO);
    }

    @PutMapping("/{id}")
    public PlanActionByIdDto update(@PathVariable Long id, @RequestBody PlanActionCreateDTO planAction) {
        return planActionService.update(id, planAction);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        planActionService.delete(id);
    }

    @PatchMapping("/{id}/status")
    public PlanActionByIdDto updateStatus(@PathVariable Long id, @RequestBody PlanStatusUpdateRequest request) {
        return planActionService.updateStatus(id, request.getStatus());
    }

    @GetMapping("/tree")
    public List<PlanActionTreeResponse> getPlanActionTree() {
        return planActionService.getPlanActionsTree();
    }
}
