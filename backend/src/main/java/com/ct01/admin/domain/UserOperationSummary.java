package com.ct01.admin.domain;

import com.ct01.core.domain.UserId;
import java.time.LocalDateTime;

/**
 * Сводка операций пользователя
 */
public record UserOperationSummary(
    UserId userId,
    String username,
    int totalOperations,
    int successfulOperations,
    int failedOperations,
    LocalDateTime lastOperationTime,
    AdminActionType mostUsedActionType,
    AdminPermissionLevel currentPermissionLevel
) {
    
    /**
     * Вычислить процент успешности
     */
    public double getSuccessRate() {
        return totalOperations > 0 ? (double) successfulOperations / totalOperations * 100 : 0.0;
    }
    
    /**
     * Проверить активность пользователя
     */
    public boolean isActiveUser() {
        return lastOperationTime != null && 
               lastOperationTime.isAfter(LocalDateTime.now().minusDays(30));
    }
} 
