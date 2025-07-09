package com.example.GestionPlanAction.repository;

import com.example.GestionPlanAction.dto.UserWithProfilView;
import com.example.GestionPlanAction.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Method needed for authentication - EAGER fetch to ensure profiles are loaded
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.profils LEFT JOIN FETCH u.serviceLine WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String username, @Param("usernameOrEmail") String email);
    
    // Additional methods for user management
    Optional<User> findByUsername(String username);
    
    @Query(value = "SELECT * FROM user_with_profiles", nativeQuery = true)
    List<UserWithProfilView> findAllUserWithProfiles();

    Optional<User> findByEmail(String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    // Method to load user with all relations for security context
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.profils LEFT JOIN FETCH u.serviceLine WHERE u.id = :id")
    Optional<User> findByIdWithProfiles(@Param("id") Long id);
    
    // Check if user has any profiles
    @Query("SELECT COUNT(p) > 0 FROM User u JOIN u.profils p WHERE u.username = :username")
    boolean hasAnyProfile(@Param("username") String username);

    @Query("""
      SELECT DISTINCT u
      FROM User u
      LEFT JOIN FETCH u.profils p
      LEFT JOIN FETCH u.serviceLine
      """)
    List<User> findAllWithRelations();
}