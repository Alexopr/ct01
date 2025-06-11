package com.ct01.subscription.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO для лимитов использования
 */
@Schema(description = "Лимит использования ресурса")
public record ApiUsageLimitDto(
    @Schema(description = "Название модуля", example = "crypto")
    @JsonProperty("moduleName")
    String moduleName,
    
    @Schema(description = "Тип ресурса", example = "api_requests")
    @JsonProperty("resourceType") 
    String resourceType,
    
    @Schema(description = "Максимальное количество", example = "10000")
    @JsonProperty("maxLimit")
    Long maxLimit,
    
    @Schema(description = "Использовано", example = "2500")
    @JsonProperty("usedAmount")
    Long usedAmount,
    
    @Schema(description = "Остаток", example = "7500")
    @JsonProperty("remainingAmount")
    Long remainingAmount,
    
    @Schema(description = "Процент использования", example = "25.0")
    @JsonProperty("usagePercentage")
    Double usagePercentage,
    
    @Schema(description = "Период сброса лимита", example = "MONTHLY")
    @JsonProperty("resetPeriod")
    String resetPeriod,
    
    @Schema(description = "Дата следующего сброса")
    @JsonProperty("nextResetDate")
    LocalDateTime nextResetDate,
    
    @Schema(description = "Превышен ли лимит", example = "false")
    @JsonProperty("isExceeded")
    Boolean isExceeded
) {} 