package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.repository.ProfilRepository;
import com.example.GestionPlanAction.repository.ServiceLineRepository;
import com.example.GestionPlanAction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private ServiceLineRepository serviceLineRepository;

    @Autowired
    private ProfilRepository profilRepository;

    @Override
    public List<User> getAll() {
        return repository.findAll();
    }

    @Override
    public User getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));
    }

    @Override
    public User create(User user) {
        return repository.save(user);
    }

    @Override
    public User update(Long id, User updated) {
        User existing = getById(id);
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
        if (serviceLineId != null) {
            user.setServiceLine(serviceLineRepository.findById(serviceLineId).orElse(null));
        }
        if (profileIds != null && !profileIds.isEmpty()) {
            user.setProfils(new HashSet<>(profilRepository.findAllById(profileIds)));
        }
        return repository.save(user);
    }    
}
