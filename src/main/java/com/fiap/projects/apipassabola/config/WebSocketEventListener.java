package com.fiap.projects.apipassabola.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket event listener to track connected users and subscriptions
 */
@Component
@Slf4j
public class WebSocketEventListener {

    // Track active sessions: sessionId -> email
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>();
    
    // Track subscriptions: sessionId -> destination
    private final Map<String, String> subscriptions = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "ANONYMOUS";
        
        activeSessions.put(sessionId, username);
        
        log.info("🟢 [WebSocket] User CONNECTED: {} (sessionId: {})", username, sessionId);
        log.info("📊 [WebSocket] Total active sessions: {}", activeSessions.size());
        log.info("👥 [WebSocket] Connected users: {}", activeSessions.values());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = activeSessions.remove(sessionId);
        subscriptions.remove(sessionId);
        
        log.info("🔴 [WebSocket] User DISCONNECTED: {} (sessionId: {})", username, sessionId);
        log.info("📊 [WebSocket] Total active sessions: {}", activeSessions.size());
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "ANONYMOUS";
        
        subscriptions.put(sessionId, destination);
        
        log.info("📬 [WebSocket] User SUBSCRIBED: {} to {} (sessionId: {})", 
                username, destination, sessionId);
        log.info("📊 [WebSocket] Active subscriptions: {}", subscriptions.size());
    }
}
