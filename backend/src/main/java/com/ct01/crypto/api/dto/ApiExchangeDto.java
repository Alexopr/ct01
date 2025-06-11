package com.ct01.crypto.api.dto;

import com.ct01.crypto.domain.Exchange;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для API представления биржи
 */
@Schema(description = "Информация о криптовалютной бирже")
public record ApiExchangeDto(
    @Schema(description = "Уникальный идентификатор", example = "1")
    @JsonProperty("id")
    Long id,
    
    @Schema(description = "Название биржи", example = "BINANCE")
    @JsonProperty("name") 
    String name,
    
    @Schema(description = "Отображаемое название", example = "Binance")
    @JsonProperty("displayName")
    String displayName,
    
    @Schema(description = "Базовый URL API", example = "https://api.binance.com")
    @JsonProperty("baseUrl")
    String baseUrl,
    
    @Schema(description = "Активна ли биржа", example = "true")
    @JsonProperty("isActive") 
    Boolean isActive,
    
    @Schema(description = "Лимит запросов в минуту", example = "1200")
    @JsonProperty("rateLimitPerMinute")
    Integer rateLimitPerMinute,
    
    @Schema(description = "Таймаут подключения в мс", example = "5000")
    @JsonProperty("connectionTimeout")
    Integer connectionTimeout,
    
    @Schema(description = "Таймаут чтения в мс", example = "10000") 
    @JsonProperty("readTimeout")
    Integer readTimeout,
    
    @Schema(description = "Количество попыток повтора", example = "3")
    @JsonProperty("retryAttempts")
    Integer retryAttempts,
    
    @Schema(description = "Статус здоровья биржи")
    @JsonProperty("healthStatus")
    HealthStatus healthStatus,
    
    @Schema(description = "Время последней проверки здоровья")
    @JsonProperty("lastHealthCheck")
    LocalDateTime lastHealthCheck,
    
    @Schema(description = "Поддерживаемые монеты")
    @JsonProperty("supportedCoins") 
    List<String> supportedCoins,
    
    @Schema(description = "Время создания")
    @JsonProperty("createdAt")
    LocalDateTime createdAt,
    
    @Schema(description = "Время обновления")
    @JsonProperty("updatedAt")
    LocalDateTime updatedAt
) {
    
    /**
     * Создать DTO из доменной модели
     */
    public static ApiExchangeDto from(Exchange exchange) {
        return new ApiExchangeDto(
            exchange.getId(),
            exchange.getName(),
            exchange.getDisplayName(),
            exchange.getBaseUrl(),
            exchange.isActive(),
            exchange.getRateLimitPerMinute(),
            exchange.getConnectionTimeout(),
            exchange.getReadTimeout(),
            exchange.getRetryAttempts(),
            HealthStatus.from(exchange.getHealthStatus()),
            exchange.getLastHealthCheck(),
            exchange.getSupportedCoins(),
            exchange.getCreatedAt(),
            exchange.getUpdatedAt()
        );
    }
    
    /**
     * Статус здоровья биржи
     */
    @Schema(description = "Статус здоровья биржи")
    public enum HealthStatus {
        @Schema(description = "Биржа работает нормально")
        HEALTHY,
        
        @Schema(description = "Биржа недоступна")
        UNHEALTHY,
        
        @Schema(description = "Статус неизвестен")
        UNKNOWN;
        
        public static HealthStatus from(com.ct01.crypto.domain.ExchangeHealthStatus domainStatus) {
            if (domainStatus == null) return UNKNOWN;
            
            return switch (domainStatus) {
                case HEALTHY -> HEALTHY;
                case UNHEALTHY -> UNHEALTHY;
                case UNKNOWN -> UNKNOWN;
            };
        }
    }
} 