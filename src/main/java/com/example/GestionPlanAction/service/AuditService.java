package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.model.Audit;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.model.PlanAction;
import com.example.GestionPlanAction.repository.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
    
    /**
     * Log plan action changes
     * @param action The type of action performed (created, updated, etc)
     * @param user The user who performed the action
     * @param planAction The plan that was modified
     * @param changeDetails Additional details about the change
     */
    public void logPlanAction(String action, User user, PlanAction planAction, String changeDetails) {
        String details = changeDetails != null ? changeDetails : "";
        logAction(action, user, details, "PlanAction", planAction.getId());
    }
    
    /**
     * Log status change for a plan
     * @param user The user who changed the status
     * @param planAction The plan that was modified
     * @param oldStatus Previous status
     * @param newStatus New status
     */
    public void logStatusChange(User user, PlanAction planAction, String oldStatus, String newStatus) {
        String details = String.format("Changed status from \"%s\" to \"%s\"", oldStatus, newStatus);
        logAction("status_update", user, details, "PlanAction", planAction.getId());
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

    /**
     * Get all audit logs for a specific plan
     * @param planId ID of the plan
     * @return List of audits for the plan
     */
    public List<Audit> getAuditsForPlan(Long planId) {
        return auditRepository.findByEntityTypeAndEntityIdOrderByDateDesc("PlanAction", planId);
    }

    public Long getUserActionCount(User user, LocalDateTime since) {
        return auditRepository.countUserActionsAfter(user, since);
    }
    
    /**
     * Format audit logs for UI display in the format shown in the sample
     * @param audits List of audit logs
     * @return List of formatted audit entries for display
     */
    public List<Map<String, Object>> formatAuditsForDisplay(List<Audit> audits) {
        List<Map<String, Object>> formattedAudits = new java.util.ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'days ago'");
        
        for (Audit audit : audits) {
            Map<String, Object> entry = new HashMap<>();
            
            // User information
            Map<String, Object> user = new HashMap<>();
            user.put("id", audit.getUtilisateur().getId());
            user.put("name", audit.getUtilisateur().getNom() + " " + audit.getUtilisateur().getPrenom());
            user.put("initials", getInitials(audit.getUtilisateur().getNom(), audit.getUtilisateur().getPrenom()));
            
            entry.put("user", user);
            entry.put("action", audit.getAction());
            entry.put("date", formatTimeAgo(audit.getDate()));
            entry.put("details", audit.getDetails());
            
            formattedAudits.add(entry);
        }
        
        return formattedAudits;
    }
    
    /**
     * Get initials from user name
     */
    private String getInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            initials.append(firstName.charAt(0));
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials.append(lastName.charAt(0));
        }
        return initials.toString().toUpperCase();
    }
    
    /**
     * Format time as relative to current time
     */
    private String formatTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(dateTime, now);
        
        if (daysBetween < 1) {
            return "today";
        } else if (daysBetween == 1) {
            return "1 day ago";
        } else {
            return daysBetween + " days ago";
        }
    }
}