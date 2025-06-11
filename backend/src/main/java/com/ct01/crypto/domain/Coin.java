package com.ct01.crypto.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Доменная модель криптовалюты (Value Object)
 * Инкапсулирует базовую информацию о криптовалюте
 */
public final class Coin {
    
    private final Long id;
    private final String symbol;
    private final String name;
    private final String iconUrl;
    private final String description;
    private final String websiteUrl;
    private final String whitepaperUrl;
    private final BigDecimal maxSupply;
    private final BigDecimal circulatingSupply;
    private final BigDecimal marketCap;
    private final Integer marketRank;
    private final CoinStatus status;
    private final Integer priority;
    private final List<String> categories;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime lastSyncAt;
    
    public Coin(Long id, String symbol, String name, String iconUrl, String description,
                String websiteUrl, String whitepaperUrl, BigDecimal maxSupply,
                BigDecimal circulatingSupply, BigDecimal marketCap, Integer marketRank,
                CoinStatus status, Integer priority, List<String> categories,
                LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastSyncAt) {
        
        this.id = id;
        this.symbol = Objects.requireNonNull(symbol, "Symbol cannot be null").toUpperCase();
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.iconUrl = iconUrl;
        this.description = description;
        this.websiteUrl = websiteUrl;
        this.whitepaperUrl = whitepaperUrl;
        this.maxSupply = maxSupply;
        this.circulatingSupply = circulatingSupply;
        this.marketCap = marketCap;
        this.marketRank = marketRank;
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.priority = priority != null ? priority : 5; // default priority
        this.categories = categories != null ? List.copyOf(categories) : List.of();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastSyncAt = lastSyncAt;
        
        validateInvariants();
    }
    
    /**
     * Проверка бизнес-правил
     */
    private void validateInvariants() {
        if (symbol.length() < 2 || symbol.length() > 10) {
            throw new IllegalArgumentException("Symbol must be between 2 and 10 characters");
        }
        
        if (name.length() < 2 || name.length() > 100) {
            throw new IllegalArgumentException("Name must be between 2 and 100 characters");
        }
        
        if (priority < 0 || priority > 10) {
            throw new IllegalArgumentException("Priority must be between 0 and 10");
        }
        
        if (maxSupply != null && maxSupply.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Max supply cannot be negative");
        }
        
        if (circulatingSupply != null && circulatingSupply.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Circulating supply cannot be negative");
        }
        
        if (marketCap != null && marketCap.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Market cap cannot be negative");
        }
    }
    
    /**
     * Проверить активность монеты
     */
    public boolean isActive() {
        return status == CoinStatus.ACTIVE;
    }
    
    /**
     * Получить текущий возраст монеты в днях
     */
    public long getAgeInDays() {
        if (createdAt == null) {
            return 0;
        }
        return java.time.ChronoUnit.DAYS.between(createdAt.toLocalDate(), LocalDateTime.now().toLocalDate());
    }
    
    /**
     * Проверить есть ли данные о рыночной капитализации
     */
    public boolean hasMarketData() {
        return marketCap != null && marketRank != null;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public String getIconUrl() { return iconUrl; }
    public String getDescription() { return description; }
    public String getWebsiteUrl() { return websiteUrl; }
    public String getWhitepaperUrl() { return whitepaperUrl; }
    public BigDecimal getMaxSupply() { return maxSupply; }
    public BigDecimal getCirculatingSupply() { return circulatingSupply; }
    public BigDecimal getMarketCap() { return marketCap; }
    public Integer getMarketRank() { return marketRank; }
    public CoinStatus getStatus() { return status; }
    public Integer getPriority() { return priority; }
    public List<String> getCategories() { return categories; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getLastSyncAt() { return lastSyncAt; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coin coin = (Coin) obj;
        return Objects.equals(symbol, coin.symbol);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }
    
    @Override
    public String toString() {
        return String.format("Coin{symbol='%s', name='%s', status=%s}", symbol, name, status);
    }
    
    /**
     * Enum для статуса монеты
     */
    public enum CoinStatus {
        ACTIVE,     // Активная торговля
        INACTIVE,   // Временно неактивна
        DELISTED,   // Исключена с бирж
        DEPRECATED  // Устарела/заменена
    }
} 
