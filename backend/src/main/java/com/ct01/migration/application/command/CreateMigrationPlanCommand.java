package com.ct01.migration.application.command;

import com.ct01.migration.domain.MigrationStrategy;

import java.util.List;
import java.util.Objects;

/**
 * Команда для создания плана миграции
 */
public class CreateMigrationPlanCommand {
    
    private final String name;
    private final String description;
    private final MigrationStrategy strategy;
    private final List<MigrationStepDefinition> stepDefinitions;
    
    public CreateMigrationPlanCommand(String name, String description, MigrationStrategy strategy,
                                     List<MigrationStepDefinition> stepDefinitions) {
        this.name = Objects.requireNonNull(name, "Название плана не может быть null");
        this.description = Objects.requireNonNull(description, "Описание плана не может быть null");
        this.strategy = Objects.requireNonNull(strategy, "Стратегия не может быть null");
        this.stepDefinitions = Objects.requireNonNull(stepDefinitions, "Шаги не могут быть null");
        
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название плана не может быть пустым");
        }
        
        if (stepDefinitions.isEmpty()) {
            throw new IllegalArgumentException("План должен содержать хотя бы один шаг");
        }
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public MigrationStrategy getStrategy() {
        return strategy;
    }
    
    public List<MigrationStepDefinition> getStepDefinitions() {
        return List.copyOf(stepDefinitions);
    }
    
    @Override
    public String toString() {
        return String.format("CreateMigrationPlanCommand{name='%s', strategy=%s, steps=%d}",
                           name, strategy, stepDefinitions.size());
    }
} 