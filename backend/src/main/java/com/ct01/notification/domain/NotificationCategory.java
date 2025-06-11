package com.ct01.notification.domain;

/**
 * Категория уведомления
 */
public enum NotificationCategory {
    
    /**
     * Системные уведомления
     */
    SYSTEM("Системные", "Уведомления о работе системы"),
    
    /**
     * Безопасность и аутентификация
     */
    SECURITY("Безопасность", "Уведомления о безопасности и входе в систему"),
    
    /**
     * Персональные уведомления пользователя
     */
    PERSONAL("Персональные", "Личные уведомления пользователя"),
    
    /**
     * Ценовые алерты
     */
    PRICE_ALERT("Ценовые алерты", "Уведомления об изменении цен"),
    
    /**
     * Объемные алерты
     */
    VOLUME_ALERT("Объемные алерты", "Уведомления об изменении объемов торгов"),
    
    /**
     * Критичные алерты
     */
    CRITICAL_ALERT("Критичные алерты", "Срочные уведомления требующие внимания"),
    
    /**
     * Подписки и платежи
     */
    SUBSCRIPTION("Подписки", "Уведомления о подписках и платежах"),
    
    /**
     * Маркетинговые уведомления
     */
    MARKETING("Маркетинг", "Рекламные и информационные сообщения"),
    
    /**
     * Telegram события
     */
    TELEGRAM("Telegram", "Уведомления из Telegram каналов"),
    
    /**
     * WebSocket события
     */
    WEBSOCKET("WebSocket", "Real-time уведомления через WebSocket");
    
    private final String displayName;
    private final String description;
    
    NotificationCategory(String displayName, String description) {
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
     * Проверить, требует ли категория высокого приоритета по умолчанию
     */
    public boolean requiresHighPriority() {
        return this == SECURITY || this == CRITICAL_ALERT;
    }
    
    /**
     * Проверить, может ли категория быть отключена пользователем
     */
    public boolean canBeDisabled() {
        return this != SECURITY && this != CRITICAL_ALERT && this != SYSTEM;
    }
    
    /**
     * Проверить, требует ли категория персонализации
     */
    public boolean requiresPersonalization() {
        return this == PRICE_ALERT || this == VOLUME_ALERT || this == PERSONAL;
    }
    
    /**
     * Получить рекомендуемые каналы для категории
     */
    public NotificationChannelType[] getRecommendedChannels() {
        switch (this) {
            case SECURITY:
            case CRITICAL_ALERT:
                return new NotificationChannelType[]{
                    NotificationChannelType.EMAIL, 
                    NotificationChannelType.TELEGRAM,
                    NotificationChannelType.PUSH
                };
            case WEBSOCKET:
                return new NotificationChannelType[]{NotificationChannelType.WEBSOCKET};
            case PRICE_ALERT:
            case VOLUME_ALERT:
                return new NotificationChannelType[]{
                    NotificationChannelType.WEBSOCKET,
                    NotificationChannelType.TELEGRAM,
                    NotificationChannelType.PUSH
                };
            case MARKETING:
                return new NotificationChannelType[]{NotificationChannelType.EMAIL};
            default:
                return new NotificationChannelType[]{
                    NotificationChannelType.EMAIL,
                    NotificationChannelType.WEBSOCKET
                };
        }
    }
    
    /**
     * Получить время жизни уведомления по умолчанию в часах
     */
    public int getDefaultTtlHours() {
        switch (this) {
            case WEBSOCKET:
                return 1; // 1 час для WebSocket
            case PRICE_ALERT:
            case VOLUME_ALERT:
                return 24; // 1 день для алертов
            case CRITICAL_ALERT:
            case SECURITY:
                return 72; // 3 дня для критичных
            case MARKETING:
                return 168; // 1 неделя для маркетинга
            default:
                return 48; // 2 дня по умолчанию
        }
    }
} 
