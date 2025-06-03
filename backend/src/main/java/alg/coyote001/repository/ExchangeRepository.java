package alg.coyote001.repository;

import alg.coyote001.entity.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с криптобиржами
 */
@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    
    /**
     * Найти биржу по названию
     */
    Optional<Exchange> findByName(String name);
    
    /**
     * Find exchange by name (case-insensitive)
     */
    Optional<Exchange> findByNameIgnoreCase(String name);
    
    /**
     * Find active exchanges (enabled and status ACTIVE)
     */
    @Query("SELECT e FROM Exchange e WHERE e.enabled = true AND e.status = 'ACTIVE'")
    List<Exchange> findByIsActiveTrue();
    
    /**
     * Найти включенные биржи
     */
    List<Exchange> findByEnabledTrue();
    
    /**
     * Найти биржи по приоритету (больше или равно указанному)
     */
    List<Exchange> findByPriorityGreaterThanEqualOrderByPriorityDesc(Integer priority);
    
    /**
     * Найти биржи, поддерживающие торговую пару
     */
    @Query("SELECT e FROM Exchange e JOIN e.supportedTradingPairs stp WHERE stp = :tradingPair")
    List<Exchange> findBySupportedTradingPair(@Param("tradingPair") String tradingPair);
    
    /**
     * Найти биржи с проблемами (много ошибок подряд)
     */
    @Query("SELECT e FROM Exchange e WHERE e.consecutiveErrors >= :threshold")
    List<Exchange> findExchangesWithErrors(@Param("threshold") Integer threshold);
    
    /**
     * Найти биржи, которые давно не проверялись
     */
    @Query("SELECT e FROM Exchange e WHERE e.lastHealthCheck IS NULL OR e.lastHealthCheck < :threshold")
    List<Exchange> findExchangesNeedingHealthCheck(@Param("threshold") LocalDateTime threshold);
    
    /**
     * Найти биржи с доступными подключениями
     */
    @Query("SELECT e FROM Exchange e WHERE e.enabled = true AND e.status = 'ACTIVE' AND " +
           "(e.currentConnections IS NULL OR e.currentConnections < e.maxWebsocketConnections)")
    List<Exchange> findExchangesWithAvailableConnections();
    
    /**
     * Проверить существование биржи по названию
     */
    boolean existsByName(String name);
    
    /**
     * Check if exchange name exists (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Найти биржи по отображаемому названию (частичное совпадение)
     */
    List<Exchange> findByDisplayNameContainingIgnoreCase(String displayName);
} 