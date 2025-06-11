package com.ct01.migration.domain;

import com.ct01.core.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Событие неудачного завершения плана миграции
 */
public class MigrationPlanFailedEvent implements DomainEvent {
    
    private final MigrationPlanId migrationPlanId;
    private final LocalDateTime occurredAt;
    private final String errorMessage;
    
    public MigrationPlanFailedEvent(MigrationPlanId migrationPlanId, LocalDateTime occurredAt, 
                                   String errorMessage) {
        this.migrationPlanId = Objects.requireNonNull(migrationPlanId, "ID плана миграции не может быть null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "Время события не может быть null");
        this.errorMessage = Objects.requireNonNull(errorMessage, "Сообщение об ошибке не может быть null");
    }
    
    public MigrationPlanId getMigrationPlanId() {
        return migrationPlanId;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public LocalDateTime occurredAt() {
        return occurredAt;
    }
    
    @Override
    public String toString() {
        return String.format("MigrationPlanFailedEvent{migrationPlanId=%s, occurredAt=%s, error='%s'}",
                           migrationPlanId, occurredAt, errorMessage);
    }
} 