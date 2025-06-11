package com.ct01.user.security.infrastructure;

import com.ct01.core.domain.UserId;
import com.ct01.user.domain.User;
import com.ct01.user.security.application.UserSecurityApplicationService;
import com.ct01.user.security.domain.event.AuthenticationFailedEvent;
import com.ct01.user.security.domain.event.UserAuthenticatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Bridge для интеграции DDD Security компонентов с legacy security системой
 * Обеспечивает постепенную миграцию между архитектурами
 */
@Component
@ConditionalOnProperty(
    name = "ct01.security.enable-legacy-bridge", 
    havingValue = "true", 
    matchIfMissing = false
)
public class LegacySecurityBridge {
    private static final Logger logger = LoggerFactory.getLogger(LegacySecurityBridge.class);
    
    private final UserSecurityApplicationService securityService;
    
    // Флаги для управления интеграцией
    private boolean useNewAuthenticationFlow = false;
    private boolean useNewRateLimiting = true;
    private boolean useNewSecurityMonitoring = true;
    private boolean syncWithLegacyUserDetails = true;
    
    public LegacySecurityBridge(UserSecurityApplicationService securityService) {
        this.securityService = securityService;
        logger.info("Legacy Security Bridge initialized with DDD security integration");
    }
    
    /**
     * Адаптер для legacy аутентификации - проверяет rate limits через новую систему
     */
    public boolean validateLegacyAuthenticationAttempt(String email, String clientIp, Long legacyUserId) {
        if (!useNewRateLimiting) {
            return true; // Fallback to legacy behavior
        }
        
        try {
            UserId userId = legacyUserId != null ? UserId.of(legacyUserId) : null;
            UserSecurityApplicationService.ValidationResult result = 
                securityService.validateAuthenticationAttempt(clientIp, email, userId);
            
            if (!result.isAllowed()) {
                logger.warn("Legacy authentication blocked by DDD rate limiting: email={}, ip={}, reason={}", 
                           maskEmail(email), clientIp, result.getReason());
            }
            
            return result.isAllowed();
            
        } catch (Exception e) {
            logger.error("Error in legacy authentication validation, allowing by default: {}", e.getMessage());
            return true; // Fail open for legacy compatibility
        }
    }
    
    /**
     * Уведомляет новую систему о неудачной попытке аутентификации в legacy системе
     */
    public void notifyLegacyAuthenticationFailure(String email, String clientIp, Long legacyUserId, 
                                                 String legacyFailureReason) {
        if (!useNewSecurityMonitoring) {
            return;
        }
        
        try {
            UserId userId = legacyUserId != null ? UserId.of(legacyUserId) : null;
            AuthenticationFailedEvent.FailureReason reason = mapLegacyFailureReason(legacyFailureReason);
            
            securityService.recordAuthenticationFailure(clientIp, email, userId, reason);
            
            logger.debug("Legacy authentication failure recorded in DDD system: email={}, reason={}", 
                        maskEmail(email), reason);
            
        } catch (Exception e) {
            logger.error("Error recording legacy authentication failure: {}", e.getMessage());
        }
    }
    
    /**
     * Уведомляет новую систему об успешной аутентификации в legacy системе
     */
    public void notifyLegacyAuthenticationSuccess(String email, String clientIp, Long legacyUserId, 
                                                String sessionToken) {
        if (!useNewSecurityMonitoring) {
            return;
        }
        
        try {
            UserId userId = legacyUserId != null ? UserId.of(legacyUserId) : null;
            
            // Сбрасываем лимиты после успешного входа
            securityService.resetAuthenticationLimits(email, userId);
            
            logger.debug("Legacy authentication success processed in DDD system: email={}", 
                        maskEmail(email));
            
        } catch (Exception e) {
            logger.error("Error processing legacy authentication success: {}", e.getMessage());
        }
    }
    
