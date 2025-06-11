package com.ct01.websocket.application.command;

import com.ct01.shared.application.Command;
import com.ct01.user.domain.UserId;

/**
 * Command для подключения WebSocket сессии
 */
public record ConnectSessionCommand(
    String springSessionId,
    UserId userId, // null для анонимных сессий
    String clientIp,
    String userAgent
) implements Command {
    
    public ConnectSessionCommand {
        if (springSessionId == null || springSessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Spring session ID cannot be null or empty");
        }
        if (clientIp == null || clientIp.trim().isEmpty()) {
            throw new IllegalArgumentException("Client IP cannot be null or empty");
        }
    }
    
    /**
     * Создать команду для аутентифицированной сессии
     */
    public static ConnectSessionCommand authenticated(String springSessionId, UserId userId, 
                                                    String clientIp, String userAgent) {
        return new ConnectSessionCommand(springSessionId, userId, clientIp, userAgent);
    }
    
    /**
     * Создать команду для анонимной сессии
     */
    public static ConnectSessionCommand anonymous(String springSessionId, String clientIp, String userAgent) {
        return new ConnectSessionCommand(springSessionId, null, clientIp, userAgent);
    }
    
    /**
     * Проверить является ли сессия аутентифицированной
     */
    public boolean isAuthenticated() {
        return userId != null;
    }
} 