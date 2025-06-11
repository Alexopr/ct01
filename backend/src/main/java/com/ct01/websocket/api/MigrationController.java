package com.ct01.websocket.api;

import com.ct01.websocket.infrastructure.migration.LegacyWebSocketAdapter;
import com.ct01.websocket.infrastructure.migration.RoutingStats;
import com.ct01.websocket.infrastructure.migration.TrafficRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * REST API для управления миграцией WebSocket системы
 */
@RestController
@RequestMapping("/api/websocket/migration")
public class MigrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(MigrationController.class);
    
    private final TrafficRouter trafficRouter;
    private final LegacyWebSocketAdapter legacyAdapter;
    
    public MigrationController(TrafficRouter trafficRouter, LegacyWebSocketAdapter legacyAdapter) {
        this.trafficRouter = trafficRouter;
        this.legacyAdapter = legacyAdapter;
    }
    
    /**
     * Получить статистику роутинга трафика
     */
    @GetMapping("/routing/stats")
    public ResponseEntity<RoutingStats> getRoutingStats() {
        logger.debug("Getting routing statistics");
        return ResponseEntity.ok(trafficRouter.getRoutingStats());
    }
    
    /**
     * Обновить процент трафика для DDD системы
     */
    @PostMapping("/routing/traffic-percentage")
    public ResponseEntity<String> updateTrafficPercentage(@RequestParam int percentage) {
        try {
            trafficRouter.updateDddTrafficPercentage(percentage);
            logger.info("Updated traffic percentage to {}%", percentage);
            return ResponseEntity.ok("Traffic percentage updated to " + percentage + "%");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Включить/выключить DDD систему
     */
    @PostMapping("/routing/ddd-system")
    public ResponseEntity<String> toggleDddSystem(@RequestParam boolean enabled) {
        trafficRouter.setDddSystemEnabled(enabled);
        String message = "DDD system " + (enabled ? "enabled" : "disabled");
        logger.info(message);
        return ResponseEntity.ok(message);
    }
    
    /**
     * Принудительно направить клиента в DDD систему
     */
    @PostMapping("/routing/force-ddd")
    public ResponseEntity<String> forceClientToDdd(@RequestParam String clientId) {
        trafficRouter.forceToDdd(clientId);
        logger.info("Client {} forced to DDD system", clientId);
        return ResponseEntity.ok("Client " + clientId + " forced to DDD system");
    }
    
    /**
     * Принудительно направить клиента в legacy систему
     */
    @PostMapping("/routing/force-legacy")
    public ResponseEntity<String> forceClientToLegacy(@RequestParam String clientId) {
        trafficRouter.forceToLegacy(clientId);
        logger.info("Client {} forced to legacy system", clientId);
        return ResponseEntity.ok("Client " + clientId + " forced to legacy system");
    }
    
    /**
     * Убрать принудительную настройку для клиента
     */
    @DeleteMapping("/routing/force/{clientId}")
    public ResponseEntity<String> removeClientForcing(@PathVariable String clientId) {
        trafficRouter.removeForcing(clientId);
        logger.info("Removed forcing for client {}", clientId);
        return ResponseEntity.ok("Removed forcing for client " + clientId);
    }
    
    /**
     * Добавить паттерн User-Agent для принудительного использования DDD
     */
    @PostMapping("/routing/ddd-pattern")
    public ResponseEntity<String> addDddPattern(@RequestParam String pattern) {
        try {
            trafficRouter.addForceDddPattern(pattern);
            logger.info("Added DDD pattern: {}", pattern);
            return ResponseEntity.ok("Added DDD pattern: " + pattern);
        } catch (Exception e) {
            logger.error("Failed to add DDD pattern: {}", pattern, e);
            return ResponseEntity.badRequest().body("Invalid pattern: " + e.getMessage());
        }
    }
    
    /**
     * Получить активные legacy сессии
     */
    @GetMapping("/legacy/sessions")
    public ResponseEntity<Set<String>> getActiveLegacySessions() {
        Set<String> sessions = legacyAdapter.getActiveLegacySessions();
        logger.debug("Retrieved {} active legacy sessions", sessions.size());
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * Проверить, является ли сессия legacy
     */
    @GetMapping("/legacy/sessions/{sessionId}")
    public ResponseEntity<Map<String, Object>> checkLegacySession(@PathVariable String sessionId) {
        boolean isLegacy = legacyAdapter.isLegacySession(sessionId);
        
        Map<String, Object> response = Map.of(
            "sessionId", sessionId,
            "isLegacy", isLegacy,
            "timestamp", LocalDateTime.now()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Очистить неактивные legacy сессии
     */
    @PostMapping("/legacy/cleanup")
    public ResponseEntity<String> cleanupInactiveLegacySessions() {
        int beforeCount = legacyAdapter.getActiveLegacySessions().size();
        legacyAdapter.cleanupInactiveSessions();
        int afterCount = legacyAdapter.getActiveLegacySessions().size();
        
        int cleaned = beforeCount - afterCount;
        String message = "Cleaned up " + cleaned + " inactive legacy sessions";
        logger.info(message);
        
        return ResponseEntity.ok(message);
    }
    
    /**
     * Получить полную информацию о миграции
     */
    @GetMapping("/status")
    public ResponseEntity<MigrationStatus> getMigrationStatus() {
        RoutingStats routingStats = trafficRouter.getRoutingStats();
        Set<String> legacySessions = legacyAdapter.getActiveLegacySessions();
        
        MigrationStatus status = new MigrationStatus(
            routingStats,
            legacySessions.size(),
            LocalDateTime.now()
        );
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Экстренный откат к legacy системе
     */
    @PostMapping("/emergency-rollback")
    public ResponseEntity<String> emergencyRollback() {
        trafficRouter.setDddSystemEnabled(false);
        trafficRouter.updateDddTrafficPercentage(0);
        
        String message = "Emergency rollback executed - all traffic routed to legacy system";
        logger.warn(message);
        
        return ResponseEntity.ok(message);
    }
}

/**
 * Статус миграции
 */
record MigrationStatus(
    RoutingStats routingStats,
    int activeLegacySessions,
    LocalDateTime timestamp
) {} 