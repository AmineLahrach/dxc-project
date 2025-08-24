package com.example.GestionPlanAction.service;

import com.example.GestionPlanAction.model.Notification;
import com.example.GestionPlanAction.model.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {
    Long getUnreadCount(User utilisateur);
    Page<Notification> getAllNotifications(int page, int size);
    void markAsRead(Long notificationId, User utilisateur);
    void markAsUnread(Long notificationId, User utilisateur);
    void markAllAsRead(User utilisateur);
    void notifyActionVariableAssigned(User user, String title);
}
