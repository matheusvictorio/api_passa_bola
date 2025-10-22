package com.fiap.projects.apipassabola.config;

import com.fiap.projects.apipassabola.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * Interceptor to authenticate WebSocket connections using JWT tokens
 * Validates JWT token from Authorization header on CONNECT frame
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            log.info("🔌 [WebSocket] Received STOMP command: {}", command);
            
            if (StompCommand.CONNECT.equals(command)) {
                // Get token from Authorization header in STOMP CONNECT frame
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                log.info("🔑 [WebSocket] Authorization header present: {}", authHeader != null);
                
                if (authHeader != null) {
                    // Remove "Bearer " prefix if present
                    String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
                    log.info("🎫 [WebSocket] Token extracted, length: {}", token.length());
                    authenticateUser(token, accessor);
                } else {
                    log.warn("⚠️ [WebSocket] CONNECT attempt without Authorization header");
                }
            } else if (StompCommand.SEND.equals(command)) {
                log.info("📨 [WebSocket] SEND command - User: {}", accessor.getUser() != null ? accessor.getUser().getName() : "ANONYMOUS");
            } else if (StompCommand.SUBSCRIBE.equals(command)) {
                log.info("📬 [WebSocket] SUBSCRIBE command - Destination: {}, User: {}", 
                        accessor.getDestination(), 
                        accessor.getUser() != null ? accessor.getUser().getName() : "ANONYMOUS");
            }
        }
        
        return message;
    }

    private void authenticateUser(String token, StompHeaderAccessor accessor) {
        try {
            log.info("🔐 [WebSocket] Starting authentication...");
            String email = jwtUtil.extractUsername(token);
            log.info("📧 [WebSocket] Extracted email from token: {}", email);
            
            if (email != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                log.info("👤 [WebSocket] User found: {}, authorities: {}", 
                        email, userDetails.getAuthorities());
                
                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    accessor.setUser(authentication);
                    
                    log.info("✅ [WebSocket] Authentication successful for user: {}", email);
                } else {
                    log.warn("⚠️ [WebSocket] Invalid JWT token for user: {}", email);
                }
            } else {
                log.warn("⚠️ [WebSocket] Could not extract email from token");
            }
        } catch (Exception e) {
            log.error("❌ [WebSocket] Error authenticating: {}", e.getMessage(), e);
        }
    }
}
