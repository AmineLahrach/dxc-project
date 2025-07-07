package com.example.GestionPlanAction.security;

import com.example.GestionPlanAction.model.Profil;
import com.example.GestionPlanAction.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    
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

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Fix: Create a defensive copy to avoid ConcurrentModificationException
        try {
            if (user.getProfils() != null && !user.getProfils().isEmpty()) {
                // Convert to ArrayList to avoid lazy loading issues
                List<Profil> profilsList = new ArrayList<>(user.getProfils());
                for (Profil profil : profilsList) {
                    if (profil != null && profil.getNom() != null) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + profil.getNom().toUpperCase()));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading user profiles: " + e.getMessage());
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