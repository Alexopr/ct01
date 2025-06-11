package com.ct01.websocket.domain.event;

import com.ct01.shared.domain.DomainEvent;
import com.ct01.websocket.domain.session.SessionId;

import java.time.LocalDateTime;

/**
 * Domain event - Удалена подписка на символ
 */
public record SubscriptionRemovedEvent(
    SessionId sessionId,
    String symbol,
    LocalDateTime removedAt,
    LocalDateTime occurredOn
) implements DomainEvent {
    
    public SubscriptionRemovedEvent(SessionId sessionId, String symbol, LocalDateTime removedAt) {
        this(sessionId, symbol, removedAt, LocalDateTime.now());
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String eventType() {
        return "websocket.subscription.removed";
    }
    
    @Override
    public String aggregateId() {
        return sessionId.getValue();
    }
    
    /**
     * Получить информацию об отписке для логирования
     */
    public String getUnsubscriptionInfo() {
        return String.format("Session %s unsubscribed from symbol %s", 
                           sessionId.getValue(), symbol);
    }
} 