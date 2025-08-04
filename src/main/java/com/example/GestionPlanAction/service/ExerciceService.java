package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.dto.ExerciceResponseDTO;
import com.example.GestionPlanAction.model.Exercice;

import java.util.List;

public interface ExerciceService {
    List<Exercice> getAll();
    ExerciceResponseDTO getById(Long id);
    Exercice create(Exercice exercice);
    Exercice update(Long id, Exercice updated);
    void delete(Long id);
}