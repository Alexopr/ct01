package com.ct01.websocket.infrastructure.migration;

import com.ct01.websocket.application.facade.WebSocketApplicationFacade;
import com.ct01.websocket.domain.message.MessageType;
import com.ct01.websocket.domain.session.SessionId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter для интеграции между legacy WebSocket системой и новой DDD архитектурой
 * Перехватывает сообщения из старой системы и транслирует их в новую
 */
@Component
public class LegacyWebSocketAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(LegacyWebSocketAdapter.class);
    
    private final WebSocketApplicationFacade webSocketFacade;
    private final MessageTranslator messageTranslator;
    private final ObjectMapper objectMapper;
    
    // Mapping между legacy session ID и новыми session ID
    private final Map<String, SessionId> sessionMapping = new ConcurrentHashMap<>();
    
    public LegacyWebSocketAdapter(
            WebSocketApplicationFacade webSocketFacade,
            MessageTranslator messageTranslator,
            ObjectMapper objectMapper) {
        this.webSocketFacade = webSocketFacade;
        this.messageTranslator = messageTranslator;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Перехватывает подключение от legacy клиента
     */
    public void handleLegacyConnection(String legacySessionId, String userAgent, String clientIp) {
        try {
            logger.info("Handling legacy connection: {}", legacySessionId);
            
            // Создаем новую сессию в DDD системе
            SessionId newSessionId = webSocketFacade.connectSession(null, userAgent, clientIp);
            
            // Сохраняем mapping
            sessionMapping.put(legacySessionId, newSessionId);
            
            logger.debug("Mapped legacy session {} to new session {}", legacySessionId, newSessionId.value());
            
        } catch (Exception e) {
            logger.error("Failed to handle legacy connection: {}", legacySessionId, e);
        }
    }
    
    /**
     * Перехватывает сообщения от legacy клиентов
     */
    public void handleLegacyMessage(String legacySessionId, String messageJson) {
        try {
            SessionId sessionId = sessionMapping.get(legacySessionId);
            if (sessionId == null) {
                logger.warn("No session mapping found for legacy session: {}", legacySessionId);
                return;
            }
            
            // Парсим legacy сообщение
            JsonNode messageNode = objectMapper.readTree(messageJson);
            
            // Транслируем в новый формат
            var translatedMessage = messageTranslator.translateFromLegacy(messageNode);
            
            if (translatedMessage != null) {
                // Обрабатываем в новой системе
                handleTranslatedMessage(sessionId, translatedMessage);
            }
            
        } catch (Exception e) {
            logger.error("Failed to handle legacy message from session: {}", legacySessionId, e);
        }
    }
    
    /**
     * Перехватывает отключение legacy клиента
     */
    public void handleLegacyDisconnection(String legacySessionId) {
        try {
            SessionId sessionId = sessionMapping.remove(legacySessionId);
            if (sessionId != null) {
                webSocketFacade.disconnectSession(sessionId);
                logger.info("Handled legacy disconnection: {} -> {}", legacySessionId, sessionId.value());
            }
        } catch (Exception e) {
            logger.error("Failed to handle legacy disconnection: {}", legacySessionId, e);
        }
    }
    
    /**
     * Транслирует сообщения из новой системы обратно в legacy формат
     */
    public String translateToLegacyFormat(SessionId sessionId, MessageType messageType, Object payload) {
        try {
            return messageTranslator.translateToLegacy(messageType, payload);
        } catch (Exception e) {
            logger.error("Failed to translate message to legacy format", e);
            return null;
        }
    }
    
    /**
     * Возвращает все активные legacy сессии
     */
    public Set<String> getActiveLegacySessions() {
        return sessionMapping.keySet();
    }
    
    /**
     * Проверяет, является ли сессия legacy
     */
    public boolean isLegacySession(String sessionId) {
        return sessionMapping.containsKey(sessionId);
    }
    
    private void handleTranslatedMessage(SessionId sessionId, TranslatedMessage message) {
        switch (message.getType()) {
            case SUBSCRIBE:
                String symbol = (String) message.getPayload();
                webSocketFacade.subscribeToSymbol(sessionId, symbol);
                break;
                
            case UNSUBSCRIBE:
                // Implement unsubscribe logic
                break;
                
            default:
                logger.debug("Unhandled message type: {}", message.getType());
        }
    }
    
    /**
     * Очистка неактивных сессий
     */
    public void cleanupInactiveSessions() {
        sessionMapping.entrySet().removeIf(entry -> {
            SessionId sessionId = entry.getValue();
            // Проверяем, активна ли сессия в новой системе
            return !webSocketFacade.isSessionActive(sessionId);
        });
    }
} 