package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.model.Audit;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.repository.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    @Autowired
    private AuditRepository auditRepository;

    public void logAction(String action, User utilisateur, String details) {
        logAction(action, utilisateur, details, null, null);
    }

    public void logAction(String action, User utilisateur, String details, String entityType, Long entityId) {
        Audit audit = Audit.builder()
                .action(action)
                .utilisateur(utilisateur)
                .details(details)
                .entityType(entityType)
                .entityId(entityId)
                .date(LocalDateTime.now())
                .build();
        
        auditRepository.save(audit);
    }

    public List<Audit> getAllAudits() {
        return auditRepository.findAll();
    }

    public Page<Audit> getAuditsByUser(User utilisateur, Pageable pageable) {
        return auditRepository.findByUtilisateurOrderByDateDesc(utilisateur, pageable);
    }

    public List<Audit> getAuditsByAction(String action) {
        return auditRepository.findByActionContainingIgnoreCaseOrderByDateDesc(action);
    }

    public List<Audit> getAuditsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditRepository.findByDateBetweenOrderByDateDesc(startDate, endDate);
    }

    public List<Audit> getAuditsForEntity(String entityType, Long entityId) {
        return auditRepository.findByEntityTypeAndEntityIdOrderByDateDesc(entityType, entityId);
    }

    public Long getUserActionCount(User user, LocalDateTime since) {
        return auditRepository.countUserActionsAfter(user, since);
    }
}