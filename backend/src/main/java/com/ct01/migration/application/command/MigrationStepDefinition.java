package com.ct01.migration.application.command;

import com.ct01.migration.domain.MigrationStepType;
import com.ct01.migration.domain.ValidationRule;

import java.util.Objects;

/**
 * Определение шага миграции для создания плана
 */
public class MigrationStepDefinition {
    
    private final String name;
    private final String description;
    private final int order;
    private final MigrationStepType type;
    private final String sourceTable;
    private final String targetTable;
    private final String transformationScript;
    private final ValidationRule validationRule;
    
    public MigrationStepDefinition(String name, String description, int order, MigrationStepType type,
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
        
        if (order < 0) {
            throw new IllegalArgumentException("Порядковый номер шага не может быть отрицательным");
        }
    }
    
    /**
     * Создать определение для миграции данных
     */
    public static MigrationStepDefinition dataMigration(String name, String description, int order,
                                                       String sourceTable, String targetTable,
                                                       String transformationScript) {
        return new MigrationStepDefinition(
            name, description, order, MigrationStepType.DATA_MIGRATION,
            sourceTable, targetTable, transformationScript, null
        );
    }
    
    /**
     * Создать определение для валидации данных
     */
    public static MigrationStepDefinition validation(String name, String description, int order,
                                                    ValidationRule validationRule) {
        return new MigrationStepDefinition(
            name, description, order, MigrationStepType.VALIDATION,
            null, null, null, validationRule
        );
    }
    
    /**
     * Создать определение для обновления схемы
     */
    public static MigrationStepDefinition schemaUpdate(String name, String description, int order,
                                                      String targetTable, String transformationScript) {
        return new MigrationStepDefinition(
            name, description, order, MigrationStepType.SCHEMA_UPDATE,
            null, targetTable, transformationScript, null
        );
    }
    
    /**
     * Создать определение для очистки данных
     */
    public static MigrationStepDefinition cleanup(String name, String description, int order,
                                                 String targetTable) {
        return new MigrationStepDefinition(
            name, description, order, MigrationStepType.CLEANUP,
            null, targetTable, null, null
        );
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
    
    @Override
    public String toString() {
        return String.format("MigrationStepDefinition{name='%s', order=%d, type=%s}",
                           name, order, type);
    }
} 