package com.ct01.user.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;
import java.time.LocalDateTime;
import java.util.List;

public record UserRolesChangedEvent(
    UserId userId,
    String username,
    List<String> roleNames,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public UserRolesChangedEvent(UserId userId, String username, List<String> roleNames) {
        this(userId, username, roleNames, LocalDateTime.now());
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
