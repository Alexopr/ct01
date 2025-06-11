package com.ct01.migration.application.command;

import com.ct01.migration.domain.MigrationPlanId;

import java.util.Objects;

/**
 * Команда для выполнения плана миграции
 */
public class ExecuteMigrationPlanCommand {
    
    private final MigrationPlanId migrationPlanId;
    private final boolean dryRun;
    private final boolean preValidationEnabled;
    private final boolean continueOnErrors;
    
    public ExecuteMigrationPlanCommand(MigrationPlanId migrationPlanId, boolean dryRun, 
                                      boolean preValidationEnabled, boolean continueOnErrors) {
        this.migrationPlanId = Objects.requireNonNull(migrationPlanId, "ID плана миграции не может быть null");
        this.dryRun = dryRun;
        this.preValidationEnabled = preValidationEnabled;
        this.continueOnErrors = continueOnErrors;
    }
    
    /**
     * Создать команду для полного выполнения с валидацией
     */
    public static ExecuteMigrationPlanCommand execute(MigrationPlanId migrationPlanId) {
        return new ExecuteMigrationPlanCommand(migrationPlanId, false, true, false);
    }
    
    /**
     * Создать команду для тестового выполнения (dry run)
     */
    public static ExecuteMigrationPlanCommand dryRun(MigrationPlanId migrationPlanId) {
        return new ExecuteMigrationPlanCommand(migrationPlanId, true, true, false);
    }
    
    /**
     * Создать команду для выполнения без валидации
     */
    public static ExecuteMigrationPlanCommand executeWithoutValidation(MigrationPlanId migrationPlanId) {
        return new ExecuteMigrationPlanCommand(migrationPlanId, false, false, false);
    }
    
    /**
     * Создать команду для выполнения с продолжением при ошибках
     */
    public static ExecuteMigrationPlanCommand executeContinueOnErrors(MigrationPlanId migrationPlanId) {
        return new ExecuteMigrationPlanCommand(migrationPlanId, false, true, true);
    }
    
    public MigrationPlanId getMigrationPlanId() {
        return migrationPlanId;
    }
    
    public boolean isDryRun() {
        return dryRun;
    }
    
    public boolean isPreValidationEnabled() {
        return preValidationEnabled;
    }
    
    public boolean isContinueOnErrors() {
        return continueOnErrors;
    }
    
    @Override
    public String toString() {
        return String.format("ExecuteMigrationPlanCommand{planId=%s, dryRun=%s, preValidation=%s, continueOnErrors=%s}",
                           migrationPlanId, dryRun, preValidationEnabled, continueOnErrors);
    }
} 