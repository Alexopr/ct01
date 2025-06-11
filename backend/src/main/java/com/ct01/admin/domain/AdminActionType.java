package com.ct01.admin.domain;

import lombok.Getter;

/**
 * AdminActionType - Enum для типов административных действий
 * 
 * Определяет категории административных операций в системе
 */
@Getter
public enum AdminActionType {
    
    // Управление пользователями
    USER_MANAGEMENT("User Management", "Управление пользователями", "USERS"),
    
    // Управление ролями и правами
    ROLE_MANAGEMENT("Role Management", "Управление ролями", "ROLES"),
    
    // Системные настройки
    SYSTEM_SETTINGS("System Settings", "Системные настройки", "SYSTEM"),
    
    // Мониторинг и метрики
    SYSTEM_MONITORING("System Monitoring", "Мониторинг системы", "MONITORING"),
    
    // Управление данными
    DATA_MANAGEMENT("Data Management", "Управление данными", "DATA"),
    
    // Безопасность
    SECURITY_MANAGEMENT("Security Management", "Управление безопасностью", "SECURITY"),
    
    // Аудит и логирование
    AUDIT("Audit", "Аудит", "AUDIT"),
    
    // Конфигурация приложения
    CONFIGURATION_MANAGEMENT("Configuration Management", "Управление конфигурацией", "CONFIG"),
    
    // Управление подписками
    SUBSCRIPTION_MANAGEMENT("Subscription Management", "Управление подписками", "SUBSCRIPTIONS"),
    
    // Управление крипто-данными
    CRYPTO_MANAGEMENT("Crypto Management", "Управление крипто-данными", "CRYPTO"),
    
    // Управление рыночными данными
    MARKET_MANAGEMENT("Market Management", "Управление рыночными данными", "MARKET"),
    
    // Управление уведомлениями
    NOTIFICATION_MANAGEMENT("Notification Management", "Управление уведомлениями", "NOTIFICATIONS"),
    
    // Техническое обслуживание
    MAINTENANCE("Maintenance", "Техническое обслуживание", "MAINTENANCE"),
    
    // Резервное копирование и восстановление
    BACKUP_RESTORE("Backup & Restore", "Резервное копирование", "BACKUP"),
    
    // Интеграции с внешними системами
    INTEGRATION_MANAGEMENT("Integration Management", "Управление интеграциями", "INTEGRATIONS");
    
    private final String displayName;
    private final String description;
    private final String category;
    
    AdminActionType(String displayName, String description, String category) {
        this.displayName = displayName;
        this.description = description;
        this.category = category;
    }
    
    /**
     * Проверить является ли тип критическим
     */
    public boolean isCriticalByDefault() {
        return switch (this) {
            case SYSTEM_SETTINGS, 
                 SECURITY_MANAGEMENT, 
                 DATA_MANAGEMENT, 
                 CONFIGURATION_MANAGEMENT, 
                 ROLE_MANAGEMENT,
                 BACKUP_RESTORE -> true;
            default -> false;
        };
    }
    
    /**
     * Получить минимальный уровень прав для типа действия
     */
    public AdminPermissionLevel getMinimumPermissionLevel() {
        return switch (this) {
            case SYSTEM_SETTINGS, 
                 CONFIGURATION_MANAGEMENT, 
                 MAINTENANCE,
                 BACKUP_RESTORE -> AdminPermissionLevel.SYSTEM_ADMIN;
                 
            case SECURITY_MANAGEMENT -> AdminPermissionLevel.SECURITY_ADMIN;
            
            case DATA_MANAGEMENT -> AdminPermissionLevel.DATA_ADMIN;
            
            case USER_MANAGEMENT, 
                 ROLE_MANAGEMENT,
                 SUBSCRIPTION_MANAGEMENT -> AdminPermissionLevel.USER_ADMIN;
                 
            case CRYPTO_MANAGEMENT,
                 MARKET_MANAGEMENT,
                 NOTIFICATION_MANAGEMENT,
                 INTEGRATION_MANAGEMENT -> AdminPermissionLevel.DOMAIN_ADMIN;
                 
            case SYSTEM_MONITORING, 
                 AUDIT -> AdminPermissionLevel.MODERATOR;
        };
    }
    
    /**
     * Получить все типы для определенной категории
     */
    public static AdminActionType[] getByCategory(String category) {
        return java.util.Arrays.stream(values())
            .filter(type -> type.getCategory().equals(category))
            .toArray(AdminActionType[]::new);
    }
    
    /**
     * Получить тип по названию (case-insensitive)
     */
    public static AdminActionType fromName(String name) {
        for (AdminActionType type : values()) {
            if (type.name().equalsIgnoreCase(name) || 
                type.getDisplayName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new AdminException("Unknown admin action type: " + name);
    }
    
    /**
     * Проверить совместимость с другим типом
     */
    public boolean isCompatibleWith(AdminActionType other) {
        return this.getCategory().equals(other.getCategory());
    }
} 
