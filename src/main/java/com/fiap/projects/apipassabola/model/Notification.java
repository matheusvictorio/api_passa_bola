package com.fiap.projects.apipassabola.model;

import com.fiap.projects.apipassabola.entity.UserType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Destinatário da notificação (userId global + tipo)
    @Column(nullable = false)
    private Long recipientId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType recipientType;
    
    // Remetente da notificação (quem causou a ação - userId global + tipo)
    @Column(nullable = false)
    private Long senderId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType senderType;
    
    @Column(nullable = false)
    private String senderUsername;
    
    @Column(nullable = false)
    private String senderName;
    
    // Tipo e conteúdo da notificação
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Column(nullable = false, length = 500)
    private String message;
    
    // Dados adicionais (JSON) - para informações específicas de cada tipo
    @Column(columnDefinition = "TEXT")
    private String metadata; // Ex: {"teamId": 123, "inviteId": 456}
    
    // Link de ação (opcional)
    private String actionUrl; // Ex: "/teams/123/invites/456"
    
    // Status
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime readAt;
    
    // Métodos helper
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
    
    public boolean isUnread() {
        return !isRead;
    }
}
