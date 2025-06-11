package com.ct01.migration.domain;

/**
 * Статус выполнения миграции
 */
public enum MigrationStatus {
    
    /**
     * Миграция запланирована, но еще не началась
     */
    PLANNED("Запланирована"),
    
    /**
     * Миграция выполняется
     */
    RUNNING("Выполняется"),
    
    /**
     * Миграция успешно завершена
     */
    COMPLETED("Завершена"),
    
    /**
     * Миграция завершилась с ошибкой
     */
    FAILED("Неудачна"),
    
    /**
     * Выполняется откат миграции
     */
    ROLLBACK("Откат"),
    
    /**
     * Откат успешно завершен
     */
    ROLLED_BACK("Откат завершен"),
    
    /**
     * Миграция отменена пользователем
     */
    CANCELLED("Отменена");
    
    private final String displayName;
    
    MigrationStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Проверить, является ли статус финальным (завершающим)
     */
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == ROLLED_BACK || this == CANCELLED;
    }
    
    /**
     * Проверить, выполняется ли миграция
     */
    public boolean isActive() {
        return this == RUNNING || this == ROLLBACK;
    }
    
    /**
     * Проверить, можно ли отменить миграцию в данном статусе
     */
    public boolean canCancel() {
        return this == PLANNED || this == RUNNING;
    }
    
    /**
     * Проверить, можно ли запустить откат в данном статусе
     */
    public boolean canRollback() {
        return this == COMPLETED || this == FAILED;
    }
} 