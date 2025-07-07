package com.example.GestionPlanAction.controller;

import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/all")
    public ResponseEntity<String> allAccess() {
        return ResponseEntity.ok("Contenu public accessible sans authentification.");
    }

    @GetMapping("/user")
    // @PreAuthorize("hasRole('COLLABORATEUR') or hasRole('ADMINISTRATEUR') or hasRole('DIRECTEUR_GENERAL')")
    public ResponseEntity<String> userAccess() {
        return ResponseEntity.ok("Contenu utilisateur - Accès autorisé pour les utilisateurs authentifiés.");
    }

    @GetMapping("/collaborator")
    // @PreAuthorize("hasRole('COLLABORATEUR')")
    public ResponseEntity<String> collaboratorAccess() {
        return ResponseEntity.ok("Contenu collaborateur - Accès autorisé pour les collaborateurs.");
    }

    @GetMapping("/director")
    // @PreAuthorize("hasRole('DIRECTEUR_GENERAL')")
    public ResponseEntity<String> directorAccess() {
        return ResponseEntity.ok("Contenu directeur - Accès autorisé pour les directeurs généraux.");
    }

    @GetMapping("/admin")
    // @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<String> adminAccess() {
        return ResponseEntity.ok("Contenu administrateur - Accès autorisé pour les administrateurs uniquement.");
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ Application is running successfully!");
    }
}