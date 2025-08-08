package com.example.GestionPlanAction.controller;

import com.example.GestionPlanAction.dto.VariableActionCreateDTO;
import com.example.GestionPlanAction.dto.VariableActionDTO;
import com.example.GestionPlanAction.dto.VariableActionDropdownDTO;
import com.example.GestionPlanAction.dto.VariableActionHierarchyDTO;
import com.example.GestionPlanAction.dto.VariableActionResponseDTO;
import com.example.GestionPlanAction.dto.VariableActionUpdateDTO;
import com.example.GestionPlanAction.model.VariableAction;
import com.example.GestionPlanAction.service.VariableActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public VariableActionResponseDTO create(@RequestBody VariableActionCreateDTO dto) {
        return variableActionService.createVariableAction(dto);
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public VariableActionResponseDTO update(@PathVariable Long id, @RequestBody VariableActionUpdateDTO dto) {
        return variableActionService.updateVariableAction(id, dto);
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

    @GetMapping("/hierarchy")
    public List<VariableActionHierarchyDTO> getHierarchy(@RequestParam(required = false) Long planActionId) {
        return variableActionService.getVariableActionHierarchy(planActionId);
    }

    @PostMapping("/{parentId}/children")
    public VariableActionResponseDTO createChild(@PathVariable Long parentId, @RequestBody VariableActionCreateDTO dto) {
        // Set parent ID in the DTO
        dto.setVaMereId(parentId);
        return variableActionService.createVariableAction(dto);
    }

    @PutMapping("/{id}/move")
    public VariableAction moveVariableAction(@PathVariable Long id, @RequestParam(required = false) Long newParentId) {
        return variableActionService.moveVariableAction(id, newParentId);
    }

    @PutMapping("/{parentId}/recalculate-weights")
    public ResponseEntity<String> recalculateWeights(@PathVariable Long parentId) {
        VariableAction parent = variableActionService.getVariableActionById(parentId);
        variableActionService.recalculateParentWeights(parent);
        return ResponseEntity.ok("Weights recalculated successfully");
    }

    /**
     * Get variable actions for dropdown by plan action ID
     */
    @GetMapping("/dropdown/{id}")
    public List<VariableActionDropdownDTO> getForDropdown(@PathVariable Long id) {
        return variableActionService.getVariableActionsByPlanForDropdown(id);
    }
}
