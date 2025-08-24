package com.example.GestionPlanAction.service;

public interface EmailService {

    void sendEmail(String to, String subject, String body);
    void sendUserRegistrationEmail(String to, String username, String password);
}
