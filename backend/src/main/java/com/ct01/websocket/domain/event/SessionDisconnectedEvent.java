package com.ct01.websocket.domain.event;

import com.ct01.shared.domain.DomainEvent;
import com.ct01.user.domain.UserId;
import com.ct01.websocket.domain.session.SessionId;

import java.time.LocalDateTime;

/**
 * Domain event - WebSocket сессия отключена
 */
public record SessionDisconnectedEvent(
    SessionId sessionId,
    UserId userId, // null для анонимных сессий
    LocalDateTime disconnectedAt,
    int subscriptionCount,
    LocalDateTime occurredOn
) implements DomainEvent {
    
    public SessionDisconnectedEvent(SessionId sessionId, UserId userId, 
                                  LocalDateTime disconnectedAt, int subscriptionCount) {
        this(sessionId, userId, disconnectedAt, subscriptionCount, LocalDateTime.now());
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String eventType() {
        return "websocket.session.disconnected";
    }
    
    @Override
    public String aggregateId() {
        return sessionId.getValue();
    }
    
    /**
     * Получить информацию об отключении для логирования
     */
    public String getDisconnectionInfo() {
        return String.format("Session %s disconnected%s with %d subscriptions", 
                           sessionId.getValue(),
                           userId != null ? " (user: " + userId.getValue() + ")" : " (anonymous)",
                           subscriptionCount);
    }
    
    /**
     * Проверить было ли отключение внезапным
     */
    public boolean isAbruptDisconnection() {
        // Считаем отключение внезапным, если у сессии были активные подписки
        return subscriptionCount > 0;
    }
} 