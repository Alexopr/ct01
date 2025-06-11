package com.ct01.websocket.infrastructure.messaging;

import com.ct01.websocket.domain.message.WebSocketMessage;
import com.ct01.websocket.domain.session.SessionId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring WebSocket реализация MessageSender
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpringWebSocketMessageSender implements WebSocketMessageSender {
    
    private final ObjectMapper objectMapper;
    
    // Храним активные Spring WebSocket сессии
    private final Map<SessionId, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    
    /**
     * Зарегистрировать Spring WebSocket сессию
     */
    public void registerSession(SessionId sessionId, WebSocketSession webSocketSession) {
        activeSessions.put(sessionId, webSocketSession);
        log.debug("Registered WebSocket session: {}", sessionId);
    }
    
    /**
     * Отменить регистрацию Spring WebSocket сессии
     */
    public void unregisterSession(SessionId sessionId) {
        activeSessions.remove(sessionId);
        log.debug("Unregistered WebSocket session: {}", sessionId);
    }
    
    @Override
    public boolean sendMessage(SessionId sessionId, WebSocketMessage message) {
        WebSocketSession webSocketSession = activeSessions.get(sessionId);
        
        if (webSocketSession == null) {
            log.warn("WebSocket session {} not found for message sending", sessionId);
            return false;
        }
        
        if (!webSocketSession.isOpen()) {
            log.warn("WebSocket session {} is not open", sessionId);
            // Удаляем закрытую сессию
            unregisterSession(sessionId);
            return false;
        }
        
        try {
            // Конвертируем domain message в JSON
            MessageDTO messageDto = MessageDTO.fromDomain(message);
            String jsonMessage = objectMapper.writeValueAsString(messageDto);
            
            // Отправляем сообщение
            webSocketSession.sendMessage(new TextMessage(jsonMessage));
            
            log.debug("Sent message to session {}: {}", sessionId, message.getType());
            return true;
            
        } catch (JsonProcessingException e) {
            log.error("Error serializing message for session {}: {}", sessionId, e.getMessage(), e);
            return false;
        } catch (IOException e) {
            log.error("Error sending message to session {}: {}", sessionId, e.getMessage(), e);
            // Сессия может быть разорвана
            unregisterSession(sessionId);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error sending message to session {}: {}", sessionId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean isSessionAvailable(SessionId sessionId) {
        WebSocketSession webSocketSession = activeSessions.get(sessionId);
        return webSocketSession != null && webSocketSession.isOpen();
    }
    
    @Override
    public void closeSession(SessionId sessionId, String reason) {
        WebSocketSession webSocketSession = activeSessions.get(sessionId);
        
        if (webSocketSession == null) {
            log.debug("WebSocket session {} not found for closing", sessionId);
            return;
        }
        
        if (!webSocketSession.isOpen()) {
            log.debug("WebSocket session {} is already closed", sessionId);
            unregisterSession(sessionId);
            return;
        }
        
        try {
            CloseStatus closeStatus = new CloseStatus(CloseStatus.NORMAL.getCode(), reason);
            webSocketSession.close(closeStatus);
            log.info("Closed WebSocket session {}: {}", sessionId, reason);
        } catch (IOException e) {
            log.error("Error closing WebSocket session {}: {}", sessionId, e.getMessage(), e);
        } finally {
            unregisterSession(sessionId);
        }
    }
    
    /**
     * Получить количество активных сессий
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * Получить все активные SessionId
     */
    public java.util.Set<SessionId> getActiveSessionIds() {
        return activeSessions.keySet();
    }
    
    /**
     * DTO для сериализации сообщений в JSON
     */
    private static class MessageDTO {
        public String type;
        public String symbol;
        public Object payload;
        public String timestamp;
        
        public static MessageDTO fromDomain(WebSocketMessage message) {
            MessageDTO dto = new MessageDTO();
            dto.type = message.getType().getValue();
            dto.symbol = message.getSymbol();
            dto.payload = message.getPayload().getData();
            dto.timestamp = message.getTimestamp().toString();
            return dto;
        }
    }
} 