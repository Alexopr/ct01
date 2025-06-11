package com.ct01.migration.infrastructure.executor;

import com.ct01.migration.domain.MigrationStep;
import com.ct01.migration.domain.MigrationStepType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Реализация по умолчанию для выполнения шагов миграции
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultMigrationStepExecutor implements MigrationStepExecutor {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public long estimateRecords(MigrationStep step) {
        log.debug("Оценка количества записей для шага: {}", step.getName());
        
        try {
            switch (step.getType()) {
                case DATA_MIGRATION -> {
                    if (step.getSourceTable() != null) {
                        return countRecordsInTable(step.getSourceTable());
                    }
                    return 0;
                }
                case VALIDATION -> {
                    if (step.getValidationRule() != null && step.getValidationRule().getValidationQuery() != null) {
                        return 1; // Валидация считается как одна операция
                    }
                    return 0;
                }
                case SCHEMA_UPDATE, INDEX_CREATION, CONFIG_UPDATE -> {
                    return 1; // DDL операции считаются как одна операция
                }
                case CLEANUP -> {
                    if (step.getTargetTable() != null) {
                        return countRecordsInTable(step.getTargetTable());
                    }
                    return 0;
                }
                default -> {
                    log.warn("Неизвестный тип шага: {}", step.getType());
                    return 0;
                }
            }
        } catch (Exception e) {
            log.warn("Ошибка при оценке количества записей для шага {}: {}", step.getName(), e.getMessage());
            return 0;
        }
    }
    
    @Override
    public void execute(MigrationStep step, boolean dryRun, Consumer<Long> progressCallback) {
        log.info("Выполнение шага: {} (режим: {})", step.getName(), dryRun ? "тестовый" : "реальный");
        
        try {
            switch (step.getType()) {
                case DATA_MIGRATION -> executeDataMigration(step, dryRun, progressCallback);
                case SCHEMA_UPDATE -> executeSchemaUpdate(step, dryRun, progressCallback);
                case VALIDATION -> executeValidation(step, dryRun, progressCallback);
                case CLEANUP -> executeCleanup(step, dryRun, progressCallback);
                case INDEX_CREATION -> executeIndexCreation(step, dryRun, progressCallback);
                case CONFIG_UPDATE -> executeConfigUpdate(step, dryRun, progressCallback);
                default -> throw new IllegalArgumentException("Неподдерживаемый тип шага: " + step.getType());
            }
        } catch (Exception e) {
            log.error("Ошибка при выполнении шага {}: {}", step.getName(), e.getMessage(), e);
            throw e;
        }
    }
    
    private void executeDataMigration(MigrationStep step, boolean dryRun, Consumer<Long> progressCallback) {
        log.info("Выполнение миграции данных из {} в {}", step.getSourceTable(), step.getTargetTable());
        
        if (step.getTransformationScript() == null || step.getTransformationScript().trim().isEmpty()) {
            throw new IllegalArgumentException("Скрипт трансформации не задан для миграции данных");
        }
        
        // В тестовом режиме просто симулируем выполнение
        if (dryRun) {
            simulateExecution(step, progressCallback);
            return;
        }
        
        // Выполняем скрипт трансформации
        String transformationScript = step.getTransformationScript();
        
        // Если скрипт содержит множественные операции, разбиваем по порциям
        if (step.getSourceTable() != null) {
            executeBatchMigration(step, transformationScript, progressCallback);
        } else {
            // Простое выполнение SQL
            jdbcTemplate.execute(transformationScript);
            progressCallback.accept(1L);
        }
    }
    
    private void executeSchemaUpdate(MigrationStep step, boolean dryRun, Consumer<Long> progressCallback) {
        log.info("Выполнение обновления схемы: {}", step.getName());
        
        if (dryRun) {
            log.info("Тестовый режим: пропуск обновления схемы");
            progressCallback.accept(1L);
            return;
        }
        
        jdbcTemplate.execute(step.getTransformationScript());
        progressCallback.accept(1L);
    }
    
    private void executeValidation(MigrationStep step, boolean dryRun, Consumer<Long> progressCallback) {
        log.info("Выполнение валидации: {}", step.getName());
        
        if (step.getValidationRule() == null) {
            throw new IllegalArgumentException("Правило валидации не задано");
        }
        
        Object result = jdbcTemplate.queryForObject(step.getValidationRule().getValidationQuery(), Object.class);
        String actualResult = result != null ? result.toString() : "null";
        
        if (!step.getValidationRule().getExpectedResult().equals(actualResult)) {
            throw new RuntimeException(String.format(
                "Валидация не пройдена. Ожидалось: %s, получено: %s",
                step.getValidationRule().getExpectedResult(), actualResult
            ));
        }
        
        progressCallback.accept(1L);
    }
    
    private void executeCleanup(MigrationStep step, boolean dryRun, Consumer<Long> progressCallback) {
        log.info("Выполнение очистки таблицы: {}", step.getTargetTable());
        
        if (step.getTargetTable() == null) {
            throw new IllegalArgumentException("Целевая таблица для очистки не указана");
        }
        
        if (dryRun) {
            long recordCount = countRecordsInTable(step.getTargetTable());
            log.info("Тестовый режим: будет удалено {} записей из {}", recordCount, step.getTargetTable());
            progressCallback.accept(recordCount);
            return;
        }
        
        String cleanupSql = "DELETE FROM " + step.getTargetTable();
        int deletedCount = jdbcTemplate.update(cleanupSql);
        progressCallback.accept((long) deletedCount);
    }
    
    private void executeIndexCreation(MigrationStep step, boolean dryRun, Consumer<Long> progressCallback) {
        log.info("Выполнение создания индекса: {}", step.getName());
        
        if (dryRun) {
            log.info("Тестовый режим: пропуск создания индекса");
            progressCallback.accept(1L);
            return;
        }
        
        jdbcTemplate.execute(step.getTransformationScript());
        progressCallback.accept(1L);
    }
    
    private void executeConfigUpdate(MigrationStep step, boolean dryRun, Consumer<Long> progressCallback) {
        log.info("Выполнение обновления конфигурации: {}", step.getName());
        
        if (dryRun) {
            log.info("Тестовый режим: пропуск обновления конфигурации");
            progressCallback.accept(1L);
            return;
        }
        
        jdbcTemplate.execute(step.getTransformationScript());
        progressCallback.accept(1L);
    }
    
    private void executeBatchMigration(MigrationStep step, String transformationScript, Consumer<Long> progressCallback) {
        // Выполняем миграцию порциями для больших таблиц
        final int BATCH_SIZE = 1000;
        long totalRecords = step.getTotalRecords();
        long processedRecords = 0;
        
        // Простая реализация - в реальности нужно более сложное разбиение на batch
        while (processedRecords < totalRecords) {
            long remainingRecords = Math.min(BATCH_SIZE, totalRecords - processedRecords);
            
            // Выполняем batch операцию
            // В реальной реализации здесь должна быть логика разбиения SQL на порции
            jdbcTemplate.execute(transformationScript);
            
            processedRecords += remainingRecords;
            progressCallback.accept(processedRecords);
            
            // Имитируем задержку для демонстрации прогресса
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void simulateExecution(MigrationStep step, Consumer<Long> progressCallback) {
        // Симуляция выполнения для тестового режима
        long totalRecords = step.getTotalRecords();
        long processedRecords = 0;
        final int SIMULATION_STEPS = 10;
        
        long recordsPerStep = Math.max(1, totalRecords / SIMULATION_STEPS);
        
        for (int i = 0; i < SIMULATION_STEPS && processedRecords < totalRecords; i++) {
            processedRecords = Math.min(processedRecords + recordsPerStep, totalRecords);
            progressCallback.accept(processedRecords);
            
            try {
                Thread.sleep(50); // Симуляция времени обработки
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private long countRecordsInTable(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM " + tableName;
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.warn("Ошибка при подсчете записей в таблице {}: {}", tableName, e.getMessage());
            return 0;
        }
    }
} 