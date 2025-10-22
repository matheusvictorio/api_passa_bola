package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.request.ChatMessageRequest;
import com.fiap.projects.apipassabola.dto.response.ChatMessageResponse;
import com.fiap.projects.apipassabola.dto.response.ConversationResponse;
import com.fiap.projects.apipassabola.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for chat functionality
 * Handles both WebSocket (real-time) and REST (history) endpoints
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.fiap.projects.apipassabola.service.UniversalUserService universalUserService;

    /**
     * WebSocket endpoint - Send message via WebSocket
     * Client sends to: /app/chat.send
     * Server broadcasts to: /user/{recipientId}/queue/messages
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload @Valid ChatMessageRequest request) {
        log.info("📨 [WebSocket] Received message request: recipientId={}, content={}", 
                request.getRecipientId(), request.getContent());
        
        try {
            // Save message to database
            ChatMessageResponse response = chatMessageService.sendMessage(request);
            log.info("💾 [WebSocket] Message saved to DB: id={}, senderId={}, recipientId={}", 
                    response.getId(), response.getSenderId(), response.getRecipientId());
            
            // Get recipient email (Spring WebSocket uses email as user identifier)
            var recipient = universalUserService.findByUserId(request.getRecipientId());
            String recipientEmail = recipient.email;
            
            String destination = "/queue/messages";
            
            log.info("📤 [WebSocket] Attempting to send to user: email={}, destination={}", 
                    recipientEmail, destination);
            log.info("📦 [WebSocket] Message payload: {}", response);
            
            // CRITICAL: Use EMAIL not ID - Spring WebSocket identifies users by email (authentication.getName())
            try {
                messagingTemplate.convertAndSendToUser(
                        recipientEmail,  // <-- CHANGED: Use email instead of ID
                        destination,
                        response
                );
                log.info("✅ [WebSocket] convertAndSendToUser() completed for email: {}", recipientEmail);
            } catch (Exception sendEx) {
                log.error("❌ [WebSocket] Exception in convertAndSendToUser(): {}", sendEx.getMessage(), sendEx);
                throw sendEx;
            }
            
            log.info("✅ [WebSocket] Message sent successfully from {} to {} (email: {})", 
                    response.getSenderId(), response.getRecipientId(), recipientEmail);
                    
        } catch (Exception e) {
            log.error("❌ [WebSocket] Error sending message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * REST endpoint - Send message via HTTP (fallback)
     * POST /api/chat/send
     * Universal - works for all user types
     */
    @PostMapping("/send")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatMessageResponse> sendMessageRest(@Valid @RequestBody ChatMessageRequest request) {
        try {
            ChatMessageResponse response = chatMessageService.sendMessage(request);
            
            // Also send via WebSocket if available
            try {
                var recipient = universalUserService.findByUserId(request.getRecipientId());
                
                messagingTemplate.convertAndSendToUser(
                        recipient.email,  // Use email instead of ID
                        "/queue/messages",
                        response
                );
            } catch (Exception e) {
                log.warn("Failed to send WebSocket notification", e);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending message via REST", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get conversation history with another user (universal)
     * GET /api/chat/conversation/{otherUserId}
     */
    @GetMapping("/conversation/{otherUserId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMessageResponse>> getConversation(@PathVariable Long otherUserId) {
        List<ChatMessageResponse> messages = chatMessageService.getConversation(otherUserId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get conversation history with pagination
     * GET /api/chat/conversation/{otherUserId}/paginated
     */
    @GetMapping("/conversation/{otherUserId}/paginated")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ChatMessageResponse>> getConversationPaginated(
            @PathVariable Long otherUserId,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<ChatMessageResponse> messages = chatMessageService.getConversation(otherUserId, pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * Mark messages from a sender as read
     * PUT /api/chat/read/{senderId}
     */
    @PutMapping("/read/{senderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable Long senderId) {
        chatMessageService.markMessagesAsRead(senderId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get all conversations
     * GET /api/chat/conversations
     */
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConversationResponse>> getConversations() {
        List<ConversationResponse> conversations = chatMessageService.getConversations();
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get unread message count
     * GET /api/chat/unread/count
     */
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadCount() {
        Long count = chatMessageService.getUnreadCount();
        return ResponseEntity.ok(count);
    }

    /**
     * Get unread messages
     * GET /api/chat/unread
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMessageResponse>> getUnreadMessages() {
        List<ChatMessageResponse> messages = chatMessageService.getUnreadMessages();
        return ResponseEntity.ok(messages);
    }
}
