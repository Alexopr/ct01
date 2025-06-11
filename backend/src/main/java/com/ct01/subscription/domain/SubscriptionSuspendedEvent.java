package com.ct01.subscription.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;
import java.time.LocalDateTime;

public record SubscriptionSuspendedEvent(
    SubscriptionId subscriptionId,
    UserId userId,
    String reason,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public SubscriptionSuspendedEvent(SubscriptionId subscriptionId, UserId userId, String reason) {
        this(subscriptionId, userId, reason, LocalDateTime.now());
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
