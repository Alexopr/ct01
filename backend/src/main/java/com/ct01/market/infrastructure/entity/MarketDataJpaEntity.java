package com.ct01.market.infrastructure.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity для рыночных данных
 */
@Entity
@Table(name = "market_data", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"symbol", "exchange"}),
       indexes = {
           @Index(name = "idx_market_data_symbol", columnList = "symbol"),
           @Index(name = "idx_market_data_exchange", columnList = "exchange"),
           @Index(name = "idx_market_data_status", columnList = "status"),
           @Index(name = "idx_market_data_timestamp", columnList = "timestamp")
       })
public class MarketDataJpaEntity {
    
    @Id
    @Column(name = "id", length = 100)
    private String id;
    
    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;
    
    @Column(name = "exchange", nullable = false, length = 50)
    private String exchange;
    
    @Column(name = "current_price", precision = 20, scale = 8)
    private BigDecimal currentPrice;
    
    @Column(name = "current_price_currency", length = 10)
    private String currentPriceCurrency;
    
    @Column(name = "bid_price", precision = 20, scale = 8)
    private BigDecimal bidPrice;
    
    @Column(name = "bid_price_currency", length = 10)
    private String bidPriceCurrency;
    
    @Column(name = "ask_price", precision = 20, scale = 8)
    private BigDecimal askPrice;
    
    @Column(name = "ask_price_currency", length = 10)
    private String askPriceCurrency;
    
    @Column(name = "volume_24h", precision = 20, scale = 8)
    private BigDecimal volume24h;
    
    @Column(name = "volume_24h_unit", length = 10)
    private String volume24hUnit;
    
    @Column(name = "change_24h_absolute", precision = 20, scale = 8)
    private BigDecimal change24hAbsolute;
    
    @Column(name = "change_24h_percentage", precision = 10, scale = 4)
    private BigDecimal change24hPercentage;
    
    @Column(name = "change_24h_currency", length = 10)
    private String change24hCurrency;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MarketDataStatusJpa status;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Конструкторы
    public MarketDataJpaEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public MarketDataJpaEntity(String id, String symbol, String exchange) {
        this();
        this.id = id;
        this.symbol = symbol;
        this.exchange = exchange;
    }
    
    // JPA Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    
    public String getCurrentPriceCurrency() { return currentPriceCurrency; }
    public void setCurrentPriceCurrency(String currentPriceCurrency) { this.currentPriceCurrency = currentPriceCurrency; }
    
    public BigDecimal getBidPrice() { return bidPrice; }
    public void setBidPrice(BigDecimal bidPrice) { this.bidPrice = bidPrice; }
    
    public String getBidPriceCurrency() { return bidPriceCurrency; }
    public void setBidPriceCurrency(String bidPriceCurrency) { this.bidPriceCurrency = bidPriceCurrency; }
    
    public BigDecimal getAskPrice() { return askPrice; }
    public void setAskPrice(BigDecimal askPrice) { this.askPrice = askPrice; }
    
    public String getAskPriceCurrency() { return askPriceCurrency; }
    public void setAskPriceCurrency(String askPriceCurrency) { this.askPriceCurrency = askPriceCurrency; }
    
    public BigDecimal getVolume24h() { return volume24h; }
    public void setVolume24h(BigDecimal volume24h) { this.volume24h = volume24h; }
    
    public String getVolume24hUnit() { return volume24hUnit; }
    public void setVolume24hUnit(String volume24hUnit) { this.volume24hUnit = volume24hUnit; }
    
    public BigDecimal getChange24hAbsolute() { return change24hAbsolute; }
    public void setChange24hAbsolute(BigDecimal change24hAbsolute) { this.change24hAbsolute = change24hAbsolute; }
    
    public BigDecimal getChange24hPercentage() { return change24hPercentage; }
    public void setChange24hPercentage(BigDecimal change24hPercentage) { this.change24hPercentage = change24hPercentage; }
    
    public String getChange24hCurrency() { return change24hCurrency; }
    public void setChange24hCurrency(String change24hCurrency) { this.change24hCurrency = change24hCurrency; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public MarketDataStatusJpa getStatus() { return status; }
    public void setStatus(MarketDataStatusJpa status) { this.status = status; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketDataJpaEntity that = (MarketDataJpaEntity) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("MarketDataJpaEntity{id='%s', symbol='%s', exchange='%s', status=%s, timestamp=%s}",
                id, symbol, exchange, status, timestamp);
    }
    
    /**
     * JPA Enum для статуса рыночных данных
     */
    public enum MarketDataStatusJpa {
        ACTIVE,
        STALE,
        ERROR,
        UNAVAILABLE,
        MAINTENANCE
    }
} 
