package com.ct01.user.security.domain.event;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;

/**
 * Domain Event для неудачной попытки аутентификации
 */
public record AuthenticationFailedEvent(
    String email,
    String clientIp,
    FailureReason reason,
    UserId userId, // null если пользователь не найден
    int attemptNumber,
    boolean accountLocked,
    LocalDateTime failedAt,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public AuthenticationFailedEvent(String email, String clientIp, FailureReason reason, 
                                   UserId userId, int attemptNumber, boolean accountLocked) {
        this(email, clientIp, reason, userId, attemptNumber, accountLocked, 
             LocalDateTime.now(), LocalDateTime.now());
    }
    
    @Override
    public String getEventType() {
        return "AuthenticationFailed";
    }
    
    @Override
    public String getAggregateId() {
        return userId != null ? userId.getValue().toString() : email;
    }
    
    public enum FailureReason {
        INVALID_CREDENTIALS,    // Неверные логин/пароль
        USER_NOT_FOUND,        // Пользователь не найден
        ACCOUNT_DISABLED,      // Аккаунт отключен
        ACCOUNT_LOCKED,        // Аккаунт заблокирован
        RATE_LIMIT_EXCEEDED,   // Превышен лимит попыток
        INVALID_INPUT,         // Некорректный ввод
        SYSTEM_ERROR          // Системная ошибка
    }
    
    public boolean isSecurityThreat() {
        return reason == FailureReason.RATE_LIMIT_EXCEEDED || 
               attemptNumber >= 3;
    }
    
    public String getMaskedEmail() {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return "***@" + domain;
        }
        
        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + domain;
    }
} 