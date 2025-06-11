package com.ct01.admin.domain;

import lombok.Getter;

/**
 * AdminPermissionLevel - Enum для уровней административных прав
 * 
 * Определяет иерархию прав доступа в административной системе
 */
@Getter
public enum AdminPermissionLevel {
    
    // Модератор - базовые права просмотра и мониторинга
    MODERATOR(1, "Moderator", "Модератор", "Базовые права просмотра и мониторинга"),
    
    // Администратор домена - управление конкретным доменом
    DOMAIN_ADMIN(2, "Domain Admin", "Администратор домена", "Управление конкретным функциональным доменом"),
    
    // Администратор пользователей - управление пользователями и ролями
    USER_ADMIN(3, "User Admin", "Администратор пользователей", "Управление пользователями, ролями и правами"),
    
    // Администратор данных - управление данными и резервными копиями
    DATA_ADMIN(4, "Data Admin", "Администратор данных", "Управление данными, миграциями и резервными копиями"),
    
    // Администратор безопасности - управление безопасностью
    SECURITY_ADMIN(5, "Security Admin", "Администратор безопасности", "Управление настройками безопасности и аудитом"),
    
    // Системный администратор - полные права на систему
    SYSTEM_ADMIN(6, "System Admin", "Системный администратор", "Полные права на систему и конфигурацию"),
    
    // Супер администратор - максимальные права
    SUPER_ADMIN(7, "Super Admin", "Супер администратор", "Максимальные права, включая управление другими администраторами");
    
    private final int level;
    private final String displayName;
    private final String russianName;
    private final String description;
    
    AdminPermissionLevel(int level, String displayName, String russianName, String description) {
        this.level = level;
        this.displayName = displayName;
        this.russianName = russianName;
        this.description = description;
    }
    
    /**
     * Проверить имеет ли этот уровень права выше или равные указанному
     */
    public boolean hasLevelOrHigher(AdminPermissionLevel requiredLevel) {
        return this.level >= requiredLevel.level;
    }
    
    /**
     * Проверить имеет ли этот уровень права строго выше указанного
     */
    public boolean hasHigherLevel(AdminPermissionLevel otherLevel) {
        return this.level > otherLevel.level;
    }
    
    /**
     * Получить разницу в уровнях
     */
    public int getLevelDifference(AdminPermissionLevel otherLevel) {
        return this.level - otherLevel.level;
    }
    
    /**
     * Проверить может ли этот уровень управлять указанным уровнем
     */
    public boolean canManage(AdminPermissionLevel targetLevel) {
        // Супер администратор может управлять всеми, включая других супер администраторов
        if (this == SUPER_ADMIN) {
            return true;
        }
        
        // Системный администратор не может управлять супер администраторами
        if (targetLevel == SUPER_ADMIN) {
            return false;
        }
        
        // Для остальных уровней - нужен уровень выше
        return this.level > targetLevel.level;
    }
    
    /**
     * Получить все уровни доступные для управления
     */
    public AdminPermissionLevel[] getManageableLevels() {
        return java.util.Arrays.stream(values())
            .filter(this::canManage)
            .toArray(AdminPermissionLevel[]::new);
    }
    
    /**
     * Получить максимальный уровень который может назначить данный уровень
     */
    public AdminPermissionLevel getMaxAssignableLevel() {
        return switch (this) {
            case SUPER_ADMIN -> SYSTEM_ADMIN; // Супер администратор может назначать до системного
            case SYSTEM_ADMIN -> SECURITY_ADMIN; // Системный - до администратора безопасности
            case SECURITY_ADMIN -> DATA_ADMIN; // И так далее...
            case DATA_ADMIN -> USER_ADMIN;
            case USER_ADMIN -> DOMAIN_ADMIN;
            case DOMAIN_ADMIN -> MODERATOR;
            case MODERATOR -> null; // Модератор не может назначать права
        };
    }
    
    /**
     * Проверить является ли уровень административным
     */
    public boolean isAdminLevel() {
        return this.level >= USER_ADMIN.level;
    }
    
    /**
     * Проверить является ли уровень системным
     */
    public boolean isSystemLevel() {
        return this.level >= SYSTEM_ADMIN.level;
    }
    
    /**
     * Получить категории действий доступные для данного уровня
     */
    public String[] getAvailableActionCategories() {
        return switch (this) {
            case SUPER_ADMIN, SYSTEM_ADMIN -> new String[]{
                "USERS", "ROLES", "SYSTEM", "MONITORING", "DATA", 
                "SECURITY", "AUDIT", "CONFIG", "SUBSCRIPTIONS", 
                "CRYPTO", "MARKET", "NOTIFICATIONS", "MAINTENANCE", 
                "BACKUP", "INTEGRATIONS"
            };
            case SECURITY_ADMIN -> new String[]{
                "SECURITY", "AUDIT", "MONITORING", "USERS", "ROLES"
            };
            case DATA_ADMIN -> new String[]{
                "DATA", "BACKUP", "MONITORING", "AUDIT"
            };
            case USER_ADMIN -> new String[]{
                "USERS", "ROLES", "SUBSCRIPTIONS", "MONITORING"
            };
            case DOMAIN_ADMIN -> new String[]{
                "CRYPTO", "MARKET", "NOTIFICATIONS", "INTEGRATIONS", "MONITORING"
            };
            case MODERATOR -> new String[]{
                "MONITORING", "AUDIT"
            };
        };
    }
    
    /**
     * Получить уровень по названию
     */
    public static AdminPermissionLevel fromName(String name) {
        for (AdminPermissionLevel level : values()) {
            if (level.name().equalsIgnoreCase(name) || 
                level.getDisplayName().equalsIgnoreCase(name) ||
                level.getRussianName().equalsIgnoreCase(name)) {
                return level;
            }
        }
        throw new AdminException("Unknown admin permission level: " + name);
    }
    
    /**
     * Получить уровень по числовому значению
     */
    public static AdminPermissionLevel fromLevel(int level) {
        for (AdminPermissionLevel permLevel : values()) {
            if (permLevel.getLevel() == level) {
                return permLevel;
            }
        }
        throw new AdminException("Unknown admin permission level: " + level);
    }
}
