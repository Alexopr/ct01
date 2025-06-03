package alg.coyote001.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для информации об использовании лимитов
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageLimitDto {
    
    private String moduleName;
    private String resourceType;
    private int currentUsage;
    private int limit;
    private int remaining;
    private double usagePercentage;
    private boolean isLimitExceeded;
    private String resetPeriod;
    private boolean isUnlimited;
    
    /**
     * Создать для неограниченного ресурса
     */
    public static UsageLimitDto unlimited(String moduleName, String resourceType) {
        return UsageLimitDto.builder()
                .moduleName(moduleName)
                .resourceType(resourceType)
                .currentUsage(0)
                .limit(-1)
                .remaining(Integer.MAX_VALUE)
                .usagePercentage(0.0)
                .isLimitExceeded(false)
                .isUnlimited(true)
                .build();
    }
    
    /**
     * Создать для недоступного ресурса
     */
    public static UsageLimitDto unavailable(String moduleName, String resourceType) {
        return UsageLimitDto.builder()
                .moduleName(moduleName)
                .resourceType(resourceType)
                .currentUsage(0)
                .limit(0)
                .remaining(0)
                .usagePercentage(0.0)
                .isLimitExceeded(true)
                .isUnlimited(false)
                .build();
    }
    
    /**
     * Получить статус использования как строку
     */
    public String getUsageStatus() {
        if (isUnlimited) {
            return "unlimited";
        }
        if (limit == 0) {
            return "unavailable";
        }
        if (isLimitExceeded) {
            return "exceeded";
        }
        if (usagePercentage >= 80) {
            return "warning";
        }
        return "normal";
    }
} 