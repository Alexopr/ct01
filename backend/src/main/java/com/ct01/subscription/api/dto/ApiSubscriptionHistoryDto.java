package com.ct01.subscription.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для записи истории подписки
 */
@Schema(description = "Запись истории подписки")
public record ApiSubscriptionHistoryDto(
    @Schema(description = "ID записи", example = "1")
    @JsonProperty("id")
    Long id,
    
    @Schema(description = "ID плана", example = "premium")
    @JsonProperty("planId")
    String planId,
    
    @Schema(description = "Название плана", example = "Premium")
    @JsonProperty("planName")
    String planName,
    
    @Schema(description = "Действие", example = "UPGRADED")
    @JsonProperty("action")
    SubscriptionAction action,
    
    @Schema(description = "Дата действия")
    @JsonProperty("actionDate")
    LocalDateTime actionDate,
    
    @Schema(description = "Дата начала действия плана")
    @JsonProperty("startDate")
    LocalDateTime startDate,
    
    @Schema(description = "Дата окончания действия плана")
    @JsonProperty("endDate")
    LocalDateTime endDate,
    
    @Schema(description = "Стоимость", example = "29.99")
    @JsonProperty("amount")
    BigDecimal amount,
    
    @Schema(description = "Валюта", example = "USD")
    @JsonProperty("currency")
    String currency,
    
    @Schema(description = "Комментарий", example = "Автоматическое продление")
    @JsonProperty("comment")
    String comment
) {
    
    /**
     * Действие с подпиской
     */
    @Schema(description = "Действие с подпиской")
    public enum SubscriptionAction {
        @Schema(description = "Создание подписки")
        CREATED,
        
        @Schema(description = "Обновление плана")
        UPGRADED,
        
        @Schema(description = "Понижение плана")
        DOWNGRADED,
        
        @Schema(description = "Продление")
        RENEWED,
        
        @Schema(description = "Отмена")
        CANCELLED,
        
        @Schema(description = "Истечение")
        EXPIRED,
        
        @Schema(description = "Приостановка")
        SUSPENDED,
        
        @Schema(description = "Возобновление")
        RESUMED
    }
} 