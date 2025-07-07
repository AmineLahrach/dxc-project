package com.example.GestionPlanAction.config;

import com.example.GestionPlanAction.model.Profil;
import com.example.GestionPlanAction.model.ServiceLine;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.repository.ProfilRepository;
import com.example.GestionPlanAction.repository.ServiceLineRepository;
import com.example.GestionPlanAction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProfilRepository profilRepository;

    @Autowired
    private ServiceLineRepository serviceLineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeProfiles();
        initializeServiceLines();
        initializeDefaultAdmin();
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

    private void initializeDefaultAdmin() {
        if (userRepository.count() == 0) {
            Profil adminProfile = profilRepository.findByNom("ADMINISTRATEUR")
                    .orElseThrow(() -> new RuntimeException("Profil ADMINISTRATEUR non trouvé"));

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

            System.out.println("✅ Administrateur par défaut créé");
            System.out.println("   Username: admin");
            System.out.println("   Password: admin123");
            System.out.println("   Email: admin@example.com");
        }
    }
}