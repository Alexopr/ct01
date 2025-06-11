package com.ct01.market.domain;

import com.ct01.core.domain.BaseAggregateRoot;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Market Data Aggregate Root
 * Представляет рыночные данные для торговой пары на конкретной бирже
 */
public class MarketData extends BaseAggregateRoot<MarketDataId> {
    
    private final String symbol;
    private final String exchange;
    private final Price currentPrice;
    private final Price bidPrice;
    private final Price askPrice;
    private final Volume volume24h;
    private final PriceChange change24h;
    private final LocalDateTime timestamp;
    private final MarketDataStatus status;
    private final String errorMessage;
    
    public MarketData(MarketDataId id, String symbol, String exchange, Price currentPrice,
                     Price bidPrice, Price askPrice, Volume volume24h, PriceChange change24h,
                     LocalDateTime timestamp, MarketDataStatus status, String errorMessage) {
        super(id);
        this.symbol = validateSymbol(symbol);
        this.exchange = validateExchange(exchange);
        this.currentPrice = currentPrice;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.volume24h = volume24h;
        this.change24h = change24h;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.status = status != null ? status : MarketDataStatus.ACTIVE;
        this.errorMessage = errorMessage;
        
        validateBusinessRules();
    }
    
    private String validateSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        return symbol.trim().toUpperCase();
    }
    
    private String validateExchange(String exchange) {
        if (exchange == null || exchange.trim().isEmpty()) {
            throw new IllegalArgumentException("Exchange cannot be null or empty");
        }
        return exchange.trim().toUpperCase();
    }
    
    private void validateBusinessRules() {
        // Проверяем, что bid <= current <= ask (если все цены присутствуют)
        if (bidPrice != null && currentPrice != null && askPrice != null) {
            if (bidPrice.getValue().compareTo(currentPrice.getValue()) > 0) {
                throw new IllegalArgumentException("Bid price cannot be higher than current price");
            }
            if (currentPrice.getValue().compareTo(askPrice.getValue()) > 0) {
                throw new IllegalArgumentException("Current price cannot be higher than ask price");
            }
        }
        
        // Проверяем, что данные не слишком старые (более 1 часа)
        if (timestamp.isBefore(LocalDateTime.now().minusHours(1))) {
            // Автоматически помечаем как устаревшие
            if (status == MarketDataStatus.ACTIVE) {
                // В реальной реализации здесь бы был domain event
            }
        }
    }
    
    /**
     * Обновить рыночные данные
     */
    public MarketData updatePrices(Price newCurrentPrice, Price newBidPrice, Price newAskPrice,
                                  Volume newVolume24h, PriceChange newChange24h) {
        return new MarketData(
            this.getId(),
            this.symbol,
            this.exchange,
            newCurrentPrice != null ? newCurrentPrice : this.currentPrice,
            newBidPrice != null ? newBidPrice : this.bidPrice,
            newAskPrice != null ? newAskPrice : this.askPrice,
            newVolume24h != null ? newVolume24h : this.volume24h,
            newChange24h != null ? newChange24h : this.change24h,
            LocalDateTime.now(),
            MarketDataStatus.ACTIVE,
            null
        );
    }
    
    /**
     * Пометить данные как устаревшие
     */
    public MarketData markAsStale() {
        return new MarketData(
            this.getId(),
            this.symbol,
            this.exchange,
            this.currentPrice,
            this.bidPrice,
            this.askPrice,
            this.volume24h,
            this.change24h,
            this.timestamp,
            MarketDataStatus.STALE,
            this.errorMessage
        );
    }
    
    /**
     * Пометить данные как ошибочные
     */
    public MarketData markAsError(String errorMessage) {
        return new MarketData(
            this.getId(),
            this.symbol,
            this.exchange,
            this.currentPrice,
            this.bidPrice,
            this.askPrice,
            this.volume24h,
            this.change24h,
            this.timestamp,
            MarketDataStatus.ERROR,
            errorMessage
        );
    }
    
    /**
     * Проверить, актуальны ли данные
     */
    public boolean isDataFresh(int maxAgeMinutes) {
        return timestamp.isAfter(LocalDateTime.now().minusMinutes(maxAgeMinutes)) 
               && status == MarketDataStatus.ACTIVE;
    }
    
    /**
     * Получить спред (разница между ask и bid)
     */
    public BigDecimal getSpread() {
        if (bidPrice == null || askPrice == null) {
            return BigDecimal.ZERO;
        }
        return askPrice.getValue().subtract(bidPrice.getValue());
    }
    
    /**
     * Получить спред в процентах
     */
    public BigDecimal getSpreadPercentage() {
        if (bidPrice == null || askPrice == null || bidPrice.getValue().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal spread = getSpread();
        return spread.divide(bidPrice.getValue(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
    }
    
    // Getters
    public String getSymbol() { return symbol; }
    public String getExchange() { return exchange; }
    public Price getCurrentPrice() { return currentPrice; }
    public Price getBidPrice() { return bidPrice; }
    public Price getAskPrice() { return askPrice; }
    public Volume getVolume24h() { return volume24h; }
    public PriceChange getChange24h() { return change24h; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public MarketDataStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketData that = (MarketData) o;
        return Objects.equals(getId(), that.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
    
    @Override
    public String toString() {
        return String.format("MarketData{id=%s, symbol='%s', exchange='%s', price=%s, status=%s, timestamp=%s}",
                getId(), symbol, exchange, currentPrice, status, timestamp);
    }
} 
