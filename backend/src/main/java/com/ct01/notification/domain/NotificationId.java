package com.ct01.notification.domain;

import com.ct01.core.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

/**
 * Идентификатор уведомления
 */
public class NotificationId implements ValueObject {
    
    private final String value;
    
    private NotificationId(String value) {
        this.value = validateAndNormalize(value);
    }
    
    /**
     * Создать новый идентификатор
     */
    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID().toString());
    }
    
    /**
     * Создать идентификатор из строки
     */
    public static NotificationId of(String value) {
        return new NotificationId(value);
    }
    
    private String validateAndNormalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("NotificationId не может быть пустым");
        }
        
        String normalized = value.trim();
        
        // Проверяем формат UUID
        try {
            UUID.fromString(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("NotificationId должен быть корректным UUID", e);
        }
        
        return normalized;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotificationId that = (NotificationId) obj;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
} 
