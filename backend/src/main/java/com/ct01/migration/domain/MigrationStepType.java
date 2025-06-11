package com.ct01.migration.domain;

/**
 * Тип шага миграции
 */
public enum MigrationStepType {
    
    /**
     * Миграция данных между таблицами
     */
    DATA_MIGRATION("Миграция данных", true),
    
    /**
     * Обновление схемы БД
     */
    SCHEMA_UPDATE("Обновление схемы", false),
    
    /**
     * Валидация данных
     */
    VALIDATION("Валидация", false),
    
    /**
     * Очистка старых данных
     */
    CLEANUP("Очистка", false),
    
    /**
     * Создание индексов
     */
    INDEX_CREATION("Создание индексов", true),
    
    /**
     * Обновление конфигурации
     */
    CONFIG_UPDATE("Обновление конфигурации", true);
    
    private final String displayName;
    private final boolean supportsRollback;
    
    MigrationStepType(String displayName, boolean supportsRollback) {
        this.displayName = displayName;
        this.supportsRollback = supportsRollback;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Поддерживает ли тип шага откат
     */
    public boolean supportsRollback() {
        return supportsRollback;
    }
    
    /**
     * Требует ли тип шага исходную таблицу
     */
    public boolean requiresSourceTable() {
        return this == DATA_MIGRATION;
    }
    
    /**
     * Требует ли тип шага целевую таблицу
     */
    public boolean requiresTargetTable() {
        return this == DATA_MIGRATION || this == SCHEMA_UPDATE || this == INDEX_CREATION;
    }
    
    /**
     * Требует ли тип шага правила валидации
     */
    public boolean requiresValidationRule() {
        return this == VALIDATION;
    }
} 