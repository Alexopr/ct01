package com.ct01.user.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;
import java.time.LocalDateTime;

public record UserActivatedEvent(
    UserId userId,
    String username,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public UserActivatedEvent(UserId userId, String username) {
        this(userId, username, LocalDateTime.now());
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
