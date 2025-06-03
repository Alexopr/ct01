package alg.coyote001.repository;

import alg.coyote001.entity.Coin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с криптовалютами
 */
@Repository
public interface CoinRepository extends JpaRepository<Coin, Long> {
    
    /**
     * Найти монету по символу
     */
    Optional<Coin> findBySymbol(String symbol);
    
    /**
     * Find coin by symbol (case-insensitive)
     */
    Optional<Coin> findBySymbolIgnoreCase(String symbol);
    
    /**
     * Find active coins (status = ACTIVE)
     */
    @Query("SELECT c FROM Coin c WHERE c.status = 'ACTIVE'")
    List<Coin> findByIsActiveTrue();
    
    /**
     * Find coins by exchange ID
     */
    @Query("SELECT DISTINCT ph.coin FROM PriceHistory ph WHERE ph.exchange.id = :exchangeId AND ph.coin.status = 'ACTIVE'")
    List<Coin> findCoinsByExchange(@Param("exchangeId") Long exchangeId);
    
    /**
     * Search coins by symbol pattern (case-insensitive, active only)
     */
    @Query("SELECT c FROM Coin c WHERE LOWER(c.symbol) LIKE LOWER(CONCAT('%', :symbol, '%')) AND c.status = 'ACTIVE'")
    List<Coin> findBySymbolContainingIgnoreCaseAndIsActiveTrue(@Param("symbol") String symbol);
    
    /**
     * Найти монеты по приоритету (больше или равно указанному)
     */
    List<Coin> findByPriorityGreaterThanEqualOrderByPriorityDesc(Integer priority);
    
    /**
     * Найти монеты по символу (частичное совпадение)
     */
    List<Coin> findBySymbolContainingIgnoreCase(String symbol);
    
    /**
     * Найти монеты по названию (частичное совпадение)
     */
    List<Coin> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find coins by status
     */
    List<Coin> findByStatus(Coin.CoinStatus status);
    
    /**
     * Find all active coins (convenience method)
     */
    default List<Coin> findActiveCoins() {
        return findByStatus(Coin.CoinStatus.ACTIVE);
    }
    
    /**
     * Check if coin symbol exists
     */
    boolean existsBySymbol(String symbol);
    
    /**
     * Check if coin symbol exists (case-insensitive)
     */
    boolean existsBySymbolIgnoreCase(String symbol);
    
    /**
     * Count active coins
     */
    @Query("SELECT COUNT(c) FROM Coin c WHERE c.status = 'ACTIVE'")
    Long countActiveCoins();
    
    /**
     * Find coins with market cap greater than threshold
     */
    @Query("SELECT c FROM Coin c WHERE c.marketCap > :threshold AND c.status = 'ACTIVE' ORDER BY c.marketCap DESC")
    List<Coin> findCoinsByMarketCapGreaterThan(@Param("threshold") java.math.BigDecimal threshold);
    
    /**
     * Найти монеты по категории
     */
    @Query("SELECT c FROM Coin c JOIN c.categories cat WHERE cat = :category")
    List<Coin> findByCategory(@Param("category") String category);
    
    /**
     * Найти топ монеты по рыночной капитализации
     */
    @Query("SELECT c FROM Coin c WHERE c.marketRank IS NOT NULL ORDER BY c.marketRank ASC")
    List<Coin> findTopCoinsByMarketRank(Pageable pageable);
    
    /**
     * Найти монеты, которые давно не обновлялись
     */
    @Query("SELECT c FROM Coin c WHERE c.lastSyncAt IS NULL OR c.lastSyncAt < :threshold")
    List<Coin> findCoinsNeedingSync(@Param("threshold") java.time.LocalDateTime threshold);
} 