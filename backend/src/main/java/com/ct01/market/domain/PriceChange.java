package com.ct01.market.domain;

import com.ct01.core.domain.ValueObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object для изменения цены
 */
public class PriceChange implements ValueObject {
    
    private final BigDecimal absoluteChange;
    private final BigDecimal percentageChange;
    private final String currency;
    
    private PriceChange(BigDecimal absoluteChange, BigDecimal percentageChange, String currency) {
        this.absoluteChange = validateAbsoluteChange(absoluteChange);
        this.percentageChange = validatePercentageChange(percentageChange);
        this.currency = validateCurrency(currency);
    }
    
    /**
     * Создать изменение цены из абсолютного и процентного изменения
     */
    public static PriceChange of(BigDecimal absoluteChange, BigDecimal percentageChange, String currency) {
        return new PriceChange(absoluteChange, percentageChange, currency);
    }
    
    /**
     * Создать изменение цены из двух цен
     */
    public static PriceChange between(Price oldPrice, Price newPrice) {
        if (oldPrice == null || newPrice == null) {
            throw new IllegalArgumentException("Prices cannot be null");
        }
        if (!oldPrice.getCurrency().equals(newPrice.getCurrency())) {
            throw new IllegalArgumentException("Prices must be in the same currency");
        }
        
        BigDecimal absoluteChange = newPrice.getValue().subtract(oldPrice.getValue());
        BigDecimal percentageChange = BigDecimal.ZERO;
        
        if (oldPrice.getValue().compareTo(BigDecimal.ZERO) != 0) {
            percentageChange = absoluteChange
                .divide(oldPrice.getValue(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        
        return new PriceChange(absoluteChange, percentageChange, oldPrice.getCurrency());
    }
    
    /**
     * Создать нулевое изменение цены
     */
    public static PriceChange zero(String currency) {
        return new PriceChange(BigDecimal.ZERO, BigDecimal.ZERO, currency);
    }
    
    /**
     * Создать положительное изменение цены
     */
    public static PriceChange positive(BigDecimal absoluteChange, BigDecimal percentageChange, String currency) {
        if (absoluteChange.compareTo(BigDecimal.ZERO) < 0 || percentageChange.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Positive change cannot have negative values");
        }
        return new PriceChange(absoluteChange, percentageChange, currency);
    }
    
    /**
     * Создать отрицательное изменение цены
     */
    public static PriceChange negative(BigDecimal absoluteChange, BigDecimal percentageChange, String currency) {
        if (absoluteChange.compareTo(BigDecimal.ZERO) > 0 || percentageChange.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("Negative change cannot have positive values");
        }
        return new PriceChange(absoluteChange, percentageChange, currency);
    }
    
    private BigDecimal validateAbsoluteChange(BigDecimal absoluteChange) {
        if (absoluteChange == null) {
            throw new IllegalArgumentException("Absolute change cannot be null");
        }
        // Нормализуем до 8 знаков после запятой
        return absoluteChange.setScale(8, RoundingMode.HALF_UP);
    }
    
    private BigDecimal validatePercentageChange(BigDecimal percentageChange) {
        if (percentageChange == null) {
            throw new IllegalArgumentException("Percentage change cannot be null");
        }
        // Нормализуем до 4 знаков после запятой для процентов
        return percentageChange.setScale(4, RoundingMode.HALF_UP);
    }
    
    private String validateCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be null or empty");
        }
        return currency.trim().toUpperCase();
    }
    
    /**
     * Проверить, является ли изменение положительным
     */
    public boolean isPositive() {
        return absoluteChange.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Проверить, является ли изменение отрицательным
     */
    public boolean isNegative() {
        return absoluteChange.compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * Проверить, является ли изменение нулевым
     */
    public boolean isZero() {
        return absoluteChange.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Проверить, является ли изменение значительным (больше порога)
     */
    public boolean isSignificant(BigDecimal thresholdPercentage) {
        if (thresholdPercentage == null || thresholdPercentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Threshold percentage must be non-negative");
        }
        return percentageChange.abs().compareTo(thresholdPercentage) > 0;
    }
    
    /**
     * Получить абсолютное значение изменения
     */
    public PriceChange abs() {
        return new PriceChange(
            absoluteChange.abs(),
            percentageChange.abs(),
            currency
        );
    }
    
    /**
     * Инвертировать изменение (сменить знак)
     */
    public PriceChange negate() {
        return new PriceChange(
            absoluteChange.negate(),
            percentageChange.negate(),
            currency
        );
    }
    
    /**
     * Сложить с другим изменением цены (должно быть в той же валюте)
     */
    public PriceChange add(PriceChange other) {
        validateSameCurrency(other);
        return new PriceChange(
            this.absoluteChange.add(other.absoluteChange),
            this.percentageChange.add(other.percentageChange),
            this.currency
        );
    }
    
    /**
     * Вычесть другое изменение цены (должно быть в той же валюте)
     */
    public PriceChange subtract(PriceChange other) {
        validateSameCurrency(other);
        return new PriceChange(
            this.absoluteChange.subtract(other.absoluteChange),
            this.percentageChange.subtract(other.percentageChange),
            this.currency
        );
    }
    
    /**
     * Сравнить с другим изменением цены по абсолютному значению
     */
    public int compareToByAbsolute(PriceChange other) {
        validateSameCurrency(other);
        return this.absoluteChange.compareTo(other.absoluteChange);
    }
    
    /**
     * Сравнить с другим изменением цены по процентному значению
     */
    public int compareToByPercentage(PriceChange other) {
        return this.percentageChange.compareTo(other.percentageChange);
    }
    
    /**
     * Получить тренд изменения цены
     */
    public PriceTrend getTrend() {
        if (isPositive()) {
            return PriceTrend.BULLISH;
        } else if (isNegative()) {
            return PriceTrend.BEARISH;
        } else {
            return PriceTrend.NEUTRAL;
        }
    }
    
    /**
     * Получить интенсивность изменения на основе процентного изменения
     */
    public ChangeIntensity getIntensity() {
        BigDecimal absPercentage = percentageChange.abs();
        
        if (absPercentage.compareTo(new BigDecimal("0.1")) <= 0) {
            return ChangeIntensity.MINIMAL;
        } else if (absPercentage.compareTo(new BigDecimal("1.0")) <= 0) {
            return ChangeIntensity.LOW;
        } else if (absPercentage.compareTo(new BigDecimal("5.0")) <= 0) {
            return ChangeIntensity.MODERATE;
        } else if (absPercentage.compareTo(new BigDecimal("10.0")) <= 0) {
            return ChangeIntensity.HIGH;
        } else {
            return ChangeIntensity.EXTREME;
        }
    }
    
    private void validateSameCurrency(PriceChange other) {
        if (other == null) {
            throw new IllegalArgumentException("Other price change cannot be null");
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Currency mismatch: %s vs %s", this.currency, other.currency));
        }
    }
    
    public BigDecimal getAbsoluteChange() {
        return absoluteChange;
    }
    
    public BigDecimal getPercentageChange() {
        return percentageChange;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceChange that = (PriceChange) o;
        return Objects.equals(absoluteChange, that.absoluteChange) &&
               Objects.equals(percentageChange, that.percentageChange) &&
               Objects.equals(currency, that.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(absoluteChange, percentageChange, currency);
    }
    
    @Override
    public String toString() {
        String sign = isPositive() ? "+" : "";
        return String.format("%s%s %s (%s%s%%)", 
            sign, absoluteChange.toPlainString(), currency,
            sign, percentageChange.toPlainString());
    }
    
    /**
     * Enum для тренда цены
     */
    public enum PriceTrend {
        BULLISH,  // Рост
        BEARISH,  // Падение
        NEUTRAL   // Без изменений
    }
    
    /**
     * Enum для интенсивности изменения
     */
    public enum ChangeIntensity {
        MINIMAL,   // <= 0.1%
        LOW,       // 0.1% - 1%
        MODERATE,  // 1% - 5%
        HIGH,      // 5% - 10%
        EXTREME    // > 10%
    }
} 
