package com.example.GestionPlanAction.controller;

import com.example.GestionPlanAction.dto.UserProfileDTO;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService service;

    @GetMapping
    public List<User> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public User create(@RequestBody UserProfileDTO dto) {
        User u = new User();
        u.setNom(dto.getNom());
        u.setPrenom(dto.getPrenom());
        u.setEmail(dto.getEmail());
        u.setUsername(dto.getUsername());
        u.setMotDePasse(new BCryptPasswordEncoder().encode(dto.motDePasse));
        u.setActif(dto.actif != null ? dto.actif : true);
        return service.createWithRelations(u, dto.serviceLine, dto.roles);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody UserProfileDTO dto) {
        User existingUser = service.getById(id);
        if (dto.motDePasse != null && !dto.motDePasse.equals(existingUser.getMotDePasse())) {
            existingUser.setMotDePasse(new BCryptPasswordEncoder().encode(dto.motDePasse));
        }
        existingUser.setNom(dto.nom);
        existingUser.setPrenom(dto.prenom);
        existingUser.setEmail(dto.email);
        existingUser.setUsername(dto.username);
        existingUser.setActif(dto.actif != null ? dto.actif : existingUser.getActif());
        return service.updateWithRelations(id, existingUser, dto.serviceLine, dto.roles);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
