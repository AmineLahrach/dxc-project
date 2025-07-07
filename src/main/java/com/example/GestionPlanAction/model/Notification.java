package com.example.GestionPlanAction.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notification")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 1000)
    private String contenu;
    
    @Column(nullable = false)
    private LocalDateTime date;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean recu = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;
    
    @Column(length = 100)
    private String type; // INFO, WARNING, SUCCESS, ERROR
    
    @Column(length = 200)
    private String titre;
    
    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = LocalDateTime.now();
        }
        if (recu == null) {
            recu = false;
        }
    }
}