package com.ct01.market.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Domain Service для рыночных данных
 * Содержит бизнес-логику, которая не принадлежит конкретному агрегату
 */
public class MarketDataDomainService {
    
    private final MarketDataRepository marketDataRepository;
    
    public MarketDataDomainService(MarketDataRepository marketDataRepository) {
        this.marketDataRepository = marketDataRepository;
    }
    
    /**
     * Найти лучшую цену покупки (максимальный bid) для символа
     */
    public Optional<Price> findBestBidPrice(String symbol) {
        List<MarketData> allData = marketDataRepository.findAllBySymbol(symbol);
        
        return allData.stream()
                .filter(data -> data.getStatus() == MarketDataStatus.ACTIVE)
                .filter(data -> data.getBidPrice() != null)
                .map(MarketData::getBidPrice)
                .max((p1, p2) -> p1.getValue().compareTo(p2.getValue()));
    }
    
    /**
     * Найти лучшую цену продажи (минимальный ask) для символа
     */
    public Optional<Price> findBestAskPrice(String symbol) {
        List<MarketData> allData = marketDataRepository.findAllBySymbol(symbol);
        
        return allData.stream()
                .filter(data -> data.getStatus() == MarketDataStatus.ACTIVE)
                .filter(data -> data.getAskPrice() != null)
                .map(MarketData::getAskPrice)
                .min((p1, p2) -> p1.getValue().compareTo(p2.getValue()));
    }
    
    /**
     * Вычислить средневзвешенную цену по объему
     */
    public Optional<Price> calculateVolumeWeightedAveragePrice(String symbol) {
        List<MarketData> allData = marketDataRepository.findAllBySymbol(symbol);
        
        List<MarketData> validData = allData.stream()
                .filter(data -> data.getStatus() == MarketDataStatus.ACTIVE)
                .filter(data -> data.getCurrentPrice() != null)
                .filter(data -> data.getVolume24h() != null)
                .filter(data -> !data.getVolume24h().isZero())
                .collect(Collectors.toList());
        
        if (validData.isEmpty()) {
            return Optional.empty();
        }
        
        BigDecimal totalWeightedPrice = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;
        String currency = validData.get(0).getCurrentPrice().getCurrency();
        
        for (MarketData data : validData) {
            BigDecimal price = data.getCurrentPrice().getValue();
            BigDecimal volume = data.getVolume24h().getValue();
            
            totalWeightedPrice = totalWeightedPrice.add(price.multiply(volume));
            totalVolume = totalVolume.add(volume);
        }
        
        if (totalVolume.compareTo(BigDecimal.ZERO) == 0) {
            return Optional.empty();
        }
        
        BigDecimal vwap = totalWeightedPrice.divide(totalVolume, 8, BigDecimal.ROUND_HALF_UP);
        return Optional.of(Price.of(vwap, currency));
    }
    
    /**
     * Найти биржу с наименьшим спредом для символа
     */
    public Optional<String> findExchangeWithBestSpread(String symbol) {
        List<MarketData> allData = marketDataRepository.findAllBySymbol(symbol);
        
        return allData.stream()
                .filter(data -> data.getStatus() == MarketDataStatus.ACTIVE)
                .filter(data -> data.getBidPrice() != null && data.getAskPrice() != null)
                .min((d1, d2) -> d1.getSpread().compareTo(d2.getSpread()))
                .map(MarketData::getExchange);
    }
    
    /**
     * Найти биржу с наибольшим объемом для символа
     */
    public Optional<String> findExchangeWithHighestVolume(String symbol) {
        List<MarketData> allData = marketDataRepository.findAllBySymbol(symbol);
        
        return allData.stream()
                .filter(data -> data.getStatus() == MarketDataStatus.ACTIVE)
                .filter(data -> data.getVolume24h() != null)
                .max((d1, d2) -> d1.getVolume24h().getValue().compareTo(d2.getVolume24h().getValue()))
                .map(MarketData::getExchange);
    }
    
