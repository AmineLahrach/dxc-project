package com.example.GestionPlanAction.config;

import com.example.GestionPlanAction.model.*;
import com.example.GestionPlanAction.repository.*;
import com.example.GestionPlanAction.security.UserPrincipal;
import com.example.GestionPlanAction.service.ExerciceService;
import com.example.GestionPlanAction.service.PlanActionService;
import com.example.GestionPlanAction.service.ProfilService;
import com.example.GestionPlanAction.service.ServiceLineService;
import com.example.GestionPlanAction.service.VariableActionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProfilRepository profilRepository;

    @Autowired
    private ServiceLineRepository serviceLineRepository;

    @Autowired
    private ExerciceRepository exerciceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PlanActionRepository planActionRepository;

    @Autowired
    private VariableActionRepository variableActionRepository;

    @Autowired
    private ProfilService profilService;

    @Autowired
    private ServiceLineService serviceLineService;

    @Autowired
    private ExerciceService exerciceService;

    @Autowired
    private PlanActionService planActionService;

    @Autowired
    private VariableActionService variableActionService;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Initialize basic entities first
        initializeProfiles();
        initializeServiceLines();
        initializeExercices();
        initializeDefaultUsers();
        
        // Now initialize entities with proper audit logs using the service methods
        initializeAuditEnabledProfiles();
        initializeAuditEnabledServiceLines();
        initializeAuditEnabledExercices();
        initializePlanActions();
        initializeVariableActions();
    }
    
    private void initializeProfiles() {
        if (profilRepository.count() == 0) {
            // First create the profiles directly to bootstrap the system
            Profil admin = new Profil();
            admin.setNom("ADMINISTRATEUR");
            profilRepository.save(admin);

            Profil collaborator = new Profil();
            collaborator.setNom("COLLABORATEUR");
            profilRepository.save(collaborator);

            Profil director = new Profil();
            director.setNom("DIRECTEUR_GENERAL");
            profilRepository.save(director);

            System.out.println("✅ Profils initialisés");
        }
    }

    private void initializeServiceLines() {
        if (serviceLineRepository.count() == 0) {
            // First create the service lines directly to bootstrap the system
            ServiceLine it = new ServiceLine();
            it.setNom("Technologies de l'Information");
            serviceLineRepository.save(it);

            ServiceLine finance = new ServiceLine();
            finance.setNom("Finance");
            serviceLineRepository.save(finance);

            ServiceLine hr = new ServiceLine();
            hr.setNom("Ressources Humaines");
            serviceLineRepository.save(hr);

            ServiceLine operations = new ServiceLine();
            operations.setNom("Opérations");
            serviceLineRepository.save(operations);

            System.out.println("✅ Lignes de service initialisées");
        }
    }

    private void initializeExercices() {
        if (exerciceRepository.count() == 0) {
            // First create the exercices directly to bootstrap the system
            Exercice ex1 = new Exercice();
            ex1.setAnnee(2024);
            ex1.setVerrouille(false);
            exerciceRepository.save(ex1);

            Exercice ex2 = new Exercice();
            ex2.setAnnee(2025);
            ex2.setVerrouille(true);
            exerciceRepository.save(ex2);

            System.out.println("✅ Exercices initialisés");
        }
    }

    private void initializeDefaultUsers() {
        if (userRepository.count() == 0) {
            // Create users directly first since we need them for authentication
            Profil adminProfile = profilRepository.findByNom("ADMINISTRATEUR")
                    .orElseThrow(() -> new RuntimeException("Profil ADMINISTRATEUR non trouvé"));
            Profil collaboratorProfile = profilRepository.findByNom("COLLABORATEUR")
                    .orElseThrow(() -> new RuntimeException("Profil COLLABORATEUR non trouvé"));
            Profil directorProfile = profilRepository.findByNom("DIRECTEUR_GENERAL")
                    .orElseThrow(() -> new RuntimeException("Profil DIRECTEUR_GENERAL non trouvé"));

            ServiceLine itServiceLine = serviceLineRepository.findAll().get(0);

            User admin = new User();
            admin.setNom("Admin");
            admin.setPrenom("Super");
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setMotDePasse(passwordEncoder.encode("admin123"));
            admin.setProfils(Set.of(adminProfile));
            admin.setServiceLine(itServiceLine);
            admin.setActif(true);
            userRepository.save(admin);

            User collaborator = new User();
            collaborator.setNom("Collab");
            collaborator.setPrenom("User");
            collaborator.setUsername("collab");
            collaborator.setEmail("collab@example.com");
            collaborator.setMotDePasse(passwordEncoder.encode("collab123"));
            collaborator.setProfils(Set.of(collaboratorProfile));
            collaborator.setServiceLine(itServiceLine);
            collaborator.setActif(true);
            userRepository.save(collaborator);

            User director = new User();
            director.setNom("Director");
            director.setPrenom("General");
            director.setUsername("director");
            director.setEmail("director@example.com");
            director.setMotDePasse(passwordEncoder.encode("director123"));
            director.setProfils(Set.of(directorProfile));
            director.setServiceLine(itServiceLine);
            director.setActif(true);
            userRepository.save(director);

            System.out.println("✅ Utilisateurs par défaut créés");
            System.out.println("   Admin: admin/admin123");
            System.out.println("   Collaborator: collab/collab123");
            System.out.println("   Director: director/director123");
        }
    }

    // Now let's add audit-enabled initialization methods

    // Method to create authenticated context for a user
    private void runAsUser(User user, Runnable action) {
        // Store the current authentication
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            // Create authentication using UserPrincipal for the specified user
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            Authentication auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
            
            // Set the authentication
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            // Run the action
            action.run();
        } finally {
            // Restore the original authentication
            SecurityContextHolder.getContext().setAuthentication(existingAuth);
        }
    }

    private void initializeAuditEnabledProfiles() {
        // Get admin user
        User admin = userRepository.findByUsername("admin")
            .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        runAsUser(admin, () -> {
            // Get existing profiles and update them to create audit logs
            List<Profil> existingProfiles = profilRepository.findAll();
            
            for (Profil profile : existingProfiles) {
                // Update an existing property to trigger an audit log
                String currentName = profile.getNom();
                profile.setNom(currentName); // Set to same value to not change it, but trigger audit
                profilService.updateProfil(profile.getId(), profile);
            }
            
            System.out.println("✅ Audit logs created for existing profiles");
        });
    }
    
    private void initializeAuditEnabledServiceLines() {
        // Get admin user
        User admin = userRepository.findByUsername("admin")
            .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        runAsUser(admin, () -> {
            // Get existing service lines and update them to create audit logs
            List<ServiceLine> existingServiceLines = serviceLineRepository.findAll();
            
            for (ServiceLine serviceLine : existingServiceLines) {
                // Update an existing property to trigger an audit log
                String currentName = serviceLine.getNom();
                serviceLine.setNom(currentName); // Set to same value to not change it, but trigger audit
                serviceLineService.update(serviceLine.getId(), serviceLine);
            }
            
            System.out.println("✅ Audit logs created for existing service lines");
        });
    }
    
    private void initializeAuditEnabledExercices() {
        // Get admin user
        User admin = userRepository.findByUsername("admin")
            .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        runAsUser(admin, () -> {
            // Get existing exercices and update them to create audit logs
            Exercice ex2024 = exerciceRepository.findByAnnee(2024);
            Exercice ex2025 = exerciceRepository.findByAnnee(2025);
            
            if (ex2024 != null) {
                // Update an existing property to trigger an audit log
                boolean currentStatus = ex2024.isVerrouille();
                ex2024.setVerrouille(currentStatus); // Set to same value to not change it, but trigger audit
                exerciceService.update(ex2024.getId(), ex2024);
                System.out.println("✅ Audit log created for 2024 exercice");
            }
            
            if (ex2025 != null) {
                // Update an existing property to trigger an audit log
                boolean currentStatus = ex2025.isVerrouille();
                ex2025.setVerrouille(currentStatus); // Set to same value to not change it, but trigger audit
                exerciceService.update(ex2025.getId(), ex2025);
                System.out.println("✅ Audit log created for 2025 exercice");
            }
            
            System.out.println("✅ Audit logs created for existing exercices");
        });
    }
    
    private void initializePlanActions() {
        if (planActionRepository.count() == 0) {
            Exercice ex1 = exerciceRepository.findByAnnee(2024);
            Exercice ex2 = exerciceRepository.findByAnnee(2025);
            
            // Get director user to create plan actions
            User director = userRepository.findByUsername("director")
                .orElseThrow(() -> new RuntimeException("Director user not found"));
            
            runAsUser(director, () -> {
                // Create plan actions with audit logs
                PlanAction pa1 = new PlanAction();
                pa1.setTitre("Improve IT Security");
                pa1.setDescription("Implement new security protocols.");
                pa1.setStatut(com.example.GestionPlanAction.enums.StatutPlanAction.EN_COURS_PLANIFICATION);
                pa1.setExercice(ex1);
                planActionService.create(pa1);
                
                PlanAction pa2 = new PlanAction();
                pa2.setTitre("Optimize Finance Processes");
                pa2.setDescription("Automate invoice processing.");
                pa2.setStatut(com.example.GestionPlanAction.enums.StatutPlanAction.EN_COURS_PLANIFICATION);
                pa2.setExercice(ex2);
                planActionService.create(pa2);
                
                PlanAction pa3 = new PlanAction();
                pa3.setTitre("HR Training Program");
                pa3.setDescription("Launch employee training sessions.");
                pa3.setStatut(com.example.GestionPlanAction.enums.StatutPlanAction.EN_COURS_PLANIFICATION);
                pa3.setExercice(ex1);
                planActionService.create(pa3);
                
                System.out.println("✅ Plan actions created with audit logs");
            });
        }
    }
    
    private void initializeVariableActions() {
        if (variableActionRepository.count() == 0) {
            List<PlanAction> plans = planActionRepository.findAll();
            
            // Get collaborator user to create variable actions
            User collaborator = userRepository.findByUsername("collab")
                .orElseThrow(() -> new RuntimeException("Collaborator user not found"));
            
            runAsUser(collaborator, () -> {
                // Create variable actions with audit logs
                VariableAction va1 = new VariableAction();
                va1.setDescription("Firewall Upgrade");
                va1.setPoids(1.0f);
                va1.setFige(false);
                va1.setNiveau(1);
                va1.setResponsable(collaborator);
                va1.setPlanAction(plans.get(0));
                variableActionService.createVariableAction(va1);
                
                VariableAction va2 = new VariableAction();
                va2.setDescription("Invoice Automation Script");
                va2.setPoids(2.0f);
                va2.setFige(false);
                va2.setNiveau(2);
                va2.setResponsable(collaborator);
                va2.setPlanAction(plans.get(1));
                variableActionService.createVariableAction(va2);
                
                VariableAction va3 = new VariableAction();
                va3.setDescription("Employee Onboarding Training");
                va3.setPoids(1.5f);
                va3.setFige(true);
                va3.setNiveau(1);
                va3.setResponsable(collaborator);
                va3.setPlanAction(plans.get(2));
                variableActionService.createVariableAction(va3);
                
                System.out.println("✅ Variable actions created with audit logs");
            });
        }
    }
}