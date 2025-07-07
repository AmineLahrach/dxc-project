package com.example.GestionPlanAction.repository;

import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.model.ServiceLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Authentication methods
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    // Check existence for validation
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    // Active users only
    List<User> findByActifTrue();
    
    Page<User> findByActifTrue(Pageable pageable);
    
    // Users by service line
    List<User> findByServiceLineAndActifTrue(ServiceLine serviceLine);
    
    // Users by profile
    @Query("SELECT u FROM User u JOIN u.profils p WHERE p.nom = :profilName AND u.actif = true")
    List<User> findByProfilNameAndActifTrue(@Param("profilName") String profilName);
    
    // Search functionality
    @Query("SELECT u FROM User u WHERE u.actif = true AND " +
           "(LOWER(u.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchActiveUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Count active users by service line
    @Query("SELECT COUNT(u) FROM User u WHERE u.serviceLine = :serviceLine AND u.actif = true")
    Long countActiveUsersByServiceLine(@Param("serviceLine") ServiceLine serviceLine);
    
    // Get users with specific roles
    @Query("SELECT u FROM User u JOIN u.profils p WHERE p.nom IN :roleNames AND u.actif = true")
    List<User> findByRoleNamesAndActifTrue(@Param("roleNames") List<String> roleNames);
}