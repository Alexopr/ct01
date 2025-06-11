package com.ct01.admin.domain;

import java.time.LocalDateTime;

/**
 * Статистика по типам административных действий
 */
public record ActionTypeStatistics(
    AdminActionType actionType,
    int totalCount,
    int successCount,
    int failureCount,
    double averageExecutionTime,
    LocalDateTime firstUsage,
    LocalDateTime lastUsage,
    int uniqueUsers
) {
    
    /**
     * Вычислить процент успешности
     */
    public double getSuccessRate() {
        return totalCount > 0 ? (double) successCount / totalCount * 100 : 0.0;
    }
    
    /**
     * Проверить популярность типа действий
     */
    public boolean isPopularActionType() {
        return totalCount >= 10 && getSuccessRate() >= 80.0;
    }
    
    /**
     * Проверить проблемный тип действий
     */
    public boolean isProblematicActionType() {
        return totalCount >= 5 && getSuccessRate() < 50.0;
    }
} 
