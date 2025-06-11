package com.ct01.crypto.application.dto;

import com.ct01.crypto.domain.Coin;
import com.ct01.crypto.domain.PriceHistory;
import com.ct01.crypto.domain.TrackedCoin;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Результаты операций в application слое
 */
public class CoinResult {
    
    /**
     * Результат операции с монетой
     */
    public record CoinOperationResult(
            boolean success,
            String message,
            Coin coin,
            String errorCode
    ) {
        public static CoinOperationResult success(Coin coin, String message) {
            return new CoinOperationResult(true, message, coin, null);
        }
        
        public static CoinOperationResult failure(String message, String errorCode) {
            return new CoinOperationResult(false, message, null, errorCode);
        }
    }
    
    /**
     * Результат операции с отслеживаемой монетой
     */
    public record TrackedCoinOperationResult(
            boolean success,
            String message,
            TrackedCoin trackedCoin,
            String errorCode
    ) {
        public static TrackedCoinOperationResult success(TrackedCoin trackedCoin, String message) {
            return new TrackedCoinOperationResult(true, message, trackedCoin, null);
        }
        
        public static TrackedCoinOperationResult failure(String message, String errorCode) {
            return new TrackedCoinOperationResult(false, message, null, errorCode);
        }
    }
    
    /**
     * Результат массовой операции
     */
    public record BulkOperationResult(
            boolean success,
            String message,
            List<TrackedCoin> processedCoins,
            int totalProcessed,
            int totalFailed,
            List<String> errors
    ) {
        public static BulkOperationResult success(List<TrackedCoin> processedCoins, String message) {
            return new BulkOperationResult(true, message, processedCoins, processedCoins.size(), 0, List.of());
        }
        
        public static BulkOperationResult partial(List<TrackedCoin> processedCoins, int failed, List<String> errors) {
            return new BulkOperationResult(false, "Partial success", processedCoins, processedCoins.size(), failed, errors);
        }
        
        public static BulkOperationResult failure(String message, List<String> errors) {
            return new BulkOperationResult(false, message, List.of(), 0, errors.size(), errors);
        }
    }
    

    
    /**
     * Результат с несколькими ценами по биржам
     */
    public record MultiExchangePriceResult(
            String coinSymbol,
            String coinName,
            LocalDateTime lastUpdated,
            Map<String, PriceResult> exchangePrices
    ) {}
    
    /**
     * Результат статистики цен
     */
    public record PriceStatisticsResult(
            String coinSymbol,
            String exchangeName,
            int periodHours,
            BigDecimal highPrice,
            BigDecimal lowPrice,
            BigDecimal averagePrice,
            BigDecimal firstPrice,
            BigDecimal lastPrice,
            BigDecimal priceChange,
            BigDecimal priceChangePercent,
            BigDecimal totalVolume,
            int dataPointsCount,
            LocalDateTime periodStart,
            LocalDateTime periodEnd
    ) {}
    
    /**
     * Результат исторических данных
     */
    public record HistoricalDataResult(
            String coinSymbol,
            String exchangeName,
            Page<PriceHistory> priceHistory,
            LocalDateTime queryTimestamp
    ) {}
    
    /**
     * Результат статистики отслеживания
     */
    public record TrackingStatisticsResult(
            long totalActiveCoins,
            long totalInactiveCoins,
            long binanceCoins,
            long bybitCoins,
            long okxCoins,
            long websocketEnabledCoins,
            long customPollingCoins,
            Map<String, Long> coinsByQuoteCurrency,
            Map<Integer, Long> coinsByPriority,
            LocalDateTime statisticsTimestamp
    ) {}
    
    /**
     * Результат списка монет
     */
    public record CoinListResult(
            List<Coin> coins,
            int totalCount,
            boolean hasMore,
            String nextPageToken
    ) {}
    

    
    /**
     * Результат поиска монет
     */
    public record CoinSearchResult(
            List<Coin> exactMatches,
            List<Coin> partialMatches,
            String searchTerm,
            int totalResults,
            LocalDateTime searchTimestamp
    ) {
        // Для совместимости добавляем метод, который возвращает все найденные монеты
        public List<Coin> coins() {
            List<Coin> allCoins = new java.util.ArrayList<>(exactMatches);
            allCoins.addAll(partialMatches);
            return allCoins;
        }
    }

    /**
     * Результат с историей цен для статистики
     */
    public record PriceResult(
            boolean success,
            PriceHistory priceHistory,
            String errorMessage
    ) {
        public static PriceResult success(PriceHistory priceHistory) {
            return new PriceResult(true, priceHistory, null);
        }
        
        public static PriceResult failure(String errorMessage) {
            return new PriceResult(false, null, errorMessage);
        }
    }

    /**
     * Результат списка исторических данных о ценах
     */
    public record PriceListResult(
            boolean success,
            List<PriceHistory> priceHistories,
            String errorMessage
    ) {
        public static PriceListResult success(List<PriceHistory> priceHistories) {
            return new PriceListResult(true, priceHistories, null);
        }
        
        public static PriceListResult failure(String errorMessage) {
            return new PriceListResult(false, List.of(), errorMessage);
        }
    }

    /**
     * Результат статистики цен
     */
    public record StatisticsResult(
            boolean success,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal avgPrice,
            BigDecimal openPrice,
            BigDecimal closePrice,
            Long dataPointsCount,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            String exchange,
            String errorMessage
    ) {
        public static StatisticsResult success(
                BigDecimal minPrice, BigDecimal maxPrice, BigDecimal avgPrice,
                BigDecimal openPrice, BigDecimal closePrice, Long dataPointsCount,
                LocalDateTime periodStart, LocalDateTime periodEnd, String exchange) {
            return new StatisticsResult(true, minPrice, maxPrice, avgPrice, openPrice, closePrice,
                    dataPointsCount, periodStart, periodEnd, exchange, null);
        }
        
        public static StatisticsResult failure(String errorMessage) {
            return new StatisticsResult(false, null, null, null, null, null, 
                    0L, null, null, null, errorMessage);
        }
    }

    /**
     * Результат списка отслеживаемых монет с пагинацией
     */
    public record TrackedCoinListResult(
            List<TrackedCoin> trackedCoins,
            long totalElements,
            int totalPages,
            int currentPage,
            int pageSize
    ) {}
} 
