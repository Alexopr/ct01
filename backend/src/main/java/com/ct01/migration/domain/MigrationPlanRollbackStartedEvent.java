package com.ct01.migration.domain;

import com.ct01.core.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Событие начала отката плана миграции
 */
public class MigrationPlanRollbackStartedEvent implements DomainEvent {
    
    private final MigrationPlanId migrationPlanId;
    private final LocalDateTime occurredAt;
    
    public MigrationPlanRollbackStartedEvent(MigrationPlanId migrationPlanId, LocalDateTime occurredAt) {
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
        return String.format("MigrationPlanRollbackStartedEvent{migrationPlanId=%s, occurredAt=%s}",
                           migrationPlanId, occurredAt);
    }
} 