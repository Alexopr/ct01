package com.ct01.websocket.domain.event;

import com.ct01.shared.domain.DomainEvent;
import com.ct01.websocket.domain.session.SessionId;

import java.time.LocalDateTime;

/**
 * Domain event - Добавлена подписка на символ
 */
public record SubscriptionAddedEvent(
    SessionId sessionId,
    String symbol,
    LocalDateTime subscribedAt,
    LocalDateTime occurredOn
) implements DomainEvent {
    
    public SubscriptionAddedEvent(SessionId sessionId, String symbol, LocalDateTime subscribedAt) {
        this(sessionId, symbol, subscribedAt, LocalDateTime.now());
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String eventType() {
        return "websocket.subscription.added";
    }
    
    @Override
    public String aggregateId() {
        return sessionId.getValue();
    }
    
    /**
     * Получить информацию о подписке для логирования
     */
    public String getSubscriptionInfo() {
        return String.format("Session %s subscribed to symbol %s", 
                           sessionId.getValue(), symbol);
    }
} 