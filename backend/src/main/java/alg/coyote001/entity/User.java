package alg.coyote001.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entity модель для пользователей системы
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_status", columnList = "status"),
        @Index(name = "idx_user_telegram", columnList = "telegramId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Имя пользователя (уникальное)
     */
    @Column(unique = true, length = 50)
    private String username;
    
    /**
     * Email (уникальный)
     */
    @Column(unique = true, length = 100)
    private String email;
    
    /**
     * Хэш пароля
     */
    @Column(length = 255)
    private String passwordHash;
    
    /**
     * Полное имя
     */
    @Column(length = 100)
    private String fullName;
    
    /**
     * Имя (для Telegram)
     */
    @Column(length = 50)
    private String firstName;
    
    /**
     * Фамилия (для Telegram)
     */
    @Column(length = 50)
    private String lastName;
    
    /**
     * Telegram ID пользователя
     */
    @Column(unique = true)
    private Long telegramId;
    
    /**
     * Telegram username
     */
    @Column(length = 50)
    private String telegramUsername;
    
    /**
     * URL фото профиля (Telegram)
     */
    @Column(length = 500)
    private String photoUrl;
    
    /**
     * Дата авторизации через Telegram
     */
    private Long authDate;
    
    /**
     * Роли пользователя
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
    
    /**
     * Статус пользователя
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
    
    /**
     * Дата последнего входа
     */
    private LocalDateTime lastLoginAt;
    
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
     * Enum для статуса пользователя
     */
    public enum UserStatus {
        ACTIVE,     // Активный пользователь
        INACTIVE,   // Неактивный
        BLOCKED,    // Заблокирован
        PENDING     // Ожидает активации
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
     * Getter for password (for Spring Security compatibility)
     */
    public String getPassword() {
        return this.passwordHash;
    }
    
    /**
     * Setter for password (for Spring Security compatibility)
     */
    public void setPassword(String password) {
        this.passwordHash = password;
    }
    
    /**
     * Проверить, есть ли у пользователя определенное право
     */
    public boolean hasPermission(String permission) {
        if (roles == null) return false;
        return roles.stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(perm -> perm.getName().equals(permission));
    }
    
    /**
     * Проверить, есть ли у пользователя определенная роль
     */
    public boolean hasRole(String roleName) {
        if (roles == null) return false;
        return roles.stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }
} 