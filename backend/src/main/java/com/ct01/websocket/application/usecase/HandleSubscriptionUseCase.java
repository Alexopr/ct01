package com.ct01.websocket.application.usecase;

import com.ct01.shared.domain.DomainEventPublisher;
import com.ct01.websocket.application.command.SubscribeToSymbolCommand;
import com.ct01.websocket.domain.session.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Use Case для управления подписками WebSocket сессий
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HandleSubscriptionUseCase {
    
    private final SessionRepository sessionRepository;
    private final DomainEventPublisher eventPublisher;
    
    /**
     * Подписать сессию на символы
     */
    @Transactional
    public SubscriptionResult subscribeToSymbols(SubscribeToSymbolCommand command) {
        log.debug("Subscribing session {} to symbols: {}", command.sessionId(), command.symbols());
        
        try {
            Optional<WebSocketSession> sessionOpt = sessionRepository.findById(command.sessionId());
            if (sessionOpt.isEmpty()) {
                log.warn("Session {} not found for subscription", command.sessionId());
                return SubscriptionResult.sessionNotFound();
            }
            
            WebSocketSession session = sessionOpt.get();
            if (!session.isActive()) {
                log.warn("Session {} is not active for subscription", command.sessionId());
                return SubscriptionResult.sessionInactive();
            }
            
            // Добавляем подписки
            List<String> normalizedSymbols = command.getNormalizedSymbols();
            for (String symbol : normalizedSymbols) {
                session.addSubscription(symbol);
            }
            
            // Сохраняем сессию
            sessionRepository.save(session);
            
            // Публикуем domain events
            eventPublisher.publish(session.getDomainEvents());
            session.clearDomainEvents();
            
            log.info("Session {} subscribed to {} symbols", command.sessionId(), normalizedSymbols.size());
            
            return SubscriptionResult.success(normalizedSymbols, session.getActiveSubscriptionCount());
            
        } catch (Exception e) {
            log.error("Error subscribing session {} to symbols: {}", command.sessionId(), e.getMessage(), e);
            throw new RuntimeException("Failed to subscribe to symbols", e);
        }
    }
    
    /**
     * Отписать сессию от символов
     */
    @Transactional
    public SubscriptionResult unsubscribeFromSymbols(SessionId sessionId, List<String> symbols) {
        log.debug("Unsubscribing session {} from symbols: {}", sessionId, symbols);
        
        try {
            Optional<WebSocketSession> sessionOpt = sessionRepository.findById(sessionId);
            if (sessionOpt.isEmpty()) {
                log.warn("Session {} not found for unsubscription", sessionId);
                return SubscriptionResult.sessionNotFound();
            }
            
            WebSocketSession session = sessionOpt.get();
            if (!session.isActive()) {
                log.warn("Session {} is not active for unsubscription", sessionId);
                return SubscriptionResult.sessionInactive();
            }
            
            // Удаляем подписки
            List<String> normalizedSymbols = symbols.stream()
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .toList();
                    
            for (String symbol : normalizedSymbols) {
                session.removeSubscription(symbol);
            }
            
            // Сохраняем сессию
            sessionRepository.save(session);
            
            // Публикуем domain events
            eventPublisher.publish(session.getDomainEvents());
            session.clearDomainEvents();
            
            log.info("Session {} unsubscribed from {} symbols", sessionId, normalizedSymbols.size());
            
            return SubscriptionResult.success(normalizedSymbols, session.getActiveSubscriptionCount());
            
        } catch (Exception e) {
            log.error("Error unsubscribing session {} from symbols: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to unsubscribe from symbols", e);
        }
    }
    
    /**
     * Получить подписки сессии
     */
    public Optional<Set<String>> getSessionSubscriptions(SessionId sessionId) {
        return sessionRepository.findById(sessionId)
                .filter(WebSocketSession::isActive)
                .map(WebSocketSession::getActiveSubscriptions);
    }
    
    /**
     * Получить сессии подписанные на символ
     */
    public List<SessionId> getSubscribedSessions(String symbol) {
        String normalizedSymbol = symbol.trim().toUpperCase();
        
        return sessionRepository.findSubscribedToSymbol(normalizedSymbol)
                .stream()
                .filter(WebSocketSession::isActive)
                .map(WebSocketSession::getId)
                .toList();
    }
    
    /**
     * Получить количество подписок на символ
     */
    public long getSymbolSubscriptionCount(String symbol) {
        String normalizedSymbol = symbol.trim().toUpperCase();
        return sessionRepository.countSubscriptionsForSymbol(normalizedSymbol);
    }
    
    /**
     * Результат операции подписки
     */
    public static class SubscriptionResult {
        private final boolean success;
        private final String error;
        private final List<String> symbols;
        private final int totalSubscriptions;
        
        private SubscriptionResult(boolean success, String error, List<String> symbols, int totalSubscriptions) {
            this.success = success;
            this.error = error;
            this.symbols = symbols;
            this.totalSubscriptions = totalSubscriptions;
        }
        
        public static SubscriptionResult success(List<String> symbols, int totalSubscriptions) {
            return new SubscriptionResult(true, null, symbols, totalSubscriptions);
        }
        
        public static SubscriptionResult sessionNotFound() {
            return new SubscriptionResult(false, "Session not found", List.of(), 0);
        }
        
        public static SubscriptionResult sessionInactive() {
            return new SubscriptionResult(false, "Session is not active", List.of(), 0);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
        public List<String> getSymbols() { return symbols; }
        public int getTotalSubscriptions() { return totalSubscriptions; }
    }
} 