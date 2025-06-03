package alg.coyote001.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO для статуса подписки пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatusDto {
    
    private Long subscriptionId;
    private String planType;
    private String planName;
    private String status;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private LocalDateTime trialEndsAt;
    private LocalDateTime nextBillingDate;
    private BigDecimal price;
    private String currency;
    private boolean autoRenewal;
    private boolean isActive;
    private boolean isInTrial;
    private long daysUntilExpiry;
    private Map<String, UsageLimitDto> currentUsage;
    
    /**
     * Проверить, скоро ли истекает подписка
     */
    public boolean isExpiringSoon() {
        return daysUntilExpiry >= 0 && daysUntilExpiry <= 7;
    }
    
    /**
     * Проверить, можно ли продлить подписку
     */
    public boolean canRenew() {
        return "ACTIVE".equals(status) || "TRIAL".equals(status) || "GRACE_PERIOD".equals(status);
    }
} 