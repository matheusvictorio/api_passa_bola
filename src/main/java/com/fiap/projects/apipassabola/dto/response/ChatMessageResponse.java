package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for chat messages (universal - supports all user types)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    private Long id;
    private Long senderId;
    private String senderUsername;
    private String senderName;
    private UserType senderType;
    private Long recipientId;
    private String recipientUsername;
    private String recipientName;
    private UserType recipientType;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
