package com.ct01.user.infrastructure.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity для Permission
 */
@Entity
@Table(name = "permissions",
    uniqueConstraints = @UniqueConstraint(columnNames = "name"),
    indexes = @Index(columnList = "name")
)
public class PermissionJpaEntity {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Конструкторы
    protected PermissionJpaEntity() {}
    
    public PermissionJpaEntity(String id, String name, String description, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }
    
    // Getters и Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 
