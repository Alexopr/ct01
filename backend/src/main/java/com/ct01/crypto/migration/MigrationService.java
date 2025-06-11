package com.ct01.crypto.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Сервис для управления миграцией функциональности от legacy к новой структуре
 * Обеспечивает логирование, мониторинг и постепенный переход
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationService {
    
    // Статистика использования
    private final Map<String, AtomicLong> legacyApiUsage = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> newApiUsage = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastUsed = new ConcurrentHashMap<>();
    
    // Флаги миграции
    private boolean migrationEnabled = true;
    private boolean parallelMode = true; // Работают оба API параллельно
    private boolean legacyDeprecated = false; // Legacy помечен устаревшим
    
    @PostConstruct
    public void initMigration() {
        log.info("Initializing Migration Service");
        log.info("Migration mode: {}", migrationEnabled ? "ENABLED" : "DISABLED");
        log.info("Parallel mode: {}", parallelMode ? "BOTH APIs ACTIVE" : "NEW API ONLY");
        
        if (migrationEnabled) {
            log.warn("MIGRATION MODE: Legacy crypto API endpoints are deprecated and will be removed in future versions");
            log.warn("Please migrate to new API endpoints: /api/v1/crypto/");
        }
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("=== CRYPTO API MIGRATION STATUS ===");
        log.info("Legacy API Base: /api/tracked-coins, /api/coins, /api/exchanges");
        log.info("New API Base: /api/v1/crypto/");
        log.info("Migration Status: {}", getMigrationStatusText());
        log.info("Recommended Action: Start using new API endpoints");
        log.info("===================================");
    }
    
    /**
     * Записать использование legacy API
     */
    public void recordLegacyApiUsage(String endpoint, String method) {
        String key = method + " " + endpoint;
        legacyApiUsage.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        lastUsed.put(key, LocalDateTime.now());
        
        if (legacyDeprecated) {
            log.warn("DEPRECATED API USAGE: {} {} - Please migrate to /api/v1/crypto/ endpoints", 
                    method, endpoint);
        } else {
            log.debug("Legacy API usage: {} {}", method, endpoint);
        }
    }
    
    /**
     * Записать использование нового API
     */
    public void recordNewApiUsage(String endpoint, String method) {
        String key = method + " " + endpoint;
        newApiUsage.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        lastUsed.put(key, LocalDateTime.now());
        
        log.debug("New API usage: {} {}", method, endpoint);
    }
    
    /**
     * Проверить, нужно ли показать предупреждение о миграции
     */
    public boolean shouldShowMigrationWarning(String endpoint) {
        if (!migrationEnabled || !legacyDeprecated) {
            return false;
        }
        
        // Показываем предупреждение каждый 10-й запрос к legacy API
        String key = "GET " + endpoint;
        AtomicLong usage = legacyApiUsage.get(key);
        return usage != null && usage.get() % 10 == 0;
    }
    
    /**
     * Получить статистику миграции
     */
    public MigrationStats getMigrationStats() {
        long totalLegacyUsage = legacyApiUsage.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
        
        long totalNewUsage = newApiUsage.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
        
        double migrationProgress = 0.0;
        if (totalLegacyUsage + totalNewUsage > 0) {
            migrationProgress = (double) totalNewUsage / (totalLegacyUsage + totalNewUsage) * 100;
        }
        
        return MigrationStats.builder()
                .legacyApiUsage(totalLegacyUsage)
                .newApiUsage(totalNewUsage)
                .migrationProgress(migrationProgress)
                .migrationEnabled(migrationEnabled)
                .parallelMode(parallelMode)
                .legacyDeprecated(legacyDeprecated)
                .lastStatsUpdate(LocalDateTime.now())
                .topLegacyEndpoints(getTopEndpoints(legacyApiUsage))
                .topNewEndpoints(getTopEndpoints(newApiUsage))
                .build();
    }
    
    /**
     * Переключить режим миграции
     */
    public void setMigrationPhase(MigrationPhase phase) {
        switch (phase) {
            case PHASE_1_PARALLEL:
                migrationEnabled = true;
                parallelMode = true;
                legacyDeprecated = false;
                log.info("Migration Phase 1: Both APIs active, no deprecation warnings");
                break;
                
            case PHASE_2_DEPRECATED:
                migrationEnabled = true;
                parallelMode = true;
                legacyDeprecated = true;
                log.warn("Migration Phase 2: Legacy API deprecated, migration warnings enabled");
                break;
                
            case PHASE_3_NEW_ONLY:
                migrationEnabled = false;
                parallelMode = false;
                legacyDeprecated = true;
                log.error("Migration Phase 3: Legacy API disabled, new API only");
                break;
        }
    }
    
    /**
     * Проверить, активен ли legacy API
     */
    public boolean isLegacyApiEnabled() {
        return parallelMode;
    }
    
    /**
     * Проверить, устарел ли legacy API
     */
    public boolean isLegacyApiDeprecated() {
        return legacyDeprecated;
    }
    
    private String getMigrationStatusText() {
        if (!migrationEnabled) {
            return "COMPLETED - New API Only";
        }
        if (legacyDeprecated) {
            return "IN_PROGRESS - Legacy Deprecated";
        }
        return "STARTING - Both APIs Active";
    }
    
    private Map<String, Long> getTopEndpoints(Map<String, AtomicLong> usage) {
        return usage.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get(),
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));
    }
    
    public enum MigrationPhase {
        PHASE_1_PARALLEL,    // Оба API работают без предупреждений
        PHASE_2_DEPRECATED,  // Legacy API помечен устаревшим
        PHASE_3_NEW_ONLY     // Только новый API
    }
    
    @lombok.Builder
    @lombok.Data
    public static class MigrationStats {
        private long legacyApiUsage;
        private long newApiUsage;
        private double migrationProgress;
        private boolean migrationEnabled;
        private boolean parallelMode;
        private boolean legacyDeprecated;
        private LocalDateTime lastStatsUpdate;
        private Map<String, Long> topLegacyEndpoints;
        private Map<String, Long> topNewEndpoints;
    }
} 
