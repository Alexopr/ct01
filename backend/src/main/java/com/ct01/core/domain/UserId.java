package com.ct01.core.domain;

/**
 * Value Object для идентификатора пользователя.
 * Обеспечивает типобезопасность и валидацию ID пользователей во всей системе.
 */
public record UserId(Long value) implements ValueObject {
    
    public UserId {
        validate();
    }
    
    @Override
    public void validate() {
        if (value == null) {
            throw new IllegalArgumentException("UserId не может быть null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("UserId должен быть положительным числом");
        }
    }
    
    /**
     * Создать UserId из Long значения
     * @param value значение ID
     * @return UserId объект
     */
    public static UserId of(Long value) {
        return new UserId(value);
    }
    
    /**
     * Получить примитивное значение ID
     * @return Long значение
     */
    public Long getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return "UserId(" + value + ")";
    }
} 
