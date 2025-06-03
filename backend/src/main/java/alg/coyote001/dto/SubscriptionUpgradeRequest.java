package alg.coyote001.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

/**
 * Request DTO для обновления подписки
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionUpgradeRequest {
    
    @NotBlank(message = "План подписки обязателен")
    @Pattern(regexp = "FREE|PREMIUM", message = "Недопустимый тип плана")
    private String planType;
    
    @NotNull(message = "Цена обязательна")
    private BigDecimal price;
    
    @NotBlank(message = "Валюта обязательна")
    private String currency;
    
    private String paymentTransactionId;
    
    private String paymentProvider;
    
    private String paymentMetadata;
    
    private boolean autoRenewal = true;
} 