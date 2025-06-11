package com.ct01.admin.application.usecase;

import com.ct01.admin.domain.*;
import com.ct01.admin.domain.service.AdminDomainService;
import com.ct01.core.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Коллекция Use Cases для Admin Domain
 */

/**
 * CreateAdminOperationUseCase - Создание административной операции
 */
@Service
@RequiredArgsConstructor
@Transactional
class CreateAdminOperationUseCase {
    
    private final AdminRepository adminRepository;
    private final AdminDomainService adminDomainService;
    
    public Admin execute(UserId userId, AdminAction action, AdminPermissionLevel userLevel, 
                        LocalDateTime scheduledTime, Map<String, Object> parameters) {
        
        // Проверить может ли пользователь выполнить операцию
        if (!adminDomainService.canUserPerformOperation(userId, action, userLevel)) {
            throw AdminException.insufficientPermissions(action.getRequiredPermissionLevel().toString(), userLevel.toString());
        }
        
        // Создать административную операцию
        Admin admin = Admin.create(
            AdminId.generate(),
            userId,
            action,
            scheduledTime != null ? scheduledTime : LocalDateTime.now(),
            parameters
        );
        
        return adminRepository.save(admin);
    }
}

/**
 * ExecuteAdminOperationUseCase - Выполнение административной операции
 */
@Service
@RequiredArgsConstructor
@Transactional
class ExecuteAdminOperationUseCase {
    
    private final AdminRepository adminRepository;
    
    public Admin execute(AdminId adminId, Map<String, Object> executionContext) {
        Admin admin = adminRepository.findById(adminId)
            .orElseThrow(() -> AdminException.operationNotFound(adminId.getValue()));
        
        // Начать выполнение
        admin.startExecution();
        
        try {
            // Здесь будет логика выполнения конкретной операции
            // В зависимости от типа операции будут вызываться соответствующие сервисы
            Map<String, Object> result = executeOperation(admin, executionContext);
            
            // Завершить успешно
            admin.complete(result);
            
        } catch (Exception e) {
            // Провалить операцию
            admin.fail(e.getMessage());
            throw e;
        }
        
        return adminRepository.save(admin);
    }
    
    private Map<String, Object> executeOperation(Admin admin, Map<String, Object> context) {
        // Здесь будет делегирование к конкретным сервисам в зависимости от типа операции
        return switch (admin.getAction().getType()) {
            case USER_MANAGEMENT -> executeUserManagement(admin, context);
            case SYSTEM_SETTINGS -> executeSystemSettings(admin, context);
            case DATA_MANAGEMENT -> executeDataManagement(admin, context);
            case SECURITY_MANAGEMENT -> executeSecurityManagement(admin, context);
            case CRYPTO_MANAGEMENT -> executeCryptoManagement(admin, context);
            default -> Map.of("status", "executed", "timestamp", LocalDateTime.now());
        };
    }
    
    private Map<String, Object> executeUserManagement(Admin admin, Map<String, Object> context) {
        // Делегирование к User Domain
        return Map.of("operation", "user_management", "status", "completed");
    }
    
    private Map<String, Object> executeSystemSettings(Admin admin, Map<String, Object> context) {
        // Делегирование к System Configuration Service
        return Map.of("operation", "system_settings", "status", "completed");
    }
    
    private Map<String, Object> executeDataManagement(Admin admin, Map<String, Object> context) {
        // Делегирование к Data Management Service
        return Map.of("operation", "data_management", "status", "completed");
    }
    
    private Map<String, Object> executeSecurityManagement(Admin admin, Map<String, Object> context) {
        // Делегирование к Security Service
        return Map.of("operation", "security_management", "status", "completed");
    }
    
    private Map<String, Object> executeCryptoManagement(Admin admin, Map<String, Object> context) {
        // Делегирование к Crypto Domain
        return Map.of("operation", "crypto_management", "status", "completed");
    }
}

/**
 * GetAdminOperationsUseCase - Получение административных операций
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class GetAdminOperationsUseCase {
    
    private final AdminRepository adminRepository;
    
    public Page<Admin> execute(UserId userId, AdminStatus status, AdminActionType actionType, 
                              LocalDateTime from, LocalDateTime to, Pageable pageable) {
        
        if (userId != null && status != null) {
            return adminRepository.findByUserId(userId, pageable)
                .filter(admin -> admin.getStatus() == status);
        }
        
        if (status != null) {
            return adminRepository.findByStatus(status, pageable);
        }
        
        if (actionType != null) {
            return adminRepository.findByActionType(actionType, pageable);
        }
        
        if (from != null && to != null) {
            return adminRepository.findByExecutedAtBetween(from, to, pageable);
        }
        
        // Если userId указан, вернуть операции пользователя
        if (userId != null) {
            return adminRepository.findByUserId(userId, pageable);
        }
        
        // По умолчанию вернуть активные операции
        return adminRepository.findByStatus(AdminStatus.PENDING, pageable);
    }
}

/**
 * GetAdminOperationUseCase - Получение конкретной административной операции
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class GetAdminOperationUseCase {
    
    private final AdminRepository adminRepository;
    
    public Admin execute(AdminId adminId) {
        return adminRepository.findById(adminId)
            .orElseThrow(() -> AdminException.operationNotFound(adminId.getValue()));
    }
}

/**
 * CancelAdminOperationUseCase - Отмена административной операции
 */
