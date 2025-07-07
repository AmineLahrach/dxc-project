package com.example.GestionPlanAction.repository;

import com.example.GestionPlanAction.model.Audit;
import com.example.GestionPlanAction.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    
    List<Audit> findByUtilisateurOrderByDateDesc(User utilisateur);
    
    Page<Audit> findByUtilisateurOrderByDateDesc(User utilisateur, Pageable pageable);
    
    List<Audit> findByActionContainingIgnoreCaseOrderByDateDesc(String action);
    
    List<Audit> findByDateBetweenOrderByDateDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT a FROM Audit a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.date DESC")
    List<Audit> findByEntityTypeAndEntityIdOrderByDateDesc(@Param("entityType") String entityType, 
                                                           @Param("entityId") Long entityId);
    
    @Query("SELECT COUNT(a) FROM Audit a WHERE a.utilisateur = :user AND a.date >= :since")
    Long countUserActionsAfter(@Param("user") User user, @Param("since") LocalDateTime since);
}