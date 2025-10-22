package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a direct chat message between any users (PLAYER, ORGANIZATION, SPECTATOR)
 * Uses global userId for cross-type messaging
 */
@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_sender_id", columnList = "sender_id"),
    @Index(name = "idx_recipient_id", columnList = "recipient_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sender info - uses global userId (snowflake)
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "sender_username", nullable = false)
    private String senderUsername;

    @Column(name = "sender_name", nullable = false)
    private String senderName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private UserType senderType;

    // Recipient info - uses global userId (snowflake)
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "recipient_username", nullable = false)
    private String recipientUsername;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false)
    private UserType recipientType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Check if this message belongs to a conversation between two specific users
     */
    public boolean belongsToConversation(Long userId1, Long userId2) {
        return (senderId.equals(userId1) && recipientId.equals(userId2)) ||
               (senderId.equals(userId2) && recipientId.equals(userId1));
    }

    /**
     * Check if this message was sent by a specific user
     */
    public boolean wasSentBy(Long userId) {
        return senderId.equals(userId);
    }

    /**
     * Check if this message was received by a specific user
     */
    public boolean wasReceivedBy(Long userId) {
        return recipientId.equals(userId);
    }
}
