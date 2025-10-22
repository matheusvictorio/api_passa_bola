package com.fiap.projects.apipassabola.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for conversation summary (used in conversation list)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    private Long otherUserId;
    private String otherUsername;
    private String otherName;
    private String otherProfilePhotoUrl;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
}
