package com.ct01.subscription.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;
import java.time.LocalDateTime;

public record SubscriptionRenewedEvent(
    SubscriptionId subscriptionId,
    UserId userId,
    LocalDateTime newExpiresAt,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public SubscriptionRenewedEvent(SubscriptionId subscriptionId, UserId userId, LocalDateTime newExpiresAt) {
        this(subscriptionId, userId, newExpiresAt, LocalDateTime.now());
    }
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    @Override
    public Object getAggregateId() {
        return subscriptionId;
    }
} 
