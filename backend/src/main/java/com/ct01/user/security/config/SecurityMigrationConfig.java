package com.ct01.user.security.config;

import com.ct01.user.security.infrastructure.LegacySecurityBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

/**
 * Конфигурация для управления миграцией от legacy security к DDD архитектуре
 * Позволяет постепенно переключать компоненты и мониторить процесс
 */
@Configuration
@EnableScheduling
@ConfigurationProperties(prefix = "ct01.security.migration")
public class SecurityMigrationConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityMigrationConfig.class);
    
    // Configuration properties for migration control
    private boolean enableLegacyBridge = false;
    private boolean enableGradualMigration = true;
    private boolean enableMigrationLogging = true;
    private int migrationPhaseCheckIntervalMinutes = 30;
    
    // Migration phases flags
    private boolean useNewRateLimiting = true;
    private boolean useNewSecurityMonitoring = true;
    private boolean useNewAuthenticationFlow = false;
    private boolean useNewAuthorizationFlow = false;
    private boolean enableDualMode = false; // Запускает оба flow одновременно для сравнения
    
    private LegacySecurityBridge legacyBridge;
    
    @Bean
    @ConditionalOnProperty(name = "ct01.security.migration.enable-legacy-bridge", havingValue = "true")
    public SecurityMigrationController migrationController(LegacySecurityBridge bridge) {
        this.legacyBridge = bridge;
        return new SecurityMigrationController(this, bridge);
    }
    
    /**
     * Периодическая проверка статуса миграции
     */
    @Scheduled(fixedDelayString = "#{${ct01.security.migration.phase-check-interval-minutes:30} * 60000}")
    @ConditionalOnProperty(name = "ct01.security.migration.enable-migration-logging", havingValue = "true")
    public void logMigrationStatus() {
        if (!enableMigrationLogging) {
            return;
        }
        
        logger.info("=== Security Migration Status ===");
        logger.info("Legacy Bridge Enabled: {}", enableLegacyBridge);
        logger.info("New Rate Limiting: {}", useNewRateLimiting);
        logger.info("New Security Monitoring: {}", useNewSecurityMonitoring);
        logger.info("New Authentication Flow: {}", useNewAuthenticationFlow);
        logger.info("New Authorization Flow: {}", useNewAuthorizationFlow);
        logger.info("Dual Mode: {}", enableDualMode);
        
        if (legacyBridge != null) {
            LegacySecurityBridge.LegacySecurityStats stats = legacyBridge.getSecurityStatsForLegacy();
            logger.info("Security Stats - Active IP Limits: {}, Email Limits: {}, IP Attempts: {}, Email Attempts: {}", 
                       stats.activeIpLimits(), stats.activeEmailLimits(), 
                       stats.totalIpAttempts(), stats.totalEmailAttempts());
        }
        logger.info("================================");
    }
    
    // Getters and setters for configuration properties
    
    public boolean isEnableLegacyBridge() {
        return enableLegacyBridge;
    }
    
    public void setEnableLegacyBridge(boolean enableLegacyBridge) {
        this.enableLegacyBridge = enableLegacyBridge;
    }
    
    public boolean isEnableGradualMigration() {
        return enableGradualMigration;
    }
    
    public void setEnableGradualMigration(boolean enableGradualMigration) {
        this.enableGradualMigration = enableGradualMigration;
    }
    
    public boolean isEnableMigrationLogging() {
        return enableMigrationLogging;
    }
    
    public void setEnableMigrationLogging(boolean enableMigrationLogging) {
        this.enableMigrationLogging = enableMigrationLogging;
    }
    
    public int getMigrationPhaseCheckIntervalMinutes() {
        return migrationPhaseCheckIntervalMinutes;
    }
    
    public void setMigrationPhaseCheckIntervalMinutes(int migrationPhaseCheckIntervalMinutes) {
        this.migrationPhaseCheckIntervalMinutes = migrationPhaseCheckIntervalMinutes;
    }
    
    public boolean isUseNewRateLimiting() {
        return useNewRateLimiting;
    }
    
    public void setUseNewRateLimiting(boolean useNewRateLimiting) {
        this.useNewRateLimiting = useNewRateLimiting;
        if (legacyBridge != null) {
            if (useNewRateLimiting) {
                legacyBridge.enableNewRateLimiting();
            } else {
                legacyBridge.disableNewRateLimiting();
            }
        }
    }
    
    public boolean isUseNewSecurityMonitoring() {
        return useNewSecurityMonitoring;
    }
    
    public void setUseNewSecurityMonitoring(boolean useNewSecurityMonitoring) {
        this.useNewSecurityMonitoring = useNewSecurityMonitoring;
    }
    
    public boolean isUseNewAuthenticationFlow() {
        return useNewAuthenticationFlow;
    }
    
    public void setUseNewAuthenticationFlow(boolean useNewAuthenticationFlow) {
        this.useNewAuthenticationFlow = useNewAuthenticationFlow;
        if (legacyBridge != null) {
            if (useNewAuthenticationFlow) {
                legacyBridge.enableNewAuthenticationFlow();
            } else {
                legacyBridge.disableNewAuthenticationFlow();
            }
        }
    }
    
    public boolean isUseNewAuthorizationFlow() {
        return useNewAuthorizationFlow;
    }
    
    public void setUseNewAuthorizationFlow(boolean useNewAuthorizationFlow) {
        this.useNewAuthorizationFlow = useNewAuthorizationFlow;
    }
    
    public boolean isEnableDualMode() {
        return enableDualMode;
    }
    
    public void setEnableDualMode(boolean enableDualMode) {
        this.enableDualMode = enableDualMode;
    }
    
    /**
     * Controller для программного управления миграцией
     */
    public static class SecurityMigrationController {
        private static final Logger logger = LoggerFactory.getLogger(SecurityMigrationController.class);
        
        private final SecurityMigrationConfig config;
        private final LegacySecurityBridge bridge;
        
        public SecurityMigrationController(SecurityMigrationConfig config, LegacySecurityBridge bridge) {
            this.config = config;
            this.bridge = bridge;
            logger.info("Security Migration Controller initialized");
        }
        
        /**
         * Начинает постепенную миграцию с rate limiting
         */
        public void startGradualMigration() {
            logger.info("Starting gradual security migration...");
            
            // Phase 1: Enable rate limiting
            enableRateLimitingMigration();
            
            // Phase 2: Enable security monitoring
            enableSecurityMonitoringMigration();
            
            logger.info("Gradual migration initiated. Authentication flow migration should be done manually.");
        }
        
        /**
         * Включает миграцию rate limiting
         */
        public void enableRateLimitingMigration() {
            logger.info("Enabling rate limiting migration...");
            config.setUseNewRateLimiting(true);
            bridge.enableNewRateLimiting();
            logger.info("Rate limiting migration enabled");
        }
        
        /**
         * Включает миграцию security monitoring
         */
        public void enableSecurityMonitoringMigration() {
            logger.info("Enabling security monitoring migration...");
            config.setUseNewSecurityMonitoring(true);
            logger.info("Security monitoring migration enabled");
        }
        
        /**
         * Включает миграцию authentication flow
         */
        public void enableAuthenticationFlowMigration() {
            logger.info("Enabling authentication flow migration...");
            config.setUseNewAuthenticationFlow(true);
            bridge.enableNewAuthenticationFlow();
            logger.info("Authentication flow migration enabled");
        }
        
        /**
         * Откатывает миграцию к legacy системе
         */
        public void rollbackToLegacy() {
            logger.warn("Rolling back to legacy security system...");
            
            config.setUseNewAuthenticationFlow(false);
            config.setUseNewRateLimiting(false);
            config.setUseNewSecurityMonitoring(false);
            
            bridge.disableNewAuthenticationFlow();
            bridge.disableNewRateLimiting();
            
            logger.warn("Rollback to legacy security completed");
        }
        
        /**
         * Включает dual mode для A/B тестирования
         */
        public void enableDualMode() {
            logger.info("Enabling dual mode for A/B testing...");
            config.setEnableDualMode(true);
            logger.info("Dual mode enabled - both legacy and DDD flows will run");
        }
        
        /**
         * Завершает миграцию - полностью переключается на DDD
         */
        public void completeMigration() {
            logger.info("Completing migration to DDD security...");
            
            config.setUseNewAuthenticationFlow(true);
            config.setUseNewRateLimiting(true);
            config.setUseNewSecurityMonitoring(true);
            config.setEnableDualMode(false);
            
            bridge.enableNewAuthenticationFlow();
            bridge.enableNewRateLimiting();
            
            logger.info("Migration to DDD security completed successfully");
        }
        
        /**
         * Получает текущий статус миграции
         */
        public MigrationStatus getMigrationStatus() {
            return new MigrationStatus(
                config.isUseNewRateLimiting(),
                config.isUseNewSecurityMonitoring(),
                config.isUseNewAuthenticationFlow(),
                config.isUseNewAuthorizationFlow(),
                config.isEnableDualMode(),
                LocalDateTime.now()
            );
        }
        
        public record MigrationStatus(
            boolean rateLimitingMigrated,
            boolean securityMonitoringMigrated,
            boolean authenticationFlowMigrated,
            boolean authorizationFlowMigrated,
            boolean dualModeEnabled,
            LocalDateTime statusTime
        ) {
            public boolean isFullyMigrated() {
                return rateLimitingMigrated && securityMonitoringMigrated && 
                       authenticationFlowMigrated && authorizationFlowMigrated;
            }
            
            public int getMigrationPercentage() {
                int total = 4; // Total migration components
                int completed = 0;
                if (rateLimitingMigrated) completed++;
                if (securityMonitoringMigrated) completed++;
                if (authenticationFlowMigrated) completed++;
                if (authorizationFlowMigrated) completed++;
                
                return (completed * 100) / total;
            }
        }
    }
} 