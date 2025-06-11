package com.ct01.subscription.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;
import java.time.LocalDateTime;

public record SubscriptionActivatedEvent(
    SubscriptionId subscriptionId,
    UserId userId,
    PlanType planType,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public SubscriptionActivatedEvent(SubscriptionId subscriptionId, UserId userId, PlanType planType) {
        this(subscriptionId, userId, planType, LocalDateTime.now());
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
