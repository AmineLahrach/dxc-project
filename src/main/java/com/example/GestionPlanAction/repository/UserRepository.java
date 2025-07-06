package com.example.GestionPlanAction.repository;

import com.example.GestionPlanAction.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
