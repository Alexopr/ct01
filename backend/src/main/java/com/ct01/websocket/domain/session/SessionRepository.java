package com.ct01.websocket.domain.session;

import com.ct01.user.domain.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository interface для WebSocket сессий
 */
public interface SessionRepository {
    
    /**
     * Сохранить сессию
     */
    void save(WebSocketSession session);
    
    /**
     * Найти сессию по ID
     */
    Optional<WebSocketSession> findById(SessionId sessionId);
    
    /**
     * Найти все активные сессии
     */
    List<WebSocketSession> findActiveSessions();
    
    /**
     * Найти сессии пользователя
     */
    List<WebSocketSession> findByUserId(UserId userId);
    
    /**
     * Найти сессии подписанные на символ
     */
    List<WebSocketSession> findSubscribedToSymbol(String symbol);
    
    /**
     * Найти просроченные сессии
     */
    List<WebSocketSession> findExpiredSessions(LocalDateTime cutoffTime);
    
    /**
     * Найти сессии по IP адресу
     */
    List<WebSocketSession> findByClientIp(String clientIp);
    
    /**
     * Удалить сессию
     */
    void delete(SessionId sessionId);
    
    /**
     * Удалить просроченные сессии
     */
    void deleteExpiredSessions(LocalDateTime cutoffTime);
    
    /**
     * Получить количество активных сессий
     */
    long countActiveSessions();
    
    /**
     * Получить количество сессий по символу
     */
    long countSubscriptionsForSymbol(String symbol);
    
    /**
     * Получить статистику сессий
     */
    SessionStatistics getStatistics();
    
    /**
     * Статистика WebSocket сессий
     */
    record SessionStatistics(
        long totalSessions,
        long activeSessions,
        long authenticatedSessions,
        long anonymousSessions,
        Map<String, Long> symbolSubscriptionCounts
    ) {
        public SessionStatistics {
            if (totalSessions < 0) {
                throw new IllegalArgumentException("Total sessions cannot be negative");
            }
            if (activeSessions < 0) {
                throw new IllegalArgumentException("Active sessions cannot be negative");
            }
            if (authenticatedSessions < 0) {
                throw new IllegalArgumentException("Authenticated sessions cannot be negative");
            }
            if (anonymousSessions < 0) {
                throw new IllegalArgumentException("Anonymous sessions cannot be negative");
            }
            if (symbolSubscriptionCounts == null) {
                symbolSubscriptionCounts = Map.of();
            }
        }
        
        /**
         * Получить общее количество подписок
         */
        public long getTotalSubscriptions() {
            return symbolSubscriptionCounts.values().stream()
                    .mapToLong(Long::longValue)
                    .sum();
        }
        
        /**
         * Получить количество уникальных символов
         */
        public int getUniqueSymbolCount() {
            return symbolSubscriptionCounts.size();
        }
        
        /**
         * Получить самый популярный символ
         */
        public Optional<String> getMostPopularSymbol() {
            return symbolSubscriptionCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey);
        }
    }
} 