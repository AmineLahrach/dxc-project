package com.example.GestionPlanAction.controller;

import com.example.GestionPlanAction.dto.ServiceLineResponseDTO;
import com.example.GestionPlanAction.model.ServiceLine;
import com.example.GestionPlanAction.service.ServiceLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicelines")
public class ServiceLineController {

    @Autowired
    private ServiceLineService service;

    @GetMapping
    public List<ServiceLine> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ServiceLineResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public ServiceLine create(@RequestBody ServiceLine s) {
        return service.create(s);
    }

    @PutMapping("/{id}")
    public ServiceLine update(@PathVariable Long id, @RequestBody ServiceLine s) {
        return service.update(id, s);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/with-audits")
    public ResponseEntity<List<ServiceLineResponseDTO>> getAllServiceLinesWithAudits() {
        List<ServiceLineResponseDTO> serviceLines = service.getAllServiceListWithAuditLogs();
        return ResponseEntity.ok(serviceLines);
    }
}
