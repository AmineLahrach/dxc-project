package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.ServiceLineResponseDTO;
import com.example.GestionPlanAction.model.Audit;
import com.example.GestionPlanAction.model.ServiceLine;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.repository.ServiceLineRepository;
import com.example.GestionPlanAction.repository.UserRepository;
import com.example.GestionPlanAction.security.SecurityUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceLineServiceImpl implements ServiceLineService {

    @Autowired
    private ServiceLineRepository repository;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<ServiceLine> getAll() {
        return repository.findAll();
    }

    @Override
    public ServiceLineResponseDTO getById(Long id) {
        ServiceLine serviceLine = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service Line non trouvée"));
        
        // Create response DTO
        ServiceLineResponseDTO responseDTO = new ServiceLineResponseDTO(serviceLine);
        
        // Add audit logs for this service line
        List<Audit> audits = auditService.getAuditsForEntity("ServiceLine", id);
        responseDTO.setAuditLogs(auditService.formatAuditsForDisplay(audits));
        
        return responseDTO;
    }

    @Override
    public ServiceLine create(ServiceLine serviceLine) {
        ServiceLine savedServiceLine = repository.save(serviceLine);
        
        // Log the creation action
        Long currentUserId = com.example.GestionPlanAction.security.SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            String details = String.format("Created service line: \"%s\"", serviceLine.getNom());
            auditService.logAction("serviceline_created", currentUser, details, "ServiceLine", savedServiceLine.getId());
        }
        
        return savedServiceLine;
    }

    @Override
    public ServiceLine update(Long id, ServiceLine updated) {
        ServiceLine existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service Line non trouvée"));
        
        // Store old name for audit log
        String oldName = existing.getNom();
        
        // Update the fields
        existing.setNom(updated.getNom());
        ServiceLine savedServiceLine = repository.save(existing);
        
        // Log the update action
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            String details = String.format("Updated service line name from \"%s\" to \"%s\"", 
                oldName, updated.getNom());
            auditService.logAction("serviceline_updated", currentUser, details, "ServiceLine", savedServiceLine.getId());
        }
        
        return savedServiceLine;
    }

    @Override
    public void delete(Long id) {
        // Get the service line before deleting for audit log
        ServiceLine serviceLine = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service Line non trouvée"));
        
        // Delete the service line
        repository.deleteById(id);
        
        // Log the deletion action
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            String details = String.format("Deleted service line: \"%s\"", serviceLine.getNom());
            auditService.logAction("serviceline_deleted", currentUser, details, "ServiceLine", id);
        }
    }

    /**
     * Get all service lines with their audit logs for UI display
     * @return List of DTOs with service line data and audit logs
     */
    public List<ServiceLineResponseDTO> getAllServiceListWithAuditLogs() {
        List<ServiceLine> serviceLines = repository.findAll();
        List<ServiceLineResponseDTO> responseDTOs = new ArrayList<>();
        
        for (ServiceLine serviceLine : serviceLines) {
            ServiceLineResponseDTO responseDTO = new ServiceLineResponseDTO(serviceLine);
            
            // Add audit logs for this service line
            List<Audit> audits = auditService.getAuditsForEntity("ServiceLine", serviceLine.getId());
            responseDTO.setAuditLogs(auditService.formatAuditsForDisplay(audits));
            
            responseDTOs.add(responseDTO);
        }
        
        return responseDTOs;
    }
}
