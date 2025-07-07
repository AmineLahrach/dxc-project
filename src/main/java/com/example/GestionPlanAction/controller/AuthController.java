package com.example.GestionPlanAction.controller;

import com.example.GestionPlanAction.dto.JwtResponseDTO;
import com.example.GestionPlanAction.dto.LoginRequestDTO;
import com.example.GestionPlanAction.dto.MessageResponseDTO;
import com.example.GestionPlanAction.dto.SignupRequestDTO;
import com.example.GestionPlanAction.model.Profil;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.repository.UserRepository;
import com.example.GestionPlanAction.security.JwtUtils;
import com.example.GestionPlanAction.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getMotDePasse()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Fix: Get UserPrincipal instead of User from authentication
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // Extract roles from authorities
            Set<String> roles = new HashSet<>();
            userPrincipal.getAuthorities().forEach(authority -> {
                String role = authority.getAuthority();
                if (role.startsWith("ROLE_")) {
                    roles.add(role.substring(5)); // Remove "ROLE_" prefix
                }
            });

            return ResponseEntity.ok(new JwtResponseDTO(jwt,
                    userPrincipal.getId(),
                    userPrincipal.getUsername(),
                    userPrincipal.getEmail(),
                    userPrincipal.getNom(),
                    userPrincipal.getPrenom(),
                    roles,
                    userPrincipal.getServiceLine()));
                    
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            System.out.println("Authentication failed: Incorrect username or password");
            return ResponseEntity.status(401)
                    .body(MessageResponseDTO.error("Incorrect username or password."));
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            System.out.println("Authentication failed: Username not found");
            return ResponseEntity.status(401)
                    .body(MessageResponseDTO.error("Username not found."));
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401)
                    .body(MessageResponseDTO.error("Authentication failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequestDTO signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(MessageResponseDTO.error("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(MessageResponseDTO.error("Error: Email is already in use!"));
        }

        User user = new User();
        user.setNom(signUpRequest.getNom());
        user.setPrenom(signUpRequest.getPrenom());
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setMotDePasse(encoder.encode(signUpRequest.getMotDePasse()));
        user.setActif(true);

        userRepository.save(user);

        return ResponseEntity.ok(MessageResponseDTO.success("User registered successfully!"));
    }
}