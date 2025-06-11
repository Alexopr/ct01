package com.ct01.crypto.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO для передачи статуса бирж
 */
@Schema(description = "Статус доступности криптовалютных бирж")
public record ExchangeStatusDto(
        
        @Schema(description = "Общее количество бирж", example = "3")
        @JsonProperty("total_exchanges")
        int totalExchanges,
        
        @Schema(description = "Количество доступных бирж", example = "2")
        @JsonProperty("available_exchanges")
        int availableExchanges,
        
        @Schema(description = "Статус каждой биржи")
        @JsonProperty("exchange_status")
        Map<String, Boolean> exchangeStatus,
        
        @Schema(description = "Список доступных бирж")
        @JsonProperty("available_list")
        List<String> availableList,
        
        @Schema(description = "Список недоступных бирж")
        @JsonProperty("unavailable_list")
        List<String> unavailableList,
        
        @Schema(description = "Время проверки статуса", example = "2024-01-15T10:30:00")
        @JsonProperty("check_timestamp")
        LocalDateTime checkTimestamp,
        
        @Schema(description = "Общий статус системы бирж", example = "HEALTHY")
        @JsonProperty("overall_status")
        String overallStatus
) {
    
    /**
     * Создать DTO из Map статусов бирж
     */
    public ExchangeStatusDto(Map<String, Boolean> exchangeStatus) {
        this(
                exchangeStatus.size(),
                (int) exchangeStatus.values().stream().mapToLong(available -> available ? 1 : 0).sum(),
                exchangeStatus,
                exchangeStatus.entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .sorted()
                        .toList(),
                exchangeStatus.entrySet().stream()
                        .filter(entry -> !entry.getValue())
                        .map(Map.Entry::getKey)
                        .sorted()
                        .toList(),
                LocalDateTime.now(),
                determineOverallStatus(exchangeStatus)
        );
    }
    
    /**
     * Определить общий статус системы на основе статусов бирж
     */
    private static String determineOverallStatus(Map<String, Boolean> exchangeStatus) {
        if (exchangeStatus.isEmpty()) {
            return "NO_EXCHANGES";
        }
        
        long availableCount = exchangeStatus.values().stream().mapToLong(available -> available ? 1 : 0).sum();
        
        if (availableCount == exchangeStatus.size()) {
            return "HEALTHY";
        } else if (availableCount > 0) {
            return "PARTIAL";
        } else {
            return "CRITICAL";
        }
    }
    
    /**
     * Проверить, доступна ли конкретная биржа
     */
    public boolean isExchangeAvailable(String exchangeName) {
        return exchangeStatus.getOrDefault(exchangeName.toUpperCase(), false);
    }
    
    /**
     * Получить процент доступности бирж
     */
    public double getAvailabilityPercentage() {
        if (totalExchanges == 0) {
            return 0.0;
        }
        return (double) availableExchanges / totalExchanges * 100.0;
    }
} 
