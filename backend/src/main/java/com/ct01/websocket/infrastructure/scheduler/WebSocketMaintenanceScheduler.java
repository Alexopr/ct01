package com.ct01.websocket.infrastructure.scheduler;

import com.ct01.websocket.application.facade.WebSocketApplicationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler для обслуживания WebSocket сессий
 * Выполняет регулярные задачи по очистке и мониторингу
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketMaintenanceScheduler {
    
    private final WebSocketApplicationFacade webSocketFacade;
    
    /**
     * Очистка просроченных сессий каждые 5 минут
     */
    @Scheduled(fixedRate = 300000) // 5 минут
    public void cleanupExpiredSessions() {
        log.debug("Starting scheduled cleanup of expired WebSocket sessions");
        
        try {
            // Очищаем сессии которые неактивны более 30 минут
            webSocketFacade.cleanupExpiredSessions(30);
            
        } catch (Exception e) {
            log.error("Error during scheduled session cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Логирование статистики каждые 10 минут
     */
    @Scheduled(fixedRate = 600000) // 10 минут
    public void logStatistics() {
        try {
            var stats = webSocketFacade.getSessionStatistics();
            
            log.info("WebSocket Statistics - Active Sessions: {}, Total Subscriptions: {}, Symbols: {}", 
                    stats.activeSessions(), 
                    stats.getTotalSubscriptions(),
                    stats.getUniqueSymbolCount());
            
            if (stats.getMostPopularSymbol().isPresent()) {
                String popularSymbol = stats.getMostPopularSymbol().get();
                long subscriptions = stats.symbolSubscriptionCounts().get(popularSymbol);
                log.info("Most popular symbol: {} with {} subscriptions", popularSymbol, subscriptions);
            }
            
        } catch (Exception e) {
            log.error("Error logging WebSocket statistics: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Здоровье системы каждый час
     */
    @Scheduled(fixedRate = 3600000) // 1 час
    public void healthCheck() {
        log.info("Performing WebSocket system health check");
        
        try {
            var stats = webSocketFacade.getSessionStatistics();
            
            // Проверяем аномалии
            if (stats.activeSessions() > 1000) {
                log.warn("High number of active WebSocket sessions: {}", stats.activeSessions());
            }
            
            if (stats.getTotalSubscriptions() > 10000) {
                log.warn("High number of total subscriptions: {}", stats.getTotalSubscriptions());
            }
            
            // Проверяем соотношения
            if (stats.activeSessions() > 0) {
                double avgSubscriptionsPerSession = (double) stats.getTotalSubscriptions() / stats.activeSessions();
                if (avgSubscriptionsPerSession > 50) {
                    log.warn("High average subscriptions per session: {}", avgSubscriptionsPerSession);
                }
            }
            
            log.info("WebSocket health check completed successfully");
            
        } catch (Exception e) {
            log.error("Error during WebSocket health check: {}", e.getMessage(), e);
        }
    }
} 