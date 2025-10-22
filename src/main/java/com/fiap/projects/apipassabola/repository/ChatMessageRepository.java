package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ChatMessage entity
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Find all messages in a conversation between two users
     */
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.senderId = :userId1 AND m.recipientId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.recipientId = :userId1) " +
           "ORDER BY m.createdAt ASC")
    List<ChatMessage> findConversationMessages(@Param("userId1") Long userId1, 
                                                @Param("userId2") Long userId2);

    /**
     * Find all messages in a conversation with pagination
     */
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.senderId = :userId1 AND m.recipientId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.recipientId = :userId1) " +
           "ORDER BY m.createdAt DESC")
    Page<ChatMessage> findConversationMessages(@Param("userId1") Long userId1, 
                                                @Param("userId2") Long userId2,
                                                Pageable pageable);

    /**
     * Count unread messages for a specific user from a specific sender
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE " +
           "m.recipientId = :recipientId AND m.senderId = :senderId AND m.isRead = false")
    Long countUnreadMessages(@Param("recipientId") Long recipientId, 
                             @Param("senderId") Long senderId);

    /**
     * Count all unread messages for a user
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE " +
           "m.recipientId = :recipientId AND m.isRead = false")
    Long countAllUnreadMessages(@Param("recipientId") Long recipientId);

    /**
     * Mark all messages from a sender as read
     */
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE " +
           "m.recipientId = :recipientId AND m.senderId = :senderId AND m.isRead = false")
    void markMessagesAsRead(@Param("recipientId") Long recipientId, 
                           @Param("senderId") Long senderId);

    /**
     * Find all users that have conversations with the given user
     */
    @Query("SELECT DISTINCT CASE " +
           "WHEN m.senderId = :userId THEN m.recipientId " +
           "ELSE m.senderId END " +
           "FROM ChatMessage m WHERE m.senderId = :userId OR m.recipientId = :userId")
    List<Long> findConversationPartners(@Param("userId") Long userId);

    /**
     * Get the last message in a conversation
     */
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.senderId = :userId1 AND m.recipientId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.recipientId = :userId1) " +
           "ORDER BY m.createdAt DESC")
    List<ChatMessage> findLastMessage(@Param("userId1") Long userId1, 
                                      @Param("userId2") Long userId2,
                                      Pageable pageable);

    /**
     * Find all messages sent by a user
     */
    List<ChatMessage> findBySenderIdOrderByCreatedAtDesc(Long senderId);

    /**
     * Find all messages received by a user
     */
    List<ChatMessage> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    /**
     * Find unread messages for a user
     */
    List<ChatMessage> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);
}
