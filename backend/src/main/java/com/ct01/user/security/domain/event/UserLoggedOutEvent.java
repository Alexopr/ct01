package com.ct01.user.security.domain.event;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;

/**
 * Domain Event для выхода пользователя из системы
 */
public record UserLoggedOutEvent(
    UserId userId,
    String email,
    String sessionToken,
    String clientIp,
    LogoutReason reason,
    LocalDateTime loggedOutAt,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public UserLoggedOutEvent(UserId userId, String email, String sessionToken, String clientIp, LogoutReason reason) {
        this(userId, email, sessionToken, clientIp, reason, LocalDateTime.now(), LocalDateTime.now());
    }
    
    @Override
    public String getEventType() {
        return "UserLoggedOut";
    }
    
    @Override
    public String getAggregateId() {
        return userId.getValue().toString();
    }
    
    public enum LogoutReason {
        USER_INITIATED,     // Пользователь сам вышел
        SESSION_EXPIRED,    // Сессия истекла
        ADMIN_FORCED,       // Принудительный выход администратором
        SECURITY_VIOLATION, // Нарушение безопасности
        SYSTEM_MAINTENANCE  // Системное обслуживание
    }
    
    public String getShortSessionToken() {
        return sessionToken != null && sessionToken.length() > 8 
            ? sessionToken.substring(0, 8) + "..." 
            : sessionToken;
    }
} 