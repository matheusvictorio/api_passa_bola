package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.NotificationResponse;
import com.fiap.projects.apipassabola.entity.UserType;
import com.fiap.projects.apipassabola.service.NotificationService;
import com.fiap.projects.apipassabola.service.UserContextService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@CrossOrigin(
    originPatterns = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS},
    allowedHeaders = "*",
    allowCredentials = "true"
)
public class NotificationController {


    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
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
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentGlobalUserIdAndType();
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
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentGlobalUserIdAndType();
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
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentGlobalUserIdAndType();
        
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
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentGlobalUserIdAndType();
        
        List<NotificationResponse> notifications = notificationService.getRecentNotifications(
                currentUser.getUserId(),
                currentUser.getUserType()
        );
        
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Marcar notificação como lida
     * Retorna 200 OK mesmo se a notificação não existir (graceful degradation)
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable String id) {
        try {
            Long notificationId = Long.parseLong(id);
            UserContextService.UserIdAndType currentUser = userContextService.getCurrentGlobalUserIdAndType();
            
            log.info("Tentando marcar notificação {} como lida. UserId: {}, UserType: {}", 
                    notificationId, currentUser.getUserId(), currentUser.getUserType());
            
            boolean success = notificationService.markAsRead(notificationId, currentUser.getUserId(), currentUser.getUserType());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            
            if (success) {
                response.put("message", "Notificação marcada como lida");
            } else {
                response.put("message", "Notificação não encontrada ou já processada");
            }
            
            // Sempre retorna 200 OK para evitar erros no frontend
            return ResponseEntity.ok(response);
            
        } catch (NumberFormatException e) {
            log.error("ID de notificação inválido: {}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "ID de notificação inválido");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao marcar notificação {} como lida: {}", id, e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erro ao processar requisição");
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Marcar todas as notificações como lidas
     */
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentGlobalUserIdAndType();
        
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
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable String id) {
        Long notificationId = Long.parseLong(id);
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentGlobalUserIdAndType();
        
        notificationService.deleteNotification(notificationId, currentUser.getUserId(), currentUser.getUserType());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notificação deletada com sucesso");
        
        return ResponseEntity.ok(response);
    }
}
