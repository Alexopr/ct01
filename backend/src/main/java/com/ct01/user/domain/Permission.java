package com.ct01.user.domain;

import com.ct01.core.domain.Entity;

import java.time.LocalDateTime;

/**
 * Permission Entity в User домене.
 * Представляет разрешение (право доступа) в системе.
 */
public class Permission implements Entity<Long> {
    
    private Long id;
    private String name;
    private String description;
    private String category;
    private PermissionLevel level;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Enum для уровней прав доступа
     */
    public enum PermissionLevel {
        READ,     // Только чтение
        WRITE,    // Чтение и запись
        DELETE,   // Чтение, запись и удаление
        ADMIN     // Полный доступ
    }
    
    // Конструктор для создания нового разрешения
    public Permission(String name, String description, String category, PermissionLevel level) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.level = level;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        checkInvariants();
    }
    
    // Конструктор для восстановления из базы данных
    public Permission(Long id, String name, String description, String category, 
                     PermissionLevel level, Boolean isActive, 
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.level = level;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        
        checkInvariants();
    }
    
    /**
     * Проверить, включает ли данное разрешение указанный уровень доступа
     */
    public boolean includesLevel(PermissionLevel requiredLevel) {
        return this.level.ordinal() >= requiredLevel.ordinal();
    }
    
    /**
     * Активировать разрешение
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Деактивировать разрешение
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Обновить описание разрешения
     */
    public void updateDescription(String newDescription) {
        this.description = newDescription;
        this.updatedAt = LocalDateTime.now();
        checkInvariants();
    }
    
    @Override
    public Long getId() {
        return id;
    }
    
    @Override
    public void checkInvariants() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission должно иметь название");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Название разрешения не может быть длиннее 100 символов");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission должно иметь категорию");
        }
        if (category.length() > 50) {
            throw new IllegalArgumentException("Категория разрешения не может быть длиннее 50 символов");
        }
        if (level == null) {
            throw new IllegalArgumentException("Permission должно иметь уровень доступа");
        }
        if (isActive == null) {
            throw new IllegalArgumentException("Статус активности разрешения не может быть null");
        }
    }
    
    // Геттеры
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public PermissionLevel getLevel() { return level; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
} 
