package com.ct01.crypto.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO для статистики биржи
 */
@Schema(description = "Статистика биржи")
public record ApiExchangeStatsDto(
    @Schema(description = "Название биржи", example = "BINANCE")
    @JsonProperty("exchangeName")
    String exchangeName,
    
    @Schema(description = "Общее количество запросов", example = "12540")
    @JsonProperty("totalRequests") 
    Long totalRequests,
    
    @Schema(description = "Успешные запросы", example = "12480")
    @JsonProperty("successfulRequests")
    Long successfulRequests,
    
    @Schema(description = "Неудачные запросы", example = "60")
    @JsonProperty("failedRequests")
    Long failedRequests,
    
    @Schema(description = "Среднее время ответа в мс", example = "245")
    @JsonProperty("averageResponseTime")
    Double averageResponseTime,
    
    @Schema(description = "Время работы в процентах", example = "99.5")
    @JsonProperty("uptime")
    Double uptime,
    
    @Schema(description = "Количество активных торговых пар", example = "850")
    @JsonProperty("activeTradingPairs")
    Integer activeTradingPairs,
    
    @Schema(description = "Время последнего обновления статистики")
    @JsonProperty("lastUpdated")
    LocalDateTime lastUpdated,
    
    @Schema(description = "Дополнительные метрики")
    @JsonProperty("additionalMetrics")
    Map<String, Object> additionalMetrics
) {
    
    /**
     * Создать DTO из мапы статистики
     */
    public static ApiExchangeStatsDto from(String exchangeName, Map<String, Object> statsMap) {
        return new ApiExchangeStatsDto(
            exchangeName,
            ((Number) statsMap.getOrDefault("totalRequests", 0L)).longValue(),
            ((Number) statsMap.getOrDefault("successfulRequests", 0L)).longValue(),
            ((Number) statsMap.getOrDefault("failedRequests", 0L)).longValue(),
            ((Number) statsMap.getOrDefault("averageResponseTime", 0.0)).doubleValue(),
            ((Number) statsMap.getOrDefault("uptime", 0.0)).doubleValue(),
            ((Number) statsMap.getOrDefault("activeTradingPairs", 0)).intValue(),
            (LocalDateTime) statsMap.get("lastUpdated"),
            statsMap
        );
    }
} 