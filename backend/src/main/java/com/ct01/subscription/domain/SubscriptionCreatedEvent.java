package com.ct01.subscription.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;
import java.time.LocalDateTime;

public record SubscriptionCreatedEvent(
    SubscriptionId subscriptionId,
    UserId userId,
    PlanType planType,
    LocalDateTime startsAt,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public SubscriptionCreatedEvent(SubscriptionId subscriptionId, UserId userId, 
                                  PlanType planType, LocalDateTime startsAt) {
        this(subscriptionId, userId, planType, startsAt, LocalDateTime.now());
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
