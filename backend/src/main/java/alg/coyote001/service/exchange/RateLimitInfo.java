package alg.coyote001.service.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Информация о лимитах API биржи
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitInfo {
    
    /**
     * Текущее количество выполненных запросов
     */
    private int currentRequests;
    
    /**
     * Максимальное количество запросов в минуту
     */
    private int maxRequestsPerMinute;
    
    /**
     * Время сброса счетчика
     */
    private LocalDateTime resetTime;
    
    /**
     * Остаток доступных запросов
     */
    private int remainingRequests;
    
    /**
     * Процент использованных лимитов
     */
    private double usagePercentage;
    
    /**
     * Рекомендуемая задержка между запросами в миллисекундах
     */
    private long recommendedDelayMs;
    
    /**
     * Статус лимитов
     */
    private RateLimitStatus status;
    
    public enum RateLimitStatus {
        NORMAL,     // Нормальное использование
        WARNING,    // Приближение к лимиту (>70%)
        CRITICAL,   // Критическое использование (>90%)
        EXCEEDED    // Лимит превышен
    }
    
    /**
     * Вычислить процент использования
     */
    public void calculateUsage() {
        this.remainingRequests = maxRequestsPerMinute - currentRequests;
        this.usagePercentage = (double) currentRequests / maxRequestsPerMinute * 100;
        
        if (currentRequests >= maxRequestsPerMinute) {
            this.status = RateLimitStatus.EXCEEDED;
            this.recommendedDelayMs = 60000; // 1 минута
        } else if (usagePercentage >= 90) {
            this.status = RateLimitStatus.CRITICAL;
            this.recommendedDelayMs = 5000; // 5 секунд
        } else if (usagePercentage >= 70) {
            this.status = RateLimitStatus.WARNING;
            this.recommendedDelayMs = 2000; // 2 секунды
        } else {
            this.status = RateLimitStatus.NORMAL;
            this.recommendedDelayMs = 1000; // 1 секунда
        }
    }
} 