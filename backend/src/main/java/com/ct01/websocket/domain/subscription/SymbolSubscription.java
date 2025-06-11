package com.ct01.websocket.domain.subscription;

import com.ct01.shared.domain.ValueObject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Symbol Subscription value object
 * Представляет подписку на определенный символ криптовалюты
 */
public final class SymbolSubscription implements ValueObject {
    
    private final String symbol;
    private final LocalDateTime subscribedAt;
    private final boolean active;
    
    private SymbolSubscription(String symbol, LocalDateTime subscribedAt, boolean active) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (subscribedAt == null) {
            throw new IllegalArgumentException("SubscribedAt cannot be null");
        }
        
        this.symbol = symbol.toUpperCase().trim();
        this.subscribedAt = subscribedAt;
        this.active = active;
    }
    
    public static SymbolSubscription create(String symbol) {
        return new SymbolSubscription(symbol, LocalDateTime.now(), true);
    }
    
    public static SymbolSubscription of(String symbol, LocalDateTime subscribedAt, boolean active) {
        return new SymbolSubscription(symbol, subscribedAt, active);
    }
    
    public SymbolSubscription deactivate() {
        return new SymbolSubscription(this.symbol, this.subscribedAt, false);
    }
    
    public SymbolSubscription activate() {
        return new SymbolSubscription(this.symbol, this.subscribedAt, true);
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public LocalDateTime getSubscribedAt() {
        return subscribedAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolSubscription that = (SymbolSubscription) o;
        return Objects.equals(symbol, that.symbol);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }
    
    @Override
    public String toString() {
        return String.format("SymbolSubscription{symbol='%s', subscribedAt=%s, active=%s}", 
                            symbol, subscribedAt, active);
    }
} 