package alg.coyote001.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity для отслеживания использования лимитов подписки
 */
@Entity
@Table(name = "subscription_usage", indexes = {
        @Index(name = "idx_usage_user_date", columnList = "user_id, usage_date"),
        @Index(name = "idx_usage_module", columnList = "module_name"),
        @Index(name = "idx_usage_reset", columnList = "reset_period")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionUsage {
    
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
     * Название модуля
     */
    @Column(name = "module_name", nullable = false, length = 50)
    private String moduleName;
    
    /**
     * Тип ресурса/лимита
     */
    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;
    
    /**
     * Дата использования
     */
    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;
    
    /**
     * Количество использованных ресурсов
     */
    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;
    
    /**
     * Максимальный лимит
     */
    @Column(name = "limit_count", nullable = false)
    private Integer limitCount;
    
    /**
     * Период сброса лимита
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reset_period", nullable = false)
    private ResetPeriod resetPeriod;
    
    /**
     * Метаданные использования (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
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
     * Периоды сброса лимитов
     */
    public enum ResetPeriod {
        DAILY,      // Ежедневный сброс
        WEEKLY,     // Еженедельный сброс
        MONTHLY,    // Ежемесячный сброс
        YEARLY,     // Ежегодный сброс
        NEVER       // Никогда не сбрасывается
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
     * Проверить, превышен ли лимит
     */
    public boolean isLimitExceeded() {
        return usedCount >= limitCount;
    }
    
    /**
     * Получить процент использования лимита
     */
    public double getUsagePercentage() {
        if (limitCount == 0) return 0.0;
        return (double) usedCount / limitCount * 100.0;
    }
    
    /**
     * Получить оставшееся количество
     */
    public int getRemainingCount() {
        return Math.max(0, limitCount - usedCount);
    }
    
    /**
     * Увеличить счетчик использования
     */
    public void incrementUsage() {
        incrementUsage(1);
    }
    
    /**
     * Увеличить счетчик использования на определенное количество
     */
    public void incrementUsage(int count) {
        this.usedCount += count;
    }
    
    /**
     * Сбросить счетчик использования
     */
    public void resetUsage() {
        this.usedCount = 0;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Проверить, нужно ли сбросить лимит
     */
    public boolean shouldReset(LocalDate currentDate) {
        switch (resetPeriod) {
            case DAILY:
                return !usageDate.equals(currentDate);
            case WEEKLY:
                return !usageDate.equals(currentDate) && 
                       currentDate.minusDays(currentDate.getDayOfWeek().getValue() - 1).isAfter(usageDate);
            case MONTHLY:
                return !usageDate.equals(currentDate) && 
                       currentDate.withDayOfMonth(1).isAfter(usageDate);
            case YEARLY:
                return !usageDate.equals(currentDate) && 
                       currentDate.withDayOfYear(1).isAfter(usageDate);
            case NEVER:
            default:
                return false;
        }
    }
} 