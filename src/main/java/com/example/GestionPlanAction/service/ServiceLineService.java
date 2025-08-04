package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.ServiceLineResponseDTO;
import com.example.GestionPlanAction.model.ServiceLine;
import java.util.List;

public interface ServiceLineService {
    List<ServiceLine> getAll();
    ServiceLineResponseDTO getById(Long id);
    ServiceLine create(ServiceLine serviceLine);
    ServiceLine update(Long id, ServiceLine updated);
    void delete(Long id);
    List<ServiceLineResponseDTO> getAllServiceListWithAuditLogs();
}
