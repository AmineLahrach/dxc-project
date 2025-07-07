package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.model.ServiceLine;
import com.example.GestionPlanAction.repository.ServiceLineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceLineServiceImpl implements ServiceLineService {

    @Autowired
    private ServiceLineRepository repository;

    @Override
    public List<ServiceLine> getAll() {
        return repository.findAll();
    }

    @Override
    public ServiceLine getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service Line non trouvée"));
    }

    @Override
    public ServiceLine create(ServiceLine serviceLine) {
        return repository.save(serviceLine);
    }

    @Override
    public ServiceLine update(Long id, ServiceLine updated) {
        ServiceLine existing = getById(id);
        existing.setNom(updated.getNom());
        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
