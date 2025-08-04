package com.example.GestionPlanAction.controller;

import com.example.GestionPlanAction.dto.VariableActionDTO;
import com.example.GestionPlanAction.dto.VariableActionResponseDTO;
import com.example.GestionPlanAction.dto.VariableReponseDTO;
import com.example.GestionPlanAction.model.VariableAction;
import com.example.GestionPlanAction.service.VariableActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variable-actions")
@CrossOrigin(origins = "*")
public class VariableActionController {

    @Autowired
    private VariableActionService variableActionService;

    // ✅ GET all
    @GetMapping
    public List<VariableActionDTO> getAll() {
        return variableActionService.getAllVariableActionDTOs();
    }

    // ✅ GET by ID
    @GetMapping("/{id}")
    public VariableActionResponseDTO getById(@PathVariable Long id) {
        return variableActionService.getVariableActionWithAudits(id);
    }

    // ✅ GET by ID
    @GetMapping("/edit/{id}")
    public VariableActionResponseDTO getByIdForEdit(@PathVariable Long id) {
        return variableActionService.getVariableActionWithAudits(id);
    }

    // ✅ CREATE
    @PostMapping
    public VariableAction create(@RequestBody VariableAction variableAction) {
        return variableActionService.createVariableAction(variableAction);
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public VariableAction update(@PathVariable Long id, @RequestBody VariableAction updated) {
        return variableActionService.updateVariableAction(id, updated);
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        variableActionService.deleteVariableAction(id);
    }

    // ✅ PATCH update fige
    @PatchMapping("/{id}/freeze")
    public VariableAction updateFige(@PathVariable Long id, @RequestBody boolean fige) {
        return variableActionService.updateFige(id, fige);
    }
}
