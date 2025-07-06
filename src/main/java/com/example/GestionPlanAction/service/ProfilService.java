package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.model.Profil;
import com.example.GestionPlanAction.repository.ProfilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfilService {

    @Autowired
    private ProfilRepository profilRepository;

    public List<Profil> getAllProfils() {
        return profilRepository.findAll();
    }

    public Optional<Profil> getProfilById(Long id) {
        return profilRepository.findById(id);
    }

    public Profil createProfil(Profil profil) {
        return profilRepository.save(profil);
    }

    public Profil updateProfil(Long id, Profil updatedProfil) {
        return profilRepository.findById(id)
                .map(profil -> {
                    profil.setNom(updatedProfil.getNom());
                    return profilRepository.save(profil);
                })
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));
    }

    public void deleteProfil(Long id) {
        profilRepository.deleteById(id);
    }
}
