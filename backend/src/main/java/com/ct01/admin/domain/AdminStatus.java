package com.ct01.admin.domain;

import lombok.Getter;

/**
 * AdminStatus - Enum для статусов административных операций
 * 
 * Представляет жизненный цикл административной операции
 */
@Getter
public enum AdminStatus {
    
    // Ожидает выполнения
    PENDING("Pending", "Ожидает", "Операция создана и ожидает выполнения"),
    
    // В процессе выполнения
    EXECUTING("Executing", "Выполняется", "Операция находится в процессе выполнения"),
    
    // Успешно завершена
    COMPLETED("Completed", "Завершена", "Операция успешно выполнена"),
    
    // Отклонена
    REJECTED("Rejected", "Отклонена", "Операция отклонена администратором или системой"),
    
    // Провалена из-за ошибки
    FAILED("Failed", "Провалена", "Операция не выполнена из-за ошибки или технической проблемы");
    
    private final String displayName;
    private final String russianName;
    private final String description;
    
    AdminStatus(String displayName, String russianName, String description) {
        this.displayName = displayName;
        this.russianName = russianName;
        this.description = description;
    }
    
    /**
     * Проверить является ли статус финальным (операция завершена)
     */
    public boolean isFinal() {
        return this == COMPLETED || this == REJECTED || this == FAILED;
    }
    
    /**
     * Проверить является ли статус успешным
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }
    
    /**
     * Проверить является ли статус активным (операция выполняется)
     */
    public boolean isActive() {
        return this == EXECUTING;
    }
    
    /**
     * Проверить может ли статус быть изменен на указанный
     */
    public boolean canTransitionTo(AdminStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == EXECUTING || newStatus == REJECTED;
            case EXECUTING -> newStatus == COMPLETED || newStatus == FAILED;
            case COMPLETED, REJECTED, FAILED -> false; // Финальные статусы не могут изменяться
        };
    }
    
    /**
     * Получить все возможные следующие статусы
     */
    public AdminStatus[] getPossibleNextStatuses() {
        return switch (this) {
            case PENDING -> new AdminStatus[]{EXECUTING, REJECTED};
            case EXECUTING -> new AdminStatus[]{COMPLETED, FAILED};
            case COMPLETED, REJECTED, FAILED -> new AdminStatus[]{};
        };
    }
    
    /**
     * Получить приоритет статуса для сортировки
     */
    public int getPriority() {
        return switch (this) {
            case EXECUTING -> 1;    // Активные операции - высший приоритет
            case PENDING -> 2;      // Ожидающие операции
            case FAILED -> 3;       // Провалившиеся операции
            case REJECTED -> 4;     // Отклоненные операции
            case COMPLETED -> 5;    // Завершенные операции - низший приоритет
        };
    }
    
    /**
     * Получить CSS класс для отображения статуса
     */
    public String getCssClass() {
        return switch (this) {
            case PENDING -> "status-pending";
            case EXECUTING -> "status-executing";
            case COMPLETED -> "status-completed";
            case REJECTED -> "status-rejected";
            case FAILED -> "status-failed";
        };
    }
    
    /**
     * Получить иконку для статуса
     */
    public String getIcon() {
        return switch (this) {
            case PENDING -> "⏳";
            case EXECUTING -> "⚡";
            case COMPLETED -> "✅";
            case REJECTED -> "❌";
            case FAILED -> "💥";
        };
    }
    
    /**
     * Получить статус по названию (case-insensitive)
     */
    public static AdminStatus fromName(String name) {
        for (AdminStatus status : values()) {
            if (status.name().equalsIgnoreCase(name) ||
                status.getDisplayName().equalsIgnoreCase(name) ||
                status.getRussianName().equalsIgnoreCase(name)) {
                return status;
            }
        }
        throw new AdminException("Unknown admin status: " + name);
    }
    
    /**
     * Получить все активные статусы (не финальные)
     */
    public static AdminStatus[] getActiveStatuses() {
        return new AdminStatus[]{PENDING, EXECUTING};
    }
    
    /**
     * Получить все финальные статусы
     */
    public static AdminStatus[] getFinalStatuses() {
        return new AdminStatus[]{COMPLETED, REJECTED, FAILED};
    }
}
