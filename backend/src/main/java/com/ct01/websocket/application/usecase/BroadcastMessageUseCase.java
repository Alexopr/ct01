package com.ct01.websocket.application.usecase;

import com.ct01.websocket.application.command.BroadcastPriceCommand;
import com.ct01.websocket.domain.message.*;
import com.ct01.websocket.domain.session.*;
import com.ct01.websocket.infrastructure.messaging.WebSocketMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Use Case для трансляции сообщений через WebSocket
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BroadcastMessageUseCase {
    
    private final SessionRepository sessionRepository;
    private final WebSocketMessageSender messageSender;
    
    /**
     * Транслировать обновление цены
     */
    public void broadcastPriceUpdate(BroadcastPriceCommand command) {
        log.debug("Broadcasting price update for symbol: {}", command.symbol());
        
        try {
            String normalizedSymbol = command.getNormalizedSymbol();
            
            // Найти сессии подписанные на символ
            List<WebSocketSession> subscribedSessions = sessionRepository.findSubscribedToSymbol(normalizedSymbol)
                    .stream()
                    .filter(WebSocketSession::isActive)
                    .toList();
            
            if (subscribedSessions.isEmpty()) {
                log.debug("No active sessions subscribed to symbol: {}", normalizedSymbol);
                return;
            }
            
            // Создать сообщение
            MessagePayload payload = MessagePayload.priceUpdate(
                    normalizedSymbol,
                    command.price(),
                    command.getSafeVolume(),
                    command.exchange()
            );
            
            WebSocketMessage message = new WebSocketMessage(
                    MessageType.PRICE_UPDATE,
                    normalizedSymbol,
                    payload,
                    LocalDateTime.now()
            );
            
            // Отправить сообщение всем подписанным сессиям
            int sentCount = 0;
            for (WebSocketSession session : subscribedSessions) {
                if (session.isSubscribedTo(normalizedSymbol)) {
                    boolean sent = messageSender.sendMessage(session.getId(), message);
                    if (sent) {
                        sentCount++;
                    }
                }
            }
            
            log.debug("Broadcasted price update for {} to {} sessions", normalizedSymbol, sentCount);
            
        } catch (Exception e) {
            log.error("Error broadcasting price update for symbol {}: {}", command.symbol(), e.getMessage(), e);
        }
    }
    
    /**
     * Отправить приветственное сообщение
     */
    public void sendWelcomeMessage(SessionId sessionId) {
        log.debug("Sending welcome message to session: {}", sessionId);
        
        try {
            MessagePayload payload = MessagePayload.welcome(
                    sessionId.getValue(),
                    "Connected to price updates service"
            );
            
            WebSocketMessage message = new WebSocketMessage(
                    MessageType.WELCOME,
                    null,
                    payload,
                    LocalDateTime.now()
            );
            
            messageSender.sendMessage(sessionId, message);
            
        } catch (Exception e) {
            log.error("Error sending welcome message to session {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    /**
     * Отправить подтверждение подписки
     */
    public void sendSubscriptionConfirmation(SessionId sessionId, List<String> symbols, int totalSubscriptions) {
        log.debug("Sending subscription confirmation to session: {}", sessionId);
        
        try {
            MessagePayload payload = MessagePayload.subscriptionConfirmed(symbols, totalSubscriptions);
            
            WebSocketMessage message = new WebSocketMessage(
                    MessageType.SUBSCRIPTION_CONFIRMED,
                    null,
                    payload,
                    LocalDateTime.now()
            );
            
            messageSender.sendMessage(sessionId, message);
            
        } catch (Exception e) {
            log.error("Error sending subscription confirmation to session {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    /**
     * Отправить подтверждение отписки
     */
    public void sendUnsubscriptionConfirmation(SessionId sessionId, List<String> symbols, int totalSubscriptions) {
        log.debug("Sending unsubscription confirmation to session: {}", sessionId);
        
        try {
            MessagePayload payload = MessagePayload.unsubscriptionConfirmed(symbols, totalSubscriptions);
            
            WebSocketMessage message = new WebSocketMessage(
                    MessageType.UNSUBSCRIPTION_CONFIRMED,
                    null,
                    payload,
                    LocalDateTime.now()
            );
            
            messageSender.sendMessage(sessionId, message);
            
        } catch (Exception e) {
            log.error("Error sending unsubscription confirmation to session {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    /**
     * Отправить pong ответ
     */
    public void sendPongResponse(SessionId sessionId) {
        try {
            MessagePayload payload = MessagePayload.pong();
            
            WebSocketMessage message = new WebSocketMessage(
                    MessageType.PONG,
                    null,
                    payload,
                    LocalDateTime.now()
            );
            
            messageSender.sendMessage(sessionId, message);
            
        } catch (Exception e) {
            log.error("Error sending pong response to session {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    /**
     * Отправить сообщение об ошибке
     */
    public void sendErrorMessage(SessionId sessionId, String errorMessage) {
        log.debug("Sending error message to session {}: {}", sessionId, errorMessage);
        
        try {
            MessagePayload payload = MessagePayload.error(errorMessage);
            
            WebSocketMessage message = new WebSocketMessage(
                    MessageType.ERROR,
                    null,
                    payload,
                    LocalDateTime.now()
            );
            
            messageSender.sendMessage(sessionId, message);
            
        } catch (Exception e) {
            log.error("Error sending error message to session {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    /**
     * Отправить уведомление
     */
    public void sendNotification(SessionId sessionId, String title, String message, String category) {
        log.debug("Sending notification to session {}: {}", sessionId, title);
        
        try {
            MessagePayload payload = MessagePayload.notification(title, message, category);
            
            WebSocketMessage wsMessage = new WebSocketMessage(
                    MessageType.NOTIFICATION,
                    null,
                    payload,
                    LocalDateTime.now()
            );
            
            messageSender.sendMessage(sessionId, wsMessage);
            
        } catch (Exception e) {
            log.error("Error sending notification to session {}: {}", sessionId, e.getMessage(), e);
        }
    }
} 