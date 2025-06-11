package com.ct01.admin.domain;

import com.ct01.core.domain.UserId;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Статистика административных операций
 */
public record AdminOperationStatistics(
    int totalOperations,
    int pendingOperations,
    int executingOperations,
    int completedOperations,
    int rejectedOperations,
    int failedOperations,
    Map<AdminActionType, Integer> operationsByType,
    Map<AdminPermissionLevel, Integer> operationsByPermissionLevel,
    Map<UserId, Integer> operationsByUser,
    LocalDateTime periodStart,
    LocalDateTime periodEnd,
    double successRate,
    double averageExecutionTime
) {
    
    /**
     * Создать статистику за весь период
     */
    public static AdminOperationStatistics forAllTime(
        int total, int pending, int executing, int completed, int rejected, int failed,
        Map<AdminActionType, Integer> byType,
        Map<AdminPermissionLevel, Integer> byPermission,
        Map<UserId, Integer> byUser,
        double avgExecutionTime
    ) {
        double successRate = total > 0 ? (double) completed / total * 100 : 0.0;
        
        return new AdminOperationStatistics(
            total, pending, executing, completed, rejected, failed,
            byType, byPermission, byUser,
            null, null, successRate, avgExecutionTime
        );
    }
    
    /**
     * Создать статистику за определенный период
     */
    public static AdminOperationStatistics forPeriod(
        LocalDateTime start, LocalDateTime end,
        int total, int pending, int executing, int completed, int rejected, int failed,
        Map<AdminActionType, Integer> byType,
        Map<AdminPermissionLevel, Integer> byPermission,
        Map<UserId, Integer> byUser,
        double avgExecutionTime
    ) {
        double successRate = total > 0 ? (double) completed / total * 100 : 0.0;
        
        return new AdminOperationStatistics(
            total, pending, executing, completed, rejected, failed,
            byType, byPermission, byUser,
            start, end, successRate, avgExecutionTime
        );
    }
} 
