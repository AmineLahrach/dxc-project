package com.example.GestionPlanAction.controller;

import com.example.GestionPlanAction.model.Exercice;
import com.example.GestionPlanAction.service.ExerciceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercices")
@CrossOrigin(origins = "*")
public class ExerciceController {

    private final ExerciceService service;

    public ExerciceController(ExerciceService service) {
        this.service = service;
    }

    @GetMapping
    public List<Exercice> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Exercice getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Exercice create(@RequestBody Exercice exercice) {
        return service.create(exercice);
    }

    @PutMapping("/{id}")
    public Exercice update(@PathVariable Long id, @RequestBody Exercice exercice) {
        return service.update(id, exercice);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}