package com.ct01.market.domain;

import com.ct01.core.domain.ValueObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object для объема торгов
 */
public class Volume implements ValueObject {
    
    private final BigDecimal value;
    private final String unit; // BTC, USD, etc.
    
    private Volume(BigDecimal value, String unit) {
        this.value = validateAndNormalizeValue(value);
        this.unit = validateUnit(unit);
    }
    
    /**
     * Создать объем
     */
    public static Volume of(BigDecimal value, String unit) {
        return new Volume(value, unit);
    }
    
    /**
     * Создать объем из строки
     */
    public static Volume of(String value, String unit) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Volume value cannot be null or empty");
        }
        try {
            BigDecimal decimalValue = new BigDecimal(value.trim());
            return new Volume(decimalValue, unit);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid volume value: " + value, e);
        }
    }
    
    /**
     * Создать нулевой объем
     */
    public static Volume zero(String unit) {
        return new Volume(BigDecimal.ZERO, unit);
    }
    
    private BigDecimal validateAndNormalizeValue(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Volume value cannot be null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Volume value cannot be negative");
        }
        
        // Нормализуем до 8 знаков после запятой
        return value.setScale(8, RoundingMode.HALF_UP);
    }
    
    private String validateUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Volume unit cannot be null or empty");
        }
        return unit.trim().toUpperCase();
    }
    
    /**
     * Сложить с другим объемом (должен быть в той же единице)
     */
    public Volume add(Volume other) {
        validateSameUnit(other);
        return new Volume(this.value.add(other.value), this.unit);
    }
    
    /**
     * Вычесть другой объем (должен быть в той же единице)
     */
    public Volume subtract(Volume other) {
        validateSameUnit(other);
        BigDecimal result = this.value.subtract(other.value);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Result of subtraction cannot be negative");
        }
        return new Volume(result, this.unit);
    }
    
    /**
     * Умножить на коэффициент
     */
    public Volume multiply(BigDecimal multiplier) {
        if (multiplier == null) {
            throw new IllegalArgumentException("Multiplier cannot be null");
        }
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Multiplier cannot be negative");
        }
        return new Volume(this.value.multiply(multiplier), this.unit);
    }
    
    /**
     * Разделить на делитель
     */
    public Volume divide(BigDecimal divisor) {
        if (divisor == null) {
            throw new IllegalArgumentException("Divisor cannot be null");
        }
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Divisor must be positive");
        }
        return new Volume(this.value.divide(divisor, 8, RoundingMode.HALF_UP), this.unit);
    }
    
    /**
     * Сравнить с другим объемом (должен быть в той же единице)
     */
    public int compareTo(Volume other) {
        validateSameUnit(other);
        return this.value.compareTo(other.value);
    }
    
    /**
     * Проверить, больше ли этот объем другого
     */
    public boolean isGreaterThan(Volume other) {
        return compareTo(other) > 0;
    }
    
    /**
     * Проверить, меньше ли этот объем другого
     */
    public boolean isLessThan(Volume other) {
        return compareTo(other) < 0;
    }
    
    /**
     * Проверить, равен ли этот объем другому
     */
    public boolean isEqualTo(Volume other) {
        return compareTo(other) == 0;
    }
    
    /**
     * Проверить, является ли объем нулевым
     */
    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Проверить, является ли объем значительным (больше минимального порога)
     */
    public boolean isSignificant() {
        // Считаем значительным объем больше 0.00000001
        BigDecimal threshold = new BigDecimal("0.00000001");
        return value.compareTo(threshold) > 0;
    }
    
    /**
     * Получить процентное изменение относительно другого объема
     */
    public BigDecimal getPercentageChange(Volume baseVolume) {
        validateSameUnit(baseVolume);
        if (baseVolume.isZero()) {
            throw new IllegalArgumentException("Base volume cannot be zero for percentage calculation");
        }
        
        BigDecimal difference = this.value.subtract(baseVolume.value);
        return difference.divide(baseVolume.value, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * Конвертировать в другую единицу измерения (требует курс конвертации)
     */
    public Volume convertTo(String newUnit, BigDecimal conversionRate) {
        if (newUnit == null || newUnit.trim().isEmpty()) {
            throw new IllegalArgumentException("New unit cannot be null or empty");
        }
        if (conversionRate == null || conversionRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Conversion rate must be positive");
        }
        
        BigDecimal convertedValue = this.value.multiply(conversionRate);
        return new Volume(convertedValue, newUnit.trim().toUpperCase());
    }
    
    private void validateSameUnit(Volume other) {
        if (other == null) {
            throw new IllegalArgumentException("Other volume cannot be null");
        }
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException(
                String.format("Unit mismatch: %s vs %s", this.unit, other.unit));
        }
    }
    
    public BigDecimal getValue() {
        return value;
    }
    
    public String getUnit() {
        return unit;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Volume volume = (Volume) o;
        return Objects.equals(value, volume.value) && Objects.equals(unit, volume.unit);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }
    
    @Override
    public String toString() {
        return String.format("%s %s", value.toPlainString(), unit);
    }
} 
