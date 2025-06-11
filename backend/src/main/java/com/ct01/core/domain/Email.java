package com.ct01.core.domain;

import java.util.regex.Pattern;

/**
 * Value Object для email адреса.
 * Обеспечивает валидацию формата email во всей системе.
 */
public record Email(String value) implements ValueObject {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    public Email {
        validate();
    }
    
    @Override
    public void validate() {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        
        String trimmedValue = value.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(trimmedValue).matches()) {
            throw new IllegalArgumentException("Неверный формат email: " + value);
        }
        
        if (trimmedValue.length() > 254) {
            throw new IllegalArgumentException("Email слишком длинный (максимум 254 символа)");
        }
    }
    
    /**
     * Создать Email из строки
     * @param value email строка
     * @return Email объект
     */
    public static Email of(String value) {
        return new Email(value);
    }
    
    /**
     * Получить нормализованный email (trimmed, lowercase)
     * @return нормализованный email
     */
    public String getNormalized() {
        return value.trim().toLowerCase();
    }
    
    /**
     * Получить доменную часть email
     * @return домен email
     */
    public String getDomain() {
        return getNormalized().substring(getNormalized().indexOf('@') + 1);
    }
    
    /**
     * Получить локальную часть email (до @)
     * @return локальная часть email
     */
    public String getLocalPart() {
        return getNormalized().substring(0, getNormalized().indexOf('@'));
    }
    
    @Override
    public String toString() {
        return "Email(" + getNormalized() + ")";
    }
} 
