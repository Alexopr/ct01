package com.ct01.crypto.migration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления процессом миграции от legacy к новой структуре API
 */
@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Migration Management", description = "Управление процессом миграции API")
public class MigrationController {
    
    private final MigrationService migrationService;
    
    /**
     * Получить статистику миграции
     */
    @Operation(summary = "Get migration statistics", 
               description = "Получить информацию о ходе миграции, включая статистику использования API")
    @GetMapping("/stats")
    public ResponseEntity<MigrationService.MigrationStats> getMigrationStats() {
        log.debug("Fetching migration statistics");
        
        MigrationService.MigrationStats stats = migrationService.getMigrationStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Получить статус миграции
     */
    @Operation(summary = "Get migration status", 
               description = "Получить текущий статус и конфигурацию миграции")
    @GetMapping("/status")
    public ResponseEntity<MigrationStatusDto> getMigrationStatus() {
        log.debug("Fetching migration status");
        
        MigrationStatusDto status = MigrationStatusDto.builder()
                .legacyApiEnabled(migrationService.isLegacyApiEnabled())
                .legacyApiDeprecated(migrationService.isLegacyApiDeprecated())
                .currentPhase(getCurrentPhase())
                .newApiEndpoints(getNewApiEndpoints())
                .legacyApiEndpoints(getLegacyApiEndpoints())
                .migrationRecommendations(getMigrationRecommendations())
                .build();
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Переключить фазу миграции (только для администраторов)
     */
    @Operation(summary = "Set migration phase", 
               description = "Переключить фазу миграции (требует административных прав)")
    @PostMapping("/phase")
    public ResponseEntity<String> setMigrationPhase(@RequestParam MigrationService.MigrationPhase phase) {
        log.info("Setting migration phase to: {}", phase);
        
        migrationService.setMigrationPhase(phase);
        
        String message = switch (phase) {
            case PHASE_1_PARALLEL -> "Migration Phase 1: Both APIs are now active without deprecation warnings";
            case PHASE_2_DEPRECATED -> "Migration Phase 2: Legacy API is now deprecated with warnings";
            case PHASE_3_NEW_ONLY -> "Migration Phase 3: Legacy API is now disabled, new API only";
        };
        
        return ResponseEntity.ok(message);
    }
    
    /**
     * Получить руководство по миграции
     */
    @Operation(summary = "Get migration guide", 
               description = "Получить подробное руководство по миграции API")
    @GetMapping("/guide")
    public ResponseEntity<MigrationGuideDto> getMigrationGuide() {
        log.debug("Fetching migration guide");
        
        MigrationGuideDto guide = MigrationGuideDto.builder()
                .title("Crypto API Migration Guide")
                .overview("Руководство по миграции от legacy API к новой модульной структуре")
                .legacyToNewMapping(getLegacyToNewMapping())
                .migrationSteps(getMigrationSteps())
                .breakingChanges(getBreakingChanges())
                .benefits(getMigrationBenefits())
                .timeline(getMigrationTimeline())
                .supportContact("tech-support@ct01.crypto")
                .build();
        
        return ResponseEntity.ok(guide);
    }
    
    private String getCurrentPhase() {
        if (!migrationService.isLegacyApiEnabled()) {
            return "PHASE_3_NEW_ONLY";
        }
        if (migrationService.isLegacyApiDeprecated()) {
            return "PHASE_2_DEPRECATED";
        }
        return "PHASE_1_PARALLEL";
    }
    
    private java.util.Map<String, String> getNewApiEndpoints() {
        return java.util.Map.of(
            "Coins", "/api/v1/crypto/coins",
            "Tracked Coins", "/api/v1/crypto/tracked-coins",
            "Prices", "/api/v1/crypto/prices"
        );
    }
    
    private java.util.Map<String, String> getLegacyApiEndpoints() {
        return java.util.Map.of(
            "Legacy Coins", "/api/v1/coins",
            "Legacy Tracked Coins", "/api/v1/tracked-coins",
            "Legacy Exchanges", "/api/v1/exchanges"
        );
    }
    
    private java.util.List<String> getMigrationRecommendations() {
        return java.util.List.of(
            "Start with read-only endpoints migration (GET requests)",
            "Test new API endpoints in development environment",
            "Update API client libraries to use new endpoints",
            "Implement error handling for deprecated API responses",
            "Plan migration for write operations (POST, PUT, DELETE)",
            "Monitor API usage statistics during transition"
        );
    }
    
    private java.util.Map<String, String> getLegacyToNewMapping() {
        return java.util.Map.of(
            "GET /api/v1/coins/{symbol}", "GET /api/v1/crypto/coins/{symbol}",
            "GET /api/v1/coins/search", "GET /api/v1/crypto/coins/search",
            "GET /api/v1/tracked-coins", "GET /api/v1/crypto/tracked-coins",
            "POST /api/v1/tracked-coins", "POST /api/v1/crypto/tracked-coins",
            "GET /api/v1/coins/{symbol}/price", "GET /api/v1/crypto/prices/current/{symbol}",
            "GET /api/v1/coins/{symbol}/stats", "GET /api/v1/crypto/prices/statistics/{symbol}"
        );
    }
    
    private java.util.List<String> getMigrationSteps() {
        return java.util.List.of(
            "1. Review new API documentation at /swagger-ui.html",
            "2. Update client code to use new endpoint URLs",
            "3. Test all operations with new API",
            "4. Update error handling for new response formats",
            "5. Deploy updated client code",
            "6. Monitor logs for any remaining legacy API usage",
            "7. Remove legacy API dependencies when ready"
        );
    }
    
    private java.util.List<String> getBreakingChanges() {
        return java.util.List.of(
            "API base path changed from /api/v1/* to /api/v1/crypto/*",
            "Response format standardized with ApiResponseDto wrapper",
            "Error codes and messages have been restructured",
            "Pagination parameters may have different names",
            "Some fields in DTOs have been renamed or restructured",
            "Authentication headers may have different requirements"
        );
    }
    
    private java.util.List<String> getMigrationBenefits() {
        return java.util.List.of(
            "Better API organization and discoverability",
            "Improved error handling and response consistency",
            "Enhanced validation and type safety",
            "Better performance with optimized query patterns",
            "Future-proof architecture for new features",
            "Comprehensive OpenAPI documentation"
        );
    }
    
    private java.util.Map<String, String> getMigrationTimeline() {
        return java.util.Map.of(
            "Phase 1", "Both APIs active (current) - 2 months",
            "Phase 2", "Legacy API deprecated with warnings - 1 month",
            "Phase 3", "Legacy API disabled, new API only - Ongoing"
        );
    }
    
    @lombok.Builder
    @lombok.Data
    public static class MigrationStatusDto {
        private boolean legacyApiEnabled;
        private boolean legacyApiDeprecated;
        private String currentPhase;
        private java.util.Map<String, String> newApiEndpoints;
        private java.util.Map<String, String> legacyApiEndpoints;
        private java.util.List<String> migrationRecommendations;
    }
    
    @lombok.Builder
    @lombok.Data
    public static class MigrationGuideDto {
        private String title;
        private String overview;
        private java.util.Map<String, String> legacyToNewMapping;
        private java.util.List<String> migrationSteps;
        private java.util.List<String> breakingChanges;
        private java.util.List<String> benefits;
        private java.util.Map<String, String> timeline;
        private String supportContact;
    }
} 
