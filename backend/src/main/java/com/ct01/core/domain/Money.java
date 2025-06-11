package com.ct01.core.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Value Object для представления денежных сумм.
 * Обеспечивает точные расчеты и валидацию денежных операций.
 */
public record Money(BigDecimal amount, Currency currency) implements ValueObject {
    
    public Money {
        validate();
    }
    
    @Override
    public void validate() {
        if (amount == null) {
            throw new IllegalArgumentException("Сумма не может быть null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Валюта не может быть null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Сумма не может быть отрицательной");
        }
        
        // Проверяем максимальное количество знаков после запятой (обычно 2 для валют)
        if (amount.scale() > currency.getDefaultFractionDigits()) {
            throw new IllegalArgumentException(
                "Слишком много знаков после запятой для валюты " + currency.getCurrencyCode()
            );
        }
    }
    
    /**
     * Создать Money объект
     * @param amount сумма
     * @param currencyCode код валюты (например, "USD", "EUR")
     * @return Money объект
     */
    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }
    
    /**
     * Создать Money объект из double
     * @param amount сумма
     * @param currencyCode код валюты
     * @return Money объект
     */
    public static Money of(double amount, String currencyCode) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance(currencyCode));
    }
    
    /**
     * Создать нулевую сумму в указанной валюте
     * @param currencyCode код валюты
     * @return Money объект с нулевой суммой
     */
    public static Money zero(String currencyCode) {
        return new Money(BigDecimal.ZERO, Currency.getInstance(currencyCode));
    }
    
    /**
     * Сложить две денежные суммы
     * @param other другая сумма
     * @return результат сложения
     * @throws IllegalArgumentException если валюты не совпадают
     */
    public Money add(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Нельзя складывать суммы в разных валютах: " 
                + currency.getCurrencyCode() + " и " + other.currency.getCurrencyCode());
        }
        return new Money(amount.add(other.amount), currency);
    }
    
    /**
     * Вычесть денежную сумму
     * @param other сумма для вычитания
     * @return результат вычитания
     * @throws IllegalArgumentException если валюты не совпадают или результат отрицательный
     */
    public Money subtract(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Нельзя вычитать суммы в разных валютах: " 
                + currency.getCurrencyCode() + " и " + other.currency.getCurrencyCode());
        }
        
        BigDecimal result = amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Результат вычитания не может быть отрицательным");
        }
        
        return new Money(result, currency);
    }
    
    /**
     * Умножить на коэффициент
     * @param multiplier коэффициент
     * @return результат умножения
     */
    public Money multiply(BigDecimal multiplier) {
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Коэффициент не может быть отрицательным");
        }
        
        BigDecimal result = amount.multiply(multiplier)
            .setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
        return new Money(result, currency);
    }
    
    /**
     * Проверить, больше ли эта сумма чем другая
     * @param other другая сумма
     * @return true если эта сумма больше
     */
    public boolean isGreaterThan(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Нельзя сравнивать суммы в разных валютах");
        }
        return amount.compareTo(other.amount) > 0;
    }
    
    /**
     * Проверить, является ли сумма нулевой
     * @return true если сумма равна нулю
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Получить код валюты
     * @return код валюты
     */
    public String getCurrencyCode() {
        return currency.getCurrencyCode();
    }
    
    @Override
    public String toString() {
        return amount.toString() + " " + currency.getCurrencyCode();
    }
} 
