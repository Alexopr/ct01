package alg.coyote001.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO для информации о плане подписки
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDto {
    
    private String planType;
    private String name;
    private BigDecimal price;
    private String currency;
    private String billingCycle;
    private int trialDays;
    private int refundDays;
    private List<String> features;
    private Map<String, Object> limits;
    private boolean isCurrent;
    private boolean isRecommended;
    
    /**
     * Проверить, является ли план бесплатным
     */
    public boolean isFree() {
        return price == null || price.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Проверить, есть ли trial период
     */
    public boolean hasTrialPeriod() {
        return trialDays > 0;
    }
} 