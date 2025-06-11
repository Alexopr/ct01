package com.ct01.user.domain;

import com.ct01.core.domain.Entity;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Role Entity в User домене.
 * Представляет роль пользователя с набором разрешений.
 */
public class Role implements Entity<Long> {
    
    private Long id;
    private String name;
    private String description;
    private Set<Permission> permissions;
    private Integer priority;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Конструктор для создания новой роли
    public Role(String name, String description, Set<Permission> permissions, Integer priority) {
        this.name = name;
        this.description = description;
        this.permissions = permissions != null ? permissions : Set.of();
        this.priority = priority != null ? priority : 1;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        checkInvariants();
    }
    
    // Конструктор для восстановления из базы данных
    public Role(Long id, String name, String description, Set<Permission> permissions, 
                Integer priority, Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissions = permissions != null ? permissions : Set.of();
        this.priority = priority;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        
        checkInvariants();
    }
    
    /**
     * Проверить, есть ли у роли определенное разрешение
     */
    public boolean hasPermission(String permissionName) {
        return permissions.stream()
            .anyMatch(permission -> permission.getName().equals(permissionName));
    }
    
    /**
     * Добавить разрешение к роли
     */
    public void addPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission не может быть null");
        }
        
        this.permissions = Set.copyOf(
            Set.<Permission>builder()
                .addAll(this.permissions)
                .add(permission)
                .build()
        );
        this.updatedAt = LocalDateTime.now();
        checkInvariants();
    }
    
    /**
     * Удалить разрешение из роли
     */
    public void removePermission(Permission permission) {
        this.permissions = permissions.stream()
            .filter(p -> !p.equals(permission))
            .collect(java.util.stream.Collectors.toSet());
        this.updatedAt = LocalDateTime.now();
        checkInvariants();
    }
    
    /**
     * Активировать роль
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Деактивировать роль
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public Long getId() {
        return id;
    }
    
    @Override
    public void checkInvariants() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role должна иметь название");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Название роли не может быть длиннее 50 символов");
        }
        if (priority == null || priority < 1) {
            throw new IllegalArgumentException("Приоритет роли должен быть положительным числом");
        }
        if (isActive == null) {
            throw new IllegalArgumentException("Статус активности роли не может быть null");
        }
        if (permissions == null) {
            throw new IllegalArgumentException("Набор разрешений не может быть null");
        }
    }
    
    // Геттеры
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Set<Permission> getPermissions() { return Set.copyOf(permissions); }
    public Integer getPriority() { return priority; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
} 
