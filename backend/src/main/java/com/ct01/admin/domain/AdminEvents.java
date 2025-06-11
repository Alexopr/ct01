package com.ct01.admin.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Файл с остальными доменными событиями Admin Domain
 */

/**
 * AdminActionExecutingEvent - Событие выполнения административной операции
 */
@Getter
@Builder
@EqualsAndHashCode
@ToString
class AdminActionExecutingEvent implements DomainEvent {
    
    private final AdminId adminId;
    private final UserId userId;
    private final AdminAction action;
    private final LocalDateTime occurredAt;
    
    public AdminActionExecutingEvent(AdminId adminId, UserId userId, AdminAction action) {
        this.adminId = adminId;
        this.userId = userId;
        this.action = action;
        this.occurredAt = LocalDateTime.now();
    }
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    @Override
    public String getEventType() {
        return "AdminActionExecuting";
    }
}

/**
 * AdminActionCompletedEvent - Событие успешного завершения административной операции
 */
@Getter
@Builder
@EqualsAndHashCode
@ToString
class AdminActionCompletedEvent implements DomainEvent {
    
    private final AdminId adminId;
    private final UserId userId;
    private final AdminAction action;
    private final Map<String, Object> result;
    private final LocalDateTime completedAt;
    private final LocalDateTime occurredAt;
    
    public AdminActionCompletedEvent(AdminId adminId, UserId userId, AdminAction action, 
                                   Map<String, Object> result, LocalDateTime completedAt) {
        this.adminId = adminId;
        this.userId = userId;
        this.action = action;
        this.result = result;
        this.completedAt = completedAt;
        this.occurredAt = LocalDateTime.now();
    }
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    @Override
    public String getEventType() {
        return "AdminActionCompleted";
    }
}

/**
 * AdminActionRejectedEvent - Событие отклонения административной операции
 */
@Getter
@Builder
@EqualsAndHashCode
@ToString
class AdminActionRejectedEvent implements DomainEvent {
    
    private final AdminId adminId;
    private final UserId userId;
    private final AdminAction action;
    private final String reason;
    private final LocalDateTime completedAt;
    private final LocalDateTime occurredAt;
    
    public AdminActionRejectedEvent(AdminId adminId, UserId userId, AdminAction action, 
                                  String reason, LocalDateTime completedAt) {
        this.adminId = adminId;
        this.userId = userId;
        this.action = action;
        this.reason = reason;
        this.completedAt = completedAt;
        this.occurredAt = LocalDateTime.now();
    }
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    @Override
    public String getEventType() {
        return "AdminActionRejected";
    }
}

/**
 * AdminActionFailedEvent - Событие провала административной операции
 */
@Getter
@Builder
@EqualsAndHashCode
@ToString
class AdminActionFailedEvent implements DomainEvent {
    
    private final AdminId adminId;
    private final UserId userId;
    private final AdminAction action;
    private final String reason;
    private final LocalDateTime completedAt;
    private final LocalDateTime occurredAt;
    
    public AdminActionFailedEvent(AdminId adminId, UserId userId, AdminAction action, 
                                String reason, LocalDateTime completedAt) {
        this.adminId = adminId;
        this.userId = userId;
        this.action = action;
        this.reason = reason;
        this.completedAt = completedAt;
        this.occurredAt = LocalDateTime.now();
    }
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    @Override
    public String getEventType() {
        return "AdminActionFailed";
    }
} 
