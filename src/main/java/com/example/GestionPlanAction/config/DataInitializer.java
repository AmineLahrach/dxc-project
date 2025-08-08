package com.example.GestionPlanAction.config;

import com.example.GestionPlanAction.dto.PlanActionDTOs.PlanActionCreateDTO;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        System.out.println("🚀 Starting data initialization...");
        
        // Clear existing data if needed (optional - for development only)
        if (shouldCleanExistingData()) {
            cleanExistingData();
        }
        
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
        
        System.out.println("✅ Data initialization completed successfully!");
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
                // Create plan actions with audit logs using DTO
                PlanActionCreateDTO pa1Dto = new PlanActionCreateDTO();
                pa1Dto.setTitre("Improve IT Security");
                pa1Dto.setDescription("Implement new security protocols.");
                pa1Dto.setStatut(com.example.GestionPlanAction.enums.StatutPlanAction.EN_COURS_PLANIFICATION);
                pa1Dto.setExerciceId(ex1.getId());
                PlanAction savedPa1 = planActionService.create(pa1Dto);
                System.out.println("✅ Created plan action: " + savedPa1.getTitre());
                
                PlanActionCreateDTO pa2Dto = new PlanActionCreateDTO();
                pa2Dto.setTitre("Optimize Finance Processes");
                pa2Dto.setDescription("Automate invoice processing.");
                pa2Dto.setStatut(com.example.GestionPlanAction.enums.StatutPlanAction.EN_COURS_PLANIFICATION);
                pa2Dto.setExerciceId(ex2.getId());
                PlanAction savedPa2 = planActionService.create(pa2Dto);
                System.out.println("✅ Created plan action: " + savedPa2.getTitre());
                
                PlanActionCreateDTO pa3Dto = new PlanActionCreateDTO();
                pa3Dto.setTitre("HR Training Program");
                pa3Dto.setDescription("Launch employee training sessions.");
                pa3Dto.setStatut(com.example.GestionPlanAction.enums.StatutPlanAction.EN_COURS_PLANIFICATION);
                pa3Dto.setExerciceId(ex1.getId());
                PlanAction savedPa3 = planActionService.create(pa3Dto);
                System.out.println("✅ Created plan action: " + savedPa3.getTitre());
                
                System.out.println("✅ Plan actions created with audit logs");
            });
        }
    }
    
    private boolean shouldCleanExistingData() {
        // Check if this is a fresh database or development mode
        // You can add a property to control this: app.clean-data-on-startup=true
        long totalRecords = variableActionRepository.count() + planActionRepository.count();
        
        if (totalRecords > 0) {
            System.out.println("⚠️ Found existing data. Records: " + totalRecords);
            // For development, you might want to clean and restart
            // For production, return false
            return false; // Set to true only for development
        }
        return false;
    }
    
    // Method to clean existing data (DEVELOPMENT ONLY)
    private void cleanExistingData() {
        System.out.println("🧹 Cleaning existing data...");
        try {
            variableActionRepository.deleteAll();
            planActionRepository.deleteAll();
            // Don't delete users, profiles, etc. as they're needed for authentication
            System.out.println("✅ Existing variable actions and plan actions cleaned");
        } catch (Exception e) {
            System.err.println("❌ Error cleaning data: " + e.getMessage());
        }
    }
    
    private void initializeVariableActions() {
        // CRITICAL: Check if ANY variable actions exist
        long existingCount = variableActionRepository.count();
        if (existingCount > 0) {
            System.out.println("ℹ️ Variable actions already exist (" + existingCount + " found). Skipping initialization.");
            
            // Optional: Validate existing structure
            validateExistingStructure();
            return;
        }
        
        List<PlanAction> plans = planActionRepository.findAll();
        
        if (plans.isEmpty()) {
            System.out.println("⚠️ No plans found. Skipping variable action initialization.");
            return;
        }
        
        // Get collaborator user
        User collaborator = userRepository.findByUsername("collab")
            .orElseThrow(() -> new RuntimeException("Collaborator user not found"));
        
        runAsUser(collaborator, () -> {
            try {
                System.out.println("🚀 Starting variable action initialization...");
                System.out.println("📊 Found " + plans.size() + " plans to process");
                
                // Process each plan separately to avoid conflicts
                for (int i = 0; i < plans.size(); i++) {
                    PlanAction plan = plans.get(i);
                    System.out.println("\n📋 Processing Plan " + (i + 1) + ": " + plan.getTitre());
                    
                    // Check if this plan already has variables
                    List<VariableAction> existingVars = variableActionRepository.findByPlanActionIdAndVaMereIsNull(plan.getId());
                    if (!existingVars.isEmpty()) {
                        System.out.println("⚠️ Plan already has " + existingVars.size() + " root variables. Skipping.");
                        continue;
                    }
                    
                    switch (i) {
                        case 0:
                            createPlan1Variables(plan, collaborator);
                            break;
                        case 1:
                            createPlan2Variables(plan, collaborator);
                            break;
                        case 2:
                            createPlan3Variables(plan, collaborator);
                            break;
                        default:
                            System.out.println("ℹ️ No template defined for plan " + (i + 1) + ". Skipping.");
                    }
                }
                
                System.out.println("\n🎉 Variable action initialization completed successfully!");
                
                // Validate the created structure
                validateCreatedStructure();
                
            } catch (Exception e) {
                System.err.println("❌ Error during variable action initialization: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to initialize variable actions", e);
            }
        });
    }
    
    private void createPlan1Variables(PlanAction plan, User collaborator) {
        System.out.println("🔧 Creating variables for: " + plan.getTitre());
        
        // Create VA1 (root)
        VariableAction va1 = new VariableAction();
        va1.setDescription("Security Infrastructure");
        va1.setPoids(50.0f);
        va1.setFige(false);
        va1.setResponsable(collaborator);
        va1.setPlanAction(plan);
        va1.setVaMere(null);
        VariableAction savedVa1 = variableActionService.createVariableAction(va1);
        System.out.println("✅ Created: " + savedVa1.getCode() + " - " + savedVa1.getDescription());
        
        // Create VA2 (root)
        VariableAction va2 = new VariableAction();
        va2.setDescription("Network Monitoring");
        va2.setPoids(50.0f);
        va2.setFige(true);
        va2.setResponsable(collaborator);
        va2.setPlanAction(plan);
        va2.setVaMere(null);
        VariableAction savedVa2 = variableActionService.createVariableAction(va2);
        System.out.println("✅ Created: " + savedVa2.getCode() + " - " + savedVa2.getDescription());
        
        // Refresh VA1 from database to ensure relationships are loaded
        savedVa1 = variableActionRepository.findById(savedVa1.getId())
            .orElseThrow(() -> new RuntimeException("Failed to refresh VA1"));
        
        // Create children of VA1
        VariableAction va11 = new VariableAction();
        va11.setDescription("Firewall Upgrade");
        va11.setPoids(60.0f);
        va11.setFige(false);
        va11.setResponsable(collaborator);
        va11.setPlanAction(plan);
        va11.setVaMere(savedVa1);
        VariableAction savedVa11 = variableActionService.createVariableAction(va11);
        System.out.println("✅ Created: " + savedVa11.getCode() + " - " + savedVa11.getDescription());
        
        // VariableAction va12 = new VariableAction();
        // va12.setDescription("Security Audit");
        // va12.setPoids(40.0f);
        // va12.setFige(false);
        // va12.setResponsable(collaborator);
        // va12.setPlanAction(plan);
        // va12.setVaMere(savedVa1);
        // VariableAction savedVa12 = variableActionService.createVariableAction(va12);
        // System.out.println("✅ Created: " + savedVa12.getCode() + " - " + savedVa12.getDescription());
        
        // Refresh VA11 and create grandchild
        savedVa11 = variableActionRepository.findById(savedVa11.getId())
            .orElseThrow(() -> new RuntimeException("Failed to refresh VA11"));
        
        VariableAction va111 = new VariableAction();
        va111.setDescription("Hardware Installation");
        va111.setPoids(100.0f);
        va111.setFige(true);
        va111.setResponsable(collaborator);
        va111.setPlanAction(plan);
        va111.setVaMere(savedVa11);
        VariableAction savedVa111 = variableActionService.createVariableAction(va111);
        System.out.println("✅ Created: " + savedVa111.getCode() + " - " + savedVa111.getDescription() + " (FIXED)");
    }
    
    private void createPlan2Variables(PlanAction plan, User collaborator) {
        System.out.println("🔧 Creating variables for: " + plan.getTitre());
        
        VariableAction va1 = new VariableAction();
        va1.setDescription("Process Automation");
        va1.setPoids(70.0f);
        va1.setFige(false);
        va1.setResponsable(collaborator);
        va1.setPlanAction(plan);
        va1.setVaMere(null);
        VariableAction savedVa1 = variableActionService.createVariableAction(va1);
        System.out.println("✅ Created: " + savedVa1.getCode() + " - " + savedVa1.getDescription());
        
        VariableAction va2 = new VariableAction();
        va2.setDescription("Quality Assurance");
        va2.setPoids(30.0f);
        va2.setFige(false);
        va2.setResponsable(collaborator);
        va2.setPlanAction(plan);
        va2.setVaMere(null);
        VariableAction savedVa2 = variableActionService.createVariableAction(va2);
        System.out.println("✅ Created: " + savedVa2.getCode() + " - " + savedVa2.getDescription());
        
        // Refresh and create child
        savedVa1 = variableActionRepository.findById(savedVa1.getId())
            .orElseThrow(() -> new RuntimeException("Failed to refresh VA1 for plan 2"));
        
        VariableAction va11 = new VariableAction();
        va11.setDescription("Invoice Automation Script");
        va11.setPoids(100.0f);
        va11.setFige(false);
        va11.setResponsable(collaborator);
        va11.setPlanAction(plan);
        va11.setVaMere(savedVa1);
        VariableAction savedVa11 = variableActionService.createVariableAction(va11);
        System.out.println("✅ Created: " + savedVa11.getCode() + " - " + savedVa11.getDescription());
    }
    
    private void createPlan3Variables(PlanAction plan, User collaborator) {
        System.out.println("🔧 Creating variables for: " + plan.getTitre());
        
        VariableAction va1 = new VariableAction();
        va1.setDescription("Human Resources Development");
        va1.setPoids(100.0f);
        va1.setFige(true);
        va1.setResponsable(collaborator);
        va1.setPlanAction(plan);
        va1.setVaMere(null);
        VariableAction savedVa1 = variableActionService.createVariableAction(va1);
        System.out.println("✅ Created: " + savedVa1.getCode() + " - " + savedVa1.getDescription() + " (FIXED)");
        
        // Refresh and create children
        savedVa1 = variableActionRepository.findById(savedVa1.getId())
            .orElseThrow(() -> new RuntimeException("Failed to refresh VA1 for plan 3"));
        
        VariableAction va11 = new VariableAction();
        va11.setDescription("Employee Onboarding Training");
        va11.setPoids(60.0f);
        va11.setFige(false);
        va11.setResponsable(collaborator);
        va11.setPlanAction(plan);
        va11.setVaMere(savedVa1);
        VariableAction savedVa11 = variableActionService.createVariableAction(va11);
        System.out.println("✅ Created: " + savedVa11.getCode() + " - " + savedVa11.getDescription());
        
        VariableAction va12 = new VariableAction();
        va12.setDescription("Performance Management System");
        va12.setPoids(40.0f);
        va12.setFige(false);
        va12.setResponsable(collaborator);
        va12.setPlanAction(plan);
        va12.setVaMere(savedVa1);
        VariableAction savedVa12 = variableActionService.createVariableAction(va12);
        System.out.println("✅ Created: " + savedVa12.getCode() + " - " + savedVa12.getDescription());
    }
    
    // Method to validate existing structure without creating new data
    private void validateExistingStructure() {
        System.out.println("\n📊 Validating Existing Structure:");
        
        List<VariableAction> allVars = variableActionRepository.findAll();
        System.out.println("Total existing variables: " + allVars.size());
        
        // Check for duplicate codes
        Map<String, Long> codeCount = allVars.stream()
            .filter(va -> va.getCode() != null)
            .collect(Collectors.groupingBy(VariableAction::getCode, Collectors.counting()));
        
        codeCount.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .forEach(entry -> System.err.println("⚠️ Duplicate code found: " + entry.getKey() + 
                                               " (appears " + entry.getValue() + " times)"));
        
        if (codeCount.values().stream().allMatch(count -> count == 1)) {
            System.out.println("✅ No duplicate codes found");
        }
    }

    // 3. FIXED VariableActionService - Add duplicate check
    private void generateCodeAndLevel(VariableAction variableAction) {
        if (variableAction.getVaMere() == null) {
            // Root level - find next available number for this plan
            String baseCode = "VA";
            int nextNumber = getNextRootNumber(variableAction.getPlanAction());
            String proposedCode = baseCode + nextNumber;
            
            // CRITICAL: Check if code already exists
            while (variableActionRepository.existsByCode(proposedCode)) {
                nextNumber++;
                proposedCode = baseCode + nextNumber;
                System.out.println("⚠️ Code " + (baseCode + (nextNumber-1)) + " exists, trying " + proposedCode);
            }
            
            variableAction.setCode(proposedCode);
            variableAction.setNiveau(1);
        } else {
            // Child level - append to parent code
            VariableAction parent = variableAction.getVaMere();
            if (parent.getCode() == null) {
                throw new RuntimeException("Parent variable must have a code before creating children");
            }
            String parentCode = parent.getCode();
            int nextChildNumber = getNextChildNumber(parent);
            String proposedCode = parentCode + nextChildNumber;
            
            // CRITICAL: Check if code already exists
            while (variableActionRepository.existsByCode(proposedCode)) {
                nextChildNumber++;
                proposedCode = parentCode + nextChildNumber;
                System.out.println("⚠️ Code " + parentCode + (nextChildNumber-1) + " exists, trying " + proposedCode);
            }
            
            variableAction.setCode(proposedCode);
            variableAction.setNiveau(parent.getNiveau() + 1);
        }
        
    }

    /**
     * FIXED: Get next root number for a specific plan
     */
    private int getNextRootNumber(PlanAction planAction) {
        List<VariableAction> rootVAs = variableActionRepository.findByPlanActionAndVaMereIsNull(planAction);
        if (rootVAs.isEmpty()) {
            return 1;
        }
        
        // Find the highest number used, considering only VA prefix
        int maxNumber = 0;
        for (VariableAction va : rootVAs) {
            if (va.getCode() != null && va.getCode().startsWith("VA")) {
                String numberPart = va.getCode().substring(2); // Remove "VA" prefix
                try {
                    // Extract just the first number part
                    StringBuilder firstNumber = new StringBuilder();
                    for (char c : numberPart.toCharArray()) {
                        if (Character.isDigit(c)) {
                            firstNumber.append(c);
                        } else {
                            break;
                        }
                    }
                    if (firstNumber.length() > 0) {
                        int number = Integer.parseInt(firstNumber.toString());
                        maxNumber = Math.max(maxNumber, number);
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid codes
                    continue;
                }
            }
        }
        return maxNumber + 1;
    }

    /**
     * FIXED: Get next child number for a parent
     */
    private int getNextChildNumber(VariableAction parent) {
        List<VariableAction> children = parent.getSousVAs();
        if (children.isEmpty()) {
            return 1;
        }
        
        String parentCode = parent.getCode();
        int maxNumber = 0;
        
        for (VariableAction child : children) {
            if (child.getCode() != null && child.getCode().startsWith(parentCode)) {
                String childSuffix = child.getCode().substring(parentCode.length());
                try {
                    // Extract the immediate child number
                    StringBuilder childNumber = new StringBuilder();
                    for (char c : childSuffix.toCharArray()) {
                        if (Character.isDigit(c)) {
                            childNumber.append(c);
                        } else {
                            break;
                        }
                    }
                    if (childNumber.length() > 0) {
                        int number = Integer.parseInt(childNumber.toString());
                        maxNumber = Math.max(maxNumber, number);
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid codes
                    continue;
                }
            }
        }
        return maxNumber + 1;
    }

    // 4. Validation method to check created structure
    private void validateCreatedStructure() {
        System.out.println("\n📊 Validating Created Structure:");
        
        List<VariableAction> allVars = variableActionRepository.findAll();
        System.out.println("Total variables created: " + allVars.size());
        
        // Group by plan
        Map<String, List<VariableAction>> byPlan = allVars.stream()
            .collect(Collectors.groupingBy(va -> va.getPlanAction().getTitre()));
        
        for (Map.Entry<String, List<VariableAction>> entry : byPlan.entrySet()) {
            System.out.println("\n📋 Plan: " + entry.getKey());
            
            List<VariableAction> rootVars = entry.getValue().stream()
                .filter(va -> va.getVaMere() == null)
                .sorted(Comparator.comparing(va -> va.getOrdre() != null ? va.getOrdre() : 0))
                .collect(Collectors.toList());
            
            for (VariableAction root : rootVars) {
                printVariableTree(root, "");
            }
        }
    }

    private void printVariableTree(VariableAction va, String indent) {
        System.out.println(indent + "├── " + 
            (va.getCode() != null ? va.getCode() : "NO_CODE") + 
            " - " + va.getDescription() + 
            " (Weight: " + va.getPoids() + "%, Level: " + va.getNiveau() + 
            ", Order: " + (va.getOrdre() != null ? va.getOrdre() : "NULL") + 
            (va.isFige() ? ", FIXED" : "") + ")");
        
        List<VariableAction> children = va.getSousVAs().stream()
            .sorted(Comparator.comparing(child -> child.getOrdre() != null ? child.getOrdre() : 0))
            .collect(Collectors.toList());
        
        for (VariableAction child : children) {
            printVariableTree(child, indent + "    ");
        }
    }
}

