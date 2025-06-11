package com.ct01.user.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;
import java.time.LocalDateTime;

public record UserBlockedEvent(
    UserId userId,
    String username,
    String reason,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public UserBlockedEvent(UserId userId, String username, String reason) {
        this(userId, username, reason, LocalDateTime.now());
    }
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    @Override
    public Object getAggregateId() {
        return userId;
    }
} 
