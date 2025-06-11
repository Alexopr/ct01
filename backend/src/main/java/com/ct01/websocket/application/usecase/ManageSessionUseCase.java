package com.ct01.websocket.application.usecase;

import com.ct01.shared.domain.DomainEventPublisher;
import com.ct01.websocket.application.command.ConnectSessionCommand;
import com.ct01.websocket.domain.session.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Use Case для управления WebSocket сессиями
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManageSessionUseCase {
    
    private final SessionRepository sessionRepository;
    private final DomainEventPublisher eventPublisher;
    
    /**
     * Подключить новую WebSocket сессию
     */
    @Transactional
    public SessionId connectSession(ConnectSessionCommand command) {
        log.debug("Connecting WebSocket session for Spring session: {}", command.springSessionId());
        
        try {
            SessionId sessionId = SessionId.of(command.springSessionId());
            
            // Проверяем не существует ли уже сессия
            Optional<WebSocketSession> existingSession = sessionRepository.findById(sessionId);
            if (existingSession.isPresent()) {
                WebSocketSession session = existingSession.get();
                if (session.isActive()) {
                    log.warn("Session {} already exists and is active", sessionId);
                    return sessionId;
                } else {
                    // Удаляем неактивную сессию
                    sessionRepository.delete(sessionId);
                }
            }
            
            // Создаем новую сессию
            WebSocketSession session;
            if (command.isAuthenticated()) {
                session = WebSocketSession.createAuthenticated(sessionId, command.userId(), command.clientIp());
                log.info("Created authenticated WebSocket session {} for user {}", 
                        sessionId, command.userId());
            } else {
                session = WebSocketSession.createAnonymous(sessionId, command.clientIp());
                log.info("Created anonymous WebSocket session {} from IP {}", 
                        sessionId, command.clientIp());
            }
            
            // Сохраняем сессию
            sessionRepository.save(session);
            
            // Публикуем domain events
            eventPublisher.publish(session.getDomainEvents());
            session.clearDomainEvents();
            
            return sessionId;
            
        } catch (Exception e) {
            log.error("Error connecting WebSocket session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to connect WebSocket session", e);
        }
    }
    
    /**
     * Отключить WebSocket сессию
     */
    @Transactional
    public void disconnectSession(SessionId sessionId) {
        log.debug("Disconnecting WebSocket session: {}", sessionId);
        
        try {
            Optional<WebSocketSession> sessionOpt = sessionRepository.findById(sessionId);
            if (sessionOpt.isEmpty()) {
                log.warn("Session {} not found for disconnection", sessionId);
                return;
            }
            
            WebSocketSession session = sessionOpt.get();
            if (!session.isActive()) {
                log.debug("Session {} is already inactive", sessionId);
                return;
            }
            
            // Отключаем сессию
            session.disconnect();
            
            // Сохраняем изменения
            sessionRepository.save(session);
            
            // Публикуем domain events
            eventPublisher.publish(session.getDomainEvents());
            session.clearDomainEvents();
            
            log.info("Disconnected WebSocket session {}", sessionId);
            
        } catch (Exception e) {
            log.error("Error disconnecting WebSocket session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to disconnect WebSocket session", e);
        }
    }
    
    /**
     * Обновить активность сессии
     */
    @Transactional
    public void updateSessionActivity(SessionId sessionId) {
        Optional<WebSocketSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            WebSocketSession session = sessionOpt.get();
            session.updateLastActivity();
            sessionRepository.save(session);
        }
    }
    
    /**
     * Получить сессию по ID
     */
    public Optional<WebSocketSession> getSession(SessionId sessionId) {
        return sessionRepository.findById(sessionId);
    }
    
    /**
     * Проверить, активна ли сессия
     */
    public boolean isSessionActive(SessionId sessionId) {
        return sessionRepository.findById(sessionId)
                .map(WebSocketSession::isActive)
                .orElse(false);
    }
    
    /**
     * Очистить просроченные сессии
     */
    @Transactional
    public void cleanupExpiredSessions(long timeoutMinutes) {
        log.debug("Cleaning up expired sessions with timeout {} minutes", timeoutMinutes);
        
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
            List<WebSocketSession> expiredSessions = sessionRepository.findExpiredSessions(cutoffTime);
            
            int cleanedCount = 0;
            for (WebSocketSession session : expiredSessions) {
                if (session.isExpired(timeoutMinutes)) {
                    session.disconnect();
                    sessionRepository.save(session);
                    
                    // Публикуем events
                    eventPublisher.publish(session.getDomainEvents());
                    session.clearDomainEvents();
                    
                    cleanedCount++;
                }
            }
            
            if (cleanedCount > 0) {
                log.info("Cleaned up {} expired WebSocket sessions", cleanedCount);
            }
            
        } catch (Exception e) {
            log.error("Error cleaning up expired sessions: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Получить статистику сессий
     */
    public SessionRepository.SessionStatistics getSessionStatistics() {
        return sessionRepository.getStatistics();
    }
} 