    /**
     * Синхронизирует данные пользователя между legacy и DDD системами
     */
    public void syncUserDataFromLegacy(Long legacyUserId, String email, boolean isActive, 
                                     LocalDateTime lastLogin, String... roles) {
        if (!syncWithLegacyUserDetails) {
            return;
        }
        
        try {
            logger.debug("Syncing user data from legacy: userId={}, email={}, active={}", 
                        legacyUserId, maskEmail(email), isActive);
            
            // В реальной реализации здесь может быть синхронизация с DDD User aggregate
            // Пока логируем для отладки
            
        } catch (Exception e) {
            logger.error("Error syncing user data from legacy: {}", e.getMessage());
        }
    }
    
    /**
     * Получает статистику безопасности для legacy dashboard
     */
    public LegacySecurityStats getSecurityStatsForLegacy() {
        try {
            UserSecurityApplicationService.SecurityStats stats = securityService.getSecurityStats();
            
            return new LegacySecurityStats(
                stats.activeIpLimits(),
                stats.activeEmailLimits(),
                stats.totalIpAttempts(),
                stats.totalEmailAttempts(),
                LocalDateTime.now()
            );
            
        } catch (Exception e) {
            logger.error("Error getting security stats for legacy: {}", e.getMessage());
            return new LegacySecurityStats(0, 0, 0, 0, LocalDateTime.now());
        }
    }
    
    /**
     * Методы для управления флагами интеграции
     */
    public void enableNewAuthenticationFlow() {
        this.useNewAuthenticationFlow = true;
        logger.info("Enabled new DDD authentication flow");
    }
    
    public void disableNewAuthenticationFlow() {
        this.useNewAuthenticationFlow = false;
        logger.info("Disabled new DDD authentication flow");
    }
    
    public void enableNewRateLimiting() {
        this.useNewRateLimiting = true;
        logger.info("Enabled new DDD rate limiting");
    }
    
    public void disableNewRateLimiting() {
        this.useNewRateLimiting = false;
        logger.info("Disabled new DDD rate limiting");
    }
    
    public boolean isNewAuthenticationFlowEnabled() {
        return useNewAuthenticationFlow;
    }
    
    public boolean isNewRateLimitingEnabled() {
        return useNewRateLimiting;
    }
    
    // Private helper methods
    
    private AuthenticationFailedEvent.FailureReason mapLegacyFailureReason(String legacyReason) {
        if (legacyReason == null) {
            return AuthenticationFailedEvent.FailureReason.SYSTEM_ERROR;
        }
        
        return switch (legacyReason.toUpperCase()) {
            case "INVALID_CREDENTIALS", "BAD_CREDENTIALS", "WRONG_PASSWORD" -> 
                AuthenticationFailedEvent.FailureReason.INVALID_CREDENTIALS;
            case "USER_NOT_FOUND", "UNKNOWN_USER" -> 
                AuthenticationFailedEvent.FailureReason.USER_NOT_FOUND;
            case "ACCOUNT_DISABLED", "USER_DISABLED" -> 
                AuthenticationFailedEvent.FailureReason.ACCOUNT_DISABLED;
            case "ACCOUNT_LOCKED", "USER_LOCKED" -> 
                AuthenticationFailedEvent.FailureReason.ACCOUNT_LOCKED;
            case "RATE_LIMIT_EXCEEDED", "TOO_MANY_ATTEMPTS" -> 
                AuthenticationFailedEvent.FailureReason.RATE_LIMIT_EXCEEDED;
            case "INVALID_INPUT", "MALFORMED_REQUEST" -> 
                AuthenticationFailedEvent.FailureReason.INVALID_INPUT;
            default -> AuthenticationFailedEvent.FailureReason.SYSTEM_ERROR;
        };
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
    
    // Data transfer objects for legacy integration
    
    public record LegacySecurityStats(
        int activeIpLimits,
        int activeEmailLimits,
        int totalIpAttempts,
        int totalEmailAttempts,
        LocalDateTime reportTime
    ) {}
} 