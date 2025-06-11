package com.ct01.admin.domain;

import com.ct01.core.domain.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;
import java.util.Set;

/**
 * AdminAction - Value Object для представления административных действий
 * 
 * Инкапсулирует тип действия, требуемые права доступа и бизнес-правила
 */
@Getter
@EqualsAndHashCode
public class AdminAction implements ValueObject {
    
    private final AdminActionType type;
    private final String target;
    private final String description;
    private final AdminPermissionLevel requiredPermissionLevel;
    private final boolean critical;
    private final Set<String> requiredParameters;
    
    private AdminAction(AdminActionType type, String target, String description,
                       AdminPermissionLevel requiredPermissionLevel, boolean critical,
                       Set<String> requiredParameters) {
        this.type = Objects.requireNonNull(type, "Action type cannot be null");
        this.target = Objects.requireNonNull(target, "Action target cannot be null");
        this.description = description;
        this.requiredPermissionLevel = Objects.requireNonNull(requiredPermissionLevel, 
            "Required permission level cannot be null");
        this.critical = critical;
        this.requiredParameters = requiredParameters != null ? Set.copyOf(requiredParameters) : Set.of();
        
        validate();
    }
    
    // Фабричные методы для создания стандартных действий
    
    /**
     * Управление пользователями
     */
    public static AdminAction userManagement(String target, String description) {
        return new AdminAction(
            AdminActionType.USER_MANAGEMENT,
            target,
            description,
            AdminPermissionLevel.USER_ADMIN,
            false,
            Set.of("userId")
        );
    }
    
    /**
     * Управление ролями
     */
    public static AdminAction roleManagement(String target, String description) {
        return new AdminAction(
            AdminActionType.ROLE_MANAGEMENT,
            target,
            description,
            AdminPermissionLevel.USER_ADMIN,
            true,
            Set.of("roleId", "permissions")
        );
    }
    
    /**
     * Системные настройки
     */
    public static AdminAction systemSettings(String target, String description) {
        return new AdminAction(
            AdminActionType.SYSTEM_SETTINGS,
            target,
            description,
            AdminPermissionLevel.SYSTEM_ADMIN,
            true,
            Set.of("category", "settings")
        );
    }
    
    /**
     * Мониторинг системы
     */
    public static AdminAction systemMonitoring(String target, String description) {
        return new AdminAction(
            AdminActionType.SYSTEM_MONITORING,
            target,
            description,
            AdminPermissionLevel.MODERATOR,
            false,
            Set.of()
        );
    }
    
    /**
     * Управление данными
     */
    public static AdminAction dataManagement(String target, String description) {
        return new AdminAction(
            AdminActionType.DATA_MANAGEMENT,
            target,
            description,
            AdminPermissionLevel.DATA_ADMIN,
            true,
            Set.of("dataType", "operation")
        );
    }
    
    /**
     * Управление безопасностью
     */
    public static AdminAction securityManagement(String target, String description) {
        return new AdminAction(
            AdminActionType.SECURITY_MANAGEMENT,
            target,
            description,
            AdminPermissionLevel.SECURITY_ADMIN,
            true,
            Set.of("securityLevel")
        );
    }
    
    /**
     * Аудит
     */
    public static AdminAction audit(String target, String description) {
        return new AdminAction(
            AdminActionType.AUDIT,
            target,
            description,
            AdminPermissionLevel.MODERATOR,
            false,
            Set.of("timeRange")
        );
    }
    
    /**
     * Управление конфигурацией
     */
    public static AdminAction configurationManagement(String target, String description) {
        return new AdminAction(
            AdminActionType.CONFIGURATION_MANAGEMENT,
            target,
            description,
            AdminPermissionLevel.SYSTEM_ADMIN,
            true,
            Set.of("configKey", "configValue")
        );
    }
    
    /**
     * Создать произвольное действие
     */
    public static AdminAction custom(AdminActionType type, String target, String description,
                                   AdminPermissionLevel requiredLevel, boolean critical,
                                   Set<String> requiredParameters) {
        return new AdminAction(type, target, description, requiredLevel, critical, requiredParameters);
    }
    
    /**
     * Получить полное название действия
     */
    public String getFullName() {
        return type.name() + ":" + target;
    }
    
    /**
     * Проверить является ли действие безопасным (не критическим)
     */
    public boolean isSafe() {
        return !critical;
    }
    
    /**
     * Проверить требует ли действие определенный параметр
     */
    public boolean requiresParameter(String parameter) {
        return requiredParameters.contains(parameter);
    }
    
    /**
     * Получить количество требуемых параметров
     */
    public int getRequiredParameterCount() {
        return requiredParameters.size();
    }
    
    /**
     * Проверить совместимость с уровнем прав
     */
    public boolean isCompatibleWith(AdminPermissionLevel permissionLevel) {
        return requiredPermissionLevel.ordinal() <= permissionLevel.ordinal();
    }
    
    /**
     * Получить категорию действия
     */
    public String getCategory() {
        return type.getCategory();
    }
    
    private void validate() {
        if (target.trim().isEmpty()) {
            throw new AdminException("Action target cannot be empty");
        }
        
        if (target.length() > 100) {
            throw new AdminException("Action target is too long (max 100 characters)");
        }
        
        if (description != null && description.length() > 500) {
            throw new AdminException("Action description is too long (max 500 characters)");
        }
    }
    
    @Override
    public String toString() {
        return String.format("AdminAction{type=%s, target='%s', critical=%s, requiredLevel=%s}", 
            type, target, critical, requiredPermissionLevel);
    }
}
