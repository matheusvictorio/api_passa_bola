package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.NotificationResponse;
import com.fiap.projects.apipassabola.entity.UserType;
import com.fiap.projects.apipassabola.service.NotificationService;
import com.fiap.projects.apipassabola.service.UserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    private final UserContextService userContextService;
    
    /**
     * Buscar minhas notificações com paginação
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        Pageable pageable = PageRequest.of(page, size);
        
        Page<NotificationResponse> notifications = notificationService.getMyNotifications(
                currentUser.getUserId(),
                currentUser.getUserType(),
                pageable
        );
        
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Buscar apenas notificações não lidas
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        Pageable pageable = PageRequest.of(page, size);
        
        Page<NotificationResponse> notifications = notificationService.getUnreadNotifications(
                currentUser.getUserId(),
                currentUser.getUserType(),
                pageable
        );
        
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Contar notificações não lidas
     */
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        long count = notificationService.countUnreadNotifications(
                currentUser.getUserId(),
                currentUser.getUserType()
        );
        
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Buscar notificações recentes (últimas 24h)
     */
    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getRecentNotifications() {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        List<NotificationResponse> notifications = notificationService.getRecentNotifications(
                currentUser.getUserId(),
                currentUser.getUserType()
        );
        
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Marcar notificação como lida
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        notificationService.markAsRead(id, currentUser.getUserId(), currentUser.getUserType());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notificação marcada como lida");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Marcar todas as notificações como lidas
     */
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        int count = notificationService.markAllAsRead(currentUser.getUserId(), currentUser.getUserType());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Todas as notificações foram marcadas como lidas");
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Deletar notificação
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        notificationService.deleteNotification(id, currentUser.getUserId(), currentUser.getUserType());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notificação deletada com sucesso");
        
        return ResponseEntity.ok(response);
    }
}
