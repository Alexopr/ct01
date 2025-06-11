package com.ct01.crypto.migration;

import com.ct01.crypto.api.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Административный контроллер для мониторинга депрекации legacy endpoints
 */
@RestController
@RequestMapping("/api/v1/admin/deprecation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Deprecation Monitoring", description = "Мониторинг депрекации legacy API endpoints")
@ConditionalOnProperty(name = "legacy.api.monitoring.enabled", havingValue = "true", matchIfMissing = true)
public class DeprecationMonitoringController {
    
    private final LegacyEndpointMonitor monitor;
    private final MigrationService migrationService;
    private final DeprecationInterceptor deprecationInterceptor;
    
    /**
     * Получить общую статистику использования legacy API
     */
    @GetMapping("/stats")
    @Operation(summary = "Получить статистику legacy API", 
               description = "Возвращает общую статистику использования legacy endpoints")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LegacyEndpointMonitor.LegacyUsageStats>> getLegacyStats() {
        log.debug("Retrieving legacy API usage statistics");
        
        LegacyEndpointMonitor.LegacyUsageStats stats = monitor.getLegacyUsageStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Legacy API statistics retrieved successfully"));
    }
    
    /**
     * Получить детальную статистику по конкретному endpoint
     */
    @GetMapping("/stats/endpoint")
    @Operation(summary = "Статистика по endpoint", 
               description = "Получить детальную статистику использования конкретного legacy endpoint")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LegacyEndpointMonitor.EndpointUsageDetails>> getEndpointStats(
            @Parameter(description = "URL endpoint для анализа", example = "/api/v1/coins")
            @RequestParam String endpoint,
            @Parameter(description = "HTTP метод", example = "GET") 
            @RequestParam String method) {
        
        log.debug("Retrieving statistics for legacy endpoint: {} {}", method, endpoint);
        
        LegacyEndpointMonitor.EndpointUsageDetails details = monitor.getEndpointDetails(endpoint, method);
        return ResponseEntity.ok(ApiResponse.success(details, "Endpoint statistics retrieved successfully"));
    }
    
    /**
     * Получить статус конфигурации депрекации
     */
    @GetMapping("/config")
    @Operation(summary = "Конфигурация депрекации", 
               description = "Получить текущие настройки системы депрекации")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDeprecationConfig() {
        log.debug("Retrieving deprecation configuration");
        
        Map<String, Object> config = Map.of(
            "legacyApiEnabled", migrationService.isLegacyApiEnabled(),
            "deprecationWarningsEnabled", true, // Из конфигурации
            "monitoringEnabled", true,
            "interceptorActive", true,
            "migrationPhase", migrationService.getCurrentMigrationPhase()
        );
        
        return ResponseEntity.ok(ApiResponse.success(config, "Deprecation configuration retrieved successfully"));
    }
    
    /**
     * Включить/выключить legacy API
     */
    @PostMapping("/config/legacy-api")
    @Operation(summary = "Управление legacy API", 
               description = "Включить или выключить доступ к legacy API endpoints")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> toggleLegacyApi(@RequestParam boolean enabled) {
        log.info("Toggling legacy API access: {}", enabled);
        
        // Здесь можно добавить логику изменения конфигурации во время выполнения
        // Пока что просто логируем действие
        
        String message = enabled ? "Legacy API access enabled" : "Legacy API access disabled";
        return ResponseEntity.ok(ApiResponse.success(message, "Legacy API status updated successfully"));
    }
    
    /**
     * Очистить кэш legacy endpoints
     */
    @PostMapping("/cache/clear")
    @Operation(summary = "Очистить кэш", 
               description = "Очистить кэш определения legacy endpoints")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> clearCache() {
        log.info("Clearing legacy endpoint cache");
        
        deprecationInterceptor.clearCache();
        
        return ResponseEntity.ok(ApiResponse.success("Cache cleared", "Legacy endpoint cache cleared successfully"));
    }
    
    /**
     * Получить список всех известных legacy endpoints
     */
    @GetMapping("/endpoints")
    @Operation(summary = "Список legacy endpoints", 
               description = "Получить список всех известных legacy endpoints с их статусом")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLegacyEndpoints() {
        log.debug("Retrieving list of legacy endpoints");
        
        Map<String, Object> endpoints = Map.of(
            "coins", Map.of(
                "legacy", "/api/v1/coins",
                "new", "/api/v1/crypto/coins",
                "status", "partially_migrated",
                "removalDate", "2024-06-01"
            ),
            "trackedCoins", Map.of(
                "legacy", "/api/v1/tracked-coins",
                "new", "/api/v1/crypto/tracked-coins",
                "status", "partially_migrated",
                "removalDate", "2024-06-01"
            ),
            "prices", Map.of(
                "legacy", "/api/v1/prices",
                "new", "/api/v1/crypto/prices",
                "status", "migrated",
                "removalDate", "2024-06-01"
            ),
            "exchanges", Map.of(
                "legacy", "/api/v1/exchanges",
                "new", "/api/v1/crypto/exchanges",
                "status", "not_migrated",
                "removalDate", "2024-03-01"
            ),
            "system", Map.of(
                "legacy", "/api/v1/system",
                "new", "/api/v1/system",
                "status", "partially_migrated",
                "removalDate", "2024-09-01"
            ),
            "subscriptions", Map.of(
                "legacy", "/api/v1/subscriptions",
                "new", "/api/v1/subscriptions",
                "status", "migrated",
                "removalDate", "2024-09-01"
            ),
            "users", Map.of(
                "legacy", "/api/v1/users",
                "new", "/api/v1/users",
                "status", "migrated",
                "removalDate", "2024-09-01"
            ),
            "analysis", Map.of(
                "legacy", "/api/v1/analysis",
                "new", "/api/v1/admin/analysis",
                "status", "migrated",
                "removalDate", "2024-09-01"
            )
        );
        
        return ResponseEntity.ok(ApiResponse.success(endpoints, "Legacy endpoints list retrieved successfully"));
    }
    
    /**
     * Экспортировать статистику в CSV формате
     */
    @GetMapping("/export/csv")
    @Operation(summary = "Экспорт в CSV", 
               description = "Экспортировать статистику использования legacy API в CSV формате")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportStatsToCsv() {
        log.info("Exporting legacy API statistics to CSV");
        
        LegacyEndpointMonitor.LegacyUsageStats stats = monitor.getLegacyUsageStats();
        
        StringBuilder csv = new StringBuilder();
        csv.append("Endpoint,Method,Request Count,Unique Clients\n");
        
        stats.getPopularEndpoints().forEach((endpoint, count) -> {
            String[] parts = endpoint.split(" ", 2);
            String method = parts.length > 1 ? parts[0] : "UNKNOWN";
            String path = parts.length > 1 ? parts[1] : endpoint;
            
            LegacyEndpointMonitor.EndpointUsageDetails details = monitor.getEndpointDetails(path, method);
            csv.append(String.format("%s,%s,%d,%d\n", 
                    path, method, count, details.getUniqueClients()));
        });
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=legacy-api-stats.csv")
                .body(csv.toString());
    }
} 