package com.ct01.subscription.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для результата проверки лимита ресурса
 */
@Schema(description = "Результат проверки лимита ресурса")
public record ApiResourceLimitCheckDto(
    @Schema(description = "Название модуля", example = "crypto")
    @JsonProperty("moduleName")
    String moduleName,
    
    @Schema(description = "Тип ресурса", example = "api_requests")
    @JsonProperty("resourceType") 
    String resourceType,
    
    @Schema(description = "Запрашиваемое количество", example = "5")
    @JsonProperty("requestedAmount")
    Integer requestedAmount,
    
    @Schema(description = "Разрешен ли запрос", example = "true")
    @JsonProperty("allowed")
    Boolean allowed,
    
    @Schema(description = "Текущий лимит", example = "10000")
    @JsonProperty("currentLimit")
    Long currentLimit,
    
    @Schema(description = "Использовано", example = "2500")
    @JsonProperty("usedAmount")
    Long usedAmount,
    
    @Schema(description = "Остаток после запроса", example = "7495")
    @JsonProperty("remainingAfterRequest")
    Long remainingAfterRequest,
    
    @Schema(description = "Сообщение", example = "Запрос разрешен")
    @JsonProperty("message")
    String message
) {} 