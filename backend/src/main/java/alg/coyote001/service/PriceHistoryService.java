package alg.coyote001.service;

import alg.coyote001.dto.PriceHistoryDto;
import alg.coyote001.entity.Coin;
import alg.coyote001.entity.Exchange;
import alg.coyote001.entity.PriceHistory;
import alg.coyote001.repository.CoinRepository;
import alg.coyote001.repository.ExchangeRepository;
import alg.coyote001.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for price history operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PriceHistoryService {
    
    private final PriceHistoryRepository priceHistoryRepository;
    private final CoinRepository coinRepository;
    private final ExchangeRepository exchangeRepository;
    
    /**
     * Get current price data for a coin across all exchanges
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentPriceData(String symbol) {
        log.debug("Fetching current price data for symbol: {}", symbol);
        
        Coin coin = coinRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Coin not found: " + symbol));
        
        List<PriceHistory> recentPrices = priceHistoryRepository.findRecentPricesForCoin(coin.getId());
        
        if (recentPrices.isEmpty()) {
            throw new IllegalArgumentException("No price data available for coin: " + symbol);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("symbol", coin.getSymbol());
        result.put("name", coin.getName());
        result.put("lastUpdated", LocalDateTime.now());
        
        // Group by exchange
        Map<String, List<PriceHistory>> pricesByExchange = recentPrices.stream()
                .collect(Collectors.groupingBy(ph -> ph.getExchange().getName()));
        
        Map<String, Object> exchanges = new HashMap<>();
        
        for (Map.Entry<String, List<PriceHistory>> entry : pricesByExchange.entrySet()) {
            String exchangeName = entry.getKey();
            List<PriceHistory> prices = entry.getValue();
            
            // Get the most recent price for this exchange
            PriceHistory latestPrice = prices.stream()
                    .max(Comparator.comparing(PriceHistory::getTimestamp))
                    .orElse(null);
            
            if (latestPrice != null) {
                Map<String, Object> exchangeData = Map.of(
                    "price", latestPrice.getPrice(),
                    "volume", latestPrice.getVolume(),
                    "timestamp", latestPrice.getTimestamp()
                );
                exchanges.put(exchangeName, exchangeData);
            }
        }
        
        result.put("exchanges", exchanges);
        
        // Calculate average price
        double avgPrice = recentPrices.stream()
                .mapToDouble(ph -> ph.getPrice().doubleValue())
                .average()
                .orElse(0.0);
        
        result.put("averagePrice", BigDecimal.valueOf(avgPrice));
        result.put("totalExchanges", exchanges.size());
        
        return result;
    }
    
    /**
     * Get current price data for a coin on a specific exchange
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentPriceOnExchange(String symbol, String exchangeName) {
        log.debug("Fetching current price for symbol: {} on exchange: {}", symbol, exchangeName);
        
        Coin coin = coinRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Coin not found: " + symbol));
        
        Exchange exchange = exchangeRepository.findByNameIgnoreCase(exchangeName)
                .orElseThrow(() -> new IllegalArgumentException("Exchange not found: " + exchangeName));
        
        Optional<PriceHistory> latestPrice = priceHistoryRepository
                .findLatestPriceForCoinAndExchange(coin.getId(), exchange.getId());
        
        if (latestPrice.isEmpty()) {
            throw new IllegalArgumentException("No price data available for " + symbol + " on " + exchangeName);
        }
        
        PriceHistory price = latestPrice.get();
        
        return Map.of(
            "symbol", coin.getSymbol(),
            "name", coin.getName(),
            "exchange", exchange.getName(),
            "price", price.getPrice(),
            "volume", price.getVolume(),
            "timestamp", price.getTimestamp(),
            "lastUpdated", LocalDateTime.now()
        );
    }
    
    /**
     * Get historical price data with filtering
     */
    @Transactional(readOnly = true)
    public Page<PriceHistoryDto> getHistoricalData(String symbol, String exchangeName, 
                                                   LocalDateTime from, LocalDateTime to, 
                                                   Pageable pageable) {
        log.debug("Fetching historical data for symbol: {} on exchange: {} from: {} to: {}", 
                symbol, exchangeName, from, to);
        
        Coin coin = coinRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Coin not found: " + symbol));
        
        Page<PriceHistory> historyPage;
        
        if (exchangeName != null) {
            Exchange exchange = exchangeRepository.findByNameIgnoreCase(exchangeName)
                    .orElseThrow(() -> new IllegalArgumentException("Exchange not found: " + exchangeName));
            
            if (from != null && to != null) {
                historyPage = priceHistoryRepository.findByCoinAndExchangeAndTimestampBetween(
                        coin, exchange, from, to, pageable);
            } else if (from != null) {
                historyPage = priceHistoryRepository.findByCoinAndExchangeAndTimestampAfter(
                        coin, exchange, from, pageable);
            } else if (to != null) {
                historyPage = priceHistoryRepository.findByCoinAndExchangeAndTimestampBefore(
                        coin, exchange, to, pageable);
            } else {
                historyPage = priceHistoryRepository.findByCoinAndExchange(coin, exchange, pageable);
            }
        } else {
            if (from != null && to != null) {
                historyPage = priceHistoryRepository.findByCoinAndTimestampBetween(coin, from, to, pageable);
            } else if (from != null) {
                historyPage = priceHistoryRepository.findByCoinAndTimestampAfter(coin, from, pageable);
            } else if (to != null) {
                historyPage = priceHistoryRepository.findByCoinAndTimestampBefore(coin, to, pageable);
            } else {
                historyPage = priceHistoryRepository.findByCoin(coin, pageable);
            }
        }
        
        return historyPage.map(this::convertToDto);
    }
    
    /**
     * Get price statistics for a time period
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPriceStatistics(String symbol, String exchangeName, int hours) {
        log.debug("Fetching price statistics for symbol: {} on exchange: {} for {} hours", 
                symbol, exchangeName, hours);
        
        Coin coin = coinRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Coin not found: " + symbol));
        
        List<PriceHistory> prices = fetchPriceDataForPeriod(coin, exchangeName, hours);
        
        if (prices.isEmpty()) {
            throw new IllegalArgumentException("No price data available for the specified period");
        }
        
        return buildStatisticsResult(coin, exchangeName, hours, prices);
    }
    
    /**
     * Fetch price data for specified period
     */
    private List<PriceHistory> fetchPriceDataForPeriod(Coin coin, String exchangeName, int hours) {
        LocalDateTime fromTime = LocalDateTime.now().minusHours(hours);
        
        if (exchangeName != null) {
            Exchange exchange = exchangeRepository.findByNameIgnoreCase(exchangeName)
                    .orElseThrow(() -> new IllegalArgumentException("Exchange not found: " + exchangeName));
            return priceHistoryRepository.findByCoinAndExchangeAndTimestampAfter(coin, exchange, fromTime);
        } else {
            return priceHistoryRepository.findByCoinAndTimestampAfter(coin, fromTime);
        }
    }
    
    /**
     * Build comprehensive statistics result map
     */
    private Map<String, Object> buildStatisticsResult(Coin coin, String exchangeName, int hours, List<PriceHistory> prices) {
        StatisticsData stats = calculateStatistics(prices);
        PriceChangeData priceChange = calculatePriceChange(prices);
        
        Map<String, Object> result = new HashMap<>();
        result.put("symbol", coin.getSymbol());
        result.put("name", coin.getName());
        result.put("period", hours + " hours");
        result.put("dataPoints", prices.size());
        result.put("minPrice", stats.minPrice);
        result.put("maxPrice", stats.maxPrice);
        result.put("avgPrice", stats.avgPrice);
        result.put("totalVolume", stats.totalVolume);
        result.put("priceChange", priceChange.absolute);
        result.put("priceChangePercent", priceChange.percentage);
        result.put("fromTime", LocalDateTime.now().minusHours(hours));
        result.put("toTime", LocalDateTime.now());
        
        if (exchangeName != null) {
            result.put("exchange", exchangeName);
        }
        
        return result;
    }
    
    /**
     * Calculate basic statistics (min, max, avg, volume)
     */
    private StatisticsData calculateStatistics(List<PriceHistory> prices) {
        DoubleSummaryStatistics stats = prices.stream()
                .mapToDouble(ph -> ph.getPrice().doubleValue())
                .summaryStatistics();
        
        BigDecimal totalVolume = prices.stream()
                .map(PriceHistory::getVolume)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new StatisticsData(
            BigDecimal.valueOf(stats.getMin()),
            BigDecimal.valueOf(stats.getMax()),
            BigDecimal.valueOf(stats.getAverage()),
            totalVolume
        );
    }
    
    /**
     * Calculate price change data (absolute and percentage)
     */
    private PriceChangeData calculatePriceChange(List<PriceHistory> prices) {
        PriceHistory oldest = prices.stream()
                .min(Comparator.comparing(PriceHistory::getTimestamp))
                .orElse(null);
        
        PriceHistory newest = prices.stream()
                .max(Comparator.comparing(PriceHistory::getTimestamp))
                .orElse(null);
        
        if (oldest == null || newest == null || oldest.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return new PriceChangeData(null, null);
        }
        
        BigDecimal priceChange = newest.getPrice().subtract(oldest.getPrice());
        BigDecimal priceChangePercent = priceChange.divide(oldest.getPrice(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return new PriceChangeData(priceChange, priceChangePercent);
    }
    
    // ===== Helper Data Classes =====
    
    /**
     * Internal class for statistics data
     */
    private static class StatisticsData {
        final BigDecimal minPrice;
        final BigDecimal maxPrice;
        final BigDecimal avgPrice;
        final BigDecimal totalVolume;
        
        StatisticsData(BigDecimal minPrice, BigDecimal maxPrice, BigDecimal avgPrice, BigDecimal totalVolume) {
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.avgPrice = avgPrice;
            this.totalVolume = totalVolume;
        }
    }
    
    /**
     * Internal class for price change data
     */
    private static class PriceChangeData {
        final BigDecimal absolute;
        final BigDecimal percentage;
        
        PriceChangeData(BigDecimal absolute, BigDecimal percentage) {
            this.absolute = absolute;
            this.percentage = percentage;
        }
    }
    
    /**
     * Convert PriceHistory entity to DTO
     */
    private PriceHistoryDto convertToDto(PriceHistory priceHistory) {
        return PriceHistoryDto.builder()
                .id(priceHistory.getId())
                .coinSymbol(priceHistory.getCoin().getSymbol())
                .coinName(priceHistory.getCoin().getName())
                .exchangeName(priceHistory.getExchange().getName())
                .price(priceHistory.getPrice())
                .volume(priceHistory.getVolume())
                .timestamp(priceHistory.getTimestamp())
                .build();
    }
} 