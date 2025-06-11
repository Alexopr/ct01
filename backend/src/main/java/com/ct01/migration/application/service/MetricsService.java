package com.ct01.migration.application.service;

import com.ct01.migration.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Сервис для расчета и отслеживания метрик миграции
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {
    
    /**
     * Рассчитать актуальные метрики для плана миграции
     */
    public MigrationMetrics calculateMetrics(MigrationPlan migrationPlan) {
        log.debug("Расчет метрик для плана миграции: {}", migrationPlan.getId());
        
        long totalRecords = 0;
        long processedRecords = 0;
        long successfulRecords = 0;
        long failedRecords = 0;
        int errorCount = 0;
        Duration totalExecutionTime = Duration.ZERO;
        
        // Агрегируем метрики по всем шагам
        for (MigrationStep step : migrationPlan.getSteps()) {
            totalRecords += step.getTotalRecords();
            processedRecords += step.getProcessedRecords();
            
            if (step.getStatus() == MigrationStepStatus.COMPLETED) {
                successfulRecords += step.getProcessedRecords();
            } else if (step.getStatus() == MigrationStepStatus.FAILED) {
                failedRecords += step.getProcessedRecords();
                errorCount++;
            }
            
            // Рассчитываем время выполнения шага
            if (step.getStartedAt() != null && step.getCompletedAt() != null) {
                Duration stepDuration = Duration.between(step.getStartedAt(), step.getCompletedAt());
                totalExecutionTime = totalExecutionTime.plus(stepDuration);
            }
        }
        
        // Рассчитываем пропускную способность
        double throughputPerSecond = calculateThroughput(processedRecords, totalExecutionTime);
        
        // Оценка использования памяти (упрощенная)
        long memoryUsedMb = estimateMemoryUsage(migrationPlan);
        
        return new MigrationMetrics(
            totalRecords,
            processedRecords,
            successfulRecords,
            failedRecords,
            totalExecutionTime,
            throughputPerSecond,
            memoryUsedMb,
            errorCount,
            LocalDateTime.now()
        );
    }
    
    /**
     * Рассчитать метрики производительности для шага
     */
    public StepMetrics calculateStepMetrics(MigrationStep step) {
        log.debug("Расчет метрик для шага: {}", step.getName());
        
        Duration executionTime = Duration.ZERO;
        if (step.getStartedAt() != null && step.getCompletedAt() != null) {
            executionTime = Duration.between(step.getStartedAt(), step.getCompletedAt());
        }
        
        double throughput = calculateThroughput(step.getProcessedRecords(), executionTime);
        double progressPercentage = calculateProgressPercentage(step);
        
        return new StepMetrics(
            step.getName(),
            step.getProcessedRecords(),
            step.getTotalRecords(),
            executionTime,
            throughput,
            progressPercentage,
            step.getStatus()
        );
    }
    
    /**
     * Получить сводную статистику выполнения
     */
    public ExecutionSummary getExecutionSummary(MigrationPlan migrationPlan) {
        MigrationMetrics metrics = migrationPlan.getMetrics();
        
        int completedSteps = (int) migrationPlan.getSteps().stream()
                                                 .filter(step -> step.getStatus() == MigrationStepStatus.COMPLETED)
                                                 .count();
        
        int failedSteps = (int) migrationPlan.getSteps().stream()
                                            .filter(step -> step.getStatus() == MigrationStepStatus.FAILED)
                                            .count();
        
        int totalSteps = migrationPlan.getSteps().size();
        double completionPercentage = totalSteps > 0 ? (double) completedSteps / totalSteps * 100 : 0;
        
        return new ExecutionSummary(
            migrationPlan.getId(),
            migrationPlan.getStatus(),
            totalSteps,
            completedSteps,
            failedSteps,
            completionPercentage,
            metrics.getTotalRecords(),
            metrics.getProcessedRecords(),
            metrics.getSuccessfulRecords(),
            metrics.getFailedRecords(),
            metrics.getExecutionTime(),
            metrics.getThroughputPerSecond()
        );
    }
    
    private double calculateThroughput(long processedRecords, Duration executionTime) {
        if (executionTime.isZero() || executionTime.isNegative()) {
            return 0.0;
        }
        
        double seconds = executionTime.toMillis() / 1000.0;
        return seconds > 0 ? processedRecords / seconds : 0.0;
    }
    
    private double calculateProgressPercentage(MigrationStep step) {
        if (step.getTotalRecords() == 0) {
            return step.getStatus() == MigrationStepStatus.COMPLETED ? 100.0 : 0.0;
        }
        
        return (double) step.getProcessedRecords() / step.getTotalRecords() * 100.0;
    }
    
    private long estimateMemoryUsage(MigrationPlan migrationPlan) {
        // Упрощенная оценка использования памяти
        // В реальной реализации можно использовать MemoryMXBean или профилирование
        long baseMemory = 50; // Базовое потребление в MB
        long recordMemory = migrationPlan.getSteps().stream()
                                        .mapToLong(step -> step.getTotalRecords() / 10000) // 1MB на 10k записей
                                        .sum();
        
        return baseMemory + recordMemory;
    }
    
    /**
     * Метрики шага миграции
     */
    public static class StepMetrics {
        private final String stepName;
        private final long processedRecords;
        private final long totalRecords;
        private final Duration executionTime;
        private final double throughputPerSecond;
        private final double progressPercentage;
        private final MigrationStepStatus status;
        
        public StepMetrics(String stepName, long processedRecords, long totalRecords,
                          Duration executionTime, double throughputPerSecond, 
                          double progressPercentage, MigrationStepStatus status) {
            this.stepName = stepName;
            this.processedRecords = processedRecords;
            this.totalRecords = totalRecords;
            this.executionTime = executionTime;
            this.throughputPerSecond = throughputPerSecond;
            this.progressPercentage = progressPercentage;
            this.status = status;
        }
        
        // Getters
        public String getStepName() { return stepName; }
        public long getProcessedRecords() { return processedRecords; }
        public long getTotalRecords() { return totalRecords; }
        public Duration getExecutionTime() { return executionTime; }
        public double getThroughputPerSecond() { return throughputPerSecond; }
        public double getProgressPercentage() { return progressPercentage; }
        public MigrationStepStatus getStatus() { return status; }
    }
    
    /**
     * Сводка выполнения миграции
     */
    public static class ExecutionSummary {
        private final MigrationPlanId planId;
        private final MigrationStatus planStatus;
        private final int totalSteps;
        private final int completedSteps;
        private final int failedSteps;
        private final double completionPercentage;
        private final long totalRecords;
        private final long processedRecords;
        private final long successfulRecords;
        private final long failedRecords;
        private final Duration totalExecutionTime;
        private final double averageThroughput;
        
        public ExecutionSummary(MigrationPlanId planId, MigrationStatus planStatus,
                               int totalSteps, int completedSteps, int failedSteps,
                               double completionPercentage, long totalRecords, long processedRecords,
                               long successfulRecords, long failedRecords, Duration totalExecutionTime,
                               double averageThroughput) {
            this.planId = planId;
            this.planStatus = planStatus;
            this.totalSteps = totalSteps;
            this.completedSteps = completedSteps;
            this.failedSteps = failedSteps;
            this.completionPercentage = completionPercentage;
            this.totalRecords = totalRecords;
            this.processedRecords = processedRecords;
            this.successfulRecords = successfulRecords;
            this.failedRecords = failedRecords;
            this.totalExecutionTime = totalExecutionTime;
            this.averageThroughput = averageThroughput;
        }
        
        // Getters
        public MigrationPlanId getPlanId() { return planId; }
        public MigrationStatus getPlanStatus() { return planStatus; }
        public int getTotalSteps() { return totalSteps; }
        public int getCompletedSteps() { return completedSteps; }
        public int getFailedSteps() { return failedSteps; }
        public double getCompletionPercentage() { return completionPercentage; }
        public long getTotalRecords() { return totalRecords; }
        public long getProcessedRecords() { return processedRecords; }
        public long getSuccessfulRecords() { return successfulRecords; }
        public long getFailedRecords() { return failedRecords; }
        public Duration getTotalExecutionTime() { return totalExecutionTime; }
        public double getAverageThroughput() { return averageThroughput; }
    }
} 