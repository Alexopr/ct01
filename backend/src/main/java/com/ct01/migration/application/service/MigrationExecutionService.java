package com.ct01.migration.application.service;

import com.ct01.migration.domain.*;
import com.ct01.migration.infrastructure.executor.MigrationStepExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Сервис выполнения миграций
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationExecutionService {
    
    private final MigrationPlanRepository migrationPlanRepository;
    private final MigrationStepExecutor stepExecutor;
    private final MetricsService metricsService;
    
    @Async
    @Transactional
    public void executeAsync(MigrationPlan migrationPlan, boolean dryRun) {
        log.info("Начало {} выполнения плана миграции: {}", 
                dryRun ? "тестового" : "реального", migrationPlan.getId());
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // Выполняем каждый шаг по порядку
            for (MigrationStep step : migrationPlan.getSteps()) {
                executeStep(migrationPlan, step, dryRun);
                
                // Обновляем метрики плана
                updatePlanMetrics(migrationPlan);
                
                // Сохраняем прогресс
                migrationPlanRepository.save(migrationPlan);
                
                // Проверяем критические проблемы
                if (migrationPlan.getMetrics().hasCriticalIssues()) {
                    log.warn("Обнаружены критические проблемы в плане миграции: {}", migrationPlan.getId());
                    if (!shouldContinueOnCriticalIssues(migrationPlan)) {
                        throw new RuntimeException("Выполнение остановлено из-за критических проблем");
                    }
                }
            }
            
            // Завершаем план миграции
            migrationPlan.complete();
            migrationPlanRepository.save(migrationPlan);
            
            Duration totalTime = Duration.between(startTime, LocalDateTime.now());
            log.info("План миграции {} успешно завершен за {}", migrationPlan.getId(), totalTime);
            
        } catch (Exception e) {
            log.error("Ошибка при выполнении плана миграции {}: {}", migrationPlan.getId(), e.getMessage(), e);
            migrationPlan.markAsFailed("Ошибка выполнения: " + e.getMessage());
            migrationPlanRepository.save(migrationPlan);
            throw e;
        }
    }
    
    private void executeStep(MigrationPlan migrationPlan, MigrationStep step, boolean dryRun) {
        log.info("Выполнение шага {}: {}", step.getOrder(), step.getName());
        
        try {
            // Получаем количество записей для обработки
            long totalRecords = stepExecutor.estimateRecords(step);
            step.start(totalRecords);
            
            // Выполняем шаг
            stepExecutor.execute(step, dryRun, (processedRecords) -> {
                step.updateProgress(processedRecords);
                // Периодически сохраняем прогресс
                if (processedRecords % 1000 == 0) {
                    migrationPlanRepository.save(migrationPlan);
                }
            });
            
            step.complete();
            log.info("Шаг {} завершен успешно", step.getName());
            
        } catch (Exception e) {
            log.error("Ошибка при выполнении шага {}: {}", step.getName(), e.getMessage(), e);
            step.markAsFailed("Ошибка выполнения: " + e.getMessage());
            throw e;
        }
    }
    
    private void updatePlanMetrics(MigrationPlan migrationPlan) {
        MigrationMetrics currentMetrics = migrationPlan.getMetrics();
        MigrationMetrics updatedMetrics = metricsService.calculateMetrics(migrationPlan);
        migrationPlan.updateMetrics(updatedMetrics);
    }
    
    private boolean shouldContinueOnCriticalIssues(MigrationPlan migrationPlan) {
        // В реальной реализации здесь может быть более сложная логика
        // основанная на конфигурации плана или пользовательских настройках
        return false;
    }
} 