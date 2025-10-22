package com.fiap.projects.apipassabola.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time chat functionality
 * Enables STOMP protocol over WebSocket for direct messaging between players
 * Includes JWT authentication via WebSocketAuthInterceptor
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker to send messages to clients
        // Prefix for messages FROM server TO client
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages FROM client TO server
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint for WebSocket connections WITHOUT SockJS (for Postman testing)
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*"); // Configure CORS as needed
        
        // Register STOMP endpoint WITH SockJS (for browser compatibility)
        registry.addEndpoint("/ws-chat-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add JWT authentication interceptor to validate tokens on CONNECT
        registration.interceptors(webSocketAuthInterceptor);
    }
    
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(new org.springframework.messaging.support.ChannelInterceptor() {
            @Override
            public org.springframework.messaging.Message<?> preSend(
                    org.springframework.messaging.Message<?> message, 
                    org.springframework.messaging.MessageChannel channel) {
                
                org.springframework.messaging.simp.stomp.StompHeaderAccessor accessor = 
                    org.springframework.messaging.support.MessageHeaderAccessor.getAccessor(
                        message, org.springframework.messaging.simp.stomp.StompHeaderAccessor.class);
                
                if (accessor != null) {
                    log.info("📤 [OUTBOUND] Command: {}, Destination: {}, User: {}", 
                            accessor.getCommand(),
                            accessor.getDestination(),
                            accessor.getUser() != null ? accessor.getUser().getName() : "null");
                }
                
                return message;
            }
        });
    }
}
