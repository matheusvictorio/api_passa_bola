package com.fiap.projects.apipassabola.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.projects.apipassabola.dto.NotificationResponse;
import com.fiap.projects.apipassabola.entity.UserType;
import com.fiap.projects.apipassabola.model.Notification;
import com.fiap.projects.apipassabola.model.NotificationType;
import com.fiap.projects.apipassabola.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Cria e envia uma notificação em tempo real
     */
    @Transactional
    public Notification createAndSendNotification(
            Long recipientId,
            UserType recipientType,
            Long senderId,
            UserType senderType,
            String senderUsername,
            String senderName,
            NotificationType type,
            String message,
            Map<String, Object> metadata,
            String actionUrl
    ) {
        // Criar notificação
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setRecipientType(recipientType);
        notification.setSenderId(senderId);
        notification.setSenderType(senderType);
        notification.setSenderUsername(senderUsername);
        notification.setSenderName(senderName);
        notification.setType(type);
        notification.setMessage(message);
        notification.setActionUrl(actionUrl);
        
        // Converter metadata para JSON
        if (metadata != null && !metadata.isEmpty()) {
            try {
                notification.setMetadata(objectMapper.writeValueAsString(metadata));
            } catch (JsonProcessingException e) {
                log.error("Erro ao converter metadata para JSON", e);
            }
        }
        
        // Salvar no banco
        notification = notificationRepository.save(notification);
        log.info("Notificação criada: {} para usuário {}/{}", type, recipientId, recipientType);
        
        // Enviar via WebSocket em tempo real
        sendNotificationViaWebSocket(notification);
        
        return notification;
    }
    
    /**
     * Envia notificação via WebSocket
     */
    private void sendNotificationViaWebSocket(Notification notification) {
        try {
            NotificationResponse response = convertToResponse(notification);
            
            // Enviar para o tópico específico do usuário
            String destination = String.format("/topic/notifications/%s/%d", 
                    notification.getRecipientType().toString().toLowerCase(),
                    notification.getRecipientId());
            
            messagingTemplate.convertAndSend(destination, response);
            log.info("Notificação enviada via WebSocket para: {}", destination);
            
        } catch (Exception e) {
            log.error("Erro ao enviar notificação via WebSocket", e);
        }
    }
    
    /**
     * Buscar notificações do usuário com paginação
     */
    public Page<NotificationResponse> getMyNotifications(
            Long userId,
            UserType userType,
            Pageable pageable
    ) {
        return notificationRepository
                .findByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(userId, userType, pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Buscar apenas notificações não lidas
     */
    public Page<NotificationResponse> getUnreadNotifications(
            Long userId,
            UserType userType,
            Pageable pageable
    ) {
        return notificationRepository
                .findByRecipientIdAndRecipientTypeAndIsReadFalseOrderByCreatedAtDesc(userId, userType, pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Contar notificações não lidas
     */
    public long countUnreadNotifications(Long userId, UserType userType) {
        return notificationRepository.countByRecipientIdAndRecipientTypeAndIsReadFalse(userId, userType);
    }
    
    /**
     * Marcar notificação como lida
     * Retorna true se marcada com sucesso, false se não encontrada ou sem permissão
     */
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId, UserType userType) {
        try {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElse(null);
            
            // Se notificação não existe, retornar false silenciosamente
            if (notification == null) {
                log.warn("Notificação {} não encontrada - pode ter sido deletada", notificationId);
                return false;
            }
            
            log.info("Tentando marcar notificação {} como lida. UserId: {}, UserType: {}, RecipientId: {}, RecipientType: {}", 
                    notificationId, userId, userType, notification.getRecipientId(), notification.getRecipientType());
            
            // Verificar se a notificação pertence ao usuário
            if (!notification.getRecipientId().equals(userId) || 
                !notification.getRecipientType().equals(userType)) {
                log.warn("Permissão negada para marcar notificação {} como lida. UserId: {} != RecipientId: {} OU UserType: {} != RecipientType: {}", 
                        notificationId, userId, notification.getRecipientId(), userType, notification.getRecipientType());
                return false;
            }
            
            // Se já está marcada como lida, não fazer nada
            if (notification.getIsRead()) {
                log.debug("Notificação {} já está marcada como lida", notificationId);
                return true;
            }
            
            notification.markAsRead();
            notificationRepository.save(notification);
            
            log.info("Notificação {} marcada como lida com sucesso", notificationId);
            
            // Enviar atualização via WebSocket
            sendNotificationUpdateViaWebSocket(userId, userType);
            
            return true;
            
        } catch (Exception e) {
            log.error("Erro ao marcar notificação {} como lida: {}", notificationId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Marcar todas as notificações como lidas
     */
    @Transactional
    public int markAllAsRead(Long userId, UserType userType) {
        int count = notificationRepository.markAllAsRead(userId, userType, LocalDateTime.now());
        
        // Enviar atualização via WebSocket
        sendNotificationUpdateViaWebSocket(userId, userType);
        
        return count;
    }
    
    /**
     * Deletar notificação
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId, UserType userType) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));
        
        // Verificar se a notificação pertence ao usuário
        if (!notification.getRecipientId().equals(userId) || 
            !notification.getRecipientType().equals(userType)) {
            throw new RuntimeException("Você não tem permissão para deletar esta notificação");
        }
        
        notificationRepository.delete(notification);
        
        // Enviar atualização via WebSocket
        sendNotificationUpdateViaWebSocket(userId, userType);
    }
    
    /**
     * Envia atualização de contador via WebSocket
     */
    private void sendNotificationUpdateViaWebSocket(Long userId, UserType userType) {
        try {
            long unreadCount = countUnreadNotifications(userId, userType);
            
            Map<String, Object> update = new HashMap<>();
            update.put("unreadCount", unreadCount);
            update.put("timestamp", LocalDateTime.now());
            
            String destination = String.format("/topic/notifications/%s/%d/count", 
                    userType.toString().toLowerCase(), userId);
            
            messagingTemplate.convertAndSend(destination, update);
            
        } catch (Exception e) {
            log.error("Erro ao enviar atualização de contador via WebSocket", e);
        }
    }
    
    /**
     * Buscar notificações recentes (últimas 24h)
     */
    public List<NotificationResponse> getRecentNotifications(Long userId, UserType userType) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return notificationRepository.findRecentNotifications(userId, userType, since)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Limpar notificações antigas (mais de 30 dias)
     */
    @Transactional
    public int cleanOldNotifications() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return notificationRepository.deleteOlderThan(thirtyDaysAgo);
    }
    
    /**
     * Converter entidade para DTO
     */
    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setSenderId(notification.getSenderId());
        response.setSenderType(notification.getSenderType());
        response.setSenderUsername(notification.getSenderUsername());
        response.setSenderName(notification.getSenderName());
        response.setType(notification.getType());
        response.setMessage(notification.getMessage());
        response.setMetadata(notification.getMetadata());
        response.setActionUrl(notification.getActionUrl());
        response.setIsRead(notification.getIsRead());
        response.setCreatedAt(notification.getCreatedAt());
        response.setReadAt(notification.getReadAt());
        return response;
    }
    
    // ========== MÉTODOS HELPER PARA CRIAR NOTIFICAÇÕES ESPECÍFICAS ==========
    
    /**
     * Notificação de convite de time recebido
     */
    public void notifyTeamInviteReceived(
            Long invitedPlayerId,
            Long inviterId,
            String inviterUsername,
            String inviterName,
            Long teamId,
            String teamName,
            Long inviteId
    ) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("teamId", teamId);
        metadata.put("teamName", teamName);
        metadata.put("inviteId", inviteId);
        
        String message = String.format("%s convidou você para entrar no time %s", inviterName, teamName);
        String actionUrl = String.format("/teams/%d/invites/%d", teamId, inviteId);
        
        createAndSendNotification(
                invitedPlayerId,
                UserType.PLAYER,
                inviterId,
                UserType.PLAYER,
                inviterUsername,
                inviterName,
                NotificationType.TEAM_INVITE_RECEIVED,
                message,
                metadata,
                actionUrl
        );
    }
    
    /**
     * Notificação de convite aceito
     */
    public void notifyTeamInviteAccepted(
            Long inviterId,
            UserType inviterType,
            Long acceptedPlayerId,
            String acceptedPlayerUsername,
            String acceptedPlayerName,
            Long teamId,
            String teamName
    ) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("teamId", teamId);
        metadata.put("teamName", teamName);
        metadata.put("playerId", acceptedPlayerId);
        
        String message = String.format("%s aceitou seu convite para o time %s", acceptedPlayerName, teamName);
        String actionUrl = String.format("/teams/%d", teamId);
        
        createAndSendNotification(
                inviterId,
                inviterType,
                acceptedPlayerId,
                UserType.PLAYER,
                acceptedPlayerUsername,
                acceptedPlayerName,
                NotificationType.TEAM_INVITE_ACCEPTED,
                message,
                metadata,
                actionUrl
        );
    }
    
    /**
     * Notificação de novo seguidor
     */
    public void notifyNewFollower(
            Long followedUserId,
            UserType followedUserType,
            Long followerId,
            UserType followerType,
            String followerUsername,
            String followerName
    ) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("followerId", followerId);
        metadata.put("followerType", followerType.toString());
        
        String message = String.format("%s começou a seguir você", followerName);
        String actionUrl = String.format("/%ss/%d", followerType.toString().toLowerCase(), followerId);
        
        createAndSendNotification(
                followedUserId,
                followedUserType,
                followerId,
                followerType,
                followerUsername,
                followerName,
                NotificationType.NEW_FOLLOWER,
                message,
                metadata,
                actionUrl
        );
    }
    
    /**
     * Notificação de post curtido
     */
    public void notifyPostLiked(
            Long postAuthorId,
            UserType postAuthorType,
            Long likerId,
            UserType likerType,
            String likerUsername,
            String likerName,
            Long postId
    ) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("postId", postId);
        metadata.put("likerId", likerId);
        
        String message = String.format("%s curtiu seu post", likerName);
        String actionUrl = String.format("/posts/%d", postId);
        
        createAndSendNotification(
                postAuthorId,
                postAuthorType,
                likerId,
                likerType,
                likerUsername,
                likerName,
                NotificationType.POST_LIKED,
                message,
                metadata,
                actionUrl
        );
    }
}
