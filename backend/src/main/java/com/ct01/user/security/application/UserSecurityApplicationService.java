package com.ct01.user.security.application;

import com.ct01.core.domain.UserId;
import com.ct01.user.security.domain.service.AuthenticationDomainService;
import com.ct01.user.security.domain.valueobject.AuthenticationAttempt;
import com.ct01.user.security.domain.event.AuthenticationFailedEvent;
import com.ct01.user.security.domain.event.UnauthorizedAccessAttemptEvent;
import com.ct01.core.domain.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Application Service для управления безопасностью пользователей в DDD архитектуре
 * Обрабатывает rate limiting, мониторинг безопасности и аудит
 */
@Service
public class UserSecurityApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(UserSecurityApplicationService.class);
    
    private final AuthenticationDomainService authenticationService;
    private final DomainEventPublisher eventPublisher;
    
    // В реальном приложении это должно быть заменено на Redis или другое хранилище
    private final ConcurrentMap<String, RateLimitEntry> ipRateLimits = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RateLimitEntry> emailRateLimits = new ConcurrentHashMap<>();
    
    // Настройки rate limiting
    private static final int MAX_ATTEMPTS_PER_IP_PER_HOUR = 20;
    private static final int MAX_ATTEMPTS_PER_EMAIL_PER_HOUR = 5;
    private static final int RATE_LIMIT_WINDOW_MINUTES = 60;
    
    public UserSecurityApplicationService(AuthenticationDomainService authenticationService,
                                        DomainEventPublisher eventPublisher) {
        this.authenticationService = authenticationService;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Проверяет, разрешена ли попытка аутентификации с данного IP и для данного email
     */
    public ValidationResult validateAuthenticationAttempt(String clientIp, String email, UserId userId) {
        logger.debug("Validating authentication attempt for email: {} from IP: {}", 
                    maskEmail(email), clientIp);
        
        // Проверяем лимиты по IP
        if (!isIpAllowed(clientIp)) {
            logger.warn("Rate limit exceeded for IP: {}", clientIp);
            publishUnauthorizedAccess(userId, clientIp, "auth", "POST", 
                                    UnauthorizedAccessAttemptEvent.AccessDenialReason.RATE_LIMIT_EXCEEDED);
            return ValidationResult.rateLimitExceeded("IP rate limit exceeded");
        }
        
        // Проверяем лимиты по email
        if (!isEmailAllowed(email)) {
            logger.warn("Rate limit exceeded for email: {}", maskEmail(email));
            publishAuthenticationFailed(email, clientIp, 
                                      AuthenticationFailedEvent.FailureReason.RATE_LIMIT_EXCEEDED, 
                                      userId, getRateLimitAttempts(email), false);
            return ValidationResult.rateLimitExceeded("Email rate limit exceeded");
        }
        
        // Проверяем блокировку аккаунта через domain service
        AuthenticationAttempt userAttempt = authenticationService.getAuthenticationAttempt(userId);
        if (userAttempt.isLocked()) {
            logger.warn("Account locked for user: {}", userId != null ? userId.getValue() : "unknown");
            publishAuthenticationFailed(email, clientIp, 
                                      AuthenticationFailedEvent.FailureReason.ACCOUNT_LOCKED, 
                                      userId, userAttempt.getFailedAttempts(), true);
            return ValidationResult.accountLocked("Account is locked due to multiple failed attempts");
        }
        
        return ValidationResult.allowed();
    }
    
    /**
     * Записывает неудачную попытку аутентификации
     */
    public void recordAuthenticationFailure(String clientIp, String email, UserId userId, 
                                          AuthenticationFailedEvent.FailureReason reason) {
        logger.warn("Recording authentication failure for email: {} from IP: {}, reason: {}", 
                   maskEmail(email), clientIp, reason);
        
        // Увеличиваем счетчики rate limiting
        incrementIpAttempts(clientIp);
        incrementEmailAttempts(email);
        
        // Записываем неудачную попытку через domain service
        if (userId != null) {
            authenticationService.recordFailedAttempt(userId);
            AuthenticationAttempt attempt = authenticationService.getAuthenticationAttempt(userId);
            
            publishAuthenticationFailed(email, clientIp, reason, userId, 
                                      attempt.getFailedAttempts(), attempt.isLocked());
        } else {
            publishAuthenticationFailed(email, clientIp, reason, null, 1, false);
        }
    }
    
    /**
     * Сбрасывает лимиты аутентификации для пользователя (после успешного входа)
     */
    public void resetAuthenticationLimits(String email, UserId userId) {
        logger.debug("Resetting authentication limits for email: {}", maskEmail(email));
        
        // Сбрасываем лимиты по email (но не по IP)
        emailRateLimits.remove(email);
        
        // Сбрасываем блокировку аккаунта через domain service
        if (userId != null) {
            authenticationService.resetFailedAttempts(userId);
        }
    }
    
    /**
     * Записывает попытку несанкционированного доступа
     */
    public void recordUnauthorizedAccess(UserId userId, String clientIp, String userAgent,
                                       String requestedResource, String httpMethod,
                                       UnauthorizedAccessAttemptEvent.AccessDenialReason reason,
                                       String sessionToken) {
        logger.warn("Unauthorized access attempt from IP: {} to resource: {}, reason: {}", 
                   clientIp, requestedResource, reason);
        
        publishUnauthorizedAccess(userId, clientIp, userAgent, requestedResource, 
                                httpMethod, reason, sessionToken);
    }
    
    /**
     * Получает статистику безопасности для мониторинга
     */
    public SecurityStats getSecurityStats() {
        cleanupExpiredEntries();
        
        return new SecurityStats(
            ipRateLimits.size(),
            emailRateLimits.size(),
            ipRateLimits.values().stream().mapToInt(RateLimitEntry::getAttempts).sum(),
            emailRateLimits.values().stream().mapToInt(RateLimitEntry::getAttempts).sum()
        );
    }
    
    // Private helper methods
    
    private boolean isIpAllowed(String clientIp) {
        RateLimitEntry entry = ipRateLimits.get(clientIp);
        return entry == null || 
               entry.isExpired(RATE_LIMIT_WINDOW_MINUTES) || 
               entry.getAttempts() < MAX_ATTEMPTS_PER_IP_PER_HOUR;
    }
    
    private boolean isEmailAllowed(String email) {
        RateLimitEntry entry = emailRateLimits.get(email);
        return entry == null || 
               entry.isExpired(RATE_LIMIT_WINDOW_MINUTES) || 
               entry.getAttempts() < MAX_ATTEMPTS_PER_EMAIL_PER_HOUR;
    }
    
    private void incrementIpAttempts(String clientIp) {
        ipRateLimits.compute(clientIp, (ip, existing) -> {
            if (existing == null || existing.isExpired(RATE_LIMIT_WINDOW_MINUTES)) {
                return new RateLimitEntry(1, LocalDateTime.now());
            }
            return existing.increment();
        });
    }
    
    private void incrementEmailAttempts(String email) {
        emailRateLimits.compute(email, (e, existing) -> {
            if (existing == null || existing.isExpired(RATE_LIMIT_WINDOW_MINUTES)) {
                return new RateLimitEntry(1, LocalDateTime.now());
            }
            return existing.increment();
        });
    }
    
    private int getRateLimitAttempts(String email) {
        RateLimitEntry entry = emailRateLimits.get(email);
        return entry != null ? entry.getAttempts() : 0;
    }
    
    private void cleanupExpiredEntries() {
        ipRateLimits.entrySet().removeIf(entry -> 
            entry.getValue().isExpired(RATE_LIMIT_WINDOW_MINUTES));
        emailRateLimits.entrySet().removeIf(entry -> 
            entry.getValue().isExpired(RATE_LIMIT_WINDOW_MINUTES));
    }
    
    private void publishAuthenticationFailed(String email, String clientIp, 
                                           AuthenticationFailedEvent.FailureReason reason,
                                           UserId userId, int attemptNumber, boolean accountLocked) {
        AuthenticationFailedEvent event = new AuthenticationFailedEvent(
            email, clientIp, reason, userId, attemptNumber, accountLocked);
        eventPublisher.publish(event);
    }
    
    private void publishUnauthorizedAccess(UserId userId, String clientIp, String resource, 
                                         String method, UnauthorizedAccessAttemptEvent.AccessDenialReason reason) {
        publishUnauthorizedAccess(userId, clientIp, null, resource, method, reason, null);
    }
    
    private void publishUnauthorizedAccess(UserId userId, String clientIp, String userAgent,
                                         String resource, String method, 
                                         UnauthorizedAccessAttemptEvent.AccessDenialReason reason,
                                         String sessionToken) {
        UnauthorizedAccessAttemptEvent event = new UnauthorizedAccessAttemptEvent(
            userId, clientIp, userAgent, resource, method, reason, sessionToken);
        eventPublisher.publish(event);
    }
    
    private String maskEmail(String email) {
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
    
    // Nested classes
    
    public static class ValidationResult {
        private final boolean allowed;
        private final String reason;
        private final ResultType type;
        
        private ValidationResult(boolean allowed, String reason, ResultType type) {
            this.allowed = allowed;
            this.reason = reason;
            this.type = type;
        }
        
        public static ValidationResult allowed() {
            return new ValidationResult(true, null, ResultType.ALLOWED);
        }
        
        public static ValidationResult rateLimitExceeded(String reason) {
            return new ValidationResult(false, reason, ResultType.RATE_LIMITED);
        }
        
        public static ValidationResult accountLocked(String reason) {
            return new ValidationResult(false, reason, ResultType.ACCOUNT_LOCKED);
        }
        
        public boolean isAllowed() { return allowed; }
        public String getReason() { return reason; }
        public ResultType getType() { return type; }
        
        public enum ResultType {
            ALLOWED, RATE_LIMITED, ACCOUNT_LOCKED
        }
    }
    
    private static class RateLimitEntry {
        private final int attempts;
        private final LocalDateTime firstAttempt;
        
        public RateLimitEntry(int attempts, LocalDateTime firstAttempt) {
            this.attempts = attempts;
            this.firstAttempt = firstAttempt;
        }
        
        public RateLimitEntry increment() {
            return new RateLimitEntry(attempts + 1, firstAttempt);
        }
        
        public boolean isExpired(int windowMinutes) {
            return firstAttempt.isBefore(LocalDateTime.now().minusMinutes(windowMinutes));
        }
        
        public int getAttempts() { return attempts; }
    }
    
    public record SecurityStats(
        int activeIpLimits,
        int activeEmailLimits, 
        int totalIpAttempts,
        int totalEmailAttempts
    ) {}
} 