package com.ct01.crypto.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API DTO для представления данных о цене
 */
@Schema(description = "Данные о цене криптовалютной монеты")
public record ApiPriceDataDto(
    
    @Schema(description = "Символ монеты", example = "BTC")
    String symbol,
    
    @Schema(description = "Котируемая валюта", example = "USDT")
    String quoteCurrency,
    
    @Schema(description = "Биржа", example = "BINANCE")
    String exchange,
    
    @Schema(description = "Цена открытия", example = "45000.00")
    BigDecimal openPrice,
    
    @Schema(description = "Максимальная цена", example = "46000.00")
    BigDecimal highPrice,
    
    @Schema(description = "Минимальная цена", example = "44500.00")
    BigDecimal lowPrice,
    
    @Schema(description = "Цена закрытия", example = "45800.00")
    BigDecimal closePrice,
    
    @Schema(description = "Объем торгов", example = "1250.75")
    BigDecimal volume,
    
    @Schema(description = "Время данных")
    LocalDateTime timestamp
) {
    
    /**
     * Фабричный метод для создания API DTO из domain объекта
     */
    public static ApiPriceDataDto from(com.ct01.crypto.domain.PriceHistory domainPriceHistory) {
        return new ApiPriceDataDto(
            domainPriceHistory.getSymbol(),
            domainPriceHistory.getQuoteCurrency(),
            domainPriceHistory.getExchange().name(),
            domainPriceHistory.getOpenPrice(),
            domainPriceHistory.getHighPrice(),
            domainPriceHistory.getLowPrice(),
            domainPriceHistory.getClosePrice(),
            domainPriceHistory.getVolume(),
            domainPriceHistory.getTimestamp()
        );
    }
}

/**
 * API DTO для статистики цен
 */
@Schema(description = "Статистика цен по монете")
record ApiPriceStatisticsDto(
    
    @Schema(description = "Символ монеты", example = "BTC")
    String symbol,
    
    @Schema(description = "Котируемая валюта", example = "USDT") 
    String quoteCurrency,
    
    @Schema(description = "Средняя цена", example = "45400.00")
    BigDecimal averagePrice,
    
    @Schema(description = "Минимальная цена за период", example = "44000.00")
    BigDecimal minPrice,
    
    @Schema(description = "Максимальная цена за период", example = "47000.00")
    BigDecimal maxPrice,
    
    @Schema(description = "Текущая цена", example = "45800.00")
    BigDecimal currentPrice,
    
    @Schema(description = "Общий объем торгов", example = "15000.50")
    BigDecimal totalVolume,
    
    @Schema(description = "Количество точек данных", example = "720")
    Long dataPoints,
    
    @Schema(description = "Период начала статистики")
    LocalDateTime periodStart,
    
    @Schema(description = "Период окончания статистики") 
    LocalDateTime periodEnd
) {
    
    /**
     * Создание статистики из результата use case
     */
    public static ApiPriceStatisticsDto from(
            String symbol,
            String quoteCurrency,
            com.ct01.crypto.application.dto.CoinResult.StatisticsResult result) {
        return new ApiPriceStatisticsDto(
            symbol,
            quoteCurrency,
            result.averagePrice(),
            result.minPrice(),
            result.maxPrice(),
            result.currentPrice(),
            result.totalVolume(),
            result.dataPoints(),
            result.periodStart(),
            result.periodEnd()
        );
    }
} 
