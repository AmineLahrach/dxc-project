package com.example.GestionPlanAction.repository;


import com.example.GestionPlanAction.model.Profil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfilRepository extends JpaRepository<Profil, Long> {
    Optional<Profil> findByNom(String nom);
    
    boolean existsByNom(String nom);

    @Query("SELECT p.nom FROM Profil p JOIN p.utilisateurs u WHERE u.id = :userId")
    List<String> findProfileNamesByUserId(@Param("userId") Long userId);
}
