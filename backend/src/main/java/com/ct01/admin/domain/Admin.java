package com.ct01.admin.domain;

import com.ct01.core.domain.BaseAggregateRoot;
import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Admin - Агрегатный корень для административных операций
 * 
 * Представляет административную сессию или операцию,
 * инкапсулирует бизнес-логику административных действий
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends BaseAggregateRoot<AdminId> {
    
    private AdminId id;
    private UserId userId;
    private AdminAction action;
    private Map<String, Object> actionData;
    private AdminPermissionLevel permissionLevel;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime executedAt;
    private AdminStatus status;
    private String reason;
    private Map<String, Object> result;
    private LocalDateTime completedAt;
    
    @Builder
    private Admin(AdminId id, UserId userId, AdminAction action, Map<String, Object> actionData,
                 AdminPermissionLevel permissionLevel, String ipAddress, String userAgent) {
        super(id);
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.actionData = actionData;
        this.permissionLevel = permissionLevel;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.executedAt = LocalDateTime.now();
        this.status = AdminStatus.PENDING;
        
        // Валидация
        validateAdmin();
        
        // Доменное событие
        addDomainEvent(AdminActionStartedEvent.builder()
            .adminId(this.id)
            .userId(this.userId)
            .action(this.action)
            .executedAt(this.executedAt)
            .build());
    }
    
    @Override
    public AdminId getId() {
        return id;
    }
    
    /**
     * Выполнить административное действие
     */
    public void execute() {
        if (status != AdminStatus.PENDING) {
            throw new AdminException("Admin action can only be executed when PENDING");
        }
        
        validatePermissions();
        
        this.status = AdminStatus.EXECUTING;
        
        addDomainEvent(AdminActionExecutingEvent.builder()
            .adminId(this.id)
            .userId(this.userId)
            .action(this.action)
            .build());
    }
    
    /**
     * Завершить административное действие успешно
     */
    public void complete(Map<String, Object> result) {
        if (status != AdminStatus.EXECUTING) {
            throw new AdminException("Admin action can only be completed when EXECUTING");
        }
        
        this.status = AdminStatus.COMPLETED;
        this.result = result;
        this.completedAt = LocalDateTime.now();
        
        addDomainEvent(AdminActionCompletedEvent.builder()
            .adminId(this.id)
            .userId(this.userId)
            .action(this.action)
            .result(this.result)
            .completedAt(this.completedAt)
            .build());
    }
    
    /**
     * Отклонить административное действие
     */
    public void reject(String reason) {
        if (status == AdminStatus.COMPLETED || status == AdminStatus.REJECTED) {
            throw new AdminException("Cannot reject completed or already rejected action");
        }
        
        this.status = AdminStatus.REJECTED;
        this.reason = reason;
        this.completedAt = LocalDateTime.now();
        
        addDomainEvent(AdminActionRejectedEvent.builder()
            .adminId(this.id)
            .userId(this.userId)
            .action(this.action)
            .reason(this.reason)
            .completedAt(this.completedAt)
            .build());
    }
    
    /**
     * Провалить административное действие из-за ошибки
     */
    public void fail(String reason) {
        if (status == AdminStatus.COMPLETED) {
            throw new AdminException("Cannot fail completed action");
        }
        
        this.status = AdminStatus.FAILED;
        this.reason = reason;
        this.completedAt = LocalDateTime.now();
        
        addDomainEvent(AdminActionFailedEvent.builder()
            .adminId(this.id)
            .userId(this.userId)
            .action(this.action)
            .reason(this.reason)
            .completedAt(this.completedAt)
            .build());
    }
    
    /**
     * Проверить права доступа для выполнения действия
     */
    public boolean canExecute() {
        return status == AdminStatus.PENDING && 
               action.getRequiredPermissionLevel().ordinal() <= permissionLevel.ordinal();
    }
    
    /**
     * Проверить является ли операция критической
     */
    public boolean isCriticalOperation() {
        return action.isCritical();
    }
    
    /**
     * Получить продолжительность выполнения операции
     */
    public long getExecutionDurationSeconds() {
        if (completedAt == null) {
            return 0;
        }
        return java.time.Duration.between(executedAt, completedAt).getSeconds();
    }
    
    /**
     * Проверить успешность операции
     */
    public boolean isSuccessful() {
        return status == AdminStatus.COMPLETED;
    }
    
    /**
     * Проверить завершенность операции
     */
    public boolean isFinished() {
        return status == AdminStatus.COMPLETED || 
               status == AdminStatus.REJECTED || 
               status == AdminStatus.FAILED;
    }
    
    private void validateAdmin() {
        Objects.requireNonNull(id, "Admin ID cannot be null");
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(action, "Admin action cannot be null");
        Objects.requireNonNull(permissionLevel, "Permission level cannot be null");
        
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            throw new AdminException("IP address is required for admin operations");
        }
    }
    
    private void validatePermissions() {
        if (!canExecute()) {
            throw new AdminException(
                String.format("Insufficient permissions. Required: %s, Actual: %s", 
                    action.getRequiredPermissionLevel(), permissionLevel)
            );
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Admin admin = (Admin) o;
        return Objects.equals(id, admin.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Admin{id=%s, action=%s, status=%s, user=%s}", 
            id, action, status, userId);
    }
} 
