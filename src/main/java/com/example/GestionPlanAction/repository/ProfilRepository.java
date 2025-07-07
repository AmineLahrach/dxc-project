package com.example.GestionPlanAction.repository;


import com.example.GestionPlanAction.model.Profil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfilRepository extends JpaRepository<Profil, Long> {
    Optional<Profil> findByNom(String nom);
    
    boolean existsByNom(String nom);
}
