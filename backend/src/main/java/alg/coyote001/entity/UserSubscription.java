package alg.coyote001.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity для пользовательских подписок
 */
@Entity
@Table(name = "user_subscriptions", indexes = {
        @Index(name = "idx_subscription_user", columnList = "user_id"),
        @Index(name = "idx_subscription_plan", columnList = "plan_type"),
        @Index(name = "idx_subscription_status", columnList = "status"),
        @Index(name = "idx_subscription_expiry", columnList = "expiresAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Пользователь
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Тип плана подписки
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;
    
    /**
     * Статус подписки
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;
    
    /**
     * Дата начала подписки
     */
    @Column(nullable = false)
    private LocalDateTime startsAt;
    
    /**
     * Дата окончания подписки
     */
    private LocalDateTime expiresAt;
    
    /**
     * Период trial (если применимо)
     */
    private LocalDateTime trialEndsAt;
    
    /**
     * Автоматическое продление
     */
    @Column(nullable = false)
    private boolean autoRenewal = true;
    
    /**
     * Цена подписки
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    /**
     * Валюта платежа
     */
    @Column(length = 10)
    private String currency;
    
    /**
     * ID транзакции платежа
     */
    @Column(length = 255)
    private String paymentTransactionId;
    
    /**
     * Провайдер платежа
     */
    @Column(length = 50)
    private String paymentProvider;
    
    /**
     * Метаданные платежа (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String paymentMetadata;
    
    /**
     * Дата следующего платежа
     */
    private LocalDateTime nextBillingDate;
    
    /**
     * Дата отмены подписки
     */
    private LocalDateTime cancelledAt;
    
    /**
     * Причина отмены
     */
    @Column(length = 500)
    private String cancellationReason;
    
    /**
     * Дата создания записи
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Дата обновления записи
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Типы планов подписки
     */
    public enum PlanType {
        FREE,
        PREMIUM
    }
    
    /**
     * Статусы подписки
     */
    public enum SubscriptionStatus {
        ACTIVE,          // Активная подписка
        TRIAL,           // Trial период
        EXPIRED,         // Истекшая
        CANCELLED,       // Отмененная
        PENDING_PAYMENT, // Ожидает оплаты
        SUSPENDED,       // Приостановлена
        GRACE_PERIOD     // Льготный период после истечения
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
     * Проверить, активна ли подписка
     */
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIAL) &&
               (expiresAt == null || expiresAt.isAfter(now));
    }
    
    /**
     * Проверить, находится ли подписка в trial периоде
     */
    public boolean isInTrial() {
        LocalDateTime now = LocalDateTime.now();
        return status == SubscriptionStatus.TRIAL &&
               trialEndsAt != null && trialEndsAt.isAfter(now);
    }
    
    /**
     * Проверить, истекла ли подписка
     */
    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        return expiresAt != null && expiresAt.isBefore(now) && 
               status != SubscriptionStatus.CANCELLED;
    }
    
    /**
     * Получить количество дней до истечения подписки
     */
    public long getDaysUntilExpiry() {
        if (expiresAt == null) return Long.MAX_VALUE;
        LocalDateTime now = LocalDateTime.now();
        return java.time.Duration.between(now, expiresAt).toDays();
    }
} 