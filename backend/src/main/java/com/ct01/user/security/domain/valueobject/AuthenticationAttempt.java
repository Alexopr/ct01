package com.ct01.user.security.domain.valueobject;

import com.ct01.core.domain.UserId;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Value Object для попытки аутентификации
 * Хранит информацию о попытках аутентификации пользователя
 */
@Getter
public class AuthenticationAttempt {
    
    private final UserId userId;
    private final String clientIp;
    private int failedAttempts;
    private LocalDateTime firstAttemptAt;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime lockedAt;
    private boolean locked;
    
    public AuthenticationAttempt(UserId userId, String clientIp) {
        this.userId = userId;
        this.clientIp = clientIp;
        this.failedAttempts = 0;
        this.firstAttemptAt = LocalDateTime.now();
        this.lastAttemptAt = LocalDateTime.now();
        this.locked = false;
    }
    
    /**
     * Записывает неудачную попытку аутентификации
     */
    public void recordFailedAttempt() {
        this.failedAttempts++;
        this.lastAttemptAt = LocalDateTime.now();
        
        if (this.firstAttemptAt == null) {
            this.firstAttemptAt = LocalDateTime.now();
        }
    }
    
    /**
     * Блокирует аккаунт
     */
    public void lockAccount() {
        this.locked = true;
        this.lockedAt = LocalDateTime.now();
    }
    
    /**
     * Проверяет, истекла ли блокировка
     */
    public boolean isLockoutExpired(long lockoutDurationMinutes) {
        if (!locked || lockedAt == null) {
            return true;
        }
        
        LocalDateTime unlockTime = lockedAt.plusMinutes(lockoutDurationMinutes);
        return LocalDateTime.now().isAfter(unlockTime);
    }
    
    /**
     * Получает время до разблокировки
     */
    public long getMinutesUntilUnlock(long lockoutDurationMinutes) {
        if (!locked || lockedAt == null) {
            return 0;
        }
        
        LocalDateTime unlockTime = lockedAt.plusMinutes(lockoutDurationMinutes);
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(unlockTime)) {
            return 0;
        }
        
        return java.time.Duration.between(now, unlockTime).toMinutes();
    }
    
    /**
     * Проверяет, заблокирован ли аккаунт в данный момент
     */
    public boolean isCurrentlyLocked(long lockoutDurationMinutes) {
        return locked && !isLockoutExpired(lockoutDurationMinutes);
    }
    
    /**
     * Сбрасывает попытки аутентификации
     */
    public void reset() {
        this.failedAttempts = 0;
        this.locked = false;
        this.lockedAt = null;
        this.firstAttemptAt = null;
        this.lastAttemptAt = null;
    }
    
    /**
     * Проверяет, превышен ли лимит попыток
     */
    public boolean isAttemptsLimitExceeded(int maxAttempts) {
        return failedAttempts >= maxAttempts;
    }
    
    @Override
    public String toString() {
        return String.format("AuthenticationAttempt{userId=%s, clientIp='%s', failedAttempts=%d, locked=%s}", 
                userId.getValue(), clientIp, failedAttempts, locked);
    }
} 