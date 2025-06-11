package com.ct01.websocket.domain.message;

import com.ct01.shared.domain.ValueObject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * WebSocket Message value object
 * Представляет сообщение отправляемое через WebSocket
 */
public final class WebSocketMessage implements ValueObject {
    
    private final MessageType type;
    private final String symbol; // Optional - для сообщений связанных с символом
    private final MessagePayload payload;
    private final LocalDateTime timestamp;
    
    public WebSocketMessage(MessageType type, String symbol, MessagePayload payload, LocalDateTime timestamp) {
        if (type == null) {
            throw new IllegalArgumentException("Message type cannot be null");
        }
        if (payload == null) {
            throw new IllegalArgumentException("Message payload cannot be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Message timestamp cannot be null");
        }
        
        this.type = type;
        this.symbol = symbol;
        this.payload = payload;
        this.timestamp = timestamp;
    }
    
    /**
     * Создать сообщение с текущим временем
     */
    public static WebSocketMessage create(MessageType type, String symbol, MessagePayload payload) {
        return new WebSocketMessage(type, symbol, payload, LocalDateTime.now());
    }
    
    /**
     * Создать сообщение без символа
     */
    public static WebSocketMessage create(MessageType type, MessagePayload payload) {
        return new WebSocketMessage(type, null, payload, LocalDateTime.now());
    }
    
    // Getters
    public MessageType getType() {
        return type;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public MessagePayload getPayload() {
        return payload;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Проверить является ли сообщение связанным с символом
     */
    public boolean isSymbolRelated() {
        return symbol != null && !symbol.trim().isEmpty();
    }
    
    /**
     * Получить размер сообщения в символах (для мониторинга)
     */
    public int getMessageSize() {
        return payload.toString().length();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebSocketMessage that = (WebSocketMessage) o;
        return Objects.equals(type, that.type) &&
               Objects.equals(symbol, that.symbol) &&
               Objects.equals(payload, that.payload) &&
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, symbol, payload, timestamp);
    }
    
    @Override
    public String toString() {
        return "WebSocketMessage{" +
               "type=" + type +
               ", symbol='" + symbol + '\'' +
               ", payload=" + payload +
               ", timestamp=" + timestamp +
               '}';
    }
} 