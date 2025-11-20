package com.example.notificationservice.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            String token = null;

            if (authHeaders != null && !authHeaders.isEmpty()) {
                String authHeader = authHeaders.get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            if (token != null) {
                try {
                    // Validate token and extract userId
                    io.jsonwebtoken.Claims claims = jwtUtil.getClaims(token);
                    String userId = claims.get("userId", String.class);
                    String role = claims.get("role", String.class);

                    if (userId == null) {
                        throw new RuntimeException("Invalid token: no userId");
                    }

                    // Set authentication for WebSocket session
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    role != null
                                            ? Collections.singletonList(new SimpleGrantedAuthority(role))
                                            : Collections.emptyList()
                            );
                    accessor.setUser(auth);

                } catch (Exception e) {
                    throw new RuntimeException("WebSocket authentication failed: " + e.getMessage());
                }
            } else {
                throw new RuntimeException("WebSocket authentication required: missing token");
            }
        }

        return message;
    }
}