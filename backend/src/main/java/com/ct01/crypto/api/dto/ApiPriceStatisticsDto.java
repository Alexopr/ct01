package com.ct01.crypto.api.dto;

import com.ct01.crypto.application.dto.CoinResult;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для статистики цен криптовалютной монеты
 */
@Data
@Builder
public class ApiPriceStatisticsDto {
    
    /**
     * Символ монеты
     */
    private String symbol;
    
    /**
     * Котируемая валюта
     */
    private String quoteCurrency;
    
    /**
     * Минимальная цена за период
     */
    private BigDecimal minPrice;
    
    /**
     * Максимальная цена за период
     */
    private BigDecimal maxPrice;
    
    /**
     * Средняя цена за период
     */
    private BigDecimal avgPrice;
    
    /**
     * Первая цена в периоде
     */
    private BigDecimal openPrice;
    
    /**
     * Последняя цена в периоде
     */
    private BigDecimal closePrice;
    
    /**
     * Изменение цены (close - open)
     */
    private BigDecimal priceChange;
    
    /**
     * Изменение цены в процентах
     */
    private BigDecimal priceChangePercent;
    
    /**
     * Количество записей в выборке
     */
    private Long dataPointsCount;
    
    /**
     * Начало периода
     */
    private LocalDateTime periodStart;
    
    /**
     * Конец периода
     */
    private LocalDateTime periodEnd;
    
    /**
     * Биржа
     */
    private String exchange;
    
    /**
     * Создать DTO из результата статистики
     */
    public static ApiPriceStatisticsDto from(String symbol, String quoteCurrency, CoinResult.StatisticsResult result) {
        BigDecimal priceChange = result.closePrice() != null && result.openPrice() != null 
            ? result.closePrice().subtract(result.openPrice()) 
            : BigDecimal.ZERO;
            
        BigDecimal priceChangePercent = BigDecimal.ZERO;
        if (result.openPrice() != null && result.openPrice().compareTo(BigDecimal.ZERO) > 0 && priceChange != null) {
            priceChangePercent = priceChange.divide(result.openPrice(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        
        return ApiPriceStatisticsDto.builder()
            .symbol(symbol)
            .quoteCurrency(quoteCurrency)
            .minPrice(result.minPrice())
            .maxPrice(result.maxPrice())
            .avgPrice(result.avgPrice())
            .openPrice(result.openPrice())
            .closePrice(result.closePrice())
            .priceChange(priceChange)
            .priceChangePercent(priceChangePercent)
            .dataPointsCount(result.dataPointsCount())
            .periodStart(result.periodStart())
            .periodEnd(result.periodEnd())
            .exchange(result.exchange())
            .build();
    }
} 
