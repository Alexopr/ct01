package com.ct01.admin.domain;

import com.ct01.core.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AdminDomainService - Доменный сервис для Admin Domain
 * 
 * Содержит бизнес-логику, которая не принадлежит конкретному агрегату,
 * но касается нескольких агрегатов или внешних доменов
 */
@Service
@RequiredArgsConstructor
public class AdminDomainService {
    
    private final AdminRepository adminRepository;
    
    /**
     * Проверить может ли пользователь выполнить указанную операцию
     */
    public boolean canUserPerformOperation(UserId userId, AdminAction action, AdminPermissionLevel userLevel) {
        // Базовая проверка уровня прав
        if (!userLevel.hasLevelOrHigher(action.getRequiredPermissionLevel())) {
            return false;
        }
        
        // Проверить лимиты операций
        if (!checkOperationLimits(userId, action)) {
            return false;
        }
        
        // Проверить нет ли блокирующих условий
        if (hasBlockingConditions(userId, action)) {
            return false;
        }
        
        // Проверить специфические правила для типа операции
        return checkOperationSpecificRules(userId, action, userLevel);
    }
    
    /**
     * Проверить лимиты операций пользователя
     */
    private boolean checkOperationLimits(UserId userId, AdminAction action) {
        LocalDateTime hourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
        
        // Базовые лимиты
        long operationsLastHour = adminRepository.countByUserIdAndExecutedAtBetween(userId, hourAgo, LocalDateTime.now());
        long operationsLastDay = adminRepository.countByUserIdAndExecutedAtBetween(userId, dayAgo, LocalDateTime.now());
        
        // Лимиты зависят от типа операции и критичности
        return switch (action.getType()) {
            case USER_MANAGEMENT -> operationsLastHour < (action.isCritical() ? 5 : 20) && operationsLastDay < 100;
            case SYSTEM_SETTINGS -> operationsLastHour < (action.isCritical() ? 2 : 10) && operationsLastDay < 50;
            case DATA_MANAGEMENT -> operationsLastHour < (action.isCritical() ? 3 : 15) && operationsLastDay < 75;
            case SECURITY_MANAGEMENT -> operationsLastHour < (action.isCritical() ? 1 : 5) && operationsLastDay < 25;
            case CRYPTO_MANAGEMENT -> operationsLastHour < (action.isCritical() ? 2 : 8) && operationsLastDay < 40;
            default -> operationsLastHour < 30 && operationsLastDay < 200;
        };
    }
    
    /**
     * Проверить наличие блокирующих условий
     */
    private boolean hasBlockingConditions(UserId userId, AdminAction action) {
        // Проверить нет ли активной операции того же типа
        if (action.isExclusive()) {
            return adminRepository.existsActiveOperationByUserIdAndActionType(userId, action.getType());
        }
        
        // Проверить нет ли конфликтующих операций
        List<Admin> activeOperations = adminRepository.findActiveOperationsByUserId(userId);
        return activeOperations.stream()
            .anyMatch(activeOp -> isConflicting(action, activeOp.getAction()));
    }
    
    /**
     * Проверить конфликтуют ли операции
     */
    private boolean isConflicting(AdminAction newAction, AdminAction activeAction) {
        // Определить конфликтующие типы операций
        Set<AdminActionType> conflictingTypes = getConflictingActionTypes(newAction.getType());
        return conflictingTypes.contains(activeAction.getType());
    }
    
    /**
     * Получить конфликтующие типы операций
     */
    private Set<AdminActionType> getConflictingActionTypes(AdminActionType actionType) {
        return switch (actionType) {
            case SYSTEM_SETTINGS -> Set.of(AdminActionType.SYSTEM_MAINTENANCE, AdminActionType.BACKUP_OPERATIONS);
            case BACKUP_OPERATIONS -> Set.of(AdminActionType.SYSTEM_SETTINGS, AdminActionType.DATA_MANAGEMENT);
            case USER_MANAGEMENT -> Set.of(AdminActionType.ROLE_MANAGEMENT);
            case CRYPTO_MANAGEMENT -> Set.of(AdminActionType.SYSTEM_MAINTENANCE);
            default -> Set.of();
        };
    }
    
    /**
     * Получить список операций готовых к выполнению
     */
    public List<Admin> getOperationsReadyForExecution() {
        List<Admin> pendingOperations = adminRepository.findByStatus(AdminStatus.PENDING);
        
        return pendingOperations.stream()
            .filter(this::isReadyForExecution)
            .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority())) // Сортировка по приоритету
            .toList();
    }
    
    /**
     * Проверить готова ли операция к выполнению
     */
    private boolean isReadyForExecution(Admin admin) {
        // Проверить не истекла ли операция
        if (admin.isExpired()) {
            return false;
        }
        
        // Проверить наступило ли время выполнения
        return !admin.getExecutedAt().isAfter(LocalDateTime.now());
    }
}
