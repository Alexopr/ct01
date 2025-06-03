package alg.coyote001.repository;

import alg.coyote001.entity.Coin;
import alg.coyote001.entity.Exchange;
import alg.coyote001.entity.PriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с историей цен
 */
@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    
    /**
     * Найти последнюю цену для монеты на бирже
     */
    @Query("SELECT ph FROM PriceHistory ph WHERE ph.coin = :coin AND ph.exchange = :exchange " +
           "ORDER BY ph.timestamp DESC")
    Optional<PriceHistory> findLatestPrice(@Param("coin") Coin coin, @Param("exchange") Exchange exchange);
    
    /**
     * Find latest price for coin and exchange by IDs
     */
    @Query("SELECT ph FROM PriceHistory ph WHERE ph.coin.id = :coinId AND ph.exchange.id = :exchangeId " +
           "ORDER BY ph.timestamp DESC LIMIT 1")
    Optional<PriceHistory> findLatestPriceForCoinAndExchange(@Param("coinId") Long coinId, @Param("exchangeId") Long exchangeId);
    
    /**
     * Find recent prices for a coin (last 24 hours)
     */
    @Query("SELECT ph FROM PriceHistory ph WHERE ph.coin.id = :coinId AND ph.timestamp > :threshold " +
           "ORDER BY ph.timestamp DESC")
    List<PriceHistory> findRecentPricesForCoin(@Param("coinId") Long coinId, @Param("threshold") LocalDateTime threshold);
    
    /**
     * Find recent prices for a coin (last 24 hours) - default threshold
     */
    default List<PriceHistory> findRecentPricesForCoin(Long coinId) {
        return findRecentPricesForCoin(coinId, LocalDateTime.now().minusHours(24));
    }
    
    /**
     * Найти историю цен для монеты за период
     */
    @Query("SELECT ph FROM PriceHistory ph WHERE ph.coin = :coin AND ph.timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY ph.timestamp DESC")
    List<PriceHistory> findPriceHistory(@Param("coin") Coin coin, 
                                       @Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);
    
    /**
     * Найти историю цен для монеты на конкретной бирже за период
     */
    @Query("SELECT ph FROM PriceHistory ph WHERE ph.coin = :coin AND ph.exchange = :exchange " +
           "AND ph.timestamp BETWEEN :startTime AND :endTime ORDER BY ph.timestamp DESC")
    List<PriceHistory> findPriceHistoryByExchange(@Param("coin") Coin coin, 
                                                  @Param("exchange") Exchange exchange,
                                                  @Param("startTime") LocalDateTime startTime, 
                                                  @Param("endTime") LocalDateTime endTime);
    
    // Historical data queries for API endpoints
    Page<PriceHistory> findByCoinAndExchangeAndTimestampBetween(Coin coin, Exchange exchange, 
                                                               LocalDateTime from, LocalDateTime to, Pageable pageable);
    
    Page<PriceHistory> findByCoinAndExchangeAndTimestampAfter(Coin coin, Exchange exchange, 
                                                             LocalDateTime from, Pageable pageable);
    
    Page<PriceHistory> findByCoinAndExchangeAndTimestampBefore(Coin coin, Exchange exchange, 
                                                              LocalDateTime to, Pageable pageable);
    
    Page<PriceHistory> findByCoinAndExchange(Coin coin, Exchange exchange, Pageable pageable);
    
    Page<PriceHistory> findByCoinAndTimestampBetween(Coin coin, LocalDateTime from, LocalDateTime to, Pageable pageable);
    
    Page<PriceHistory> findByCoinAndTimestampAfter(Coin coin, LocalDateTime from, Pageable pageable);
    
    Page<PriceHistory> findByCoinAndTimestampBefore(Coin coin, LocalDateTime to, Pageable pageable);
    
    Page<PriceHistory> findByCoin(Coin coin, Pageable pageable);
    
    // Statistics queries for exchanges
    Long countByExchange(Exchange exchange);
    
    Long countByExchangeAndTimestampAfter(Exchange exchange, LocalDateTime timestamp);
    
    @Query("SELECT COUNT(DISTINCT ph.coin.id) FROM PriceHistory ph WHERE ph.exchange.id = :exchangeId")
    Long countDistinctCoinsByExchange(@Param("exchangeId") Long exchangeId);
    
    @Query("SELECT DISTINCT CONCAT(ph.coin.symbol, '/', ph.quoteCurrency) FROM PriceHistory ph WHERE ph.exchange.id = :exchangeId")
    List<String> findDistinctTradingPairsByExchange(@Param("exchangeId") Long exchangeId);
    
    // Price statistics queries
    List<PriceHistory> findByCoinAndExchangeAndTimestampAfter(Coin coin, Exchange exchange, LocalDateTime timestamp);
    
    List<PriceHistory> findByCoinAndTimestampAfter(Coin coin, LocalDateTime timestamp);
    
    /**
     * Найти записи по типу данных
     */
    List<PriceHistory> findByPriceType(String priceType);
    
    /**
     * Найти актуальные тикеры (не старше указанного времени)
     */
    @Query("SELECT ph FROM PriceHistory ph WHERE ph.timestamp > :threshold")
    List<PriceHistory> findRecentTickers(@Param("threshold") LocalDateTime threshold);
    
    /**
     * Найти среднюю цену за период
     */
    @Query("SELECT AVG(ph.closePrice) FROM PriceHistory ph WHERE ph.coin = :coin " +
           "AND ph.timestamp BETWEEN :startTime AND :endTime")
    Optional<BigDecimal> findAveragePrice(@Param("coin") Coin coin, 
                                         @Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * Найти максимальную цену за период
     */
    @Query("SELECT MAX(ph.closePrice) FROM PriceHistory ph WHERE ph.coin = :coin " +
           "AND ph.timestamp BETWEEN :startTime AND :endTime")
    Optional<BigDecimal> findMaxPrice(@Param("coin") Coin coin, 
                                     @Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * Найти минимальную цену за период
     */
    @Query("SELECT MIN(ph.closePrice) FROM PriceHistory ph WHERE ph.coin = :coin " +
           "AND ph.timestamp BETWEEN :startTime AND :endTime")
    Optional<BigDecimal> findMinPrice(@Param("coin") Coin coin, 
                                     @Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * Найти записи с пагинацией
     */
    Page<PriceHistory> findByCoinOrderByTimestampDesc(Coin coin, Pageable pageable);
    
    /**
     * Удалить старые записи
     */
    @Query("DELETE FROM PriceHistory ph WHERE ph.timestamp < :threshold")
    void deleteOldRecords(@Param("threshold") LocalDateTime threshold);
    
    /**
     * Найти торговые пары для монеты
     */
    @Query("SELECT DISTINCT ph.quoteCurrency FROM PriceHistory ph WHERE ph.coin = :coin")
    List<String> findTradingPairsByCoin(@Param("coin") Coin coin);
    
    /**
     * Найти объем торгов за период
     */
    @Query("SELECT SUM(ph.volume) FROM PriceHistory ph WHERE ph.coin = :coin " +
           "AND ph.timestamp BETWEEN :startTime AND :endTime")
    Optional<BigDecimal> findTotalVolume(@Param("coin") Coin coin, 
                                        @Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime);
} 