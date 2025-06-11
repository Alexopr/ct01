package com.ct01.websocket.domain.message;

/**
 * Типы WebSocket сообщений
 */
public enum MessageType {
    
    // Server → Client messages
    WELCOME("welcome", "Приветственное сообщение"),
    PRICE_UPDATE("price_update", "Обновление цены"),
    SUBSCRIPTION_CONFIRMED("subscription_confirmed", "Подтверждение подписки"),
    UNSUBSCRIPTION_CONFIRMED("unsubscription_confirmed", "Подтверждение отписки"),
    PONG("pong", "Ответ на ping"),
    ERROR("error", "Сообщение об ошибке"),
    NOTIFICATION("notification", "Уведомление пользователю"),
    
    // Client → Server messages (для валидации)
    SUBSCRIBE("subscribe", "Запрос подписки"),
    UNSUBSCRIBE("unsubscribe", "Запрос отписки"),
    PING("ping", "Проверка связи");
    
    private final String value;
    private final String description;
    
    MessageType(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Получить тип сообщения по строковому значению
     */
    public static MessageType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Message type cannot be null");
        }
        
        for (MessageType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("Unknown message type: " + value);
    }
    
    /**
     * Проверить является ли тип клиентским (от клиента к серверу)
     */
    public boolean isClientMessage() {
        return this == SUBSCRIBE || this == UNSUBSCRIBE || this == PING;
    }
    
    /**
     * Проверить является ли тип серверным (от сервера к клиенту)
     */
    public boolean isServerMessage() {
        return !isClientMessage();
    }
    
    /**
     * Проверить требует ли тип ответа
     */
    public boolean requiresResponse() {
        return this == SUBSCRIBE || this == UNSUBSCRIBE || this == PING;
    }
} 