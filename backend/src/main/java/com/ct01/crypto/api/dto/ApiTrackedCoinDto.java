package com.ct01.crypto.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * API DTO для представления отслеживаемой монеты
 */
@Schema(description = "Отслеживаемая криптовалютная монета с настройками мониторинга")
public record ApiTrackedCoinDto(
    
    @Schema(description = "Уникальный идентификатор", example = "1")
    Long id,
    
    @Schema(description = "Информация о монете")
    @Valid
    @NotNull(message = "Информация о монете обязательна")
    ApiCoinDto coin,
    
    @Schema(description = "Список бирж для отслеживания", example = "[\"BINANCE\", \"BYBIT\"]")
    @NotEmpty(message = "Должна быть указана хотя бы одна биржа")
    Set<String> exchanges,
    
    @Schema(description = "Список котируемых валют", example = "[\"USDT\", \"BTC\"]")
    @NotEmpty(message = "Должна быть указана хотя бы одна котируемая валюта")
    @JsonProperty("quoteCurrencies")
    Set<@Pattern(regexp = "^[A-Z0-9]+$", message = "Котируемая валюта может содержать только заглавные буквы и цифры") String> quoteCurrencies,
    
    @Schema(description = "Активна ли монета для отслеживания", example = "true")
    @JsonProperty("isActive")
    @NotNull(message = "Статус активности обязателен")
    Boolean isActive,
    
    @Schema(description = "Интервал опроса в секундах", example = "30")
    @Min(value = 1, message = "Интервал опроса должен быть не менее 1 секунды")
    @Max(value = 86400, message = "Интервал опроса не может превышать 24 часа (86400 секунд)")
    Integer pollingIntervalSeconds,
    
    @Schema(description = "Включен ли WebSocket для этой монеты", example = "true")
    @JsonProperty("websocketEnabled")
    Boolean websocketEnabled,
    
    @Schema(description = "Приоритет отслеживания (1-10)", example = "5")
    @Min(value = 1, message = "Приоритет должен быть не менее 1")
    @Max(value = 10, message = "Приоритет не может превышать 10")
    Integer priority,
    
    @Schema(description = "Дополнительные заметки", example = "Высоковолатильная монета")
    @Size(max = 500, message = "Заметки не могут превышать 500 символов")
    String notes,
    
    @Schema(description = "Время создания записи")
    LocalDateTime createdAt,
    
    @Schema(description = "Время последнего обновления")
    LocalDateTime updatedAt
) {
    
    /**
     * Фабричный метод для создания API DTO из domain объекта
     */
    public static ApiTrackedCoinDto from(com.ct01.crypto.domain.TrackedCoin domainTrackedCoin) {
        return new ApiTrackedCoinDto(
            domainTrackedCoin.getId(),
            ApiCoinDto.from(new com.ct01.crypto.domain.Coin(
                domainTrackedCoin.getSymbol(), 
                domainTrackedCoin.getName()
            )),
            domainTrackedCoin.getExchanges().stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toSet()),
            domainTrackedCoin.getQuoteCurrencies(),
            domainTrackedCoin.getIsActive(),
            domainTrackedCoin.getPollingIntervalSeconds(),
            domainTrackedCoin.getWebsocketEnabled(),
            domainTrackedCoin.getPriority(),
            domainTrackedCoin.getNotes(),
            domainTrackedCoin.getCreatedAt(),
            domainTrackedCoin.getUpdatedAt()
        );
    }
    
    /**
     * Создание request DTO без системных полей для создания монеты
     */
    public static ApiTrackedCoinDto forRequest(
            ApiCoinDto coin,
            Set<String> exchanges,
            Set<String> quoteCurrencies,
            Boolean isActive,
            Integer pollingIntervalSeconds,
            Boolean websocketEnabled,
            Integer priority,
            String notes) {
        return new ApiTrackedCoinDto(
            null, coin, exchanges, quoteCurrencies, isActive,
            pollingIntervalSeconds, websocketEnabled, priority, notes,
            null, null
        );
    }
    
    /**
     * Конвертация exchanges из строк в enum
     */
    public Set<com.ct01.crypto.domain.TrackedCoin.Exchange> getExchangesAsEnum() {
        return exchanges.stream()
            .map(com.ct01.crypto.domain.TrackedCoin.Exchange::valueOf)
            .collect(java.util.stream.Collectors.toSet());
    }
} 
