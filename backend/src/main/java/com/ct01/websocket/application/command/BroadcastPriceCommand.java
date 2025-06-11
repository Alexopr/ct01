package com.ct01.websocket.application.command;

import com.ct01.shared.application.Command;

import java.math.BigDecimal;

/**
 * Command для трансляции обновления цены
 */
public record BroadcastPriceCommand(
    String symbol,
    BigDecimal price,
    BigDecimal volume,
    String exchange,
    String tradingPair
) implements Command {
    
    public BroadcastPriceCommand {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (exchange == null || exchange.trim().isEmpty()) {
            throw new IllegalArgumentException("Exchange cannot be null or empty");
        }
    }
    
    /**
     * Создать команду с минимальными данными
     */
    public static BroadcastPriceCommand create(String symbol, BigDecimal price, String exchange) {
        return new BroadcastPriceCommand(symbol, price, null, exchange, null);
    }
    
    /**
     * Создать команду с полными данными
     */
    public static BroadcastPriceCommand full(String symbol, BigDecimal price, BigDecimal volume,
                                           String exchange, String tradingPair) {
        return new BroadcastPriceCommand(symbol, price, volume, exchange, tradingPair);
    }
    
    /**
     * Получить нормализованный символ
     */
    public String getNormalizedSymbol() {
        return symbol.trim().toUpperCase();
    }
    
    /**
     * Получить объем (с fallback на 0)
     */
    public BigDecimal getSafeVolume() {
        return volume != null ? volume : BigDecimal.ZERO;
    }
} 