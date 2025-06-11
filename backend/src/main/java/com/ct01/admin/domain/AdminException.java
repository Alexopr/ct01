package com.ct01.admin.domain;

import com.ct01.core.domain.DomainException;

/**
 * AdminException - Доменное исключение для Admin Domain
 * 
 * Представляет ошибки бизнес-логики в административном домене
 */
public class AdminException extends DomainException {
    
    public AdminException(String message) {
        super(message);
    }
    
    public AdminException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AdminException(String message, String errorCode) {
        super(message, errorCode);
    }
    
    public AdminException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
    
    // Специфические типы ошибок админского домена
    
    /**
     * Недостаточные права доступа
     */
    public static AdminException insufficientPermissions(String required, String actual) {
        return new AdminException(
            String.format("Insufficient permissions. Required: %s, Actual: %s", required, actual),
            "ADMIN_INSUFFICIENT_PERMISSIONS"
        );
    }
    
    /**
     * Недопустимая операция
     */
    public static AdminException invalidOperation(String operation, String reason) {
        return new AdminException(
            String.format("Invalid operation '%s': %s", operation, reason),
            "ADMIN_INVALID_OPERATION"
        );
    }
    
    /**
     * Недопустимый переход статуса
     */
    public static AdminException invalidStatusTransition(String from, String to) {
        return new AdminException(
            String.format("Invalid status transition from %s to %s", from, to),
            "ADMIN_INVALID_STATUS_TRANSITION"
        );
    }
    
    /**
     * Операция уже выполнена
     */
    public static AdminException operationAlreadyCompleted(String operationId) {
        return new AdminException(
            String.format("Operation %s is already completed", operationId),
            "ADMIN_OPERATION_ALREADY_COMPLETED"
        );
    }
    
    /**
     * Операция не найдена
     */
    public static AdminException operationNotFound(String operationId) {
        return new AdminException(
            String.format("Admin operation not found: %s", operationId),
            "ADMIN_OPERATION_NOT_FOUND"
        );
    }
    
    /**
     * Критическая операция требует подтверждения
     */
    public static AdminException criticalOperationRequiresConfirmation(String operation) {
        return new AdminException(
            String.format("Critical operation '%s' requires explicit confirmation", operation),
            "ADMIN_CRITICAL_OPERATION_REQUIRES_CONFIRMATION"
        );
    }
    
    /**
     * Недопустимые параметры операции
     */
    public static AdminException invalidOperationParameters(String operation, String missingParams) {
        return new AdminException(
            String.format("Invalid parameters for operation '%s'. Missing: %s", operation, missingParams),
            "ADMIN_INVALID_OPERATION_PARAMETERS"
        );
    }
    
    /**
     * Операция заблокирована системой безопасности
     */
    public static AdminException operationBlocked(String operation, String reason) {
        return new AdminException(
            String.format("Operation '%s' blocked by security system: %s", operation, reason),
            "ADMIN_OPERATION_BLOCKED"
        );
    }
    
    /**
     * Превышен лимит операций
     */
    public static AdminException operationLimitExceeded(String userId, int limit, String period) {
        return new AdminException(
            String.format("Operation limit exceeded for user %s: %d operations per %s", userId, limit, period),
            "ADMIN_OPERATION_LIMIT_EXCEEDED"
        );
    }
    
    /**
     * Недопустимый уровень административных прав
     */
    public static AdminException invalidPermissionLevel(String level) {
        return new AdminException(
            String.format("Invalid admin permission level: %s", level),
            "ADMIN_INVALID_PERMISSION_LEVEL"
        );
    }
} 
