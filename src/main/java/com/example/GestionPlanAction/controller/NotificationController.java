package com.example.GestionPlanAction.controller;

import com.example.GestionPlanAction.model.Notification;
import com.example.GestionPlanAction.security.SecurityUtils;
import com.example.GestionPlanAction.service.NotificationService;
import com.example.GestionPlanAction.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping("/unread-count")
    public Long allUnreadNotificationCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationService.getUnreadCount(userService.findEntityById(userId));
    }

    @GetMapping("/all")
    public Page<Notification> allNotifications(@RequestParam(value = "page", defaultValue = "0") int page,
                                               @RequestParam(value = "size", defaultValue = "10") int size) {
        return notificationService.getAllNotifications(page, size);
    }

    @PutMapping("/{id}/read")
    public void markNotificationAsRead(@PathVariable(value = "id") Long id) {

        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.markAsRead(id, userService.findEntityById(userId));
    }

    @PutMapping("/{id}/unread")
    public void markNotificationAsUnread(@PathVariable(value = "id") Long id) {

        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.markAsUnread(id, userService.findEntityById(userId));
    }

    @PutMapping("/read-all")
    public void markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.markAllAsRead(userService.findEntityById(userId));
    }

}