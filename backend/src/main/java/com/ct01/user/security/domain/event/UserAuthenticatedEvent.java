package com.ct01.user.security.domain.event;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;

/**
 * Domain Event для успешной аутентификации пользователя
 */
public record UserAuthenticatedEvent(
    UserId userId,
    String email,
    String clientIp,
    String sessionToken,
    LocalDateTime authenticatedAt,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public UserAuthenticatedEvent(UserId userId, String email, String clientIp, String sessionToken) {
        this(userId, email, clientIp, sessionToken, LocalDateTime.now(), LocalDateTime.now());
    }
    
    @Override
    public String getEventType() {
        return "UserAuthenticated";
    }
    
    @Override
    public String getAggregateId() {
        return userId.getValue().toString();
    }
    
    public String getShortSessionToken() {
        return sessionToken != null && sessionToken.length() > 8 
            ? sessionToken.substring(0, 8) + "..." 
            : sessionToken;
    }
} 