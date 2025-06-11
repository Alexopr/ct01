package com.ct01.subscription.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO для планов подписки
 */
@Schema(description = "План подписки")
public record ApiSubscriptionPlanDto(
    @Schema(description = "ID плана", example = "premium")
    @JsonProperty("planId")
    String planId,
    
    @Schema(description = "Название плана", example = "Premium")
    @JsonProperty("name") 
    String name,
    
    @Schema(description = "Описание плана", example = "Расширенные возможности")
    @JsonProperty("description")
    String description,
    
    @Schema(description = "Цена в месяц", example = "29.99")
    @JsonProperty("monthlyPrice")
    BigDecimal monthlyPrice,
    
    @Schema(description = "Цена в год", example = "299.99")
    @JsonProperty("yearlyPrice")
    BigDecimal yearlyPrice,
    
    @Schema(description = "Активен ли план", example = "true")
    @JsonProperty("isActive")
    Boolean isActive,
    
    @Schema(description = "Популярный план", example = "false")
    @JsonProperty("isPopular")
    Boolean isPopular,
    
    @Schema(description = "Текущий план пользователя", example = "false")
    @JsonProperty("isCurrent")
    Boolean isCurrent,
    
    @Schema(description = "Лимиты плана")
    @JsonProperty("limits")
    Map<String, Object> limits,
    
    @Schema(description = "Функции плана")
    @JsonProperty("features")
    Map<String, Object> features
) {} 