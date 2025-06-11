package com.ct01.websocket.api;

import com.ct01.websocket.application.facade.WebSocketApplicationFacade;
import com.ct01.websocket.domain.session.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST API для управления и мониторинга WebSocket сессий
 * DDD версия legacy WebSocketStatsController
 */
@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
@Slf4j
public class WebSocketStatsController {
    
    private final WebSocketApplicationFacade webSocketFacade;
    
    /**
     * Получить статистику WebSocket сессий
     */
    @GetMapping("/stats")
    public ResponseEntity<SessionStatsResponse> getSessionStats() {
        log.debug("Getting WebSocket session statistics");
        
        try {
            SessionRepository.SessionStatistics stats = webSocketFacade.getSessionStatistics();
            
            SessionStatsResponse response = new SessionStatsResponse(
                    stats.totalSessions(),
                    stats.activeSessions(),
                    stats.authenticatedSessions(),
                    stats.anonymousSessions(),
                    stats.getTotalSubscriptions(),
                    stats.getUniqueSymbolCount(),
                    stats.getMostPopularSymbol().orElse(null),
                    stats.symbolSubscriptionCounts()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting WebSocket statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Получить статистику подписок на символ
     */
    @GetMapping("/stats/symbol/{symbol}")
    public ResponseEntity<SymbolStatsResponse> getSymbolStats(@PathVariable String symbol) {
        log.debug("Getting statistics for symbol: {}", symbol);
        
        try {
            long subscriptionCount = webSocketFacade.getSymbolSubscriptionCount(symbol);
            
            SymbolStatsResponse response = new SymbolStatsResponse(
                    symbol.toUpperCase(),
                    subscriptionCount
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting statistics for symbol {}: {}", symbol, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Отправить тестовое обновление цены (для тестирования)
     */
    @PostMapping("/test/broadcast")
    public ResponseEntity<String> broadcastTestMessage(@RequestBody TestBroadcastRequest request) {
        log.info("Broadcasting test price update: {} = {}", request.symbol(), request.price());
        
        try {
            webSocketFacade.broadcastPriceUpdate(
                    request.symbol(),
                    request.price(),
                    "TEST_EXCHANGE"
            );
            
            return ResponseEntity.ok("Test message broadcasted successfully");
            
        } catch (Exception e) {
            log.error("Error broadcasting test message: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error broadcasting test message: " + e.getMessage());
        }
    }
    
    /**
     * Очистить просроченные сессии
     */
    @PostMapping("/cleanup")
    public ResponseEntity<String> cleanupExpiredSessions(
            @RequestParam(defaultValue = "30") long timeoutMinutes) {
        log.info("Cleaning up expired WebSocket sessions with timeout {} minutes", timeoutMinutes);
        
        try {
            webSocketFacade.cleanupExpiredSessions(timeoutMinutes);
            return ResponseEntity.ok("Expired sessions cleanup completed");
            
        } catch (Exception e) {
            log.error("Error cleaning up expired sessions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error cleaning up expired sessions: " + e.getMessage());
        }
    }
    
    /**
     * Ответ со статистикой сессий
     */
    public record SessionStatsResponse(
            long totalSessions,
            long activeSessions,
            long authenticatedSessions,
            long anonymousSessions,
            long totalSubscriptions,
            int uniqueSymbolCount,
            String mostPopularSymbol,
            Map<String, Long> symbolSubscriptionCounts
    ) {}
    
    /**
     * Ответ со статистикой символа
     */
    public record SymbolStatsResponse(
            String symbol,
            long subscriptionCount
    ) {}
    
    /**
     * Запрос для тестовой трансляции
     */
    public record TestBroadcastRequest(
            String symbol,
            BigDecimal price
    ) {
        public TestBroadcastRequest {
            if (symbol == null || symbol.trim().isEmpty()) {
                throw new IllegalArgumentException("Symbol cannot be null or empty");
            }
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Price must be positive");
            }
        }
    }
} 