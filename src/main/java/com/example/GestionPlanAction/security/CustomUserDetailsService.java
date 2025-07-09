package com.example.GestionPlanAction.security;

import com.example.GestionPlanAction.model.Profil;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.repository.UserRepository;
import com.example.GestionPlanAction.repository.ProfilRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfilRepository profilRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        logger.debug("Loading user by username or email: {}", usernameOrEmail);
        
        // First check if the user has any profiles at all (for debugging)
        boolean hasProfiles = false;
        try {
            if (usernameOrEmail != null && usernameOrEmail.contains("@")) {
                // Handle email case
                User checkUser = userRepository.findByEmail(usernameOrEmail).orElse(null);
                if (checkUser != null) {
                    hasProfiles = userRepository.hasAnyProfile(checkUser.getUsername());
                    logger.debug("User {} found by email has profiles: {}", checkUser.getUsername(), hasProfiles);
                }
            } else {
                // Handle username case
                hasProfiles = userRepository.hasAnyProfile(usernameOrEmail);
                logger.debug("User {} has profiles: {}", usernameOrEmail, hasProfiles);
            }
        } catch (Exception e) {
            logger.error("Error checking if user has profiles: {}", e.getMessage());
        }
        
        // Use the query with JOIN FETCH to load profiles eagerly
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", usernameOrEmail);
                    return new UsernameNotFoundException("Utilisateur non trouvé: " + usernameOrEmail);
                });

        logger.debug("User found: {} with ID: {}", user.getUsername(), user.getId());
        
        // Force initialization of profiles in the session
        if (user.getProfils() != null) {
            int profileCount = user.getProfils().size();
            logger.debug("User has {} profiles in memory", profileCount);
            
            if (profileCount == 0 && hasProfiles) {
                logger.warn("PROFILE LOADING ISSUE: User {} should have profiles but none were loaded", user.getUsername());
                // Try an alternative approach to load profiles
                try {
                    // Manual query to retrieve user's profiles
                    logger.debug("Attempting manual profile loading for user: {}", user.getUsername());
                    // Further actions to debug or resolve the profile loading issue could go here
                }
                catch (Exception e) {
                    logger.error("Error during manual profile loading: {}", e.getMessage());
                }
            }
        } else {
            logger.warn("User's profile collection is null");
        }
        
        // Load user authorities (roles)
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getProfils() != null && !user.getProfils().isEmpty()) {
            for (Profil profil : user.getProfils()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + profil.getNom()));
                logger.debug("Added role: ROLE_{}", profil.getNom());
            }
        } else {
            // If no profiles in user object, try direct database query
            List<String> profileNames = profilRepository.findProfileNamesByUserId(user.getId());
            if (profileNames != null && !profileNames.isEmpty()) {
                for (String profileName : profileNames) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + profileName));
                    logger.debug("Added role from direct query: ROLE_{}", profileName);
                }
            } else {
                // Only fall back to default role if truly no profiles
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                logger.debug("No roles found, added default ROLE_USER");
            }
        }

        UserDetails userDetails = UserPrincipal.create(user, authorities);
        logger.debug("UserDetails created with {} authorities", 
            userDetails.getAuthorities() != null ? userDetails.getAuthorities().size() : 0);
            
        return userDetails;
    }

    // This method is used by JWTAuthenticationFilter
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        logger.debug("Loading user by ID: {}", id);
        // Use the query with JOIN FETCH to load profiles eagerly
        User user = userRepository.findByIdWithProfiles(id)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new UsernameNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
                });

        logger.debug("User found by ID: {} with username: {}", id, user.getUsername());
        return UserPrincipal.create(user);
    }
}