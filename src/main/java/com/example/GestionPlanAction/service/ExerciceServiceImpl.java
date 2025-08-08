package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.ExerciceResponseDTO;
import com.example.GestionPlanAction.model.Audit;
import com.example.GestionPlanAction.model.Exercice;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.repository.ExerciceRepository;
import com.example.GestionPlanAction.repository.UserRepository;
import com.example.GestionPlanAction.security.SecurityUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciceServiceImpl implements ExerciceService {

    private final ExerciceRepository repository;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private UserRepository userRepository;

    public ExerciceServiceImpl(ExerciceRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Exercice> getAll() {
        return repository.findAll();
    }

    

    @Override
    public ExerciceResponseDTO getById(Long id) {
        Exercice exercice = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercice introuvable"));
        
        // Create response DTO
        ExerciceResponseDTO responseDTO = new ExerciceResponseDTO(exercice);
        
        // Add audit logs for this exercice
        List<Audit> audits = auditService.getAuditsForEntity("Exercice", id);
        responseDTO.setAuditLogs(auditService.formatAuditsForDisplay(audits));
        
        return responseDTO;
    }

    @Override
    public Exercice create(Exercice exercice) {
        Exercice savedExercice = repository.save(exercice);
        
        // Log the creation action
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            String details = String.format("Created exercice for year %d (Locked: %s)", 
                exercice.getAnnee(), 
                exercice.isVerrouille() ? "Yes" : "No");
            auditService.logAction("exercice_created", currentUser, details, "Exercice", savedExercice.getId());
        }
        
        return savedExercice;
    }

    @Override
    public Exercice update(Long id, Exercice updated) {
        Exercice existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercice introuvable"));
        
        // Store old values for audit log
        Integer oldAnnee = existing.getAnnee();
        boolean oldVerrouille = existing.isVerrouille();
        
        // Update fields
        existing.setAnnee(updated.getAnnee());
        existing.setVerrouille(updated.isVerrouille());
        
        Exercice savedExercice = repository.save(existing);
        
        // Log the update action
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            StringBuilder details = new StringBuilder("Updated exercice: ");
            
            if (!oldAnnee.equals(updated.getAnnee())) {
                details.append("Year changed from ")
                      .append(oldAnnee)
                      .append(" to ")
                      .append(updated.getAnnee())
                      .append("; ");
            }
            
            if (oldVerrouille != updated.isVerrouille()) {
                details.append("Lock status changed from ")
                      .append(oldVerrouille ? "Locked" : "Unlocked")
                      .append(" to ")
                      .append(updated.isVerrouille() ? "Locked" : "Unlocked");
            }
            
            auditService.logAction("exercice_updated", currentUser, details.toString(), "Exercice", savedExercice.getId());
        }
        
        return savedExercice;
    }

    @Override
    public void delete(Long id) {
        // Get the exercice before deletion for audit logging
        Exercice exercice = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercice introuvable"));
                
        // Delete the exercice
        repository.deleteById(id);
        
        // Log the deletion
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            String details = String.format("Deleted exercice for year %d", exercice.getAnnee());
            auditService.logAction("exercice_deleted", currentUser, details, "Exercice", id);
        }
    }

    public Integer getExerciceYearById(Long id) {
        Exercice exercice = repository.findById(id)
            .orElse(null);
        return exercice != null ? exercice.getAnnee() : null;
    }
}