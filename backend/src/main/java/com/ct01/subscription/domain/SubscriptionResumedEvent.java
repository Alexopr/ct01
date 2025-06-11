package com.ct01.subscription.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;
import java.time.LocalDateTime;

public record SubscriptionResumedEvent(
    SubscriptionId subscriptionId,
    UserId userId,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public SubscriptionResumedEvent(SubscriptionId subscriptionId, UserId userId) {
        this(subscriptionId, userId, LocalDateTime.now());
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
