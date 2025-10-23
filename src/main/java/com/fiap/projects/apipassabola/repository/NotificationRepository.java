package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.UserType;
import com.fiap.projects.apipassabola.model.Notification;
import com.fiap.projects.apipassabola.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Buscar notificações de um usuário
    Page<Notification> findByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(
            Long recipientId, 
            UserType recipientType, 
            Pageable pageable
    );
    
    // Buscar notificações não lidas
    Page<Notification> findByRecipientIdAndRecipientTypeAndIsReadFalseOrderByCreatedAtDesc(
            Long recipientId, 
            UserType recipientType, 
            Pageable pageable
    );
    
    List<Notification> findByRecipientIdAndRecipientTypeAndIsReadFalseOrderByCreatedAtDesc(
            Long recipientId, 
            UserType recipientType
    );
    
    // Contar notificações não lidas
    long countByRecipientIdAndRecipientTypeAndIsReadFalse(
            Long recipientId, 
            UserType recipientType
    );
    
    // Buscar por tipo
    Page<Notification> findByRecipientIdAndRecipientTypeAndTypeOrderByCreatedAtDesc(
            Long recipientId, 
            UserType recipientType, 
            NotificationType type,
            Pageable pageable
    );
    
    // Marcar todas como lidas
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt " +
           "WHERE n.recipientId = :recipientId AND n.recipientType = :recipientType AND n.isRead = false")
    int markAllAsRead(
            @Param("recipientId") Long recipientId,
            @Param("recipientType") UserType recipientType,
            @Param("readAt") LocalDateTime readAt
    );
    
    // Deletar notificações antigas
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :date")
    int deleteOlderThan(@Param("date") LocalDateTime date);
    
    // Buscar notificações recentes (últimas 24h)
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId " +
           "AND n.recipientType = :recipientType AND n.createdAt > :since " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(
            @Param("recipientId") Long recipientId,
            @Param("recipientType") UserType recipientType,
            @Param("since") LocalDateTime since
    );
}
