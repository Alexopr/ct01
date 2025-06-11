package com.ct01.notification.domain;

import com.ct01.core.domain.ValueObject;

import java.util.Map;
import java.util.Objects;

/**
 * Канал доставки уведомления
 */
public class NotificationChannel implements ValueObject {
    
    private final NotificationChannelType type;
    private final String address;
    private final Map<String, Object> configuration;
    
    public NotificationChannel(NotificationChannelType type, String address, Map<String, Object> configuration) {
        this.type = validateType(type);
        this.address = validateAddress(type, address);
        this.configuration = configuration != null ? Map.copyOf(configuration) : Map.of();
    }
    
    public NotificationChannel(NotificationChannelType type, String address) {
        this(type, address, Map.of());
    }
    
    /**
     * Создать email канал
     */
    public static NotificationChannel email(String emailAddress) {
        return new NotificationChannel(NotificationChannelType.EMAIL, emailAddress);
    }
    
    /**
     * Создать WebSocket канал
     */
    public static NotificationChannel webSocket(String sessionId) {
        return new NotificationChannel(NotificationChannelType.WEBSOCKET, sessionId);
    }
    
    /**
     * Создать Telegram канал
     */
    public static NotificationChannel telegram(String telegramId) {
        return new NotificationChannel(NotificationChannelType.TELEGRAM, telegramId);
    }
    
    /**
     * Создать push канал
     */
    public static NotificationChannel push(String deviceToken) {
        return new NotificationChannel(NotificationChannelType.PUSH, deviceToken);
    }
    
    /**
     * Создать webhook канал
     */
    public static NotificationChannel webhook(String webhookUrl) {
        return new NotificationChannel(NotificationChannelType.WEBHOOK, webhookUrl);
    }
    
    private NotificationChannelType validateType(NotificationChannelType type) {
        if (type == null) {
            throw new IllegalArgumentException("Тип канала не может быть null");
        }
        return type;
    }
    
    private String validateAddress(NotificationChannelType type, String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Адрес канала не может быть пустым");
        }
        
        String normalized = address.trim();
        
        // Валидация в зависимости от типа канала
        switch (type) {
            case EMAIL:
                validateEmailAddress(normalized);
                break;
            case TELEGRAM:
                validateTelegramId(normalized);
                break;
            case WEBHOOK:
                validateWebhookUrl(normalized);
                break;
            case WEBSOCKET:
                validateSessionId(normalized);
                break;
            case PUSH:
                validateDeviceToken(normalized);
                break;
        }
        
        return normalized;
    }
    
    private void validateEmailAddress(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Некорректный email адрес: " + email);
        }
    }
    
    private void validateTelegramId(String telegramId) {
        try {
            Long.parseLong(telegramId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Telegram ID должен быть числом: " + telegramId);
        }
    }
    
    private void validateWebhookUrl(String url) {
        if (!url.matches("^https?://.*")) {
            throw new IllegalArgumentException("Webhook URL должен начинаться с http:// или https://");
        }
    }
    
    private void validateSessionId(String sessionId) {
        if (sessionId.length() < 10) {
            throw new IllegalArgumentException("Session ID слишком короткий");
        }
    }
    
    private void validateDeviceToken(String token) {
        if (token.length() < 20) {
            throw new IllegalArgumentException("Device token слишком короткий");
        }
    }
    
    /**
     * Проверить, требует ли канал подтверждения доставки
     */
    public boolean requiresDeliveryConfirmation() {
        return type == NotificationChannelType.EMAIL || 
               type == NotificationChannelType.TELEGRAM ||
               type == NotificationChannelType.WEBHOOK;
    }
    
    /**
     * Проверить, поддерживает ли канал rich content
     */
    public boolean supportsRichContent() {
        return type == NotificationChannelType.EMAIL || 
               type == NotificationChannelType.WEBSOCKET ||
               type == NotificationChannelType.TELEGRAM;
    }
    
    /**
     * Проверить, является ли канал real-time
     */
    public boolean isRealTime() {
        return type == NotificationChannelType.WEBSOCKET || 
               type == NotificationChannelType.PUSH;
    }
    
    /**
     * Получить максимальную длину сообщения для канала
     */
    public int getMaxMessageLength() {
        switch (type) {
            case TELEGRAM:
                return 4096;
            case PUSH:
                return 256;
            case EMAIL:
                return 10000;
            case WEBSOCKET:
            case WEBHOOK:
            default:
                return 2000;
        }
    }
    
    /**
     * Получить значение конфигурации
     */
    public Object getConfiguration(String key) {
        return configuration.get(key);
    }
    
    /**
     * Проверить наличие конфигурации
     */
    public boolean hasConfiguration(String key) {
        return configuration.containsKey(key);
    }
    
    public NotificationChannelType getType() {
        return type;
    }
    
    public String getAddress() {
        return address;
    }
    
    public Map<String, Object> getConfiguration() {
        return configuration;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotificationChannel that = (NotificationChannel) obj;
        return Objects.equals(type, that.type) &&
               Objects.equals(address, that.address) &&
               Objects.equals(configuration, that.configuration);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, address, configuration);
    }
    
    @Override
    public String toString() {
        return String.format("NotificationChannel{type=%s, address='%s'}", type, address);
    }
} 
