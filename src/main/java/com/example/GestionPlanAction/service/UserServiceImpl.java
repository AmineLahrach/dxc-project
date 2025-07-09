package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.ProfilDTO;
import com.example.GestionPlanAction.dto.ServiceLineDTO;
import com.example.GestionPlanAction.dto.UserResponseDTO;
import com.example.GestionPlanAction.dto.UserWithProfilView;
import com.example.GestionPlanAction.dto.UserWithProfilesDTO;
import com.example.GestionPlanAction.model.Profil;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.repository.ProfilRepository;
import com.example.GestionPlanAction.repository.ServiceLineRepository;
import com.example.GestionPlanAction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private ServiceLineRepository serviceLineRepository;

    @Autowired
    private ProfilRepository profilRepository;

    @Override
    public List<UserResponseDTO> getAll() {
        return repository
                .findAll()          // ← use fetch-join
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getById(Long id) {
        User u = repository
                .findByIdWithProfiles(id)        // ← already fetches profils+serviceLine
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'ID: " + id));
        return convertToResponseDTO(u);
    }

    @Override
    public User create(User user) {
        return repository.save(user);
    }

    @Override
    public User update(Long id, User updated) {
        User existing = findEntityById(id);
        existing.setNom(updated.getNom());
        existing.setPrenom(updated.getPrenom());
        existing.setEmail(updated.getEmail());
        existing.setUsername(updated.getUsername());
        existing.setMotDePasse(updated.getMotDePasse());
        existing.setProfils(updated.getProfils());
        existing.setServiceLine(updated.getServiceLine());
        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public User createWithRelations(User user, Long serviceLineId, Set<Long> profileIds) {
        if (serviceLineId != null) {
            user.setServiceLine(serviceLineRepository.findById(serviceLineId).orElse(null));
        }
        if (profileIds != null && !profileIds.isEmpty()) {
            user.setProfils(new HashSet<>(profilRepository.findAllById(profileIds)));
        }
        return repository.save(user);
    }

    @Override
    public User updateWithRelations(Long id, User user, Long serviceLineId, Set<Long> profileIds) {
        User existing = findEntityById(id);

        if (serviceLineId != null) {
            existing.setServiceLine(serviceLineRepository.findById(serviceLineId).orElse(null));
        }

        if (existing.getProfils() != null) {
            // Detach the collection to avoid lazy loading issues
            Set<Profil> newProfils = new HashSet<>();
            for (Profil profil : existing.getProfils()) {
                Profil managedProfil = profilRepository.findById(profil.getId())
                        .orElseThrow(() -> new RuntimeException("Profil not found: " + profil.getId()));
                newProfils.add(managedProfil);
            }
            
            // Use the helper method from the entity
            existing.setProfils(newProfils);
        }

        existing.setNom(user.getNom());
        existing.setPrenom(user.getPrenom());
        existing.setEmail(user.getEmail());
        existing.setUsername(user.getUsername());
        existing.setMotDePasse(user.getMotDePasse());
        existing.setActif(user.getActif());

        return repository.save(existing);
    }    

    @Override
    public User findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'ID: " + id));
    }

    public List<UserWithProfilesDTO> getAllUsersWithProfiles() {
        List<UserWithProfilView> flatList = repository.findAllUserWithProfiles();
        Map<Long, UserWithProfilesDTO> userMap = new LinkedHashMap<>();

        for (UserWithProfilView row : flatList) {
            UserWithProfilesDTO dto = userMap.computeIfAbsent(
                row.getUserId(),
                id -> {
                    UserWithProfilesDTO u = new UserWithProfilesDTO();
                    u.setId(row.getUserId());
                    u.setNom(row.getNom());
                    u.setPrenom(row.getPrenom());
                    u.setEmail(row.getEmail());
                    u.setUsername(row.getUsername());
                    u.setActif(row.getActif());
                    u.setServiceLineId(row.getServiceLineId());
                    return u;
                }
            );
            if (row.getProfilId() != null) {
                dto.getProfils().add(new ProfilDTO(row.getProfilId(), row.getProfilNom()));
            }
        }
        return new ArrayList<>(userMap.values());
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        var dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setActif(user.getActif());
        
        // Create a new set to avoid ConcurrentModificationException
        Set<Profil> profils = new HashSet<>(user.getProfils());
        dto.setRoles(profils.stream()
                .map(profil -> new ProfilDTO(profil.getId(), profil.getNom()))
                .collect(Collectors.toSet())
        );
        
        // Convert service line
        if (user.getServiceLine() != null) {
            dto.setServiceLine(new ServiceLineDTO(
                    user.getServiceLine().getId(), 
                    user.getServiceLine().getNom()
            ));
        }
        
        return dto;
    }
}
