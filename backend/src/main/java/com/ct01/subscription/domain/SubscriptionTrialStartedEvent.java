package com.ct01.subscription.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;
import java.time.LocalDateTime;

public record SubscriptionTrialStartedEvent(
    SubscriptionId subscriptionId,
    UserId userId,
    PlanType planType,
    LocalDateTime trialEndsAt,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public SubscriptionTrialStartedEvent(SubscriptionId subscriptionId, UserId userId, 
                                       PlanType planType, LocalDateTime trialEndsAt) {
        this(subscriptionId, userId, planType, trialEndsAt, LocalDateTime.now());
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
