package com.ct01.migration.application.usecase;

import com.ct01.migration.application.command.CreateMigrationPlanCommand;
import com.ct01.migration.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case для создания планов миграции
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateMigrationPlanUseCase {
    
    private final MigrationPlanRepository migrationPlanRepository;
    
    @Transactional
    public MigrationPlan execute(CreateMigrationPlanCommand command) {
        log.info("Создание плана миграции: {}", command.getName());
        
        // Проверяем, что нет активных миграций
        if (migrationPlanRepository.hasActiveMigration()) {
            throw new IllegalStateException("Нельзя создать новый план миграции пока есть активные");
        }
        
        // Создаем шаги миграции из команды
        List<MigrationStep> steps = createMigrationSteps(command);
        
        // Создаем план миграции
        MigrationPlan migrationPlan = new MigrationPlan(
            MigrationPlanId.generate(),
            command.getName(),
            command.getDescription(),
            command.getStrategy(),
            steps
        );
        
        // Сохраняем план
        MigrationPlan savedPlan = migrationPlanRepository.save(migrationPlan);
        
        log.info("План миграции создан: {}", savedPlan.getId());
        return savedPlan;
    }
    
    private List<MigrationStep> createMigrationSteps(CreateMigrationPlanCommand command) {
        return command.getStepDefinitions().stream()
                     .map(stepDef -> new MigrationStep(
                         stepDef.getName(),
                         stepDef.getDescription(),
                         stepDef.getOrder(),
                         stepDef.getType(),
                         stepDef.getSourceTable(),
                         stepDef.getTargetTable(),
                         stepDef.getTransformationScript(),
                         stepDef.getValidationRule()
                     ))
                     .toList();
    }
} 