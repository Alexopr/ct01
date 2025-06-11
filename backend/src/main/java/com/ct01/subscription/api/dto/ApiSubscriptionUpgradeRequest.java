package com.ct01.subscription.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO для запроса обновления подписки
 */
@Schema(description = "Запрос на обновление подписки")
public record ApiSubscriptionUpgradeRequest(
    @Schema(description = "ID нового плана", example = "premium", required = true)
    @JsonProperty("planId")
    @NotBlank(message = "ID плана обязателен")
    String planId,
    
    @Schema(description = "Тип биллинга", example = "MONTHLY")
    @JsonProperty("billingType")
    BillingType billingType,
    
    @Schema(description = "Автопродление", example = "true")
    @JsonProperty("autoRenewal")
    Boolean autoRenewal,
    
    @Schema(description = "Промокод", example = "SAVE20")
    @JsonProperty("promoCode")
    String promoCode
) {
    
    /**
     * Тип биллинга
     */
    @Schema(description = "Тип биллинга")
    public enum BillingType {
        @Schema(description = "Ежемесячный")
        MONTHLY,
        
        @Schema(description = "Годовой")
        YEARLY
    }
} 