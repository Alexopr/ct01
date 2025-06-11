package com.ct01.migration.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Метрики выполнения миграции
 */
public class MigrationMetrics {
    
    private final long totalRecords;
    private final long processedRecords;
    private final long successfulRecords;
    private final long failedRecords;
    private final Duration executionTime;
    private final double throughputPerSecond;
    private final long memoryUsedMb;
    private final int errorCount;
    private final LocalDateTime lastUpdated;
    
    public MigrationMetrics() {
        this(0, 0, 0, 0, Duration.ZERO, 0.0, 0, 0, LocalDateTime.now());
    }
    
    public MigrationMetrics(long totalRecords, long processedRecords, long successfulRecords,
                           long failedRecords, Duration executionTime, double throughputPerSecond,
                           long memoryUsedMb, int errorCount, LocalDateTime lastUpdated) {
        this.totalRecords = totalRecords;
        this.processedRecords = processedRecords;
        this.successfulRecords = successfulRecords;
        this.failedRecords = failedRecords;
        this.executionTime = Objects.requireNonNull(executionTime, "Время выполнения не может быть null");
        this.throughputPerSecond = throughputPerSecond;
        this.memoryUsedMb = memoryUsedMb;
        this.errorCount = errorCount;
        this.lastUpdated = Objects.requireNonNull(lastUpdated, "Время обновления не может быть null");
        
        validateMetrics();
    }
    
    /**
     * Обновить метрики с новыми значениями
     */
    public MigrationMetrics update(long processedRecords, long successfulRecords, 
                                  long failedRecords, Duration executionTime,
                                  long memoryUsedMb, int errorCount) {
        double newThroughput = calculateThroughput(processedRecords, executionTime);
        
        return new MigrationMetrics(
            this.totalRecords,
            processedRecords,
            successfulRecords,
            failedRecords,
            executionTime,
            newThroughput,
            memoryUsedMb,
            errorCount,
            LocalDateTime.now()
        );
    }
    
    /**
     * Установить общее количество записей
     */
    public MigrationMetrics withTotalRecords(long totalRecords) {
        return new MigrationMetrics(
            totalRecords,
            this.processedRecords,
            this.successfulRecords,
            this.failedRecords,
            this.executionTime,
            this.throughputPerSecond,
            this.memoryUsedMb,
            this.errorCount,
            LocalDateTime.now()
        );
    }
    
    /**
     * Получить процент выполнения
     */
    public double getProgressPercentage() {
        if (totalRecords == 0) return 0.0;
        return (double) processedRecords / totalRecords * 100.0;
    }
    
    /**
     * Получить процент успешных записей
     */
    public double getSuccessRate() {
        if (processedRecords == 0) return 0.0;
        return (double) successfulRecords / processedRecords * 100.0;
    }
    
    /**
     * Получить процент ошибок
     */
    public double getErrorRate() {
        if (processedRecords == 0) return 0.0;
        return (double) failedRecords / processedRecords * 100.0;
    }
    
    /**
     * Оценить оставшееся время выполнения
     */
    public Duration getEstimatedTimeRemaining() {
        if (processedRecords == 0 || throughputPerSecond <= 0) {
            return Duration.ZERO;
        }
        
        long remainingRecords = totalRecords - processedRecords;
        long remainingSeconds = (long) (remainingRecords / throughputPerSecond);
        
        return Duration.ofSeconds(remainingSeconds);
    }
    
    /**
     * Проверить критические показатели
     */
    public boolean hasCriticalIssues() {
        return getErrorRate() > 5.0 || // Более 5% ошибок
               memoryUsedMb > 1024 || // Более 1GB памяти
               errorCount > 100; // Более 100 ошибок
    }
    
    private double calculateThroughput(long records, Duration time) {
        if (time.isZero() || time.isNegative()) return 0.0;
        
        double seconds = time.toMillis() / 1000.0;
        return records / seconds;
    }
    
    private void validateMetrics() {
        if (totalRecords < 0) {
            throw new IllegalArgumentException("Общее количество записей не может быть отрицательным");
        }
        if (processedRecords < 0) {
            throw new IllegalArgumentException("Количество обработанных записей не может быть отрицательным");
        }
        if (successfulRecords < 0) {
            throw new IllegalArgumentException("Количество успешных записей не может быть отрицательным");
        }
        if (failedRecords < 0) {
            throw new IllegalArgumentException("Количество неудачных записей не может быть отрицательным");
        }
        if (processedRecords > totalRecords) {
            throw new IllegalArgumentException("Обработанных записей не может быть больше общего количества");
        }
        if (successfulRecords + failedRecords != processedRecords) {
            throw new IllegalArgumentException("Сумма успешных и неудачных записей должна равняться обработанным");
        }
        if (throughputPerSecond < 0) {
            throw new IllegalArgumentException("Пропускная способность не может быть отрицательной");
        }
        if (memoryUsedMb < 0) {
            throw new IllegalArgumentException("Использованная память не может быть отрицательной");
        }
        if (errorCount < 0) {
            throw new IllegalArgumentException("Количество ошибок не может быть отрицательным");
        }
    }
    
    // Getters
    public long getTotalRecords() { return totalRecords; }
    public long getProcessedRecords() { return processedRecords; }
    public long getSuccessfulRecords() { return successfulRecords; }
    public long getFailedRecords() { return failedRecords; }
    public Duration getExecutionTime() { return executionTime; }
    public double getThroughputPerSecond() { return throughputPerSecond; }
    public long getMemoryUsedMb() { return memoryUsedMb; }
    public int getErrorCount() { return errorCount; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MigrationMetrics that = (MigrationMetrics) o;
        return totalRecords == that.totalRecords &&
               processedRecords == that.processedRecords &&
               successfulRecords == that.successfulRecords &&
               failedRecords == that.failedRecords &&
               Objects.equals(executionTime, that.executionTime) &&
               Objects.equals(lastUpdated, that.lastUpdated);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalRecords, processedRecords, successfulRecords, 
                          failedRecords, executionTime, lastUpdated);
    }
    
    @Override
    public String toString() {
        return String.format("MigrationMetrics{processed=%d/%d (%.1f%%), success=%.1f%%, errors=%d, throughput=%.1f rec/sec}",
                           processedRecords, totalRecords, getProgressPercentage(),
                           getSuccessRate(), errorCount, throughputPerSecond);
    }
} 