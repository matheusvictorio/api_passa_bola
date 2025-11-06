package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.request.ChatMessageRequest;
import com.fiap.projects.apipassabola.dto.response.ChatMessageResponse;
import com.fiap.projects.apipassabola.dto.response.ConversationResponse;
import com.fiap.projects.apipassabola.entity.ChatMessage;
import com.fiap.projects.apipassabola.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Universal service for managing chat messages between all user types
 * Uses global userId (snowflake) for cross-type messaging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UniversalUserService universalUserService;

    /**
     * Send a universal chat message (works for all user types)
     */
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        // Get current user (sender)
        UniversalUserService.UserInfo sender = universalUserService.getCurrentUser();
        return sendMessageInternal(request, sender);
    }

    /**
     * Send a universal chat message using Principal (for WebSocket contexts)
     */
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request, java.security.Principal principal) {
        // Get current user from Principal (sender)
        UniversalUserService.UserInfo sender = universalUserService.getUserFromPrincipal(principal);
        return sendMessageInternal(request, sender);
    }

    /**
     * Internal method to send message
     */
    private ChatMessageResponse sendMessageInternal(ChatMessageRequest request, UniversalUserService.UserInfo sender) {
        
        // Get recipient by userId
        UniversalUserService.UserInfo recipient = universalUserService.findByUserId(request.getRecipientId());
        
        // Validate that sender and recipient are different
        if (sender.userId.equals(recipient.userId)) {
            throw new RuntimeException("Cannot send message to yourself");
        }
        
        // Create message
        ChatMessage message = new ChatMessage();
        message.setSenderId(sender.userId);
        message.setSenderUsername(sender.username);
        message.setSenderName(sender.name);
        message.setSenderType(sender.userType);
        message.setRecipientId(recipient.userId);
        message.setRecipientUsername(recipient.username);
        message.setRecipientName(recipient.name);
        message.setRecipientType(recipient.userType);
        message.setContent(request.getContent());
        message.setIsRead(false);
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        log.info("💬 [SendMessage] Message saved: id={}, senderId={}, recipientId={}, senderType={}, recipientType={}", 
                savedMessage.getId(), savedMessage.getSenderId(), savedMessage.getRecipientId(),
                savedMessage.getSenderType(), savedMessage.getRecipientType());
        
        return convertToResponse(savedMessage);
    }

    /**
     * Get conversation messages between current user and another user (universal)
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getConversation(Long otherUserId) {
        UniversalUserService.UserInfo currentUser = universalUserService.getCurrentUser();
        
        // Validate other user exists
        universalUserService.findByUserId(otherUserId);
        
        List<ChatMessage> messages = chatMessageRepository.findConversationMessages(currentUser.userId, otherUserId);
        
        return messages.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get conversation messages with pagination (universal)
     */
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getConversation(Long otherUserId, Pageable pageable) {
        UniversalUserService.UserInfo currentUser = universalUserService.getCurrentUser();
        
        // Validate other user exists
        universalUserService.findByUserId(otherUserId);
        
        Page<ChatMessage> messages = chatMessageRepository.findConversationMessages(currentUser.userId, otherUserId, pageable);
        
        return messages.map(this::convertToResponse);
    }

    /**
     * Mark messages from a specific sender as read (universal)
     */
    @Transactional
    public void markMessagesAsRead(Long senderId) {
        UniversalUserService.UserInfo currentUser = universalUserService.getCurrentUser();
        chatMessageRepository.markMessagesAsRead(currentUser.userId, senderId);
        log.info("Messages from {} marked as read by {}", senderId, currentUser.userId);
    }

    /**
     * Get all conversations for current user (universal)
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations() {
        UniversalUserService.UserInfo currentUser = universalUserService.getCurrentUser();
        
        log.info("🔍 [Conversations] Getting conversations for userId: {}, userType: {}", 
                currentUser.userId, currentUser.userType);
        
        List<Long> partnerIds = chatMessageRepository.findConversationPartners(currentUser.userId);
        
        log.info("🔍 [Conversations] Found {} conversation partners: {}", 
                partnerIds.size(), partnerIds);
        
        List<ConversationResponse> conversations = new ArrayList<>();
        
        for (Long partnerId : partnerIds) {
            try {
                UniversalUserService.UserInfo partner = universalUserService.findByUserId(partnerId);
                
                // Get last message
                List<ChatMessage> lastMessages = chatMessageRepository.findLastMessage(
                        currentUser.userId, partnerId, PageRequest.of(0, 1));
                
                if (lastMessages.isEmpty()) continue;
                
                ChatMessage lastMessage = lastMessages.get(0);
                
                // Count unread messages
                Long unreadCount = chatMessageRepository.countUnreadMessages(currentUser.userId, partnerId);
                
                ConversationResponse conversation = new ConversationResponse();
                conversation.setOtherUserId(partner.userId);
                conversation.setOtherUsername(partner.username);
                conversation.setOtherName(partner.name);
                conversation.setOtherProfilePhotoUrl(null); // TODO: Add profile photo to UserInfo
                conversation.setLastMessage(lastMessage.getContent());
                conversation.setLastMessageTime(lastMessage.getCreatedAt());
                conversation.setUnreadCount(unreadCount);
                
                conversations.add(conversation);
            } catch (Exception e) {
                log.warn("Could not load conversation partner {}: {}", partnerId, e.getMessage());
            }
        }
        
        // Sort by last message time (most recent first)
        conversations.sort((c1, c2) -> c2.getLastMessageTime().compareTo(c1.getLastMessageTime()));
        
        return conversations;
    }

    /**
     * Get unread message count (universal)
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount() {
        UniversalUserService.UserInfo currentUser = universalUserService.getCurrentUser();
        return chatMessageRepository.countAllUnreadMessages(currentUser.userId);
    }

    /**
     * Get unread messages (universal)
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getUnreadMessages() {
        UniversalUserService.UserInfo currentUser = universalUserService.getCurrentUser();
        
        List<ChatMessage> messages = chatMessageRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(currentUser.userId);
        
        return messages.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert ChatMessage entity to response DTO
     */
    private ChatMessageResponse convertToResponse(ChatMessage message) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSenderId());
        response.setSenderUsername(message.getSenderUsername());
        response.setSenderName(message.getSenderName());
        response.setSenderType(message.getSenderType());
        response.setRecipientId(message.getRecipientId());
        response.setRecipientUsername(message.getRecipientUsername());
        response.setRecipientName(message.getRecipientName());
        response.setRecipientType(message.getRecipientType());
        response.setContent(message.getContent());
        response.setIsRead(message.getIsRead());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}
