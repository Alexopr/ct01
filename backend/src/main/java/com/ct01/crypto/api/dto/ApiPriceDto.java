package com.ct01.crypto.api.dto;

import com.ct01.crypto.domain.PriceHistory;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для передачи информации о цене криптовалюты с биржи
 */
@Schema(description = "Информация о цене криптовалюты с биржи")
public record ApiPriceDto(
        
        @Schema(description = "Символ монеты", example = "BTC")
        @JsonProperty("symbol")
        String symbol,
        
        @Schema(description = "Название биржи", example = "BINANCE")
        @JsonProperty("exchange")
        String exchange,
        
        @Schema(description = "Торговая пара", example = "BTC/USDT")
        @JsonProperty("trading_pair")
        String tradingPair,
        
        @Schema(description = "Валюта котировки", example = "USDT")
        @JsonProperty("quote_currency")
        String quoteCurrency,
        
        @Schema(description = "Текущая цена", example = "45000.50")
        @JsonProperty("current_price")
        BigDecimal currentPrice,
        
        @Schema(description = "Цена открытия", example = "44500.00")
        @JsonProperty("open_price")
        BigDecimal openPrice,
        
        @Schema(description = "Максимальная цена", example = "45200.00")
        @JsonProperty("high_price")
        BigDecimal highPrice,
        
        @Schema(description = "Минимальная цена", example = "44300.00")
        @JsonProperty("low_price")
        BigDecimal lowPrice,
        
        @Schema(description = "Объем торгов за 24 часа", example = "1250.75")
        @JsonProperty("volume_24h")
        BigDecimal volume24h,
        
        @Schema(description = "Объем в USD", example = "56281875.50")
        @JsonProperty("volume_usd")
        BigDecimal volumeUsd,
        
        @Schema(description = "Количество сделок", example = "15420")
        @JsonProperty("trades_count")
        Long tradesCount,
        
        @Schema(description = "Время последнего обновления", example = "2024-01-15T10:30:00")
        @JsonProperty("timestamp")
        LocalDateTime timestamp,
        
        @Schema(description = "Тип цены", example = "TICKER")
        @JsonProperty("price_type")
        String priceType
) {
    
    /**
     * Создать DTO из domain объекта PriceHistory
     */
    public static ApiPriceDto from(PriceHistory priceHistory) {
        return new ApiPriceDto(
                priceHistory.symbol(),
                priceHistory.exchange(),
                priceHistory.tradingPair(),
                priceHistory.quoteCurrency(),
                priceHistory.closePrice(), // current price = close price
                priceHistory.openPrice(),
                priceHistory.highPrice(),
                priceHistory.lowPrice(),
                priceHistory.volume(),
                priceHistory.volumeUsd(),
                priceHistory.tradesCount(),
                priceHistory.timestamp(),
                priceHistory.priceType()
        );
    }
    
    /**
     * Создать упрощенный DTO только с основной информацией
     */
    public static ApiPriceDto simple(String symbol, String exchange, BigDecimal price) {
        return new ApiPriceDto(
                symbol,
                exchange,
                symbol + "/USDT",
                "USDT",
                price,
                price,
                price,
                price,
                BigDecimal.ZERO,
                null,
                null,
                LocalDateTime.now(),
                "SIMPLE"
        );
    }
} 
