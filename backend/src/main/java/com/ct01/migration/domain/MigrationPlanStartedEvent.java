package com.ct01.migration.domain;

import com.ct01.core.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Событие начала выполнения плана миграции
 */
public class MigrationPlanStartedEvent implements DomainEvent {
    
    private final MigrationPlanId migrationPlanId;
    private final LocalDateTime occurredAt;
    
    public MigrationPlanStartedEvent(MigrationPlanId migrationPlanId, LocalDateTime occurredAt) {
        this.migrationPlanId = Objects.requireNonNull(migrationPlanId, "ID плана миграции не может быть null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "Время события не может быть null");
    }
    
    public MigrationPlanId getMigrationPlanId() {
        return migrationPlanId;
    }
    
    @Override
    public LocalDateTime occurredAt() {
        return occurredAt;
    }
    
    @Override
    public String toString() {
        return String.format("MigrationPlanStartedEvent{migrationPlanId=%s, occurredAt=%s}",
                           migrationPlanId, occurredAt);
    }
} 