package com.ct01.migration.infrastructure.executor;

import com.ct01.migration.domain.MigrationStep;

import java.util.function.Consumer;

/**
 * Интерфейс для выполнения шагов миграции
 */
public interface MigrationStepExecutor {
    
    /**
     * Оценить количество записей для обработки в шаге
     */
    long estimateRecords(MigrationStep step);
    
    /**
     * Выполнить шаг миграции
     * 
     * @param step шаг миграции
     * @param dryRun тестовый режим (без изменения данных)
     * @param progressCallback callback для отслеживания прогресса
     */
    void execute(MigrationStep step, boolean dryRun, Consumer<Long> progressCallback);
} 