package com.ct01.crypto.domain.service;

import com.ct01.crypto.domain.Coin;
import com.ct01.crypto.domain.repository.CoinRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Доменный сервис для работы с криптовалютами
 * Инкапсулирует бизнес-логику работы с монетами
 */
public class CoinDomainService {
    
    private final CoinRepository coinRepository;
    
    public CoinDomainService(CoinRepository coinRepository) {
        this.coinRepository = coinRepository;
    }
    
    /**
     * Получить активные монеты с высоким приоритетом
     */
    public List<Coin> getHighPriorityActiveCoins() {
        return coinRepository.findActiveCoins()
                .stream()
                .filter(coin -> coin.getPriority() >= 7)
                .toList();
    }
    
    /**
     * Получить популярные монеты (с рыночными данными)
     */
    public List<Coin> getPopularCoins(int limit) {
        return coinRepository.findTopRankedCoins(limit)
                .stream()
                .filter(Coin::hasMarketData)
                .limit(limit)
                .toList();
    }
    
    /**
     * Найти монеты по паттерну (символ или название)
     */
    public List<Coin> searchCoins(String pattern) {
        String normalizedPattern = pattern.toUpperCase().trim();
        
        List<Coin> bySymbol = coinRepository.findBySymbolContaining(normalizedPattern);
        List<Coin> byName = coinRepository.findByNameContaining(pattern);
        
        // Объединить результаты, избегая дубликатов
        return bySymbol.stream()
                .distinct()
                .toList();
    }
    
    /**
     * Получить монету по символу с валидацией
     */
    public Optional<Coin> findValidCoin(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return coinRepository.findBySymbol(symbol.toUpperCase().trim())
                .filter(Coin::isActive);
    }
    
    /**
     * Получить новые монеты (созданные в последние дни)
     */
    public List<Coin> getNewCoins(int maxAgeDays) {
        return coinRepository.findActiveCoins()
                .stream()
                .filter(coin -> coin.getAgeInDays() <= maxAgeDays)
                .toList();
    }
    
    /**
     * Проверить можно ли использовать символ для новой монеты
     */
    public boolean isSymbolAvailable(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }
        
        String normalizedSymbol = symbol.toUpperCase().trim();
        
        // Проверить базовые требования к символу
        if (normalizedSymbol.length() < 2 || normalizedSymbol.length() > 10) {
            return false;
        }
        
        if (!normalizedSymbol.matches("^[A-Z0-9]+$")) {
            return false;
        }
        
        return !coinRepository.existsBySymbol(normalizedSymbol);
    }
    
    /**
     * Валидировать данные монеты перед сохранением
     */
    public void validateCoinData(Coin coin) {
        if (coin == null) {
            throw new IllegalArgumentException("Coin cannot be null");
        }
        
        // Проверить уникальность символа (если это новая монета)
        if (coin.getId() == null && coinRepository.existsBySymbol(coin.getSymbol())) {
            throw new IllegalArgumentException("Coin with symbol " + coin.getSymbol() + " already exists");
        }
        
        // Дополнительные бизнес-правила можно добавить здесь
    }
    
    /**
     * Получить статистику по монетам
     */
    public CoinStatistics getCoinStatistics() {
        long totalActive = coinRepository.countActiveCoins();
        List<Coin> allActive = coinRepository.findActiveCoins();
        
        long withMarketData = allActive.stream()
                .filter(Coin::hasMarketData)
                .count();
        
        long highPriority = allActive.stream()
                .filter(coin -> coin.getPriority() >= 7)
                .count();
        
        return new CoinStatistics(totalActive, withMarketData, highPriority);
    }
    
    /**
     * Статистика по монетам
     */
    public static class CoinStatistics {
        private final long totalActiveCoins;
        private final long coinsWithMarketData;
        private final long highPriorityCoins;
        
        public CoinStatistics(long totalActiveCoins, long coinsWithMarketData, long highPriorityCoins) {
            this.totalActiveCoins = totalActiveCoins;
            this.coinsWithMarketData = coinsWithMarketData;
            this.highPriorityCoins = highPriorityCoins;
        }
        
        public long getTotalActiveCoins() { return totalActiveCoins; }
        public long getCoinsWithMarketData() { return coinsWithMarketData; }
        public long getHighPriorityCoins() { return highPriorityCoins; }
        
        @Override
        public String toString() {
            return String.format("CoinStatistics{active=%d, withMarketData=%d, highPriority=%d}", 
                               totalActiveCoins, coinsWithMarketData, highPriorityCoins);
        }
    }
} 
