package com.ct01.migration.application.usecase;

import com.ct01.migration.application.command.ExecuteMigrationPlanCommand;
import com.ct01.migration.application.service.MigrationExecutionService;
import com.ct01.migration.application.service.ValidationService;
import com.ct01.migration.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case для выполнения планов миграции
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExecuteMigrationPlanUseCase {
    
    private final MigrationPlanRepository migrationPlanRepository;
    private final MigrationExecutionService executionService;
    private final ValidationService validationService;
    
    @Transactional
    public void execute(ExecuteMigrationPlanCommand command) {
        log.info("Запуск выполнения плана миграции: {}", command.getMigrationPlanId());
        
        // Находим план миграции
        MigrationPlan migrationPlan = migrationPlanRepository.findById(command.getMigrationPlanId())
            .orElseThrow(() -> new IllegalArgumentException("План миграции не найден: " + command.getMigrationPlanId()));
        
        // Проверяем готовность к выполнению
        if (!migrationPlan.isReadyToExecute()) {
            throw new IllegalStateException("План миграции не готов к выполнению: " + migrationPlan.getStatus());
        }
        
        try {
            // Предварительная валидация
            if (command.isPreValidationEnabled()) {
                log.info("Выполняется предварительная валидация плана миграции");
                validationService.validateBeforeExecution(migrationPlan);
            }
            
            // Запускаем план миграции
            migrationPlan.start();
            migrationPlanRepository.save(migrationPlan);
            
            // Выполняем миграцию асинхронно
            executionService.executeAsync(migrationPlan, command.isDryRun());
            
        } catch (Exception e) {
            log.error("Ошибка при запуске миграции: {}", e.getMessage(), e);
            migrationPlan.markAsFailed("Ошибка при запуске: " + e.getMessage());
            migrationPlanRepository.save(migrationPlan);
            throw e;
        }
    }
} 