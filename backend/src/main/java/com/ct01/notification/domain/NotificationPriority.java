package com.ct01.notification.domain;

/**
 * Приоритет уведомления
 */
public enum NotificationPriority {
    
    /**
     * Низкий приоритет - обычные уведомления
     */
    LOW(1, "Низкий"),
    
    /**
     * Обычный приоритет - стандартные уведомления
     */
    NORMAL(2, "Обычный"),
    
    /**
     * Высокий приоритет - важные уведомления
     */
    HIGH(3, "Высокий"),
    
    /**
     * Срочный приоритет - критичные уведомления
     */
    URGENT(4, "Срочный");
    
    private final int level;
    private final String description;
    
    NotificationPriority(int level, String description) {
        this.level = level;
        this.description = description;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Проверить, выше ли приоритет чем указанный
     */
    public boolean isHigherThan(NotificationPriority other) {
        return this.level > other.level;
    }
    
    /**
     * Проверить, является ли приоритет критичным
     */
    public boolean isCritical() {
        return this == HIGH || this == URGENT;
    }
    
    /**
     * Получить максимальное время ожидания отправки в минутах
     */
    public long getMaxWaitTimeMinutes() {
        switch (this) {
            case URGENT:
                return 1; // 1 минута
            case HIGH:
                return 5; // 5 минут
            case NORMAL:
                return 30; // 30 минут
            case LOW:
            default:
                return 120; // 2 часа
        }
    }
    
    /**
     * Получить интервал повторных попыток в минутах
     */
    public long getRetryIntervalMinutes() {
        switch (this) {
            case URGENT:
                return 1; // Каждую минуту
            case HIGH:
                return 2; // Каждые 2 минуты
            case NORMAL:
                return 5; // Каждые 5 минут
            case LOW:
            default:
                return 15; // Каждые 15 минут
        }
    }
    
    /**
     * Получить максимальное количество попыток
     */
    public int getMaxRetries() {
        switch (this) {
            case URGENT:
                return 5;
            case HIGH:
                return 4;
            case NORMAL:
                return 3;
            case LOW:
            default:
                return 2;
        }
    }
}
