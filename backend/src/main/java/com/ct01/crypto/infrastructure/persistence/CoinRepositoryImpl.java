package com.ct01.crypto.infrastructure.persistence;

import com.ct01.crypto.domain.Coin;
import com.ct01.crypto.domain.repository.CoinRepository;
import com.ct01.crypto.infrastructure.config.CachingConfig;
import com.ct01.crypto.infrastructure.mapper.CoinMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Реализация доменного репозитория CoinRepository
 * Адаптер между доменом и JPA репозиторием с оптимизацией производительности
 */
@Repository
@Slf4j
public class CoinRepositoryImpl implements CoinRepository {
    
    private final alg.coyote001.repository.CoinRepository jpaCoinRepository;
    private final CoinMapper coinMapper;
    
    public CoinRepositoryImpl(alg.coyote001.repository.CoinRepository jpaCoinRepository, 
                             CoinMapper coinMapper) {
        this.jpaCoinRepository = jpaCoinRepository;
        this.coinMapper = coinMapper;
    }
    
    @Override
    @Cacheable(cacheNames = CachingConfig.CacheNames.COIN_BY_ID, key = "#id", unless = "#result.isEmpty()")
    public Optional<Coin> findById(Long id) {
        log.debug("Finding coin by ID: {}", id);
        return jpaCoinRepository.findById(id)
                .map(coinMapper::toDomain);
    }
    
    @Override
    @Cacheable(cacheNames = CachingConfig.CacheNames.COIN_BY_SYMBOL, key = "#symbol.toLowerCase()", unless = "#result.isEmpty()")
    public Optional<Coin> findBySymbol(String symbol) {
        log.debug("Finding coin by symbol: {}", symbol);
        return jpaCoinRepository.findBySymbolIgnoreCase(symbol)
                .map(coinMapper::toDomain);
    }
    
    @Override
    @Cacheable(cacheNames = CachingConfig.CacheNames.ACTIVE_COINS)
    public List<Coin> findActiveCoins() {
        log.debug("Finding all active coins");
        return coinMapper.toDomainList(jpaCoinRepository.findByIsActiveTrue());
    }
    
    @Override
    public List<Coin> findByPriorityGreaterThanEqual(Integer priority) {
        return coinMapper.toDomainList(
                jpaCoinRepository.findByPriorityGreaterThanEqualOrderByPriorityDesc(priority)
        );
    }
    
    @Override
    public List<Coin> findBySymbolContaining(String symbolPattern) {
        return coinMapper.toDomainList(
                jpaCoinRepository.findBySymbolContainingIgnoreCase(symbolPattern)
        );
    }
    
    @Override
    public List<Coin> findByNameContaining(String namePattern) {
        return coinMapper.toDomainList(
                jpaCoinRepository.findByNameContainingIgnoreCase(namePattern)
        );
    }
    
    @Override
    public Page<Coin> findAll(Pageable pageable) {
        var entityPage = jpaCoinRepository.findAll(pageable);
        var domainCoins = coinMapper.toDomainList(entityPage.getContent());
        
        return new PageImpl<>(domainCoins, pageable, entityPage.getTotalElements());
    }
    
    @Override
    @Cacheable(cacheNames = CachingConfig.CacheNames.TOP_RANKED_COINS, key = "#limit")
    public List<Coin> findTopRankedCoins(int limit) {
        log.debug("Finding top {} ranked coins", limit);
        var entities = jpaCoinRepository.findTopRankedCoins(limit);
        return coinMapper.toDomainList(entities);
    }
    
    @Override
    public boolean existsBySymbol(String symbol) {
        return jpaCoinRepository.existsBySymbolIgnoreCase(symbol);
    }
    
    @Override
    @CacheEvict(cacheNames = {
            CachingConfig.CacheNames.ACTIVE_COINS,
            CachingConfig.CacheNames.TOP_RANKED_COINS,
            CachingConfig.CacheNames.COIN_BY_SYMBOL,
            CachingConfig.CacheNames.COIN_BY_ID
    }, allEntries = true)
    public Coin save(Coin coin) {
        log.debug("Saving coin: {} (cache will be evicted)", coin.getSymbol());
        var entity = coinMapper.toEntity(coin);
        var savedEntity = jpaCoinRepository.save(entity);
        return coinMapper.toDomain(savedEntity);
    }
    
    @Override
    @CacheEvict(cacheNames = {
            CachingConfig.CacheNames.ACTIVE_COINS,
            CachingConfig.CacheNames.TOP_RANKED_COINS,
            CachingConfig.CacheNames.COIN_BY_SYMBOL,
            CachingConfig.CacheNames.COIN_BY_ID
    }, allEntries = true)
    public void delete(Coin coin) {
        log.debug("Deleting coin: {} (cache will be evicted)", coin.getSymbol());
        var entity = coinMapper.toEntity(coin);
        jpaCoinRepository.delete(entity);
    }
    
    @Override
    public long countActiveCoins() {
        return jpaCoinRepository.countByStatusAndIsActiveTrue();
    }
} 
