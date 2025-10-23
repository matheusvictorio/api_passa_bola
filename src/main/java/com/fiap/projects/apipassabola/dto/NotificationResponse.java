package com.fiap.projects.apipassabola.dto;

import com.fiap.projects.apipassabola.entity.UserType;
import com.fiap.projects.apipassabola.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private Long id;
    
    // Remetente (quem causou a notificação)
    private Long senderId;
    private UserType senderType;
    private String senderUsername;
    private String senderName;
    
    // Tipo e conteúdo
    private NotificationType type;
    private String message;
    private String metadata;
    private String actionUrl;
    
    // Status
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
