package com.ct01.user.domain;

/**
 * Value Object для статуса пользователя.
 * Определяет возможные состояния пользователя в системе.
 */
public enum UserStatus {
    
    /**
     * Активный пользователь - может полноценно использовать систему
     */
    ACTIVE("Активный", true),
    
    /**
     * Неактивный пользователь - временно не использует систему
     */
    INACTIVE("Неактивный", false),
    
    /**
     * Заблокированный пользователь - доступ запрещен
     */
    BLOCKED("Заблокирован", false),
    
    /**
     * Ожидает активации - новый пользователь, не подтвердивший регистрацию
     */
    PENDING("Ожидает активации", false);
    
    private final String displayName;
    private final boolean canLogin;
    
    UserStatus(String displayName, boolean canLogin) {
        this.displayName = displayName;
        this.canLogin = canLogin;
    }
    
    /**
     * Получить отображаемое название статуса
     * @return название для UI
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Может ли пользователь с данным статусом войти в систему
     * @return true если может войти
     */
    public boolean canLogin() {
        return canLogin;
    }
    
    /**
     * Может ли пользователь быть активирован из текущего статуса
     * @return true если может быть активирован
     */
    public boolean canBeActivated() {
        return this == INACTIVE || this == PENDING;
    }
    
    /**
     * Может ли пользователь быть заблокирован из текущего статуса
     * @return true если может быть заблокирован
     */
    public boolean canBeBlocked() {
        return this == ACTIVE || this == INACTIVE;
    }
    
    /**
     * Проверить, является ли статус активным
     * @return true если статус активный
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
} 
