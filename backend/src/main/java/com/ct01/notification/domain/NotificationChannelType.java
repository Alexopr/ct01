package com.ct01.notification.domain;

/**
 * Тип канала доставки уведомления
 */
public enum NotificationChannelType {
    
    /**
     * Email уведомления
     */
    EMAIL("Email", "Уведомления по электронной почте"),
    
    /**
     * WebSocket уведомления
     */
    WEBSOCKET("WebSocket", "Real-time уведомления через WebSocket"),
    
    /**
     * Telegram уведомления
     */
    TELEGRAM("Telegram", "Уведомления через Telegram бота"),
    
    /**
     * Push уведомления
     */
    PUSH("Push", "Push уведомления на устройство"),
    
    /**
     * Webhook уведомления
     */
    WEBHOOK("Webhook", "HTTP уведомления на внешний URL");
    
    private final String displayName;
    private final String description;
    
    NotificationChannelType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Проверить, поддерживает ли канал HTML форматирование
     */
    public boolean supportsHtml() {
        return this == EMAIL || this == TELEGRAM;
    }
    
    /**
     * Проверить, является ли канал асинхронным
     */
    public boolean isAsync() {
        return this != WEBSOCKET;
    }
    
    /**
     * Проверить, требует ли канал внешнего сервиса
     */
    public boolean requiresExternalService() {
        return this == EMAIL || this == TELEGRAM || this == PUSH || this == WEBHOOK;
    }
    
    /**
     * Получить рекомендуемый таймаут в секундах
     */
    public int getTimeoutSeconds() {
        switch (this) {
            case WEBSOCKET:
                return 5;
            case PUSH:
                return 10;
            case TELEGRAM:
                return 15;
            case EMAIL:
                return 30;
            case WEBHOOK:
            default:
                return 60;
        }
    }
    
    /**
     * Получить приоритет доставки (чем больше, тем выше приоритет)
     */
    public int getDeliveryPriority() {
        switch (this) {
            case WEBSOCKET:
                return 5; // Самый высокий
            case PUSH:
                return 4;
            case TELEGRAM:
                return 3;
            case EMAIL:
                return 2;
            case WEBHOOK:
            default:
                return 1; // Самый низкий
        }
    }
    
    /**
     * Проверить, поддерживает ли канал файлы
     */
    public boolean supportsAttachments() {
        return this == EMAIL || this == TELEGRAM;
    }
} 
