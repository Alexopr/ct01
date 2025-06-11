package com.ct01.admin.domain;

import com.ct01.core.domain.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;
import java.util.UUID;

/**
 * AdminId - Value Object для идентификации административных операций
 * 
 * Представляет уникальный идентификатор административной операции
 */
@Getter
@EqualsAndHashCode
@ToString
public class AdminId implements ValueObject {
    
    private final String value;
    
    private AdminId(String value) {
        this.value = Objects.requireNonNull(value, "Admin ID value cannot be null");
        validate();
    }
    
    /**
     * Создать новый AdminId с сгенерированным UUID
     */
    public static AdminId generate() {
        return new AdminId(UUID.randomUUID().toString());
    }
    
    /**
     * Создать AdminId из строки
     */
    public static AdminId from(String value) {
        return new AdminId(value);
    }
    
    /**
     * Создать AdminId из UUID
     */
    public static AdminId from(UUID uuid) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        return new AdminId(uuid.toString());
    }
    
    /**
     * Получить как UUID
     */
    public UUID asUUID() {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new AdminException("Invalid UUID format for AdminId: " + value);
        }
    }
    
    /**
     * Проверить является ли значение валидным UUID
     */
    public boolean isValidUUID() {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Получить короткую версию идентификатора (первые 8 символов)
     */
    public String getShortValue() {
        return value.length() > 8 ? value.substring(0, 8) : value;
    }
    
    private void validate() {
        if (value.trim().isEmpty()) {
            throw new AdminException("Admin ID cannot be empty");
        }
        
        if (value.length() > 255) {
            throw new AdminException("Admin ID is too long (max 255 characters)");
        }
        
        // Проверяем что это валидный UUID если строка имеет соответствующий формат
        if (value.length() == 36 && value.contains("-")) {
            if (!isValidUUID()) {
                throw new AdminException("Invalid UUID format for AdminId: " + value);
            }
        }
    }
} 
