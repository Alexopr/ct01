package com.ct01.notification.domain;

import com.ct01.core.domain.ValueObject;

import java.util.Map;
import java.util.Objects;

/**
 * Содержимое уведомления
 */
public class NotificationContent implements ValueObject {
    
    private final String title;
    private final String message;
    private final NotificationCategory category;
    private final Map<String, Object> metadata;
    
    public NotificationContent(String title, String message, NotificationCategory category, Map<String, Object> metadata) {
        this.title = validateTitle(title);
        this.message = validateMessage(message);
        this.category = validateCategory(category);
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }
    
    public NotificationContent(String title, String message, NotificationCategory category) {
        this(title, message, category, Map.of());
    }
    
    private String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Заголовок уведомления не может быть пустым");
        }
        
        String normalized = title.trim();
        if (normalized.length() > 255) {
            throw new IllegalArgumentException("Заголовок уведомления не может превышать 255 символов");
        }
        
        return normalized;
    }
    
    private String validateMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Сообщение уведомления не может быть пустым");
        }
        
        String normalized = message.trim();
        if (normalized.length() > 2000) {
            throw new IllegalArgumentException("Сообщение уведомления не может превышать 2000 символов");
        }
        
        return normalized;
    }
    
    private NotificationCategory validateCategory(NotificationCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Категория уведомления не может быть null");
        }
        return category;
    }
    
    /**
     * Получить краткое содержание для отображения
     */
    public String getSummary(int maxLength) {
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Проверить, содержит ли уведомление персональные данные
     */
    public boolean containsPersonalData() {
        return category == NotificationCategory.PERSONAL || 
               metadata.containsKey("personal_data");
    }
    
    /**
     * Проверить, требуется ли срочная доставка
     */
    public boolean isUrgent() {
        return category == NotificationCategory.SECURITY || 
               category == NotificationCategory.CRITICAL_ALERT;
    }
    
    /**
     * Получить значение из метаданных
     */
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    /**
     * Проверить наличие ключа в метаданных
     */
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public NotificationCategory getCategory() {
        return category;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotificationContent that = (NotificationContent) obj;
        return Objects.equals(title, that.title) &&
               Objects.equals(message, that.message) &&
               Objects.equals(category, that.category) &&
               Objects.equals(metadata, that.metadata);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, message, category, metadata);
    }
    
    @Override
    public String toString() {
        return String.format("NotificationContent{title='%s', category=%s}", title, category);
    }
} 
