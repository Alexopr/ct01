package com.ct01.admin.application.facade;

import com.ct01.admin.application.usecase.*;
import com.ct01.admin.domain.*;
import com.ct01.core.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AdminApplicationFacade - Фасад для Admin Application Layer
 * 
 * Предоставляет упрощенный интерфейс для работы с административными операциями
 */
@Service
@RequiredArgsConstructor
public class AdminApplicationFacade {
    
    private final CreateAdminOperationUseCase createOperationUseCase;
    private final ExecuteAdminOperationUseCase executeOperationUseCase;
    private final GetAdminOperationsUseCase getOperationsUseCase;
    private final GetAdminOperationUseCase getOperationUseCase;
    private final CancelAdminOperationUseCase cancelOperationUseCase;
    private final GetAdminOperationStatisticsUseCase getStatisticsUseCase;
    private final ProcessPendingOperationsUseCase processPendingUseCase;
    private final CleanupExpiredOperationsUseCase cleanupUseCase;
    
    // ===== Создание операций =====
    
    /**
     * Создать административную операцию
     */
    public Admin createOperation(UserId userId, AdminAction action, AdminPermissionLevel userLevel) {
        return createOperationUseCase.execute(userId, action, userLevel, null, Map.of());
    }
    
    /**
     * Создать административную операцию с параметрами
     */
    public Admin createOperation(UserId userId, AdminAction action, AdminPermissionLevel userLevel, 
                               Map<String, Object> parameters) {
        return createOperationUseCase.execute(userId, action, userLevel, null, parameters);
    }
    
    /**
     * Создать запланированную административную операцию
     */
    public Admin createScheduledOperation(UserId userId, AdminAction action, AdminPermissionLevel userLevel, 
                                        LocalDateTime scheduledTime, Map<String, Object> parameters) {
        return createOperationUseCase.execute(userId, action, userLevel, scheduledTime, parameters);
    }
    
    // ===== Быстрые методы создания для популярных операций =====
    
    /**
     * Создать операцию управления пользователем
     */
    public Admin createUserManagementOperation(UserId userId, AdminPermissionLevel userLevel, 
                                             String targetUserId, String operation) {
        AdminAction action = AdminAction.userManagement(operation, targetUserId);
        return createOperation(userId, action, userLevel, Map.of("targetUserId", targetUserId, "operation", operation));
    }
    
    /**
     * Создать операцию изменения системных настроек
     */
    public Admin createSystemSettingsOperation(UserId userId, AdminPermissionLevel userLevel, 
                                             String settingKey, Object settingValue) {
        AdminAction action = AdminAction.systemSettings(settingKey, String.valueOf(settingValue));
        return createOperation(userId, action, userLevel, Map.of("settingKey", settingKey, "settingValue", settingValue));
    }
    
    /**
     * Создать операцию резервного копирования
     */
    public Admin createBackupOperation(UserId userId, AdminPermissionLevel userLevel, String backupType) {
        AdminAction action = AdminAction.backupOperation(backupType);
        return createOperation(userId, action, userLevel, Map.of("backupType", backupType));
    }
    
    /**
     * Создать операцию управления криптовалютой
     */
    public Admin createCryptoManagementOperation(UserId userId, AdminPermissionLevel userLevel, 
                                               String cryptoOperation, String symbol) {
        AdminAction action = AdminAction.cryptoManagement(cryptoOperation, symbol);
        return createOperation(userId, action, userLevel, Map.of("operation", cryptoOperation, "symbol", symbol));
    }
    
    // ===== Выполнение операций =====
    
    /**
     * Выполнить административную операцию
     */
    public Admin executeOperation(AdminId adminId) {
        return executeOperationUseCase.execute(adminId, Map.of());
    }
    
    /**
     * Выполнить административную операцию с контекстом
     */
    public Admin executeOperation(AdminId adminId, Map<String, Object> executionContext) {
        return executeOperationUseCase.execute(adminId, executionContext);
    }
    
    /**
     * Отменить административную операцию
     */
    public Admin cancelOperation(AdminId adminId, String reason) {
        return cancelOperationUseCase.execute(adminId, reason);
    }
    
    // ===== Получение операций =====
    
    /**
     * Получить операцию по ID
     */
    public Admin getOperation(AdminId adminId) {
        return getOperationUseCase.execute(adminId);
    }
    
