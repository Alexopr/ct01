package com.ct01.crypto.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Доменная модель отслеживаемой криптовалюты (Aggregate Root)
 * Инкапсулирует логику отслеживания криптовалют
 */
public class TrackedCoin {
    
    private final Long id;
    private final String symbol;
    private final String name;
    private final Set<Exchange> exchanges;
    private final Set<String> quoteCurrencies;
    private Boolean isActive;
    private Integer pollingIntervalSeconds;
    private Boolean websocketEnabled;
    private Integer priority;
    private String notes;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public TrackedCoin(Long id, String symbol, String name, Set<Exchange> exchanges,
                       Set<String> quoteCurrencies, Boolean isActive,
                       Integer pollingIntervalSeconds, Boolean websocketEnabled,
                       Integer priority, String notes, LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        
        this.id = id;
        this.symbol = Objects.requireNonNull(symbol, "Symbol cannot be null").toUpperCase();
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.exchanges = Objects.requireNonNull(exchanges, "Exchanges cannot be null");
        this.quoteCurrencies = Objects.requireNonNull(quoteCurrencies, "Quote currencies cannot be null");
        this.isActive = isActive != null ? isActive : true;
        this.pollingIntervalSeconds = pollingIntervalSeconds;
        this.websocketEnabled = websocketEnabled != null ? websocketEnabled : true;
        this.priority = priority != null ? priority : 1;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        
        validateInvariants();
    }
    
    /**
     * Проверка бизнес-правил
     */
    private void validateInvariants() {
        if (symbol.length() < 2 || symbol.length() > 20) {
            throw new IllegalArgumentException("Symbol must be between 2 and 20 characters");
        }
        
        if (name.length() < 2 || name.length() > 100) {
            throw new IllegalArgumentException("Name must be between 2 and 100 characters");
        }
        
        if (exchanges.isEmpty()) {
            throw new IllegalArgumentException("At least one exchange must be specified");
        }
        
        if (quoteCurrencies.isEmpty()) {
            throw new IllegalArgumentException("At least one quote currency must be specified");
        }
        
        if (priority < 1 || priority > 10) {
            throw new IllegalArgumentException("Priority must be between 1 and 10");
        }
        
        if (pollingIntervalSeconds != null && (pollingIntervalSeconds < 1 || pollingIntervalSeconds > 86400)) {
            throw new IllegalArgumentException("Polling interval must be between 1 and 86400 seconds");
        }
        
        if (notes != null && notes.length() > 500) {
            throw new IllegalArgumentException("Notes cannot exceed 500 characters");
        }
    }
    
    /**
     * Активировать отслеживание
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Деактивировать отслеживание
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Изменить приоритет отслеживания
     */
    public void changePriority(Integer newPriority) {
        if (newPriority == null || newPriority < 1 || newPriority > 10) {
            throw new IllegalArgumentException("Priority must be between 1 and 10");
        }
        this.priority = newPriority;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Изменить интервал опроса
     */
    public void changePollingInterval(Integer intervalSeconds) {
        if (intervalSeconds != null && (intervalSeconds < 1 || intervalSeconds > 86400)) {
            throw new IllegalArgumentException("Polling interval must be between 1 and 86400 seconds");
        }
        this.pollingIntervalSeconds = intervalSeconds;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Включить/выключить WebSocket обновления
     */
    public void setWebsocketEnabled(Boolean enabled) {
        this.websocketEnabled = enabled != null ? enabled : true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Обновить заметки
     */
    public void updateNotes(String newNotes) {
        if (newNotes != null && newNotes.length() > 500) {
            throw new IllegalArgumentException("Notes cannot exceed 500 characters");
        }
        this.notes = newNotes;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Проверить поддерживается ли биржа
     */
    public boolean supportsExchange(Exchange exchange) {
        return exchanges.contains(exchange);
    }
    
    /**
     * Проверить поддерживается ли котируемая валюта
     */
    public boolean supportsQuoteCurrency(String quoteCurrency) {
        return quoteCurrencies.contains(quoteCurrency.toUpperCase());
    }
    
    /**
     * Получить эффективный интервал опроса (с учетом приоритета)
     */
    public int getEffectivePollingInterval() {
        if (pollingIntervalSeconds != null) {
            return pollingIntervalSeconds;
        }
        
        // Базовый интервал 30 секунд, корректируется по приоритету
        int baseInterval = 30;
        return Math.max(5, baseInterval - ((priority - 1) * 5));
    }
    
    // Getters
    public Long getId() { return id; }
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public Set<Exchange> getExchanges() { return Set.copyOf(exchanges); }
    public Set<String> getQuoteCurrencies() { return Set.copyOf(quoteCurrencies); }
    public Boolean getIsActive() { return isActive; }
    public Integer getPollingIntervalSeconds() { return pollingIntervalSeconds; }
    public Boolean getWebsocketEnabled() { return websocketEnabled; }
    public Integer getPriority() { return priority; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TrackedCoin that = (TrackedCoin) obj;
        return Objects.equals(id, that.id) && Objects.equals(symbol, that.symbol);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, symbol);
    }
    
    @Override
    public String toString() {
        return String.format("TrackedCoin{id=%d, symbol='%s', name='%s', active=%s, priority=%d}", 
                           id, symbol, name, isActive, priority);
    }
    
    /**
     * Enum для поддерживаемых бирж
     */
    public enum Exchange {
        BINANCE,
        BYBIT,
        OKX
    }
} 
