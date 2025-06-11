package com.ct01.migration.domain;

/**
 * Статус выполнения шага миграции
 */
public enum MigrationStepStatus {
    
    /**
     * Шаг ожидает выполнения
     */
    PENDING("Ожидает"),
    
    /**
     * Шаг выполняется
     */
    RUNNING("Выполняется"),
    
    /**
     * Шаг успешно завершен
     */
    COMPLETED("Завершен"),
    
    /**
     * Шаг завершился с ошибкой
     */
    FAILED("Неудачен"),
    
    /**
     * Шаг пропущен
     */
    SKIPPED("Пропущен");
    
    private final String displayName;
    
    MigrationStepStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Проверить, является ли статус финальным
     */
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == SKIPPED;
    }
    
    /**
     * Проверить, выполняется ли шаг
     */
    public boolean isActive() {
        return this == RUNNING;
    }
} 