package com.ct01.user.domain;

import com.ct01.core.domain.ValueObject;

/**
 * Value Object для имени пользователя.
 * Обеспечивает валидацию и нормализацию username.
 */
public record Username(String value) implements ValueObject {
    
    public Username {
        validate();
    }
    
    @Override
    public void validate() {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Username не может быть пустым");
        }
        
        String trimmed = value.trim();
        
        if (trimmed.length() < 3) {
            throw new IllegalArgumentException("Username должен содержать минимум 3 символа");
        }
        
        if (trimmed.length() > 50) {
            throw new IllegalArgumentException("Username не может быть длиннее 50 символов");
        }
        
        // Проверяем что содержит только допустимые символы
        if (!trimmed.matches("^[a-zA-Z0-9_.-]+$")) {
            throw new IllegalArgumentException("Username может содержать только буквы, цифры, точки, дефисы и подчеркивания");
        }
        
        // Не может начинаться или заканчиваться специальными символами
        if (trimmed.matches("^[._-].*|.*[._-]$")) {
            throw new IllegalArgumentException("Username не может начинаться или заканчиваться специальными символами");
        }
    }
    
    /**
     * Создать Username из строки
     * @param value строка username
     * @return Username объект
     */
    public static Username of(String value) {
        return new Username(value);
    }
    
    /**
     * Получить нормализованное значение (trimmed, lowercase)
     * @return нормализованный username
     */
    public String getNormalized() {
        return value.trim().toLowerCase();
    }
    
    @Override
    public String toString() {
        return "Username(" + getNormalized() + ")";
    }
} 
