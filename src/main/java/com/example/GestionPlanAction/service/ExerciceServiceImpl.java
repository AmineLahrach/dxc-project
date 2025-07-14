package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.model.Exercice;
import com.example.GestionPlanAction.repository.ExerciceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciceServiceImpl implements ExerciceService {

    private final ExerciceRepository repository;

    public ExerciceServiceImpl(ExerciceRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Exercice> getAll() {
    	List<Exercice> exercices = repository.findAll();
    	System.out.println("Nombre d'exercices trouvés: " + exercices.size());
        return repository.findAll();
    }

    @Override
    public Exercice getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercice introuvable"));
    }

    @Override
    public Exercice create(Exercice exercice) {
        return repository.save(exercice);
    }

    @Override
    public Exercice update(Long id, Exercice updated) {
        Exercice existing = getById(id);
        existing.setAnnee(updated.getAnnee());
        existing.setVerrouille(updated.isVerrouille());
        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}