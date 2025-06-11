package com.ct01.crypto.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * JPA Repository для TrackedCoinEntity
 */
@Repository
public interface TrackedCoinJpaRepository extends JpaRepository<TrackedCoinEntity, Long> {

    Optional<TrackedCoinEntity> findBySymbolIgnoreCase(String symbol);

    List<TrackedCoinEntity> findByIsActiveTrue();

    List<TrackedCoinEntity> findByIsActiveTrueOrderByPriorityDesc();

    @Query("SELECT tc FROM TrackedCoinEntity tc JOIN tc.exchanges e WHERE e = :exchange")
    List<TrackedCoinEntity> findByExchange(@Param("exchange") TrackedCoinEntity.Exchange exchange);

    List<TrackedCoinEntity> findByWebsocketEnabledTrueAndIsActiveTrue();

    @Query("SELECT tc FROM TrackedCoinEntity tc JOIN tc.quoteCurrencies qc WHERE qc = :quoteCurrency")
    List<TrackedCoinEntity> findByQuoteCurrency(@Param("quoteCurrency") String quoteCurrency);

    List<TrackedCoinEntity> findBySymbolIgnoreCaseInAndIsActiveTrue(Set<String> symbols);

    List<TrackedCoinEntity> findByPollingIntervalSecondsIsNotNullAndIsActiveTrue();

    List<TrackedCoinEntity> findByPriorityGreaterThanEqualAndIsActiveTrueOrderByPriorityDesc(Integer minPriority);

    boolean existsBySymbolIgnoreCase(String symbol);

    @Query("SELECT COUNT(tc) FROM TrackedCoinEntity tc WHERE tc.isActive = true")
    long countActiveCoins();

    @Query("SELECT COUNT(tc) FROM TrackedCoinEntity tc JOIN tc.exchanges e WHERE e = :exchange")
    long countByExchange(@Param("exchange") TrackedCoinEntity.Exchange exchange);
} 