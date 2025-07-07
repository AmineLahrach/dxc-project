package com.example.GestionPlanAction.repository;

import com.example.GestionPlanAction.model.Notification;
import com.example.GestionPlanAction.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUtilisateurAndRecuFalseOrderByDateDesc(User utilisateur);
    
    Page<Notification> findByUtilisateurOrderByDateDesc(User utilisateur, Pageable pageable);
    
    Long countByUtilisateurAndRecuFalse(User utilisateur);
    
    List<Notification> findByUtilisateurAndDateAfterOrderByDateDesc(User utilisateur, LocalDateTime since);
    
    @Modifying
    @Query("UPDATE Notification n SET n.recu = true WHERE n.utilisateur = :user AND n.recu = false")
    void markAllAsReadForUser(@Param("user") User user);
    
    @Modifying
    @Query("UPDATE Notification n SET n.recu = true WHERE n.id = :notificationId AND n.utilisateur = :user")
    void markAsReadForUser(@Param("notificationId") Long notificationId, @Param("user") User user);
    
    @Query("SELECT n FROM Notification n WHERE n.type = :type AND n.date >= :since ORDER BY n.date DESC")
    List<Notification> findByTypeAndDateAfter(@Param("type") String type, @Param("since") LocalDateTime since);
}