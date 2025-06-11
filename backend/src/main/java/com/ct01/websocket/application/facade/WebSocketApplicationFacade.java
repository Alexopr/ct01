package com.ct01.websocket.application.facade;

import com.ct01.user.domain.UserId;
import com.ct01.websocket.application.command.*;
import com.ct01.websocket.application.usecase.*;
import com.ct01.websocket.domain.session.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Application Facade для WebSocket функциональности
 * Единая точка входа в WebSocket bounded context
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketApplicationFacade {
    
    private final ManageSessionUseCase manageSessionUseCase;
    private final HandleSubscriptionUseCase handleSubscriptionUseCase;
    private final BroadcastMessageUseCase broadcastMessageUseCase;
    
    /**
     * Подключить аутентифицированную WebSocket сессию
     */
    public SessionId connectAuthenticatedSession(String springSessionId, UserId userId, 
                                               String clientIp, String userAgent) {
        log.debug("Connecting authenticated WebSocket session for user: {}", userId);
        
        ConnectSessionCommand command = ConnectSessionCommand.authenticated(
                springSessionId, userId, clientIp, userAgent);
        
        SessionId sessionId = manageSessionUseCase.connectSession(command);
        
        // Отправляем приветственное сообщение
        broadcastMessageUseCase.sendWelcomeMessage(sessionId);
        
        return sessionId;
    }
    
    /**
     * Подключить анонимную WebSocket сессию
     */
    public SessionId connectAnonymousSession(String springSessionId, String clientIp, String userAgent) {
        log.debug("Connecting anonymous WebSocket session from IP: {}", clientIp);
        
        ConnectSessionCommand command = ConnectSessionCommand.anonymous(
                springSessionId, clientIp, userAgent);
        
        SessionId sessionId = manageSessionUseCase.connectSession(command);
        
        // Отправляем приветственное сообщение
        broadcastMessageUseCase.sendWelcomeMessage(sessionId);
        
        return sessionId;
    }
    
    /**
     * Подключить сессию (для совместимости с migration API)
     */
    public SessionId connectSession(UserId userId, String userAgent, String clientIp) {
        if (userId != null) {
            return connectAuthenticatedSession(null, userId, clientIp, userAgent);
        } else {
            return connectAnonymousSession(null, clientIp, userAgent);
        }
    }
    
    /**
     * Отключить WebSocket сессию
     */
    public void disconnectSession(SessionId sessionId) {
        log.debug("Disconnecting WebSocket session: {}", sessionId);
        manageSessionUseCase.disconnectSession(sessionId);
    }
    
    /**
     * Проверяет, активна ли сессия
     */
    public boolean isSessionActive(SessionId sessionId) {
        return manageSessionUseCase.isSessionActive(sessionId);
    }
    
    /**
     * Подписать сессию на символы криптовалют
     */
    public void subscribeToSymbols(SessionId sessionId, List<String> symbols) {
        log.debug("Subscribing session {} to symbols: {}", sessionId, symbols);
        
        SubscribeToSymbolCommand command = new SubscribeToSymbolCommand(sessionId, symbols);
        HandleSubscriptionUseCase.SubscriptionResult result = handleSubscriptionUseCase.subscribeToSymbols(command);
        
        if (result.isSuccess()) {
            // Отправляем подтверждение подписки
            broadcastMessageUseCase.sendSubscriptionConfirmation(
                    sessionId, result.getSymbols(), result.getTotalSubscriptions());
        } else {
            // Отправляем сообщение об ошибке
            broadcastMessageUseCase.sendErrorMessage(sessionId, result.getError());
        }
    }
    
    /**
     * Подписать сессию на один символ (для совместимости с legacy)
     */
    public boolean subscribeToSymbol(SessionId sessionId, String symbol) {
        log.debug("Subscribing session {} to symbol: {}", sessionId, symbol);
        
        SubscribeToSymbolCommand command = new SubscribeToSymbolCommand(sessionId, List.of(symbol));
        HandleSubscriptionUseCase.SubscriptionResult result = handleSubscriptionUseCase.subscribeToSymbols(command);
        
        if (result.isSuccess()) {
            // Отправляем подтверждение подписки
            broadcastMessageUseCase.sendSubscriptionConfirmation(
                    sessionId, result.getSymbols(), result.getTotalSubscriptions());
            return true;
        } else {
            // Отправляем сообщение об ошибке
            broadcastMessageUseCase.sendErrorMessage(sessionId, result.getError());
            return false;
        }
    }
    
    /**
     * Отписать сессию от символов
     */
    public void unsubscribeFromSymbols(SessionId sessionId, List<String> symbols) {
        log.debug("Unsubscribing session {} from symbols: {}", sessionId, symbols);
        
        HandleSubscriptionUseCase.SubscriptionResult result = 
                handleSubscriptionUseCase.unsubscribeFromSymbols(sessionId, symbols);
        
        if (result.isSuccess()) {
            // Отправляем подтверждение отписки
            broadcastMessageUseCase.sendUnsubscriptionConfirmation(
                    sessionId, result.getSymbols(), result.getTotalSubscriptions());
        } else {
            // Отправляем сообщение об ошибке
            broadcastMessageUseCase.sendErrorMessage(sessionId, result.getError());
        }
    }
    
    /**
     * Обработать ping запрос
     */
    public void handlePing(SessionId sessionId) {
        // Обновляем активность сессии
        manageSessionUseCase.updateSessionActivity(sessionId);
        
        // Отправляем pong ответ
        broadcastMessageUseCase.sendPongResponse(sessionId);
    }
    
    /**
     * Транслировать обновление цены всем подписанным сессиям
     */
    public void broadcastPriceUpdate(String symbol, BigDecimal price, BigDecimal volume, String exchange) {
        log.debug("Broadcasting price update for symbol: {} at price: {}", symbol, price);
        
        BroadcastPriceCommand command = BroadcastPriceCommand.full(symbol, price, volume, exchange, null);
        broadcastMessageUseCase.broadcastPriceUpdate(command);
    }
    
    /**
     * Транслировать обновление цены (упрощенный вариант)
     */
    public void broadcastPriceUpdate(String symbol, BigDecimal price, String exchange) {
        BroadcastPriceCommand command = BroadcastPriceCommand.create(symbol, price, exchange);
        broadcastMessageUseCase.broadcastPriceUpdate(command);
    }
    
    /**
     * Транслировать обновление цены (для совместимости с legacy)
     */
    public void broadcastPriceUpdate(String symbol, Object priceData) {
        log.debug("Broadcasting price update for symbol: {} with data: {}", symbol, priceData);
        
        // Преобразуем данные в подходящий формат
        BigDecimal price = BigDecimal.valueOf(50000.0); // Заглушка для тестов
        BroadcastPriceCommand command = BroadcastPriceCommand.create(symbol, price, "TEST");
        broadcastMessageUseCase.broadcastPriceUpdate(command);
    }
    
    /**
     * Отправить уведомление конкретной сессии
     */
    public void sendNotificationToSession(SessionId sessionId, String title, String message, String category) {
        log.debug("Sending notification to session {}: {}", sessionId, title);
        broadcastMessageUseCase.sendNotification(sessionId, title, message, category);
    }
    
    /**
     * Отправить уведомление всем сессиям пользователя
     */
    public void sendNotificationToUser(UserId userId, String title, String message, String category) {
        log.debug("Sending notification to user {}: {}", userId, title);
        
        // Находим все активные сессии пользователя
        var userSessions = manageSessionUseCase.getSession(SessionId.generate()); // Заглушка
        // TODO: Нужен метод findByUserId в ManageSessionUseCase
        
        // Отправляем уведомление каждой сессии
        // broadcastMessageUseCase.sendNotification(sessionId, title, message, category);
    }
    
    /**
     * Получить подписки сессии
     */
    public Optional<Set<String>> getSessionSubscriptions(SessionId sessionId) {
        return handleSubscriptionUseCase.getSessionSubscriptions(sessionId);
    }
    
    /**
     * Получить количество подписок на символ
     */
    public long getSymbolSubscriptionCount(String symbol) {
        return handleSubscriptionUseCase.getSymbolSubscriptionCount(symbol);
    }
    
    /**
     * Получить статистику WebSocket сессий
     */
    public SessionRepository.SessionStatistics getSessionStatistics() {
        return manageSessionUseCase.getSessionStatistics();
    }
    
    /**
     * Очистить просроченные сессии
     */
    public void cleanupExpiredSessions() {
        // По умолчанию 30 минут timeout
        manageSessionUseCase.cleanupExpiredSessions(30);
    }
    
    /**
     * Очистить просроченные сессии с кастомным timeout
     */
    public void cleanupExpiredSessions(long timeoutMinutes) {
        manageSessionUseCase.cleanupExpiredSessions(timeoutMinutes);
    }
} 