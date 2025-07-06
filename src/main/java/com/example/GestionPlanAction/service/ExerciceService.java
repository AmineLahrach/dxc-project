package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.model.Exercice;

import java.util.List;

public interface ExerciceService {
    List<Exercice> getAll();
    Exercice getById(Long id);
    Exercice create(Exercice exercice);
    Exercice update(Long id, Exercice exercice);
    void delete(Long id);
}