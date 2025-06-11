package com.ct01.market.domain;

import com.ct01.core.domain.ValueObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object для цены
 */
public class Price implements ValueObject {
    
    private final BigDecimal value;
    private final String currency;
    
    private Price(BigDecimal value, String currency) {
        this.value = validateAndNormalizeValue(value);
        this.currency = validateCurrency(currency);
    }
    
    /**
     * Создать цену
     */
    public static Price of(BigDecimal value, String currency) {
        return new Price(value, currency);
    }
    
    /**
     * Создать цену из строки
     */
    public static Price of(String value, String currency) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Price value cannot be null or empty");
        }
        try {
            BigDecimal decimalValue = new BigDecimal(value.trim());
            return new Price(decimalValue, currency);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price value: " + value, e);
        }
    }
    
    /**
     * Создать цену в USD (по умолчанию)
     */
    public static Price ofUsd(BigDecimal value) {
        return new Price(value, "USD");
    }
    
    /**
     * Создать цену в USD из строки
     */
    public static Price ofUsd(String value) {
        return of(value, "USD");
    }
    
    /**
     * Создать нулевую цену
     */
    public static Price zero(String currency) {
        return new Price(BigDecimal.ZERO, currency);
    }
    
    private BigDecimal validateAndNormalizeValue(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Price value cannot be null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price value cannot be negative");
        }
        
        // Нормализуем до 8 знаков после запятой (стандарт для криптовалют)
        return value.setScale(8, RoundingMode.HALF_UP);
    }
    
    private String validateCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be null or empty");
        }
        return currency.trim().toUpperCase();
    }
    
    /**
     * Сложить с другой ценой (должна быть в той же валюте)
     */
    public Price add(Price other) {
        validateSameCurrency(other);
        return new Price(this.value.add(other.value), this.currency);
    }
    
    /**
     * Вычесть другую цену (должна быть в той же валюте)
     */
    public Price subtract(Price other) {
        validateSameCurrency(other);
        BigDecimal result = this.value.subtract(other.value);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Result of subtraction cannot be negative");
        }
        return new Price(result, this.currency);
    }
    
    /**
     * Умножить на коэффициент
     */
    public Price multiply(BigDecimal multiplier) {
        if (multiplier == null) {
            throw new IllegalArgumentException("Multiplier cannot be null");
        }
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Multiplier cannot be negative");
        }
        return new Price(this.value.multiply(multiplier), this.currency);
    }
    
    /**
     * Разделить на делитель
     */
    public Price divide(BigDecimal divisor) {
        if (divisor == null) {
            throw new IllegalArgumentException("Divisor cannot be null");
        }
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Divisor must be positive");
        }
        return new Price(this.value.divide(divisor, 8, RoundingMode.HALF_UP), this.currency);
    }
    
    /**
     * Сравнить с другой ценой (должна быть в той же валюте)
     */
    public int compareTo(Price other) {
        validateSameCurrency(other);
        return this.value.compareTo(other.value);
    }
    
    /**
     * Проверить, больше ли эта цена другой
     */
    public boolean isGreaterThan(Price other) {
        return compareTo(other) > 0;
    }
    
    /**
     * Проверить, меньше ли эта цена другой
     */
    public boolean isLessThan(Price other) {
        return compareTo(other) < 0;
    }
    
    /**
     * Проверить, равна ли эта цена другой
     */
    public boolean isEqualTo(Price other) {
        return compareTo(other) == 0;
    }
    
    /**
     * Проверить, является ли цена нулевой
     */
    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Получить процентное изменение относительно другой цены
     */
    public BigDecimal getPercentageChange(Price basePrice) {
        validateSameCurrency(basePrice);
        if (basePrice.isZero()) {
            throw new IllegalArgumentException("Base price cannot be zero for percentage calculation");
        }
        
        BigDecimal difference = this.value.subtract(basePrice.value);
        return difference.divide(basePrice.value, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
    }
    
    private void validateSameCurrency(Price other) {
        if (other == null) {
            throw new IllegalArgumentException("Other price cannot be null");
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Currency mismatch: %s vs %s", this.currency, other.currency));
        }
    }
    
    public BigDecimal getValue() {
        return value;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return Objects.equals(value, price.value) && Objects.equals(currency, price.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, currency);
    }
    
    @Override
    public String toString() {
        return String.format("%s %s", value.toPlainString(), currency);
    }
} 
