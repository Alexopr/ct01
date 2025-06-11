package com.ct01.crypto.application.dto;

import com.ct01.crypto.domain.TrackedCoin;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Запросы для получения данных о монетах в application слое
 */
public class CoinQuery {
    
    /**
     * Запрос для получения информации о монете
     */
    public record GetCoinQuery(
            String symbol
    ) {
        public GetCoinQuery {
            if (symbol == null || symbol.trim().isEmpty()) {
                throw new IllegalArgumentException("Symbol cannot be null or empty");
            }
        }
    }
    
    /**
     * Запрос для получения отслеживаемой монеты
     */
    public record GetTrackedCoinQuery(
            Long id,
            String symbol
    ) {
        public GetTrackedCoinQuery {
            if (id == null && (symbol == null || symbol.trim().isEmpty())) {
                throw new IllegalArgumentException("Either ID or symbol must be provided");
            }
        }
    }
    
    /**
     * Запрос для получения монет по бирже
     */
    public record GetCoinsByExchangeQuery(
            TrackedCoin.Exchange exchange
    ) {
        public GetCoinsByExchangeQuery {
            if (exchange == null) {
                throw new IllegalArgumentException("Exchange cannot be null");
            }
        }
    }
    
    /**
     * Запрос для получения монет по приоритету
     */
    public record GetCoinsByPriorityQuery(
            Integer minPriority
    ) {
        public GetCoinsByPriorityQuery {
            if (minPriority == null || minPriority < 0) {
                throw new IllegalArgumentException("Priority must be a positive number");
            }
        }
    }
    
    /**
     * Запрос для получения монет по котируемой валюте
     */
    public record GetCoinsByQuoteCurrencyQuery(
            String quoteCurrency
    ) {
        public GetCoinsByQuoteCurrencyQuery {
            if (quoteCurrency == null || quoteCurrency.trim().isEmpty()) {
                throw new IllegalArgumentException("Quote currency cannot be null or empty");
            }
        }
    }
    
    /**
     * Запрос для получения монет по символам
     */
    public record GetCoinsBySymbolsQuery(
            Set<String> symbols
    ) {
        public GetCoinsBySymbolsQuery {
            if (symbols == null || symbols.isEmpty()) {
                throw new IllegalArgumentException("Symbols cannot be null or empty");
            }
        }
    }
    
    /**
     * Запрос для получения текущей цены
     */
    public record GetCurrentPriceQuery(
            String coinSymbol,
            String exchangeName
    ) {
        public GetCurrentPriceQuery {
            if (coinSymbol == null || coinSymbol.trim().isEmpty()) {
                throw new IllegalArgumentException("Coin symbol cannot be null or empty");
            }
        }
    }
    
    /**
     * Запрос для получения исторических данных о цене
     */
    public record GetHistoricalPriceQuery(
            String coinSymbol,
            String exchangeName,
            LocalDateTime from,
            LocalDateTime to,
            Integer pageNumber,
            Integer pageSize
    ) {
        public GetHistoricalPriceQuery {
            if (coinSymbol == null || coinSymbol.trim().isEmpty()) {
                throw new IllegalArgumentException("Coin symbol cannot be null or empty");
            }
            if (pageNumber != null && pageNumber < 0) {
                throw new IllegalArgumentException("Page number must be non-negative");
            }
            if (pageSize != null && pageSize <= 0) {
                throw new IllegalArgumentException("Page size must be positive");
            }
        }
    }
    
    /**
     * Запрос для получения статистики цен
     */
    public record GetPriceStatisticsQuery(
            String coinSymbol,
            String exchangeName,
            Integer hours
    ) {
        public GetPriceStatisticsQuery {
            if (coinSymbol == null || coinSymbol.trim().isEmpty()) {
                throw new IllegalArgumentException("Coin symbol cannot be null or empty");
            }
            if (hours != null && hours <= 0) {
                throw new IllegalArgumentException("Hours must be positive");
            }
        }
    }
    
    /**
     * Запрос для получения недавних цен
     */
    public record GetRecentPricesQuery(
            String coinSymbol,
            String exchangeName,
            Integer hours
    ) {
        public GetRecentPricesQuery {
            if (coinSymbol == null || coinSymbol.trim().isEmpty()) {
                throw new IllegalArgumentException("Coin symbol cannot be null or empty");
            }
            if (hours != null && hours <= 0) {
                throw new IllegalArgumentException("Hours must be positive");
            }
        }
    }
} 
