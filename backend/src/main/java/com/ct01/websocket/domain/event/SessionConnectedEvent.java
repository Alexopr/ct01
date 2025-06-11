package com.ct01.websocket.domain.event;

import com.ct01.shared.domain.DomainEvent;
import com.ct01.user.domain.UserId;
import com.ct01.websocket.domain.session.SessionId;

import java.time.LocalDateTime;

/**
 * Domain event - WebSocket сессия подключена
 */
public record SessionConnectedEvent(
    SessionId sessionId,
    UserId userId, // null для анонимных сессий
    String clientIp,
    boolean authenticated,
    LocalDateTime occurredOn
) implements DomainEvent {
    
    public SessionConnectedEvent(SessionId sessionId, UserId userId, String clientIp, boolean authenticated) {
        this(sessionId, userId, clientIp, authenticated, LocalDateTime.now());
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String eventType() {
        return "websocket.session.connected";
    }
    
    @Override
    public String aggregateId() {
        return sessionId.getValue();
    }
    
    /**
     * Получить информацию о подключении для логирования
     */
    public String getConnectionInfo() {
        return String.format("Session %s connected from IP %s%s", 
                           sessionId.getValue(), 
                           clientIp, 
                           authenticated ? " (authenticated)" : " (anonymous)");
    }
    
    /**
     * Получить уровень риска подключения
     */
    public String getRiskLevel() {
        // Базовая оценка риска на основе типа подключения
        return authenticated ? "LOW" : "MEDIUM";
    }
} 