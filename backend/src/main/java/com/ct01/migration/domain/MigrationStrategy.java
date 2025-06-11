package com.ct01.migration.domain;

/**
 * Стратегия миграции данных
 */
public enum MigrationStrategy {
    
    /**
     * Миграция в один шаг (останавливает систему)
     */
    BIG_BANG("Миграция в один шаг", false),
    
    /**
     * Пошаговая миграция с минимальным downtime
     */
    INCREMENTAL("Пошаговая миграция", true),
    
    /**
     * Миграция с дублированием данных (dual-write)
     */
    DUAL_WRITE("Миграция с дублированием", true),
    
    /**
     * Откат к предыдущему состоянию
     */
    ROLLBACK("Откат", false);
    
    private final String displayName;
    private final boolean supportsZeroDowntime;
    
    MigrationStrategy(String displayName, boolean supportsZeroDowntime) {
        this.displayName = displayName;
        this.supportsZeroDowntime = supportsZeroDowntime;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Поддерживает ли стратегия миграцию без остановки системы
     */
    public boolean supportsZeroDowntime() {
        return supportsZeroDowntime;
    }
    
    /**
     * Требует ли стратегия предварительной подготовки
     */
    public boolean requiresPreparation() {
        return this == INCREMENTAL || this == DUAL_WRITE;
    }
    
    /**
     * Позволяет ли стратегия параллельное выполнение шагов
     */
    public boolean allowsParallelExecution() {
        return this == INCREMENTAL || this == DUAL_WRITE;
    }
} 