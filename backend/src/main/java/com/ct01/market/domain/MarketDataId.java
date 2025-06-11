package com.ct01.market.domain;

import com.ct01.core.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object для идентификатора рыночных данных
 */
public class MarketDataId implements ValueObject {
    
    private final String value;
    
    private MarketDataId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("MarketDataId value cannot be null or empty");
        }
        this.value = value.trim();
    }
    
    /**
     * Создать новый уникальный идентификатор
     */
    public static MarketDataId generate() {
        return new MarketDataId(UUID.randomUUID().toString());
    }
    
    /**
     * Создать идентификатор из существующего значения
     */
    public static MarketDataId of(String value) {
        return new MarketDataId(value);
    }
    
    /**
     * Создать составной идентификатор на основе символа и биржи
     */
    public static MarketDataId fromSymbolAndExchange(String symbol, String exchange) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (exchange == null || exchange.trim().isEmpty()) {
            throw new IllegalArgumentException("Exchange cannot be null or empty");
        }
        
        String compositeId = String.format("%s_%s", 
            symbol.trim().toUpperCase(), 
            exchange.trim().toUpperCase());
        return new MarketDataId(compositeId);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketDataId that = (MarketDataId) o;
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
