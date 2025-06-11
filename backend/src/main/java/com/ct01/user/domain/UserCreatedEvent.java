package com.ct01.user.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;

/**
 * Доменное событие: пользователь создан
 */
public record UserCreatedEvent(
    UserId userId,
    String username,
    String email,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public UserCreatedEvent(UserId userId, String username, String email) {
        this(userId, username, email, LocalDateTime.now());
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
