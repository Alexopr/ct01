package com.ct01.websocket.infrastructure.handler;

import com.ct01.user.domain.UserId;
import com.ct01.websocket.application.facade.WebSocketApplicationFacade;
import com.ct01.websocket.domain.session.SessionId;
import com.ct01.websocket.infrastructure.messaging.SpringWebSocketMessageSender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.List;

/**
 * DDD WebSocket Handler
 * Новая архитектура для обработки WebSocket соединений
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DddWebSocketHandler implements WebSocketHandler {
    
    private final WebSocketApplicationFacade webSocketFacade;
    private final SpringWebSocketMessageSender messageSender;
    private final ObjectMapper objectMapper;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("New WebSocket connection established: {}", session.getId());
        
        try {
            // Получаем данные о сессии
            String springSessionId = session.getId();
            String clientIp = extractClientIp(session);
            String userAgent = extractUserAgent(session);
            
            // Создаем domain SessionId
            SessionId sessionId = SessionId.of(springSessionId);
            
            // Регистрируем Spring WebSocket сессию в message sender
            messageSender.registerSession(sessionId, session);
            
            // Определяем является ли сессия аутентифицированной
            // TODO: Интегрироваться с Spring Security для получения UserId
            UserId userId = extractUserId(session);
            
            if (userId != null) {
                // Подключаем аутентифицированную сессию
                webSocketFacade.connectAuthenticatedSession(springSessionId, userId, clientIp, userAgent);
                log.info("Connected authenticated WebSocket session {} for user {}", sessionId, userId);
            } else {
                // Подключаем анонимную сессию
                webSocketFacade.connectAnonymousSession(springSessionId, clientIp, userAgent);
                log.info("Connected anonymous WebSocket session {} from IP {}", sessionId, clientIp);
            }
            
        } catch (Exception e) {
            log.error("Error establishing WebSocket connection {}: {}", session.getId(), e.getMessage(), e);
            session.close(CloseStatus.SERVER_ERROR);
        }
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.debug("Received WebSocket message from session {}: {}", session.getId(), message.getPayload());
        
        try {
            SessionId sessionId = SessionId.of(session.getId());
            
            if (message instanceof TextMessage textMessage) {
                handleTextMessage(sessionId, textMessage.getPayload());
            } else if (message instanceof PongMessage) {
                // Обрабатываем pong
                webSocketFacade.handlePing(sessionId);
            } else {
                log.warn("Unsupported message type from session {}: {}", session.getId(), message.getClass());
            }
            
        } catch (Exception e) {
            log.error("Error handling message from session {}: {}", session.getId(), e.getMessage(), e);
            // Отправляем ошибку клиенту
            SessionId sessionId = SessionId.of(session.getId());
            webSocketFacade.sendNotificationToSession(sessionId, "Error", e.getMessage(), "error");
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage(), exception);
        
        try {
            SessionId sessionId = SessionId.of(session.getId());
            webSocketFacade.disconnectSession(sessionId);
            messageSender.unregisterSession(sessionId);
        } catch (Exception e) {
            log.error("Error handling transport error for session {}: {}", session.getId(), e.getMessage(), e);
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket connection closed for session {}: {}", session.getId(), closeStatus);
        
        try {
            SessionId sessionId = SessionId.of(session.getId());
            webSocketFacade.disconnectSession(sessionId);
            messageSender.unregisterSession(sessionId);
        } catch (Exception e) {
            log.error("Error handling connection close for session {}: {}", session.getId(), e.getMessage(), e);
        }
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false; // Не поддерживаем частичные сообщения
    }
    
    /**
     * Обработать текстовое сообщение
     */
    private void handleTextMessage(SessionId sessionId, String payload) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(payload);
        
        String type = jsonNode.get("type") != null ? jsonNode.get("type").asText() : "";
        
        switch (type.toLowerCase()) {
            case "subscribe" -> handleSubscribeMessage(sessionId, jsonNode);
            case "unsubscribe" -> handleUnsubscribeMessage(sessionId, jsonNode);
            case "ping" -> webSocketFacade.handlePing(sessionId);
            default -> {
                log.warn("Unknown message type from session {}: {}", sessionId, type);
                webSocketFacade.sendNotificationToSession(sessionId, "Error", 
                    "Unknown message type: " + type, "error");
            }
        }
    }
    
    /**
     * Обработать запрос подписки
     */
    private void handleSubscribeMessage(SessionId sessionId, JsonNode jsonNode) {
        try {
            JsonNode symbolsNode = jsonNode.get("symbols");
            if (symbolsNode == null || !symbolsNode.isArray()) {
                webSocketFacade.sendNotificationToSession(sessionId, "Error", 
                    "Missing or invalid 'symbols' array", "error");
                return;
            }
            
            List<String> symbols = objectMapper.convertValue(symbolsNode, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            
            webSocketFacade.subscribeToSymbols(sessionId, symbols);
            
        } catch (Exception e) {
            log.error("Error processing subscribe message for session {}: {}", sessionId, e.getMessage(), e);
            webSocketFacade.sendNotificationToSession(sessionId, "Error", 
                "Invalid subscribe request: " + e.getMessage(), "error");
        }
    }
    
    /**
     * Обработать запрос отписки
     */
    private void handleUnsubscribeMessage(SessionId sessionId, JsonNode jsonNode) {
        try {
            JsonNode symbolsNode = jsonNode.get("symbols");
            if (symbolsNode == null || !symbolsNode.isArray()) {
                webSocketFacade.sendNotificationToSession(sessionId, "Error", 
                    "Missing or invalid 'symbols' array", "error");
                return;
            }
            
            List<String> symbols = objectMapper.convertValue(symbolsNode, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            
            webSocketFacade.unsubscribeFromSymbols(sessionId, symbols);
            
        } catch (Exception e) {
            log.error("Error processing unsubscribe message for session {}: {}", sessionId, e.getMessage(), e);
            webSocketFacade.sendNotificationToSession(sessionId, "Error", 
                "Invalid unsubscribe request: " + e.getMessage(), "error");
        }
    }
    
    /**
     * Извлечь IP адрес клиента
     */
    private String extractClientIp(WebSocketSession session) {
        // Сначала проверяем заголовки прокси
        String xForwardedFor = (String) session.getAttributes().get("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = (String) session.getAttributes().get("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Возвращаем remote address
        return session.getRemoteAddress() != null ? 
            session.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
    
    /**
     * Извлечь User-Agent
     */
    private String extractUserAgent(WebSocketSession session) {
        return (String) session.getAttributes().getOrDefault("User-Agent", "unknown");
    }
    
    /**
     * Извлечь UserId из Spring Security Principal
     */
    private UserId extractUserId(WebSocketSession session) {
        // TODO: Интегрироваться с Spring Security
        // Principal principal = session.getPrincipal();
        // if (principal instanceof UserPrincipal userPrincipal) {
        //     return userPrincipal.getUserId();
        // }
        return null; // Пока возвращаем null для анонимных сессий
    }
} 