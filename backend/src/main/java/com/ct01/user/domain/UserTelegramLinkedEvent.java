package com.ct01.user.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;
import java.time.LocalDateTime;

public record UserTelegramLinkedEvent(
    UserId userId,
    String username,
    Long telegramId,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public UserTelegramLinkedEvent(UserId userId, String username, Long telegramId) {
        this(userId, username, telegramId, LocalDateTime.now());
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
