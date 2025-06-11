package com.ct01.market.domain;

import com.ct01.core.domain.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository интерфейс для рыночных данных
 */
public interface MarketDataRepository extends Repository<MarketData, MarketDataId> {
    
    /**
     * Найти рыночные данные по символу и бирже
     */
    Optional<MarketData> findBySymbolAndExchange(String symbol, String exchange);
    
    /**
     * Найти все рыночные данные для символа со всех бирж
     */
    List<MarketData> findAllBySymbol(String symbol);
    
    /**
     * Найти все рыночные данные для биржи
     */
    List<MarketData> findAllByExchange(String exchange);
    
    /**
     * Найти актуальные рыночные данные (статус ACTIVE)
     */
    List<MarketData> findActiveData();
    
    /**
     * Найти рыночные данные по статусу
     */
    List<MarketData> findByStatus(MarketDataStatus status);
    
    /**
     * Найти устаревшие данные (старше указанного времени)
     */
    List<MarketData> findStaleData(LocalDateTime olderThan);
    
    /**
     * Найти данные, обновленные после указанного времени
     */
    List<MarketData> findUpdatedAfter(LocalDateTime timestamp);
    
    /**
     * Найти лучшие цены для символа (минимальный ask, максимальный bid)
     */
    Optional<MarketData> findBestAskPrice(String symbol);
    Optional<MarketData> findBestBidPrice(String symbol);
    
    /**
     * Найти данные с наибольшим объемом для символа
     */
    Optional<MarketData> findHighestVolumeForSymbol(String symbol);
    
    /**
     * Найти все поддерживаемые символы
     */
    List<String> findAllSupportedSymbols();
    
    /**
     * Найти все активные биржи
     */
    List<String> findAllActiveExchanges();
    
    /**
     * Подсчитать количество записей по статусу
     */
    long countByStatus(MarketDataStatus status);
    
    /**
     * Подсчитать количество записей для биржи
     */
    long countByExchange(String exchange);
    
    /**
     * Удалить устаревшие данные
     */
    void deleteStaleData(LocalDateTime olderThan);
    
    /**
     * Удалить данные с ошибками старше указанного времени
     */
    void deleteErrorData(LocalDateTime olderThan);
    
    /**
     * Проверить существование данных для символа и биржи
     */
    boolean existsBySymbolAndExchange(String symbol, String exchange);
} 
