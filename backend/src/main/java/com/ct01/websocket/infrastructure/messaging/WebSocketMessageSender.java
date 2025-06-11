package com.ct01.websocket.infrastructure.messaging;

import com.ct01.websocket.domain.message.WebSocketMessage;
import com.ct01.websocket.domain.session.SessionId;

/**
 * Interface для отправки WebSocket сообщений
 */
public interface WebSocketMessageSender {
    
    /**
     * Отправить сообщение конкретной сессии
     * @param sessionId ID сессии
     * @param message сообщение для отправки
     * @return true если сообщение успешно отправлено
     */
    boolean sendMessage(SessionId sessionId, WebSocketMessage message);
    
    /**
     * Проверить доступность сессии для отправки
     * @param sessionId ID сессии
     * @return true если сессия доступна
     */
    boolean isSessionAvailable(SessionId sessionId);
    
    /**
     * Закрыть WebSocket соединение
     * @param sessionId ID сессии
     * @param reason причина закрытия
     */
    void closeSession(SessionId sessionId, String reason);
} 