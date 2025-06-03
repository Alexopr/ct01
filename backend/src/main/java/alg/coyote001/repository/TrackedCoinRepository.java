package alg.coyote001.repository;

import alg.coyote001.entity.TrackedCoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for TrackedCoin entity
 */
@Repository
public interface TrackedCoinRepository extends JpaRepository<TrackedCoin, Long> {
    
    /**
     * Find tracked coin by symbol (case-insensitive)
     */
    Optional<TrackedCoin> findBySymbolIgnoreCase(String symbol);
    
    /**
     * Find all active tracked coins
     */
    List<TrackedCoin> findByIsActiveTrue();
    
    /**
     * Find all tracked coins ordered by priority (descending)
     */
    List<TrackedCoin> findByIsActiveTrueOrderByPriorityDesc();
    
    /**
     * Find tracked coins by exchange
     */
    @Query("SELECT tc FROM TrackedCoin tc JOIN tc.exchanges e WHERE e = :exchange AND tc.isActive = true")
    List<TrackedCoin> findByExchange(@Param("exchange") TrackedCoin.Exchange exchange);
    
    /**
     * Find tracked coins that use WebSocket
     */
    List<TrackedCoin> findByWebsocketEnabledTrueAndIsActiveTrue();
    
    /**
     * Find tracked coins by quote currency
     */
    @Query("SELECT tc FROM TrackedCoin tc JOIN tc.quoteCurrencies qc WHERE qc = :quoteCurrency AND tc.isActive = true")
    List<TrackedCoin> findByQuoteCurrency(@Param("quoteCurrency") String quoteCurrency);
    
    /**
     * Find tracked coins by multiple symbols
     */
    List<TrackedCoin> findBySymbolIgnoreCaseInAndIsActiveTrue(Set<String> symbols);
    
    /**
     * Get count of active tracked coins
     */
    @Query("SELECT COUNT(tc) FROM TrackedCoin tc WHERE tc.isActive = true")
    Long countActiveCoins();
    
    /**
     * Get count of tracked coins by exchange
     */
    @Query("SELECT COUNT(tc) FROM TrackedCoin tc JOIN tc.exchanges e WHERE e = :exchange AND tc.isActive = true")
    Long countByExchange(@Param("exchange") TrackedCoin.Exchange exchange);
    
    /**
     * Check if symbol exists (case-insensitive)
     */
    boolean existsBySymbolIgnoreCase(String symbol);
    
    /**
     * Find coins with custom polling intervals
     */
    List<TrackedCoin> findByPollingIntervalSecondsIsNotNullAndIsActiveTrue();
    
    /**
     * Find coins by priority range
     */
    List<TrackedCoin> findByPriorityGreaterThanEqualAndIsActiveTrueOrderByPriorityDesc(Integer minPriority);
} 