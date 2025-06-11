package com.ct01.subscription.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO для статуса подписки
 */
@Schema(description = "Статус подписки пользователя")
public record ApiSubscriptionStatusDto(
    @Schema(description = "ID подписки", example = "1")
    @JsonProperty("id")
    Long id,
    
    @Schema(description = "ID пользователя", example = "123")
    @JsonProperty("userId") 
    Long userId,
    
    @Schema(description = "ID плана", example = "premium")
    @JsonProperty("planId")
    String planId,
    
    @Schema(description = "Название плана", example = "Premium")
    @JsonProperty("planName")
    String planName,
    
    @Schema(description = "Статус подписки")
    @JsonProperty("status")
    SubscriptionStatus status,
    
    @Schema(description = "Активна ли подписка", example = "true")
    @JsonProperty("active")
    Boolean active,
    
    @Schema(description = "Автопродление", example = "true")
    @JsonProperty("autoRenewal")
    Boolean autoRenewal,
    
    @Schema(description = "Дата начала")
    @JsonProperty("startDate")
    LocalDateTime startDate,
    
    @Schema(description = "Дата окончания")
    @JsonProperty("endDate")
    LocalDateTime endDate,
    
    @Schema(description = "Активен ли пробный период", example = "false")
    @JsonProperty("trialActive")
    Boolean trialActive,
    
    @Schema(description = "Дата окончания пробного периода")
    @JsonProperty("trialEndDate")
    LocalDateTime trialEndDate,
    
    @Schema(description = "Дата следующего платежа")
    @JsonProperty("nextBillingDate")
    LocalDateTime nextBillingDate
) {
    
    /**
     * Статус подписки
     */
    @Schema(description = "Статус подписки")
    public enum SubscriptionStatus {
        @Schema(description = "Активная подписка")
        ACTIVE,
        
        @Schema(description = "Неактивная подписка")
        INACTIVE,
        
        @Schema(description = "Отмененная подписка")
        CANCELLED,
        
        @Schema(description = "Истекшая подписка")
        EXPIRED
    }
} 