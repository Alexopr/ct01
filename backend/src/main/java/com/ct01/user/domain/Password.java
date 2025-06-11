package com.ct01.user.domain;

import com.ct01.core.domain.ValueObject;

/**
 * Value Object для пароля пользователя.
 * Обеспечивает валидацию сложности пароля.
 */
public record Password(String hashedValue) implements ValueObject {
    
    public Password {
        validate();
    }
    
    @Override
    public void validate() {
        if (hashedValue == null || hashedValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Хэш пароля не может быть пустым");
        }
        
        // Проверяем что это действительно хэш (минимальная длина)
        if (hashedValue.length() < 60) {
            throw new IllegalArgumentException("Неверный формат хэша пароля");
        }
    }
    
    /**
     * Создать Password из уже захэшированного значения
     * @param hashedValue захэшированный пароль
     * @return Password объект
     */
    public static Password fromHash(String hashedValue) {
        return new Password(hashedValue);
    }
    
    /**
     * Создать Password из сырого пароля с валидацией
     * @param rawPassword сырой пароль
     * @return Password объект (нужно будет захэшировать отдельно)
     */
    public static void validateRawPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        
        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 8 символов");
        }
        
        if (rawPassword.length() > 128) {
            throw new IllegalArgumentException("Пароль не может быть длиннее 128 символов");
        }
        
        // Проверяем сложность пароля
        boolean hasUpper = rawPassword.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = rawPassword.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = rawPassword.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = rawPassword.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        int complexityScore = 0;
        if (hasUpper) complexityScore++;
        if (hasLower) complexityScore++;
        if (hasDigit) complexityScore++;
        if (hasSpecial) complexityScore++;
        
        if (complexityScore < 3) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 3 из 4 типов символов: " +
                "заглавные буквы, строчные буквы, цифры, специальные символы");
        }
    }
    
    /**
     * Получить хэш пароля
     * @return хэшированное значение
     */
    public String getHashedValue() {
        return hashedValue;
    }
    
    @Override
    public String toString() {
        return "Password(****)"; // Никогда не показываем реальный хэш
    }
} 
