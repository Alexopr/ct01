package com.ct01.user.security.domain.service;

import com.ct01.core.domain.UserId;
import com.ct01.user.security.domain.valueobject.AuthenticationAttempt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Domain Service для аутентификации
 * Содержит бизнес-логику аутентификации
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationDomainService {
    
    // Временное хранилище попыток аутентификации (в production использовать Redis)
    private final ConcurrentHashMap<String, AuthenticationAttempt> authenticationAttempts = new ConcurrentHashMap<>();
    
    // Ограничения
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MINUTES = 15;
    
    /**
     * Проверяет, можно ли выполнить попытку аутентификации
     */
    public boolean validateAuthenticationAttempt(String clientIp, UserId userId) {
        String key = buildAttemptKey(clientIp, userId);
        AuthenticationAttempt attempt = authenticationAttempts.get(key);
        
        if (attempt == null) {
            return true;
        }
        
        // Проверяем, не заблокирован ли пользователь
        if (attempt.isLocked() && !attempt.isLockoutExpired(LOCKOUT_DURATION_MINUTES)) {
            log.warn("Authentication blocked for user {} from IP {} - lockout active", 
                    userId.getValue(), clientIp);
            return false;
        }
        
        // Если блокировка истекла, сбрасываем счетчик
        if (attempt.isLocked() && attempt.isLockoutExpired(LOCKOUT_DURATION_MINUTES)) {
            authenticationAttempts.remove(key);
            log.info("Lockout expired for user {} from IP {}, resetting attempts", 
                    userId.getValue(), clientIp);
            return true;
        }
        
        return true;
    }
    
    /**
     * Записывает неудачную попытку аутентификации
     */
    public void recordFailedAuthentication(UserId userId, String clientIp) {
        String key = buildAttemptKey(clientIp, userId);
        AuthenticationAttempt attempt = authenticationAttempts.computeIfAbsent(key, 
            k -> new AuthenticationAttempt(userId, clientIp));
        
        attempt.recordFailedAttempt();
        
        if (attempt.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            attempt.lockAccount();
            log.warn("User {} from IP {} locked due to {} failed attempts", 
                    userId.getValue(), clientIp, attempt.getFailedAttempts());
        } else {
            log.info("Failed authentication attempt {} for user {} from IP {}", 
                    attempt.getFailedAttempts(), userId.getValue(), clientIp);
        }
        
        authenticationAttempts.put(key, attempt);
    }
    
    /**
     * Записывает успешную аутентификацию
     */
    public void recordSuccessfulAuthentication(UserId userId, String clientIp, String sessionToken) {
        String key = buildAttemptKey(clientIp, userId);
        
        // Удаляем записи о неудачных попытках при успешной аутентификации
        authenticationAttempts.remove(key);
        
        log.info("Successful authentication for user {} from IP {} with session {}", 
                userId.getValue(), clientIp, sessionToken.substring(0, 8) + "...");
    }
    
    /**
     * Сбрасывает лимиты аутентификации для пользователя
     */
    public void resetAuthenticationLimits(UserId userId) {
        // Удаляем все записи для данного пользователя
        authenticationAttempts.entrySet().removeIf(entry -> 
            entry.getValue().getUserId().equals(userId));
        
        log.info("Authentication limits reset for user {}", userId.getValue());
    }
    
    /**
     * Создает ключ для попытки аутентификации
     */
    private String buildAttemptKey(String clientIp, UserId userId) {
        return clientIp + ":" + userId.getValue();
    }
    
    /**
     * Проверяет статус блокировки для пользователя/IP
     */
    public boolean isBlocked(String clientIp, UserId userId) {
        String key = buildAttemptKey(clientIp, userId);
        AuthenticationAttempt attempt = authenticationAttempts.get(key);
        
        if (attempt == null) {
            return false;
        }
        
        return attempt.isLocked() && !attempt.isLockoutExpired(LOCKOUT_DURATION_MINUTES);
    }
    
    /**
     * Получает количество неудачных попыток
     */
    public int getFailedAttempts(String clientIp, UserId userId) {
        String key = buildAttemptKey(clientIp, userId);
        AuthenticationAttempt attempt = authenticationAttempts.get(key);
        
        if (attempt == null) {
            return 0;
        }
        
        return attempt.getFailedAttempts();
    }
} 