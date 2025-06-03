package alg.coyote001.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entity модель для ролей пользователей
 */
@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Название роли (уникальное)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    /**
     * Описание роли
     */
    @Column(length = 255)
    private String description;
    
    /**
     * Права доступа для роли
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
    
    /**
     * Приоритет роли (выше число - больше прав)
     */
    @Column(nullable = false)
    private Integer priority = 1;
    
    /**
     * Активна ли роль
     */
    @Column(nullable = false)
    private Boolean isActive = true;
    
    /**
     * Дата создания
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Дата обновления
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Проверить, есть ли у роли определенное право
     */
    public boolean hasPermission(String permission) {
        if (permissions == null) return false;
        return permissions.stream()
            .anyMatch(perm -> perm.getName().equals(permission));
    }
} 