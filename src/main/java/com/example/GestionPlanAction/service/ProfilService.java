package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.ProfilResponseDTO;
import com.example.GestionPlanAction.model.Audit;
import com.example.GestionPlanAction.model.Profil;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.repository.ProfilRepository;
import com.example.GestionPlanAction.repository.UserRepository;
import com.example.GestionPlanAction.security.SecurityUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProfilService {

    @Autowired
    private ProfilRepository profilRepository;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private UserRepository userRepository;

    public List<Profil> getAllProfils() {
        return profilRepository.findAll();
    }

    public Optional<Profil> getProfilById(Long id) {
        return profilRepository.findById(id);
    }
    
    /**
     * Get profile by ID with audit logs for UI display
     * @param id Profile ID
     * @return DTO with profile data and audit logs
     */
    public ProfilResponseDTO getProfilWithAudits(Long id) {
        Profil profil = profilRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Profil introuvable"));
        
        ProfilResponseDTO responseDTO = new ProfilResponseDTO(profil);
        
        // Add audit logs for this profile
        List<Audit> audits = auditService.getAuditsForEntity("Profil", id);
        responseDTO.setAuditLogs(auditService.formatAuditsForDisplay(audits));
        
        return responseDTO;
    }

    public Profil createProfil(Profil profil) {
        Profil savedProfil = profilRepository.save(profil);
        
        // Log the creation action
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            String details = String.format("Created profile: \"%s\"", profil.getNom());
            auditService.logAction("profile_created", currentUser, details, "Profil", savedProfil.getId());
        }
        
        return savedProfil;
    }

    public Profil updateProfil(Long id, Profil updatedProfil) {
        return profilRepository.findById(id)
                .map(profil -> {
                    // Store old name for audit log
                    String oldName = profil.getNom();
                    
                    // Update the field
                    profil.setNom(updatedProfil.getNom());
                    Profil savedProfil = profilRepository.save(profil);
                    
                    // Log the update action
                    Long currentUserId = SecurityUtils.getCurrentUserId();
                    User currentUser = userRepository.findById(currentUserId).orElse(null);
                    if (currentUser != null) {
                        String details = String.format("Updated profile name from \"%s\" to \"%s\"", 
                            oldName, updatedProfil.getNom());
                        auditService.logAction("profile_updated", currentUser, details, "Profil", savedProfil.getId());
                    }
                    
                    return savedProfil;
                })
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));
    }

    public void deleteProfil(Long id) {
        // Get the profile before deletion for audit log
        Optional<Profil> profilOpt = profilRepository.findById(id);
        if (profilOpt.isPresent()) {
            Profil profil = profilOpt.get();
            
            // Delete the profile
            profilRepository.deleteById(id);
            
            // Log the deletion
            Long currentUserId = SecurityUtils.getCurrentUserId();
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null) {
                String details = String.format("Deleted profile: \"%s\"", profil.getNom());
                auditService.logAction("profile_deleted", currentUser, details, "Profil", id);
            }
        } else {
            throw new RuntimeException("Profil introuvable");
        }
    }

    /**
     * Get all profiles with their audit logs for UI display
     * @return List of DTOs with profile data and audit logs
     */
    public List<ProfilResponseDTO> getAllProfilsListWithAudits() {
        List<Profil> profils = profilRepository.findAll();
        List<ProfilResponseDTO> responseDTOs = new ArrayList<>();
        
        for (Profil profil : profils) {
            ProfilResponseDTO responseDTO = new ProfilResponseDTO(profil);
            
            // Add audit logs for this profile
            List<Audit> audits = auditService.getAuditsForEntity("Profil", profil.getId());
            responseDTO.setAuditLogs(auditService.formatAuditsForDisplay(audits));
            
            responseDTOs.add(responseDTO);
        }
        
        return responseDTOs;
    }
}
