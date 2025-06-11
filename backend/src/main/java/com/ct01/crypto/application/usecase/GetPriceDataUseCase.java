package com.ct01.crypto.application.usecase;

import com.ct01.crypto.application.dto.CoinQuery;
import com.ct01.crypto.application.dto.CoinResult;
import com.ct01.crypto.domain.PriceHistory;
import com.ct01.crypto.domain.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Use Case для получения данных о ценах
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetPriceDataUseCase {
    
    private final PriceHistoryRepository priceHistoryRepository;
    
    /**
     * Получить текущую цену монеты на бирже
     */
    public Optional<CoinResult.PriceResult> getCurrentPrice(CoinQuery.GetCurrentPriceQuery query) {
        log.debug("Getting current price for: {} on exchange: {}", query.coinSymbol(), query.exchangeName());
        
        Optional<PriceHistory> latestPrice = priceHistoryRepository.findLatestPrice(
            query.coinSymbol(), 
            query.exchangeName()
        );
        
        return latestPrice.map(this::convertToPriceResult);
    }
    
    /**
     * Получить текущие цены монеты на всех биржах
     */
    public Optional<CoinResult.MultiExchangePriceResult> getCurrentPricesAllExchanges(String coinSymbol) {
        log.debug("Getting current prices for: {} on all exchanges", coinSymbol);
        
        List<PriceHistory> latestPrices = priceHistoryRepository.findLatestPricesForCoin(coinSymbol);
        
        if (latestPrices.isEmpty()) {
            return Optional.empty();
        }
        
        Map<String, CoinResult.PriceResult> exchangePrices = new HashMap<>();
        String coinName = null;
        
        for (PriceHistory price : latestPrices) {
            if (coinName == null) {
                coinName = price.getCoinSymbol(); // В реальности нужно получить имя из Coin
            }
            
            CoinResult.PriceResult priceResult = convertToPriceResult(price);
            exchangePrices.put(price.getExchangeName(), priceResult);
        }
        
        return Optional.of(new CoinResult.MultiExchangePriceResult(
            coinSymbol,
            coinName,
            LocalDateTime.now(),
            exchangePrices
        ));
    }
    
    /**
     * Получить исторические данные о ценах
     */
    public CoinResult.HistoricalDataResult getHistoricalData(CoinQuery.GetHistoricalPriceQuery query) {
        log.debug("Getting historical data for: {} on exchange: {} from: {} to: {}", 
                query.coinSymbol(), query.exchangeName(), query.from(), query.to());
        
        int page = query.pageNumber() != null ? query.pageNumber() : 0;
        int size = query.pageSize() != null ? query.pageSize() : 100;
        
        Pageable pageable = PageRequest.of(page, size);
        
        Page<PriceHistory> historicalData = priceHistoryRepository.findHistoricalData(
            query.coinSymbol(),
            query.exchangeName(),
            query.from(),
            query.to(),
            pageable
        );
        
        return new CoinResult.HistoricalDataResult(
            query.coinSymbol(),
            query.exchangeName(),
            historicalData,
            LocalDateTime.now()
        );
    }
    
    /**
     * Получить статистику цен за период
     */
    public Optional<CoinResult.PriceStatisticsResult> getPriceStatistics(CoinQuery.GetPriceStatisticsQuery query) {
        log.debug("Getting price statistics for: {} on exchange: {} for {} hours", 
                query.coinSymbol(), query.exchangeName(), query.hours());
        
        int hours = query.hours() != null ? query.hours() : 24;
        
        List<PriceHistory> recentPrices = priceHistoryRepository.findRecentPricesOnExchange(
            query.coinSymbol(),
            query.exchangeName(),
            hours
        );
        
        if (recentPrices.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(calculateStatistics(query.coinSymbol(), query.exchangeName(), hours, recentPrices));
    }
    
    /**
     * Получить недавние цены
     */
    public List<PriceHistory> getRecentPrices(CoinQuery.GetRecentPricesQuery query) {
        log.debug("Getting recent prices for: {} on exchange: {} for {} hours", 
                query.coinSymbol(), query.exchangeName(), query.hours());
        
        int hours = query.hours() != null ? query.hours() : 24;
        
        if (query.exchangeName() != null) {
            return priceHistoryRepository.findRecentPricesOnExchange(
                query.coinSymbol(),
                query.exchangeName(),
                hours
            );
        } else {
            return priceHistoryRepository.findRecentPrices(query.coinSymbol(), hours);
        }
    }
    
    /**
     * Получить свежие данные (максимальный возраст в минутах)
     */
    public List<PriceHistory> getFreshData(int maxAgeMinutes) {
        log.debug("Getting fresh data with max age: {} minutes", maxAgeMinutes);
        
        return priceHistoryRepository.findFreshData(maxAgeMinutes);
    }
    
    /**
     * Получить данные по торговым парам на бирже
     */
    public List<String> getTradingPairsByExchange(String exchangeName) {
        log.debug("Getting trading pairs for exchange: {}", exchangeName);
        
        return priceHistoryRepository.findTradingPairsByExchange(exchangeName);
    }
    
    /**
     * Получить доступные котируемые валюты
     */
    public List<String> getAvailableQuoteCurrencies() {
        log.debug("Getting available quote currencies");
        
        return priceHistoryRepository.findDistinctQuoteCurrencies();
    }
    
    /**
     * Преобразовать PriceHistory в PriceResult
     */
    private CoinResult.PriceResult convertToPriceResult(PriceHistory priceHistory) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("tradingPair", priceHistory.getTradingPair());
        additionalData.put("quoteCurrency", priceHistory.getQuoteCurrency());
        
        // Добавляем OHLC данные если доступны
        if (priceHistory.getOpenPrice() != null) {
            Map<String, BigDecimal> ohlc = new HashMap<>();
            ohlc.put("open", priceHistory.getOpenPrice());
            ohlc.put("high", priceHistory.getHighPrice());
            ohlc.put("low", priceHistory.getLowPrice());
            ohlc.put("close", priceHistory.getClosePrice());
            additionalData.put("ohlc", ohlc);
        }
        
        if (priceHistory.getVolumeUsd() != null) {
            additionalData.put("volumeUsd", priceHistory.getVolumeUsd());
        }
        
        if (priceHistory.getTradesCount() != null) {
            additionalData.put("tradesCount", priceHistory.getTradesCount());
        }
        
        return new CoinResult.PriceResult(
            priceHistory.getCoinSymbol(),
            priceHistory.getCoinSymbol(), // В реальности нужно получить имя из Coin
            priceHistory.getExchangeName(),
            priceHistory.getClosePrice(), // Используем close price как основную цену
            priceHistory.getVolume(),
            priceHistory.getTimestamp(),
            LocalDateTime.now(),
            additionalData
        );
    }
    
    /**
     * Рассчитать статистику цен
     */
    private CoinResult.PriceStatisticsResult calculateStatistics(String coinSymbol, String exchangeName, 
                                                               int hours, List<PriceHistory> prices) {
        if (prices.isEmpty()) {
            throw new IllegalArgumentException("No price data available for statistics");
        }
        
        // Сортируем по времени
        prices = prices.stream()
            .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
            .toList();
        
        PriceHistory firstPrice = prices.get(0);
        PriceHistory lastPrice = prices.get(prices.size() - 1);
        
        BigDecimal highPrice = prices.stream()
            .map(PriceHistory::getHighPrice)
            .filter(price -> price != null)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        BigDecimal lowPrice = prices.stream()
            .map(PriceHistory::getLowPrice)
            .filter(price -> price != null)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        BigDecimal averagePrice = prices.stream()
            .map(PriceHistory::getClosePrice)
            .filter(price -> price != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(prices.size()), 8, RoundingMode.HALF_UP);
        
        BigDecimal totalVolume = prices.stream()
            .map(PriceHistory::getVolume)
            .filter(volume -> volume != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal priceChange = BigDecimal.ZERO;
        BigDecimal priceChangePercent = BigDecimal.ZERO;
        
        if (firstPrice.getClosePrice() != null && lastPrice.getClosePrice() != null 
            && firstPrice.getClosePrice().compareTo(BigDecimal.ZERO) > 0) {
            
            priceChange = lastPrice.getClosePrice().subtract(firstPrice.getClosePrice());
            priceChangePercent = priceChange
                .divide(firstPrice.getClosePrice(), 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        
        return new CoinResult.PriceStatisticsResult(
            coinSymbol,
            exchangeName,
            hours,
            highPrice,
            lowPrice,
            averagePrice,
            firstPrice.getClosePrice(),
            lastPrice.getClosePrice(),
            priceChange,
            priceChangePercent,
            totalVolume,
            prices.size(),
            firstPrice.getTimestamp(),
            lastPrice.getTimestamp()
        );
    }
} 
