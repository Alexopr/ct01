package com.ct01.user.security.domain.event;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;

/**
 * Domain Event для попыток несанкционированного доступа
 */
public record UnauthorizedAccessAttemptEvent(
    UserId userId, // null для анонимных попыток
    String clientIp,
    String userAgent,
    String requestedResource,
    String httpMethod,
    AccessDenialReason reason,
    String sessionToken, // null если нет сессии
    LocalDateTime attemptedAt,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public UnauthorizedAccessAttemptEvent(UserId userId, String clientIp, String userAgent,
                                        String requestedResource, String httpMethod,
                                        AccessDenialReason reason, String sessionToken) {
        this(userId, clientIp, userAgent, requestedResource, httpMethod, reason, sessionToken,
             LocalDateTime.now(), LocalDateTime.now());
    }
    
    @Override
    public String getEventType() {
        return "UnauthorizedAccessAttempt";
    }
    
    @Override
    public String getAggregateId() {
        return userId != null ? userId.getValue().toString() : clientIp;
    }
    
    public enum AccessDenialReason {
        NOT_AUTHENTICATED,      // Не аутентифицирован
        INSUFFICIENT_PRIVILEGES, // Недостаточно прав
        ACCOUNT_DISABLED,       // Аккаунт отключен
        SESSION_EXPIRED,        // Сессия истекла
        INVALID_TOKEN,          // Недействительный токен
        RATE_LIMIT_EXCEEDED,    // Превышен лимит запросов
        SUSPICIOUS_ACTIVITY,    // Подозрительная активность
        RESOURCE_NOT_FOUND,     // Ресурс не найден (возможно, сканирование)
        INVALID_REQUEST         // Некорректный запрос
    }
    
    public boolean isHighRiskAttempt() {
        return reason == AccessDenialReason.SUSPICIOUS_ACTIVITY ||
               reason == AccessDenialReason.RATE_LIMIT_EXCEEDED ||
               (reason == AccessDenialReason.RESOURCE_NOT_FOUND && 
                (requestedResource.contains("admin") || requestedResource.contains("api")));
    }
    
    public boolean isAnonymousAttempt() {
        return userId == null;
    }
    
    public String getShortSessionToken() {
        return sessionToken != null && sessionToken.length() > 8 
            ? sessionToken.substring(0, 8) + "..." 
            : sessionToken;
    }
    
    public String getMaskedUserAgent() {
        return userAgent != null && userAgent.length() > 50
            ? userAgent.substring(0, 50) + "..."
            : userAgent;
    }
} 