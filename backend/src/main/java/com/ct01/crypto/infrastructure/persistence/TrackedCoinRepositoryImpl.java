package com.ct01.crypto.infrastructure.persistence;

import com.ct01.crypto.domain.TrackedCoin;
import com.ct01.crypto.domain.repository.TrackedCoinRepository;
import com.ct01.crypto.infrastructure.mapper.TrackedCoinMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Реализация доменного репозитория TrackedCoinRepository
 * Адаптер между доменом и JPA репозиторием
 */
@Repository
public class TrackedCoinRepositoryImpl implements TrackedCoinRepository {
    
    private final TrackedCoinJpaRepository jpaTrackedCoinRepository;
    private final TrackedCoinMapper trackedCoinMapper;
    
    public TrackedCoinRepositoryImpl(TrackedCoinJpaRepository jpaTrackedCoinRepository,
                                    TrackedCoinMapper trackedCoinMapper) {
        this.jpaTrackedCoinRepository = jpaTrackedCoinRepository;
        this.trackedCoinMapper = trackedCoinMapper;
    }
    
    @Override
    public Optional<TrackedCoin> findById(Long id) {
        return jpaTrackedCoinRepository.findById(id)
                .map(trackedCoinMapper::toDomain);
    }
    
    @Override
    public Optional<TrackedCoin> findBySymbol(String symbol) {
        return jpaTrackedCoinRepository.findBySymbolIgnoreCase(symbol)
                .map(trackedCoinMapper::toDomain);
    }
    
    @Override
    public List<TrackedCoin> findActiveCoins() {
        return trackedCoinMapper.toDomainList(jpaTrackedCoinRepository.findByIsActiveTrue());
    }
    
    @Override
    public List<TrackedCoin> findActiveCoinsByPriority() {
        return trackedCoinMapper.toDomainList(jpaTrackedCoinRepository.findByIsActiveTrueOrderByPriorityDesc());
    }
    
    @Override
    public List<TrackedCoin> findByExchange(TrackedCoin.Exchange exchange) {
        TrackedCoinEntity.Exchange entityExchange = mapExchangeToEntity(exchange);
        return trackedCoinMapper.toDomainList(jpaTrackedCoinRepository.findByExchange(entityExchange));
    }
    
    @Override
    public List<TrackedCoin> findWebSocketEnabledCoins() {
        return trackedCoinMapper.toDomainList(jpaTrackedCoinRepository.findByWebsocketEnabledTrueAndIsActiveTrue());
    }
    
    @Override
    public List<TrackedCoin> findByQuoteCurrency(String quoteCurrency) {
        return trackedCoinMapper.toDomainList(jpaTrackedCoinRepository.findByQuoteCurrency(quoteCurrency));
    }
    
    @Override
    public List<TrackedCoin> findBySymbolsIn(Set<String> symbols) {
        return trackedCoinMapper.toDomainList(jpaTrackedCoinRepository.findBySymbolIgnoreCaseInAndIsActiveTrue(symbols));
    }
    
    @Override
    public List<TrackedCoin> findWithCustomPollingInterval() {
        return trackedCoinMapper.toDomainList(jpaTrackedCoinRepository.findByPollingIntervalSecondsIsNotNullAndIsActiveTrue());
    }
    
    @Override
    public List<TrackedCoin> findByMinPriority(Integer minPriority) {
        return trackedCoinMapper.toDomainList(
                jpaTrackedCoinRepository.findByPriorityGreaterThanEqualAndIsActiveTrueOrderByPriorityDesc(minPriority)
        );
    }
    
    @Override
    public Page<TrackedCoin> findAll(Pageable pageable) {
        var entityPage = jpaTrackedCoinRepository.findAll(pageable);
        var domainCoins = trackedCoinMapper.toDomainList(entityPage.getContent());
        
        return new PageImpl<>(domainCoins, pageable, entityPage.getTotalElements());
    }
    
    @Override
    public boolean existsBySymbol(String symbol) {
        return jpaTrackedCoinRepository.existsBySymbolIgnoreCase(symbol);
    }
    
    @Override
    public TrackedCoin save(TrackedCoin trackedCoin) {
        var entity = trackedCoinMapper.toEntity(trackedCoin);
        var savedEntity = jpaTrackedCoinRepository.save(entity);
        return trackedCoinMapper.toDomain(savedEntity);
    }
    
    @Override
    public void delete(TrackedCoin trackedCoin) {
        var entity = trackedCoinMapper.toEntity(trackedCoin);
        jpaTrackedCoinRepository.delete(entity);
    }
    
    @Override
    public long countActiveCoins() {
        return jpaTrackedCoinRepository.countActiveCoins();
    }
    
    @Override
    public long countByExchange(TrackedCoin.Exchange exchange) {
        TrackedCoinEntity.Exchange entityExchange = mapExchangeToEntity(exchange);
        return jpaTrackedCoinRepository.countByExchange(entityExchange);
    }
    
    /**
     * Маппинг биржи из домена в Entity
     */
    private TrackedCoinEntity.Exchange mapExchangeToEntity(TrackedCoin.Exchange domainExchange) {
        if (domainExchange == null) {
            return null;
        }
        
        return switch (domainExchange) {
            case BINANCE -> TrackedCoinEntity.Exchange.BINANCE;
            case BYBIT -> TrackedCoinEntity.Exchange.BYBIT;
            case OKX -> TrackedCoinEntity.Exchange.OKX;
        };
    }
} 