    /**
     * Проверить арбитражные возможности для символа
     */
    public Optional<ArbitrageOpportunity> findArbitrageOpportunity(String symbol) {
        List<MarketData> allData = marketDataRepository.findAllBySymbol(symbol);
        
        List<MarketData> validData = allData.stream()
                .filter(data -> data.getStatus() == MarketDataStatus.ACTIVE)
                .filter(data -> data.getBidPrice() != null && data.getAskPrice() != null)
                .collect(Collectors.toList());
        
        if (validData.size() < 2) {
            return Optional.empty();
        }
        
        // Найти максимальный bid и минимальный ask
        Optional<MarketData> maxBidData = validData.stream()
                .max((d1, d2) -> d1.getBidPrice().getValue().compareTo(d2.getBidPrice().getValue()));
        
        Optional<MarketData> minAskData = validData.stream()
                .min((d1, d2) -> d1.getAskPrice().getValue().compareTo(d2.getAskPrice().getValue()));
        
        if (maxBidData.isEmpty() || minAskData.isEmpty()) {
            return Optional.empty();
        }
        
        MarketData buyFrom = minAskData.get();
        MarketData sellTo = maxBidData.get();
        
        // Проверить, есть ли возможность арбитража
        if (sellTo.getBidPrice().getValue().compareTo(buyFrom.getAskPrice().getValue()) > 0) {
            BigDecimal profit = sellTo.getBidPrice().getValue().subtract(buyFrom.getAskPrice().getValue());
            BigDecimal profitPercentage = profit.divide(buyFrom.getAskPrice().getValue(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            return Optional.of(new ArbitrageOpportunity(
                    symbol,
                    buyFrom.getExchange(),
                    sellTo.getExchange(),
                    buyFrom.getAskPrice(),
                    sellTo.getBidPrice(),
                    profit,
                    profitPercentage
            ));
        }
        
        return Optional.empty();
    }
    
    /**
     * Получить статистику рынка для символа
     */
    public MarketStatistics getMarketStatistics(String symbol) {
        List<MarketData> allData = marketDataRepository.findAllBySymbol(symbol);
        
        List<MarketData> activeData = allData.stream()
                .filter(data -> data.getStatus() == MarketDataStatus.ACTIVE)
                .collect(Collectors.toList());
        
        if (activeData.isEmpty()) {
            return MarketStatistics.empty(symbol);
        }
        
        // Вычисляем статистики
        Optional<Price> minPrice = activeData.stream()
                .filter(data -> data.getCurrentPrice() != null)
                .map(MarketData::getCurrentPrice)
                .min((p1, p2) -> p1.getValue().compareTo(p2.getValue()));
        
        Optional<Price> maxPrice = activeData.stream()
                .filter(data -> data.getCurrentPrice() != null)
                .map(MarketData::getCurrentPrice)
                .max((p1, p2) -> p1.getValue().compareTo(p2.getValue()));
        
        Optional<Volume> totalVolume = activeData.stream()
                .filter(data -> data.getVolume24h() != null)
                .map(MarketData::getVolume24h)
                .reduce(Volume::add);
        
        int exchangeCount = activeData.size();
        
        return new MarketStatistics(
                symbol,
                minPrice.orElse(null),
                maxPrice.orElse(null),
                totalVolume.orElse(null),
                exchangeCount,
                LocalDateTime.now()
        );
    }
    
    /**
     * Проверить качество данных для символа
     */
    public DataQualityReport assessDataQuality(String symbol) {
        List<MarketData> allData = marketDataRepository.findAllBySymbol(symbol);
        
        long totalRecords = allData.size();
        long activeRecords = allData.stream()
                .filter(data -> data.getStatus() == MarketDataStatus.ACTIVE)
                .count();
        
        long staleRecords = allData.stream()
                .filter(data -> data.getStatus() == MarketDataStatus.STALE)
                .count();
        
        long errorRecords = allData.stream()
                .filter(data -> data.getStatus() == MarketDataStatus.ERROR)
                .count();
        
        long freshRecords = allData.stream()
                .filter(data -> data.isDataFresh(5)) // Свежие данные за последние 5 минут
                .count();
        
        double qualityScore = totalRecords > 0 ? (double) activeRecords / totalRecords * 100 : 0;
        double freshnessScore = totalRecords > 0 ? (double) freshRecords / totalRecords * 100 : 0;
        
        return new DataQualityReport(
                symbol,
                totalRecords,
                activeRecords,
                staleRecords,
                errorRecords,
                qualityScore,
                freshnessScore,
                LocalDateTime.now()
        );
    }
    
    /**
     * Класс для представления арбитражной возможности
     */
    public static class ArbitrageOpportunity {
        private final String symbol;
        private final String buyExchange;
        private final String sellExchange;
        private final Price buyPrice;
        private final Price sellPrice;
        private final BigDecimal profit;
        private final BigDecimal profitPercentage;
        
        public ArbitrageOpportunity(String symbol, String buyExchange, String sellExchange,
                                  Price buyPrice, Price sellPrice, BigDecimal profit, BigDecimal profitPercentage) {
            this.symbol = symbol;
            this.buyExchange = buyExchange;
            this.sellExchange = sellExchange;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
            this.profit = profit;
            this.profitPercentage = profitPercentage;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public String getBuyExchange() { return buyExchange; }
        public String getSellExchange() { return sellExchange; }
        public Price getBuyPrice() { return buyPrice; }
        public Price getSellPrice() { return sellPrice; }
        public BigDecimal getProfit() { return profit; }
        public BigDecimal getProfitPercentage() { return profitPercentage; }
    }
    
    /**
     * Класс для статистики рынка
     */
    public static class MarketStatistics {
        private final String symbol;
        private final Price minPrice;
        private final Price maxPrice;
        private final Volume totalVolume;
        private final int exchangeCount;
        private final LocalDateTime calculatedAt;
        
        public MarketStatistics(String symbol, Price minPrice, Price maxPrice,
                              Volume totalVolume, int exchangeCount, LocalDateTime calculatedAt) {
            this.symbol = symbol;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.totalVolume = totalVolume;
            this.exchangeCount = exchangeCount;
            this.calculatedAt = calculatedAt;
        }
        
        public static MarketStatistics empty(String symbol) {
            return new MarketStatistics(symbol, null, null, null, 0, LocalDateTime.now());
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public Price getMinPrice() { return minPrice; }
        public Price getMaxPrice() { return maxPrice; }
        public Volume getTotalVolume() { return totalVolume; }
        public int getExchangeCount() { return exchangeCount; }
        public LocalDateTime getCalculatedAt() { return calculatedAt; }
    }
    
    /**
     * Класс для отчета о качестве данных
     */
    public static class DataQualityReport {
        private final String symbol;
        private final long totalRecords;
        private final long activeRecords;
        private final long staleRecords;
        private final long errorRecords;
        private final double qualityScore;
        private final double freshnessScore;
        private final LocalDateTime generatedAt;
        
        public DataQualityReport(String symbol, long totalRecords, long activeRecords,
                               long staleRecords, long errorRecords, double qualityScore,
                               double freshnessScore, LocalDateTime generatedAt) {
            this.symbol = symbol;
            this.totalRecords = totalRecords;
            this.activeRecords = activeRecords;
            this.staleRecords = staleRecords;
            this.errorRecords = errorRecords;
            this.qualityScore = qualityScore;
            this.freshnessScore = freshnessScore;
            this.generatedAt = generatedAt;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public long getTotalRecords() { return totalRecords; }
        public long getActiveRecords() { return activeRecords; }
        public long getStaleRecords() { return staleRecords; }
        public long getErrorRecords() { return errorRecords; }
        public double getQualityScore() { return qualityScore; }
        public double getFreshnessScore() { return freshnessScore; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
    }
} 