@Service
@RequiredArgsConstructor
@Transactional
class CancelAdminOperationUseCase {
    
    private final AdminRepository adminRepository;
    
    public Admin execute(AdminId adminId, String reason) {
        Admin admin = adminRepository.findById(adminId)
            .orElseThrow(() -> AdminException.operationNotFound(adminId.getValue()));
        
        admin.reject(reason);
        
        return adminRepository.save(admin);
    }
}

/**
 * GetAdminOperationStatisticsUseCase - Получение статистики административных операций
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class GetAdminOperationStatisticsUseCase {
    
    private final AdminRepository adminRepository;
    
    public AdminOperationStatistics execute(UserId userId, LocalDateTime from, LocalDateTime to) {
        if (userId != null) {
            return adminRepository.getOperationStatistics(userId);
        }
        
        if (from != null && to != null) {
            return adminRepository.getOperationStatistics(from, to);
        }
        
        // По умолчанию статистика за последние 30 дней
        LocalDateTime defaultFrom = LocalDateTime.now().minusDays(30);
        LocalDateTime defaultTo = LocalDateTime.now();
        return adminRepository.getOperationStatistics(defaultFrom, defaultTo);
    }
}

/**
 * ProcessPendingOperationsUseCase - Обработка ожидающих операций
 */
@Service
@RequiredArgsConstructor
@Transactional
class ProcessPendingOperationsUseCase {
    
    private final AdminDomainService adminDomainService;
    private final ExecuteAdminOperationUseCase executeOperationUseCase;
    
    public List<Admin> execute() {
        List<Admin> readyOperations = adminDomainService.getOperationsReadyForExecution();
        
        return readyOperations.stream()
            .map(admin -> {
                try {
                    return executeOperationUseCase.execute(admin.getId(), Map.of());
                } catch (Exception e) {
                    // Логирование ошибки и продолжение обработки других операций
                    return admin;
                }
            })
            .toList();
    }
}

/**
 * CleanupExpiredOperationsUseCase - Очистка устаревших операций
 */
@Service
@RequiredArgsConstructor
@Transactional
class CleanupExpiredOperationsUseCase {
    
    private final AdminDomainService adminDomainService;
    
    public void execute() {
        if (adminDomainService.needsCleanup()) {
            adminDomainService.performCleanup();
        }
    }
}

/**
 * Use Cases для административных операций
 */
@Service
@RequiredArgsConstructor
public class AdminUseCases {
    
    private final AdminRepository adminRepository;
    private final AdminDomainService adminDomainService;
    
    /**
     * Создание новой административной операции
     */
    @Transactional
    public AdminId createAdminOperation(
            UserId userId, 
            AdminActionType actionType, 
            Map<String, Object> actionData,
            AdminPermissionLevel permissionLevel,
            String ipAddress,
            String userAgent) {
        
        AdminAction action = AdminAction.builder()
            .actionType(actionType)
            .actionData(actionData)
            .build();
            
        Admin admin = Admin.builder()
            .id(AdminId.nextId())
            .userId(userId)
            .action(action)
            .actionData(actionData)
            .permissionLevel(permissionLevel)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
            
        adminRepository.save(admin);
        return admin.getId();
    }
    
    /**
     * Выполнение административной операции
     */
    @Transactional
    public void executeAdminOperation(AdminId adminId) {
        Admin admin = adminRepository.findById(adminId)
            .orElseThrow(() -> new AdminException("Административная операция не найдена: " + adminId));
            
        admin.execute();
        adminRepository.save(admin);
    }
    
    /**
     * Получение всех операций
     */
    @Transactional(readOnly = true)
    public List<Admin> getAdminOperations(AdminStatus status, int page, int size) {
        return adminRepository.findByStatus(status, page, size);
    }
    
    /**
     * Получение операции по ID
     */
    @Transactional(readOnly = true)
    public Optional<Admin> getAdminOperation(AdminId adminId) {
        return adminRepository.findById(adminId);
    }
    
    /**
     * Отмена операции
     */
    @Transactional
    public void cancelAdminOperation(AdminId adminId, String reason) {
        Admin admin = adminRepository.findById(adminId)
            .orElseThrow(() -> new AdminException("Операция не найдена: " + adminId));
            
        admin.reject(reason);
        adminRepository.save(admin);
    }
    
    /**
     * Получение статистики операций
     */
    @Transactional(readOnly = true)
    public AdminOperationStatistics getAdminOperationStatistics(LocalDateTime from, LocalDateTime to) {
        return adminRepository.getOperationStatistics(from, to);
    }
    
    /**
     * Обработка ожидающих операций
     */
    @Transactional
    public void processPendingOperations() {
        List<Admin> pending = adminRepository.findByStatus(AdminStatus.PENDING, 0, 100);
        
        for (Admin admin : pending) {
            if (adminDomainService.canExecuteOperation(admin)) {
                admin.execute();
                adminRepository.save(admin);
            }
        }
    }
    
    /**
     * Очистка истекших операций
     */
    @Transactional
    public void cleanupExpiredOperations() {
        List<Admin> expired = adminRepository.findExpiredOperations(LocalDateTime.now());
        
        for (Admin admin : expired) {
            admin.fail("Операция истекла");
            adminRepository.save(admin);
        }
    }
} 
