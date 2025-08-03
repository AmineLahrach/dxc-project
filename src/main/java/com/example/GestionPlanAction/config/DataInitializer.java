package com.example.GestionPlanAction.config;

import com.example.GestionPlanAction.model.Exercice;
import com.example.GestionPlanAction.model.PlanAction;
import com.example.GestionPlanAction.model.Profil;
import com.example.GestionPlanAction.model.ServiceLine;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.model.VariableAction;
import com.example.GestionPlanAction.repository.ExerciceRepository;
import com.example.GestionPlanAction.repository.ProfilRepository;
import com.example.GestionPlanAction.repository.ServiceLineRepository;
import com.example.GestionPlanAction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
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
    private com.example.GestionPlanAction.repository.PlanActionRepository planActionRepository;

    @Autowired
    private com.example.GestionPlanAction.repository.VariableActionRepository variableActionRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeProfiles();
        initializeServiceLines();
        initializeExercices();
        initializeDefaultUsers();
        initializePlanActions();
        initializeVariableActions();
    }

    private void initializeProfiles() {
        if (profilRepository.count() == 0) {
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

    private void initializePlanActions() {
        if (planActionRepository.count() == 0) {
            Exercice ex1 = exerciceRepository.findAll().get(0);
            Exercice ex2 = exerciceRepository.findAll().get(1);

            // Get the admin user (or any user you want as creator)
            User admin = userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals("admin"))
                .findFirst()
                .orElse(null);

            Long adminId = (admin != null) ? admin.getId() : null;

            PlanAction pa1 = new PlanAction();
            pa1.setTitre("Improve IT Security");
            pa1.setDescription("Implement new security protocols.");
            pa1.setStatut(com.example.GestionPlanAction.enums.StatutPlanAction.EN_COURS_PLANIFICATION);
            pa1.setExercice(ex1);
            pa1.setCreatedBy(adminId);
            planActionRepository.save(pa1);

            PlanAction pa2 = new PlanAction();
            pa2.setTitre("Optimize Finance Processes");
            pa2.setDescription("Automate invoice processing.");
            pa2.setStatut(com.example.GestionPlanAction.enums.StatutPlanAction.EN_COURS_PLANIFICATION);
            pa2.setExercice(ex2);
            pa2.setCreatedBy(adminId); 
            planActionRepository.save(pa2);

            PlanAction pa3 = new PlanAction();
            pa3.setTitre("HR Training Program");
            pa3.setDescription("Launch employee training sessions.");
            pa3.setStatut(com.example.GestionPlanAction.enums.StatutPlanAction.EN_COURS_PLANIFICATION);
            pa3.setExercice(ex1);
            pa3.setCreatedBy(adminId);
            planActionRepository.save(pa3);

            System.out.println("✅ PlanActions initialisés");
        }
    }

    private void initializeVariableActions() {
        if (variableActionRepository.count() == 0) {
            List<PlanAction> plans = planActionRepository.findAll();
            User admin = userRepository.findAll().stream().filter(u -> u.getUsername().equals("admin")).findFirst().orElse(null);

            VariableAction va1 = new VariableAction();
            va1.setDescription("Firewall Upgrade");
            va1.setPoids(1.0f);
            va1.setFige(false);
            va1.setNiveau(1);
            va1.setResponsable(admin);
            va1.setPlanAction(plans.get(0));
            variableActionRepository.save(va1);

            VariableAction va2 = new VariableAction();
            va2.setDescription("Invoice Automation Script");
            va2.setPoids(2.0f);
            va2.setFige(false);
            va2.setNiveau(2);
            va2.setResponsable(admin);
            va2.setPlanAction(plans.get(1));
            variableActionRepository.save(va2);

            VariableAction va3 = new VariableAction();
            va3.setDescription("Employee Onboarding Training");
            va3.setPoids(1.5f);
            va3.setFige(true);
            va3.setNiveau(1);
            va3.setResponsable(admin);
            va3.setPlanAction(plans.get(2));
            variableActionRepository.save(va3);

            System.out.println("✅ VariableActions initialisés");
        }
    }
}