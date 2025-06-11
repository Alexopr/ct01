package com.ct01.crypto.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Доменная модель истории цен (Value Object)
 * Инкапсулирует данные о ценах криптовалют
 */
public final class PriceHistory {
    
    private final Long id;
    private final String coinSymbol;
    private final String exchangeName;
    private final String tradingPair;
    private final String quoteCurrency;
    private final LocalDateTime timestamp;
    private final BigDecimal openPrice;
    private final BigDecimal highPrice;
    private final BigDecimal lowPrice;
    private final BigDecimal closePrice;
    private final BigDecimal volume;
    private final BigDecimal volumeUsd;
    private final Long tradesCount;
    private final String priceType;
    
    public PriceHistory(Long id, String coinSymbol, String exchangeName, String tradingPair,
                        String quoteCurrency, LocalDateTime timestamp, BigDecimal openPrice,
                        BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice,
                        BigDecimal volume, BigDecimal volumeUsd, Long tradesCount,
                        String priceType) {
        
        this.id = id;
        this.coinSymbol = Objects.requireNonNull(coinSymbol, "Coin symbol cannot be null").toUpperCase();
        this.exchangeName = Objects.requireNonNull(exchangeName, "Exchange name cannot be null");
        this.tradingPair = Objects.requireNonNull(tradingPair, "Trading pair cannot be null").toUpperCase();
        this.quoteCurrency = quoteCurrency != null ? quoteCurrency.toUpperCase() : null;
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = Objects.requireNonNull(closePrice, "Close price cannot be null");
        this.volume = volume;
        this.volumeUsd = volumeUsd;
        this.tradesCount = tradesCount;
        this.priceType = priceType != null ? priceType : "TICKER";
        
        validateInvariants();
    }
    
    /**
     * Проверка бизнес-правил
     */
    private void validateInvariants() {
        if (closePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Close price must be positive");
        }
        
        if (openPrice != null && openPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Open price must be positive");
        }
        
        if (highPrice != null && highPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("High price must be positive");
        }
        
        if (lowPrice != null && lowPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Low price must be positive");
        }
        
        if (volume != null && volume.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Volume cannot be negative");
        }
        
        if (volumeUsd != null && volumeUsd.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Volume USD cannot be negative");
        }
        
        if (tradesCount != null && tradesCount < 0) {
            throw new IllegalArgumentException("Trades count cannot be negative");
        }
        
        // Проверка логики OHLC
        if (isOhlcData()) {
            validateOhlcLogic();
        }
        
        if (timestamp.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new IllegalArgumentException("Timestamp cannot be in the future");
        }
    }
    
    /**
     * Проверка логики OHLC данных
     */
    private void validateOhlcLogic() {
        if (openPrice == null || highPrice == null || lowPrice == null) {
            throw new IllegalArgumentException("OHLC data requires open, high, low, and close prices");
        }
        
        if (highPrice.compareTo(lowPrice) < 0) {
            throw new IllegalArgumentException("High price cannot be lower than low price");
        }
        
        if (openPrice.compareTo(lowPrice) < 0 || openPrice.compareTo(highPrice) > 0) {
            throw new IllegalArgumentException("Open price must be between low and high prices");
        }
        
        if (closePrice.compareTo(lowPrice) < 0 || closePrice.compareTo(highPrice) > 0) {
            throw new IllegalArgumentException("Close price must be between low and high prices");
        }
    }
    
    /**
     * Проверить есть ли полные OHLC данные
     */
    public boolean isOhlcData() {
        return openPrice != null && highPrice != null && lowPrice != null;
    }
    
    /**
     * Проверить является ли это тикерными данными
     */
    public boolean isTickerData() {
        return "TICKER".equals(priceType);
    }
    
    /**
     * Проверить является ли это данными свечи
     */
    public boolean isCandleData() {
        return "CANDLE".equals(priceType) && isOhlcData();
    }
    
    /**
     * Получить изменение цены (если есть open price)
     */
    public BigDecimal getPriceChange() {
        if (openPrice == null) {
            return BigDecimal.ZERO;
        }
        return closePrice.subtract(openPrice);
    }
    
    /**
     * Получить изменение цены в процентах
     */
    public BigDecimal getPriceChangePercent() {
        if (openPrice == null || openPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getPriceChange()
                .divide(openPrice, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * Получить возраст данных в минутах
     */
    public long getAgeInMinutes() {
        return java.time.ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now());
    }
    
    /**
     * Проверить являются ли данные свежими (не старше указанного времени)
     */
    public boolean isFresh(int maxAgeMinutes) {
        return getAgeInMinutes() <= maxAgeMinutes;
    }
    
    /**
     * Получить основную цену (close price)
     */
    public BigDecimal getPrice() {
        return closePrice;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getCoinSymbol() { return coinSymbol; }
    public String getExchangeName() { return exchangeName; }
    public String getTradingPair() { return tradingPair; }
    public String getQuoteCurrency() { return quoteCurrency; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public BigDecimal getOpenPrice() { return openPrice; }
    public BigDecimal getHighPrice() { return highPrice; }
    public BigDecimal getLowPrice() { return lowPrice; }
    public BigDecimal getClosePrice() { return closePrice; }
    public BigDecimal getVolume() { return volume; }
    public BigDecimal getVolumeUsd() { return volumeUsd; }
    public Long getTradesCount() { return tradesCount; }
    public String getPriceType() { return priceType; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PriceHistory that = (PriceHistory) obj;
        return Objects.equals(coinSymbol, that.coinSymbol) &&
               Objects.equals(exchangeName, that.exchangeName) &&
               Objects.equals(tradingPair, that.tradingPair) &&
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(coinSymbol, exchangeName, tradingPair, timestamp);
    }
    
    @Override
    public String toString() {
        return String.format("PriceHistory{coin='%s', exchange='%s', pair='%s', price=%s, time=%s}", 
                           coinSymbol, exchangeName, tradingPair, closePrice, timestamp);
    }
} 
