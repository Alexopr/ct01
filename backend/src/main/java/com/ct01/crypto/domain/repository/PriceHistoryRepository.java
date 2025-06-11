package com.ct01.crypto.domain.repository;

import com.ct01.crypto.domain.PriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Доменный репозиторий для работы с историей цен
 */
public interface PriceHistoryRepository {
    
    /**
     * Найти последнюю цену для монеты на бирже
     */
    Optional<PriceHistory> findLatestPrice(String coinSymbol, String exchangeName);
    
    /**
     * Найти последние цены для монеты на всех биржах
     */
    List<PriceHistory> findLatestPricesForCoin(String coinSymbol);
    
    /**
     * Найти недавние цены для монеты (за последние N часов)
     */
    List<PriceHistory> findRecentPrices(String coinSymbol, int hours);
    
    /**
     * Найти недавние цены для монеты на конкретной бирже
     */
    List<PriceHistory> findRecentPricesOnExchange(String coinSymbol, String exchangeName, int hours);
    
    /**
     * Найти историю цен за период
     */
    Page<PriceHistory> findHistoricalData(String coinSymbol, String exchangeName, 
                                         LocalDateTime from, LocalDateTime to, 
                                         Pageable pageable);
    
    /**
     * Найти историю цен для торговой пары
     */
    List<PriceHistory> findByTradingPair(String tradingPair, LocalDateTime from, LocalDateTime to);
    
    /**
     * Найти свежие данные (не старше указанного времени)
     */
    List<PriceHistory> findFreshData(int maxAgeMinutes);
    
    /**
     * Найти данные по типу (TICKER, CANDLE)
     */
    List<PriceHistory> findByPriceType(String priceType);
    
    /**
     * Найти торговые пары для биржи
     */
    List<String> findTradingPairsByExchange(String exchangeName);
    
    /**
     * Найти уникальные котируемые валюты
     */
    List<String> findDistinctQuoteCurrencies();
    
    /**
     * Сохранить историю цен
     */
    PriceHistory save(PriceHistory priceHistory);
    
    /**
     * Сохранить список истории цен
     */
    List<PriceHistory> saveAll(List<PriceHistory> priceHistories);
    
    /**
     * Удалить старые данные (старше указанной даты)
     */
    void deleteOldData(LocalDateTime cutoffDate);
    
    /**
     * Подсчитать количество записей для биржи
     */
    long countByExchange(String exchangeName);
    
    /**
     * Подсчитать количество записей за период
     */
    long countByTimestampBetween(LocalDateTime from, LocalDateTime to);
    
    /**
     * Проверить существование данных для торговой пары
     */
    boolean existsByTradingPair(String tradingPair);
} 