    /**
     * Получить все операции пользователя
     */
    public Page<Admin> getUserOperations(UserId userId, Pageable pageable) {
        return getOperationsUseCase.execute(userId, null, null, null, null, pageable);
    }
    
    /**
     * Получить операции пользователя по статусу
     */
    public Page<Admin> getUserOperationsByStatus(UserId userId, AdminStatus status, Pageable pageable) {
        return getOperationsUseCase.execute(userId, status, null, null, null, pageable);
    }
    
    /**
     * Получить операции по типу
     */
    public Page<Admin> getOperationsByType(AdminActionType actionType, Pageable pageable) {
        return getOperationsUseCase.execute(null, null, actionType, null, null, pageable);
    }
    
    /**
     * Получить операции за период
     */
    public Page<Admin> getOperationsByPeriod(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return getOperationsUseCase.execute(null, null, null, from, to, pageable);
    }
    
    /**
     * Получить активные операции
     */
    public Page<Admin> getActiveOperations(Pageable pageable) {
        return getOperationsUseCase.execute(null, AdminStatus.PENDING, null, null, null, pageable);
    }
    
    /**
     * Получить завершенные операции
     */
    public Page<Admin> getCompletedOperations(Pageable pageable) {
        return getOperationsUseCase.execute(null, AdminStatus.COMPLETED, null, null, null, pageable);
    }
    
    /**
     * Получить проваленные операции
     */
    public Page<Admin> getFailedOperations(Pageable pageable) {
        return getOperationsUseCase.execute(null, AdminStatus.FAILED, null, null, null, pageable);
    }
    
    // ===== Статистика =====
    
    /**
     * Получить статистику операций пользователя
     */
    public AdminOperationStatistics getUserOperationStatistics(UserId userId) {
        return getStatisticsUseCase.execute(userId, null, null);
    }
    
    /**
     * Получить общую статистику операций за период
     */
    public AdminOperationStatistics getOperationStatistics(LocalDateTime from, LocalDateTime to) {
        return getStatisticsUseCase.execute(null, from, to);
    }
    
    /**
     * Получить статистику операций за последний месяц
     */
    public AdminOperationStatistics getMonthlyStatistics() {
        LocalDateTime from = LocalDateTime.now().minusMonths(1);
        LocalDateTime to = LocalDateTime.now();
        return getStatisticsUseCase.execute(null, from, to);
    }
    
    /**
     * Получить статистику операций за последнюю неделю
     */
    public AdminOperationStatistics getWeeklyStatistics() {
        LocalDateTime from = LocalDateTime.now().minusWeeks(1);
        LocalDateTime to = LocalDateTime.now();
        return getStatisticsUseCase.execute(null, from, to);
    }
    
    /**
     * Получить статистику операций за сегодня
     */
    public AdminOperationStatistics getTodayStatistics() {
        LocalDateTime from = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime to = LocalDateTime.now();
        return getStatisticsUseCase.execute(null, from, to);
    }
    
    // ===== Системные операции =====
    
    /**
     * Обработать ожидающие операции
     */
    public List<Admin> processPendingOperations() {
        return processPendingUseCase.execute();
    }
    
    /**
     * Выполнить очистку устаревших операций
     */
    public void cleanupExpiredOperations() {
        cleanupUseCase.execute();
    }
    
    // ===== Утилитарные методы =====
    
    /**
     * Проверить может ли пользователь выполнить операцию
     */
    public boolean canUserPerformOperation(UserId userId, AdminAction action, AdminPermissionLevel userLevel) {
        try {
            createOperationUseCase.execute(userId, action, userLevel, null, Map.of());
            return true;
        } catch (AdminException e) {
            return false;
        }
    }
    
    /**
     * Получить информацию об операции в виде строки
     */
    public String getOperationInfo(AdminId adminId) {
        Admin admin = getOperation(adminId);
        return String.format("Operation %s: %s [%s] - %s", 
            admin.getId().getValue(),
            admin.getAction().getFullName(),
            admin.getStatus().getDisplayName(),
            admin.getExecutedAt()
        );
    }
    
    /**
     * Получить краткую сводку по операциям пользователя
     */
    public String getUserOperationsSummary(UserId userId) {
        AdminOperationStatistics stats = getUserOperationStatistics(userId);
        return String.format("User %s: %d total operations, %d pending, %d completed, %d failed", 
            userId.getValue(),
            stats.getTotalOperations(),
            stats.getPendingOperations(),
            stats.getCompletedOperations(),
            stats.getFailedOperations()
        );
    }
} 
