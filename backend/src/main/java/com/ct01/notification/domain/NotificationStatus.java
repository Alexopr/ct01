package com.ct01.notification.domain;

/**
 * Статус уведомления
 */
public enum NotificationStatus {
    
    /**
     * Уведомление создано и ожидает отправки
     */
    PENDING("Ожидает отправки"),
    
    /**
     * Уведомление отправлено поставщику услуг
     */
    SENT("Отправлено"),
    
    /**
     * Уведомление доставлено получателю
     */
    DELIVERED("Доставлено"),
    
    /**
     * Уведомление прочитано получателем
     */
    READ("Прочитано"),
    
    /**
     * Не удалось отправить уведомление
     */
    FAILED("Неудача"),
    
    /**
     * Уведомление истекло и не будет отправлено
     */
    EXPIRED("Истекло");
    
    private final String description;
    
    NotificationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Проверить, является ли статус финальным (не может изменяться)
     */
    public boolean isFinal() {
        return this == READ || this == FAILED || this == EXPIRED;
    }
    
    /**
     * Проверить, успешен ли статус
     */
    public boolean isSuccessful() {
        return this == DELIVERED || this == READ;
    }
    
    /**
     * Проверить, можно ли повторить отправку
     */
    public boolean canRetry() {
        return this == FAILED;
    }
} 
