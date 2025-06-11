package com.ct01.migration.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Шаг миграции данных
 */
public class MigrationStep {
    
    private final String name;
    private final String description;
    private final int order;
    private final MigrationStepType type;
    private final String sourceTable;
    private final String targetTable;
    private final String transformationScript;
    private final ValidationRule validationRule;
    private MigrationStepStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    private long processedRecords;
    private long totalRecords;
    
    public MigrationStep(String name, String description, int order, MigrationStepType type,
                        String sourceTable, String targetTable, String transformationScript,
                        ValidationRule validationRule) {
        this.name = Objects.requireNonNull(name, "Название шага не может быть null");
        this.description = Objects.requireNonNull(description, "Описание шага не может быть null");
        this.order = order;
        this.type = Objects.requireNonNull(type, "Тип шага не может быть null");
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
        this.transformationScript = transformationScript;
        this.validationRule = validationRule;
        this.status = MigrationStepStatus.PENDING;
        this.processedRecords = 0;
        this.totalRecords = 0;
        
        if (order < 0) {
            throw new IllegalArgumentException("Порядковый номер шага должен быть неотрицательным");
        }
        
        validateStepConfiguration();
    }
    
    /**
     * Начать выполнение шага
     */
    public void start(long totalRecords) {
        if (status != MigrationStepStatus.PENDING) {
            throw new IllegalStateException("Шаг уже запущен или завершен");
        }
        
        this.status = MigrationStepStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
        this.totalRecords = totalRecords;
        this.processedRecords = 0;
    }
    
    /**
     * Обновить прогресс выполнения
     */
    public void updateProgress(long processedRecords) {
        if (status != MigrationStepStatus.RUNNING) {
            throw new IllegalStateException("Шаг не в состоянии выполнения");
        }
        
        if (processedRecords < 0 || processedRecords > totalRecords) {
            throw new IllegalArgumentException("Неверное количество обработанных записей");
        }
        
        this.processedRecords = processedRecords;
    }
    
    /**
     * Завершить выполнение шага
     */
    public void complete() {
        if (status != MigrationStepStatus.RUNNING) {
            throw new IllegalStateException("Шаг не в состоянии выполнения");
        }
        
        this.status = MigrationStepStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.processedRecords = this.totalRecords;
    }
    
    /**
     * Отметить шаг как неудачный
     */
    public void markAsFailed(String errorMessage) {
        this.status = MigrationStepStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Пропустить шаг
     */
    public void skip(String reason) {
        if (status == MigrationStepStatus.COMPLETED || status == MigrationStepStatus.FAILED) {
            throw new IllegalStateException("Нельзя пропустить завершенный или неудачный шаг");
        }
        
        this.status = MigrationStepStatus.SKIPPED;
        this.errorMessage = reason;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Проверить валидность конфигурации шага
     */
    public boolean isValid() {
        try {
            validateStepConfiguration();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Получить процент выполнения шага
     */
    public double getProgressPercentage() {
        if (totalRecords == 0) return 0.0;
        return (double) processedRecords / totalRecords * 100.0;
    }
    
    /**
     * Проверить завершенность шага
     */
    public boolean isCompleted() {
        return status == MigrationStepStatus.COMPLETED || status == MigrationStepStatus.SKIPPED;
    }
    
    /**
     * Проверить возможность отката
     */
    public boolean canRollback() {
        return isCompleted() && type.supportsRollback();
    }
    
    private void validateStepConfiguration() {
        switch (type) {
            case DATA_MIGRATION:
                if (sourceTable == null || sourceTable.trim().isEmpty()) {
                    throw new IllegalArgumentException("Источник данных обязателен для миграции данных");
                }
                if (targetTable == null || targetTable.trim().isEmpty()) {
                    throw new IllegalArgumentException("Целевая таблица обязательна для миграции данных");
                }
                break;
            case SCHEMA_UPDATE:
                // Для обновления схемы может не требоваться sourceTable
                if (targetTable == null || targetTable.trim().isEmpty()) {
                    throw new IllegalArgumentException("Целевая таблица обязательна для обновления схемы");
                }
                break;
            case VALIDATION:
                if (validationRule == null) {
                    throw new IllegalArgumentException("Правило валидации обязательно для шага валидации");
                }
                break;
            case CLEANUP:
                // Очистка может работать с любой таблицей
                break;
        }
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getOrder() { return order; }
    public MigrationStepType getType() { return type; }
    public String getSourceTable() { return sourceTable; }
    public String getTargetTable() { return targetTable; }
    public String getTransformationScript() { return transformationScript; }
    public ValidationRule getValidationRule() { return validationRule; }
    public MigrationStepStatus getStatus() { return status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getErrorMessage() { return errorMessage; }
    public long getProcessedRecords() { return processedRecords; }
    public long getTotalRecords() { return totalRecords; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MigrationStep that = (MigrationStep) o;
        return order == that.order && 
               Objects.equals(name, that.name) && 
               Objects.equals(type, that.type);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, order, type);
    }
    
    @Override
    public String toString() {
        return String.format("MigrationStep{name='%s', order=%d, status=%s, progress=%.1f%%}",
                           name, order, status, getProgressPercentage());
    }
} 