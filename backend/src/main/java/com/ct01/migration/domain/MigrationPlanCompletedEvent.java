package com.ct01.migration.domain;

import com.ct01.core.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Событие успешного завершения плана миграции
 */
public class MigrationPlanCompletedEvent implements DomainEvent {
    
    private final MigrationPlanId migrationPlanId;
    private final LocalDateTime occurredAt;
    private final MigrationMetrics metrics;
    
    public MigrationPlanCompletedEvent(MigrationPlanId migrationPlanId, LocalDateTime occurredAt, 
                                      MigrationMetrics metrics) {
        this.migrationPlanId = Objects.requireNonNull(migrationPlanId, "ID плана миграции не может быть null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "Время события не может быть null");
        this.metrics = Objects.requireNonNull(metrics, "Метрики не могут быть null");
    }
    
    public MigrationPlanId getMigrationPlanId() {
        return migrationPlanId;
    }
    
    public MigrationMetrics getMetrics() {
        return metrics;
    }
    
    @Override
    public LocalDateTime occurredAt() {
        return occurredAt;
    }
    
    @Override
    public String toString() {
        return String.format("MigrationPlanCompletedEvent{migrationPlanId=%s, occurredAt=%s, metrics=%s}",
                           migrationPlanId, occurredAt, metrics);
    }
} 