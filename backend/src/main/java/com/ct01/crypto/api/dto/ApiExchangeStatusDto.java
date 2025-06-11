package com.ct01.crypto.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO для статуса биржи
 */
@Schema(description = "Статус биржи")
public record ApiExchangeStatusDto(
    @Schema(description = "Название биржи", example = "BINANCE")
    @JsonProperty("exchangeName")
    String exchangeName,
    
    @Schema(description = "Доступна ли биржа", example = "true")
    @JsonProperty("isHealthy") 
    Boolean isHealthy,
    
    @Schema(description = "Время ответа в мс", example = "150")
    @JsonProperty("responseTime")
    Long responseTime,
    
    @Schema(description = "Время последней проверки")
    @JsonProperty("lastChecked")
    LocalDateTime lastChecked,
    
    @Schema(description = "Статус API")
    @JsonProperty("apiStatus")
    String apiStatus,
    
    @Schema(description = "Ошибки если есть")
    @JsonProperty("errors")
    String errors,
    
    @Schema(description = "Дополнительные данные статуса")
    @JsonProperty("additionalInfo")
    Map<String, Object> additionalInfo
) {
    
    /**
     * Создать DTO из мапы статуса
     */
    public static ApiExchangeStatusDto from(String exchangeName, Map<String, Object> statusMap) {
        return new ApiExchangeStatusDto(
            exchangeName,
            (Boolean) statusMap.getOrDefault("healthy", false),
            ((Number) statusMap.getOrDefault("responseTime", 0L)).longValue(),
            (LocalDateTime) statusMap.get("lastChecked"),
            (String) statusMap.getOrDefault("apiStatus", "UNKNOWN"),
            (String) statusMap.get("errors"),
            statusMap
        );
    }
} 