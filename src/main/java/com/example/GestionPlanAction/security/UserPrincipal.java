package com.example.GestionPlanAction.security;

import com.example.GestionPlanAction.model.Profil;
import com.example.GestionPlanAction.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    
    private static final Logger logger = LoggerFactory.getLogger(UserPrincipal.class);
    
    private Long id;
    private String nom;
    private String prenom;
    private String username;
    private String email;
    
    @JsonIgnore
    private String password;
    
    private Boolean actif;
    private String serviceLine;
    
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Creates a UserPrincipal from a User entity
     * @param user The user entity
     * @return A new UserPrincipal instance
     */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Fix: Create a defensive copy to avoid ConcurrentModificationException
        try {
            if (user.getProfils() != null) {
                // Force initialization of profils
                Set<Profil> profiles = new HashSet<>(user.getProfils());
                logger.debug("User {} has {} profiles", user.getUsername(), profiles.size());
                
                for (Profil profil : profiles) {
                    if (profil != null && profil.getNom() != null) {
                        String role = "ROLE_" + profil.getNom().toUpperCase();
                        authorities.add(new SimpleGrantedAuthority(role));
                        logger.debug("Added role: {} for user: {}", role, user.getUsername());
                    }
                }
            } else {
                logger.warn("User {} has no profiles", user.getUsername());
            }
            
            // If no roles were assigned, add a default ROLE_USER
            if (authorities.isEmpty()) {
                logger.debug("No roles found for user {}, adding default ROLE_USER", user.getUsername());
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
        } catch (Exception e) {
            logger.error("Error loading user profiles for {}: {}", user.getUsername(), e.getMessage());
            e.printStackTrace();
            // Add default role if profile loading fails
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new UserPrincipal(
                user.getId(),
                user.getNom(),
                user.getPrenom(),
                user.getUsername(),
                user.getEmail(),
                user.getMotDePasse(),
                user.getActif(),
                user.getServiceLine() != null ? user.getServiceLine().getNom() : null,
                authorities
        );
    }

    /**
     * Creates a UserPrincipal with custom authorities
     * @param user The user entity
     * @param authorities Pre-defined authorities to use instead of extracting from user profiles
     * @return A new UserPrincipal instance
     */
    public static UserPrincipal create(User user, List<GrantedAuthority> authorities) {
        logger.debug("Creating UserPrincipal with {} pre-defined authorities for user: {}", 
                     authorities.size(), user.getUsername());
                     
        return new UserPrincipal(
            user.getId(),
            user.getNom(),
            user.getPrenom(),
            user.getUsername(),
            user.getEmail(),
            user.getMotDePasse(),
            user.getActif(),
            user.getServiceLine() != null ? user.getServiceLine().getNom() : null,
            authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return actif != null && actif;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    // Helper methods for role checking
    public boolean hasRole(String role) {
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role.toUpperCase()));
    }
    
    public boolean isAdmin() {
        return hasRole("ADMINISTRATEUR");
    }
    
    public boolean isCollaborator() {
        return hasRole("COLLABORATEUR");
    }
    
    public boolean isDirector() {
        return hasRole("DIRECTEUR_GENERAL");
    }
}