package com.ct01.websocket.domain.message;

import com.ct01.shared.domain.ValueObject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Payload WebSocket сообщения
 */
public final class MessagePayload implements ValueObject {
    
    private final Map<String, Object> data;
    
    private MessagePayload(Map<String, Object> data) {
        this.data = Map.copyOf(data != null ? data : Map.of());
    }
    
    public static MessagePayload of(Map<String, Object> data) {
        return new MessagePayload(data);
    }
    
    public static MessagePayload empty() {
        return new MessagePayload(Map.of());
    }
    
    /**
     * Создать payload для приветственного сообщения
     */
    public static MessagePayload welcome(String sessionId, String message) {
        return new MessagePayload(Map.of(
            "sessionId", sessionId,
            "message", message
        ));
    }
    
    /**
     * Создать payload для обновления цены
     */
    public static MessagePayload priceUpdate(String symbol, BigDecimal price, 
                                           BigDecimal volume, String exchange) {
        return new MessagePayload(Map.of(
            "symbol", symbol,
            "price", price,
            "volume", volume != null ? volume : BigDecimal.ZERO,
            "exchange", exchange,
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Создать payload для подтверждения подписки
     */
    public static MessagePayload subscriptionConfirmed(List<String> symbols, int totalSubscriptions) {
        return new MessagePayload(Map.of(
            "symbols", symbols,
            "totalSubscriptions", totalSubscriptions
        ));
    }
    
    /**
     * Создать payload для подтверждения отписки
     */
    public static MessagePayload unsubscriptionConfirmed(List<String> symbols, int totalSubscriptions) {
        return new MessagePayload(Map.of(
            "symbols", symbols,
            "totalSubscriptions", totalSubscriptions
        ));
    }
    
    /**
     * Создать payload для pong ответа
     */
    public static MessagePayload pong() {
        return new MessagePayload(Map.of(
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Создать payload для ошибки
     */
    public static MessagePayload error(String message) {
        return new MessagePayload(Map.of(
            "message", message,
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Создать payload для уведомления
     */
    public static MessagePayload notification(String title, String message, String category) {
        return new MessagePayload(Map.of(
            "title", title,
            "message", message,
            "category", category,
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Получить данные payload
     */
    public Map<String, Object> getData() {
        return data;
    }
    
    /**
     * Получить значение по ключу
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }
    
    /**
     * Получить строковое значение
     */
    public String getString(String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Получить число
     */
    public Number getNumber(String key) {
        Object value = data.get(key);
        return value instanceof Number ? (Number) value : null;
    }
    
    /**
     * Получить список
     */
    @SuppressWarnings("unchecked")
    public List<String> getStringList(String key) {
        Object value = data.get(key);
        return value instanceof List ? (List<String>) value : List.of();
    }
    
    /**
     * Проверить содержит ли ключ
     */
    public boolean containsKey(String key) {
        return data.containsKey(key);
    }
    
    /**
     * Проверить пустой ли payload
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessagePayload that = (MessagePayload) o;
        return Objects.equals(data, that.data);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
    
    @Override
    public String toString() {
        return "MessagePayload{data=" + data + "}";
    }
} 