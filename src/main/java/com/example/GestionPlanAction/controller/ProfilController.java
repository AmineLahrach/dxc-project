package com.example.GestionPlanAction.controller;

import com.example.GestionPlanAction.model.Profil;
import com.example.GestionPlanAction.service.ProfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profils")
/*@CrossOrigin(origins = "*") // si tu veux appeler depuis Angular*/
public class ProfilController {

    @Autowired
    private ProfilService profilService;

    @GetMapping
    public List<Profil> getAll() {
        return profilService.getAllProfils();
    }

    @GetMapping("/{id}")
    public Profil getById(@PathVariable Long id) {
        return profilService.getProfilById(id)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));
    }

    @PostMapping
    public Profil create(@RequestBody Profil profil) {
        return profilService.createProfil(profil);
    }

    @PutMapping("/{id}")
    public Profil update(@PathVariable Long id, @RequestBody Profil profil) {
        return profilService.updateProfil(id, profil);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        profilService.deleteProfil(id);
    }
}
