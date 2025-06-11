package com.ct01.migration.domain;

import com.ct01.core.domain.AggregateRoot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * План миграции данных между legacy и новой схемой
 */
public class MigrationPlan extends AggregateRoot<MigrationPlanId> {
    
    private final MigrationPlanId id;
    private final String name;
    private final String description;
    private final MigrationStrategy strategy;
    private final List<MigrationStep> steps;
    private MigrationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    private MigrationMetrics metrics;
    
    public MigrationPlan(MigrationPlanId id, String name, String description, 
                        MigrationStrategy strategy, List<MigrationStep> steps) {
        this.id = Objects.requireNonNull(id, "Migration plan ID не может быть null");
        this.name = Objects.requireNonNull(name, "Название плана не может быть null");
        this.description = Objects.requireNonNull(description, "Описание плана не может быть null");
        this.strategy = Objects.requireNonNull(strategy, "Стратегия миграции не может быть null");
        this.steps = Objects.requireNonNull(steps, "Шаги миграции не могут быть null");
        this.status = MigrationStatus.PLANNED;
        this.createdAt = LocalDateTime.now();
        this.metrics = new MigrationMetrics();
        
        validateSteps();
    }
    
    /**
     * Начать выполнение плана миграции
     */
    public void start() {
        if (status != MigrationStatus.PLANNED) {
            throw new IllegalStateException("План миграции не в статусе PLANNED");
        }
        
        this.status = MigrationStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
        
        recordEvent(new MigrationPlanStartedEvent(this.id, LocalDateTime.now()));
    }
    
    /**
     * Завершить выполнение плана миграции
     */
    public void complete() {
        if (status != MigrationStatus.RUNNING) {
            throw new IllegalStateException("План миграции не в статусе RUNNING");
        }
        
        this.status = MigrationStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        
        recordEvent(new MigrationPlanCompletedEvent(this.id, LocalDateTime.now(), metrics));
    }
    
    /**
     * Отметить план как неудачный
     */
    public void markAsFailed(String errorMessage) {
        this.status = MigrationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
        
        recordEvent(new MigrationPlanFailedEvent(this.id, LocalDateTime.now(), errorMessage));
    }
    
    /**
     * Откатить миграцию
     */
    public void rollback() {
        if (status != MigrationStatus.COMPLETED && status != MigrationStatus.FAILED) {
            throw new IllegalStateException("Откат возможен только для завершенных или неудачных миграций");
        }
        
        this.status = MigrationStatus.ROLLBACK;
        
        recordEvent(new MigrationPlanRollbackStartedEvent(this.id, LocalDateTime.now()));
    }
    
    /**
     * Обновить метрики миграции
     */
    public void updateMetrics(MigrationMetrics newMetrics) {
        this.metrics = Objects.requireNonNull(newMetrics, "Метрики не могут быть null");
    }
    
    /**
     * Проверить готовность к выполнению
     */
    public boolean isReadyToExecute() {
        return status == MigrationStatus.PLANNED && 
               !steps.isEmpty() && 
               steps.stream().allMatch(MigrationStep::isValid);
    }
    
    /**
     * Получить следующий шаг для выполнения
     */
    public MigrationStep getNextStep() {
        return steps.stream()
                   .filter(step -> step.getStatus() == MigrationStepStatus.PENDING)
                   .findFirst()
                   .orElse(null);
    }
    
    /**
     * Получить процент выполнения
     */
    public double getProgressPercentage() {
        if (steps.isEmpty()) return 0.0;
        
        long completedSteps = steps.stream()
                                  .mapToLong(step -> step.getStatus() == MigrationStepStatus.COMPLETED ? 1 : 0)
                                  .sum();
        
        return (double) completedSteps / steps.size() * 100.0;
    }
    
    private void validateSteps() {
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("План миграции должен содержать хотя бы один шаг");
        }
        
        // Проверка уникальности порядковых номеров
        long uniqueOrders = steps.stream()
                                 .mapToInt(MigrationStep::getOrder)
                                 .distinct()
                                 .count();
        
        if (uniqueOrders != steps.size()) {
            throw new IllegalArgumentException("Порядковые номера шагов должны быть уникальными");
        }
    }
    
    @Override
    public MigrationPlanId getId() {
        return id;
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public MigrationStrategy getStrategy() { return strategy; }
    public List<MigrationStep> getSteps() { return List.copyOf(steps); }
    public MigrationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getErrorMessage() { return errorMessage; }
    public MigrationMetrics getMetrics() { return metrics; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MigrationPlan that = (MigrationPlan) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("MigrationPlan{id=%s, name='%s', status=%s, progress=%.1f%%}",
                           id, name, status, getProgressPercentage());
    }
} 