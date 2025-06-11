package com.ct01.admin.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * AdminActionStartedEvent - Событие начала административной операции
 * 
 * Публикуется при создании и инициации административной операции
 */
@Getter
@Builder
@EqualsAndHashCode
@ToString
public class AdminActionStartedEvent implements DomainEvent {
    
    private final AdminId adminId;
    private final UserId userId;
    private final AdminAction action;
    private final LocalDateTime executedAt;
    private final LocalDateTime occurredAt;
    
    public AdminActionStartedEvent(AdminId adminId, UserId userId, AdminAction action, LocalDateTime executedAt) {
        this.adminId = adminId;
        this.userId = userId;
        this.action = action;
        this.executedAt = executedAt;
        this.occurredAt = LocalDateTime.now();
    }
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    @Override
    public String getEventType() {
        return "AdminActionStarted";
    }
    
    /**
     * Получить название операции
     */
    public String getOperationName() {
        return action.getFullName();
    }
    
    /**
     * Проверить является ли операция критической
     */
    public boolean isCriticalOperation() {
        return action.isCritical();
    }
    
    /**
     * Получить категорию операции
     */
    public String getCategory() {
        return action.getCategory();
    }
    
    /**
     * Получить требуемый уровень прав
     */
    public AdminPermissionLevel getRequiredPermissionLevel() {
        return action.getRequiredPermissionLevel();
    }
} 
