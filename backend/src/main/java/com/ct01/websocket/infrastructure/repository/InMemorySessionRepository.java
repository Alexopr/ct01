package com.ct01.websocket.infrastructure.repository;

import com.ct01.user.domain.UserId;
import com.ct01.websocket.domain.session.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory реализация SessionRepository
 * Для production использовать Redis или другое подходящее хранилище
 */
@Repository
@Slf4j
public class InMemorySessionRepository implements SessionRepository {
    
    private final Map<SessionId, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<UserId, Set<SessionId>> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Set<SessionId>> symbolSubscriptions = new ConcurrentHashMap<>();
    
    @Override
    public void save(WebSocketSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }
        
        SessionId sessionId = session.getId();
        
        // Удаляем старые индексы если сессия существует
        if (sessions.containsKey(sessionId)) {
            removeFromIndexes(sessionId);
        }
        
        // Сохраняем сессию
        sessions.put(sessionId, session);
        
        // Обновляем индексы
        updateIndexes(session);
        
        log.debug("Saved session {}", sessionId);
    }
    
    @Override
    public Optional<WebSocketSession> findById(SessionId sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
    
    @Override
    public List<WebSocketSession> findActiveSessions() {
        return sessions.values().stream()
                .filter(WebSocketSession::isActive)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<WebSocketSession> findByUserId(UserId userId) {
        Set<SessionId> userSessionIds = userSessions.getOrDefault(userId, Set.of());
        
        return userSessionIds.stream()
                .map(sessions::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<WebSocketSession> findSubscribedToSymbol(String symbol) {
        Set<SessionId> subscribedSessionIds = symbolSubscriptions.getOrDefault(symbol, Set.of());
        
        return subscribedSessionIds.stream()
                .map(sessions::get)
                .filter(Objects::nonNull)
                .filter(session -> session.isSubscribedTo(symbol))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<WebSocketSession> findExpiredSessions(LocalDateTime cutoffTime) {
        return sessions.values().stream()
                .filter(session -> session.getLastActivity().isBefore(cutoffTime))
                .collect(Collectors.toList());
    }
    
    @Override
    public long countActiveSessions() {
        return sessions.values().stream()
                .filter(WebSocketSession::isActive)
                .count();
    }
    
    @Override
    public long countAuthenticatedSessions() {
        return sessions.values().stream()
                .filter(WebSocketSession::isActive)
                .filter(WebSocketSession::isAuthenticated)
                .count();
    }
    
    @Override
    public long countAnonymousSessions() {
        return sessions.values().stream()
                .filter(WebSocketSession::isActive)
                .filter(session -> !session.isAuthenticated())
                .count();
    }
    
    @Override
    public long countSubscriptionsForSymbol(String symbol) {
        return symbolSubscriptions.getOrDefault(symbol, Set.of()).size();
    }
    
    @Override
    public void delete(SessionId sessionId) {
        removeFromIndexes(sessionId);
        sessions.remove(sessionId);
        log.debug("Deleted session {}", sessionId);
    }
    
    @Override
    public void deleteAll() {
        sessions.clear();
        userSessions.clear();
        symbolSubscriptions.clear();
        log.info("Cleared all sessions from repository");
    }
    
    @Override
    public SessionStatistics getStatistics() {
        long totalSessions = sessions.size();
        long activeSessions = countActiveSessions();
        long authenticatedSessions = countAuthenticatedSessions();
        long anonymousSessions = countAnonymousSessions();
        
        Map<String, Long> symbolCounts = symbolSubscriptions.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (long) entry.getValue().size()
                ));
        
        return new SessionStatistics(
                totalSessions,
                activeSessions,
                authenticatedSessions,
                anonymousSessions,
                symbolCounts
        );
    }
    
    /**
     * Обновить индексы для сессии
     */
    private void updateIndexes(WebSocketSession session) {
        SessionId sessionId = session.getId();
        
        // Индекс по пользователю
        if (session.isAuthenticated()) {
            UserId userId = session.getUserId();
            userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        }
        
        // Индекс по подпискам
        for (String symbol : session.getActiveSubscriptions()) {
            symbolSubscriptions.computeIfAbsent(symbol, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        }
    }
    
    /**
     * Удалить сессию из всех индексов
     */
    private void removeFromIndexes(SessionId sessionId) {
        WebSocketSession existingSession = sessions.get(sessionId);
        if (existingSession == null) {
            return;
        }
        
        // Удаляем из индекса пользователей
        if (existingSession.isAuthenticated()) {
            UserId userId = existingSession.getUserId();
            Set<SessionId> userSessionIds = userSessions.get(userId);
            if (userSessionIds != null) {
                userSessionIds.remove(sessionId);
                if (userSessionIds.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }
        
        // Удаляем из индекса подписок
        for (String symbol : existingSession.getActiveSubscriptions()) {
            Set<SessionId> subscribedSessionIds = symbolSubscriptions.get(symbol);
            if (subscribedSessionIds != null) {
                subscribedSessionIds.remove(sessionId);
                if (subscribedSessionIds.isEmpty()) {
                    symbolSubscriptions.remove(symbol);
                }
            }
        }
    }
    
    /**
     * Получить размер хранилища (для debugging)
     */
    public int size() {
        return sessions.size();
    }
    
    /**
     * Получить количество символов с подписками (для debugging)
     */
    public int getSymbolCount() {
        return symbolSubscriptions.size();
    }
    
    /**
     * Получить количество пользователей с сессиями (для debugging)
     */
    public int getUserCount() {
        return userSessions.size();
    }
} 