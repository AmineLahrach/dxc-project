package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.model.Notification;
import com.example.GestionPlanAction.model.User;
import com.example.GestionPlanAction.repository.NotificationRepository;
import com.example.GestionPlanAction.repository.UserRepository;
import com.example.GestionPlanAction.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public Notification createNotification(String titre, String contenu, String type, User utilisateur) {
        Notification notification = Notification.builder()
                .titre(titre)
                .contenu(contenu)
                .type(type)
                .utilisateur(utilisateur)
                .recu(false)
                .date(LocalDateTime.now())
                .build();
        
        return notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotifications() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) return Collections.emptyList();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        return notificationRepository.findByUtilisateurAndRecuFalseOrderByDateDesc(currentUser);
    }

    @Override
    public Page<Notification> getAllNotifications(int page, int size) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) return Page.empty();
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        return notificationRepository.findByUtilisateurOrderByDateDesc(currentUser, PageRequest.of(page, size));
    }

    public Long getUnreadCount(User utilisateur) {
        return notificationRepository.countByUtilisateurAndRecuFalse(utilisateur);
    }

    @Override
    public void markAsRead(Long notificationId, User utilisateur) {
        notificationRepository.markAsReadForUser(notificationId, utilisateur);
    }

    @Override
    public void markAsUnread(Long notificationId, User utilisateur) {
        notificationRepository.markAsUnreadForUser(notificationId, utilisateur);
    }

    @Override
    public void markAllAsRead(User utilisateur) {
        notificationRepository.markAllAsReadForUser(utilisateur);
    }

    public List<Notification> getRecentNotifications(User utilisateur, LocalDateTime since) {
        return notificationRepository.findByUtilisateurAndDateAfterOrderByDateDesc(utilisateur, since);
    }

    // Notification types
    public static final String TYPE_INFO = "INFO";
    public static final String TYPE_WARNING = "WARNING";
    public static final String TYPE_SUCCESS = "SUCCESS";
    public static final String TYPE_ERROR = "ERROR";

    // Helper methods for common notifications
    public void notifyPlanCreated(User creator, String planTitle) {
        createNotification(
            "Plan d'action créé",
            "Votre plan d'action '" + planTitle + "' a été créé avec succès.",
            TYPE_SUCCESS,
            creator
        );
    }

    public void notifyActionVariableAssigned(User user, String title) {
        createNotification(
            "Action variable assignée",
            "L’action variable « " + title + " » vous a été assignée.",
                TYPE_INFO,
            user
        );
    }

    public void notifyPlanApproved(User responsible, String planTitle) {
        createNotification(
            "Plan d'action approuvé",
            "Le plan d'action '" + planTitle + "' a été approuvé.",
            TYPE_SUCCESS,
            responsible
        );
    }

    public void notifyPlanRejected(User responsible, String planTitle, String reason) {
        createNotification(
            "Plan d'action rejeté",
            "Le plan d'action '" + planTitle + "' a été rejeté. Raison: " + reason,
            TYPE_WARNING,
            responsible
        );
    }

    public void notifyUserCreated(User newUser) {
        createNotification(
            "Bienvenue!",
            "Votre compte a été créé avec succès. Bienvenue dans le système de gestion des plans d'action!",
            TYPE_INFO,
            newUser
        );
    }
}