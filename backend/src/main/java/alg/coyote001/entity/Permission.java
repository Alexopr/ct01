package alg.coyote001.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity модель для прав доступа
 */
@Entity
@Table(name = "permissions", indexes = {
        @Index(name = "idx_permission_name", columnList = "name"),
        @Index(name = "idx_permission_category", columnList = "category")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Название права (уникальное)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    /**
     * Описание права
     */
    @Column(length = 255)
    private String description;
    
    /**
     * Категория права (COIN_MANAGEMENT, EXCHANGE_MANAGEMENT, USER_MANAGEMENT, SYSTEM_SETTINGS)
     */
    @Column(nullable = false, length = 50)
    private String category;
    
    /**
     * Уровень права (READ, WRITE, DELETE, ADMIN)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionLevel level;
    
    /**
     * Активно ли право
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
    
    /**
     * Enum для уровней прав доступа
     */
    public enum PermissionLevel {
        READ,     // Только чтение
        WRITE,    // Чтение и запись
        DELETE,   // Чтение, запись и удаление
        ADMIN     // Полный доступ
    }
    
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
     * Проверить, включает ли данное право указанный уровень доступа
     */
    public boolean includesLevel(PermissionLevel requiredLevel) {
        return this.level.ordinal() >= requiredLevel.ordinal();
    }
} 