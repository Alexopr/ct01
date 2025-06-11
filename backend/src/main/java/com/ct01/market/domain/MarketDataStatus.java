package com.ct01.market.domain;

/**
 * Enum для статуса рыночных данных
 */
public enum MarketDataStatus {
    
    /**
     * Данные актуальные и свежие
     */
    ACTIVE("Актуальные данные"),
    
    /**
     * Данные устарели, но еще могут использоваться
     */
    STALE("Устаревшие данные"),
    
    /**
     * Ошибка при получении данных
     */
    ERROR("Ошибка получения данных"),
    
    /**
     * Данные временно недоступны
     */
    UNAVAILABLE("Данные недоступны"),
    
    /**
     * Биржа на техническом обслуживании
     */
    MAINTENANCE("Техническое обслуживание");
    
    private final String description;
    
    MarketDataStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Проверить, можно ли использовать данные для торговли
     */
    public boolean isTradeable() {
        return this == ACTIVE || this == STALE;
    }
    
    /**
     * Проверить, есть ли проблемы с данными
     */
    public boolean hasIssues() {
        return this == ERROR || this == UNAVAILABLE || this == MAINTENANCE;
    }
    
    /**
     * Проверить, нужно ли обновить данные
     */
    public boolean needsUpdate() {
        return this != ACTIVE;
    }
} 
