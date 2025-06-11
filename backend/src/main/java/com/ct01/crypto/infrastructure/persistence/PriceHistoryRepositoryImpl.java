package com.ct01.crypto.infrastructure.persistence;

import com.ct01.crypto.domain.PriceHistory;
import com.ct01.crypto.domain.repository.PriceHistoryRepository;
import com.ct01.crypto.infrastructure.config.CachingConfig;
import com.ct01.crypto.infrastructure.mapper.PriceHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Реализация доменного репозитория PriceHistoryRepository
 * Адаптер между доменом и JPA репозиторием с оптимизацией производительности
 */
@Repository
@Slf4j
public class PriceHistoryRepositoryImpl implements PriceHistoryRepository {
    
    private final alg.coyote001.repository.PriceHistoryRepository jpaPriceHistoryRepository;
    private final alg.coyote001.repository.CoinRepository jpaCoinRepository;
    private final alg.coyote001.repository.ExchangeRepository jpaExchangeRepository;
    private final PriceHistoryMapper priceHistoryMapper;
    
    public PriceHistoryRepositoryImpl(alg.coyote001.repository.PriceHistoryRepository jpaPriceHistoryRepository,
                                     alg.coyote001.repository.CoinRepository jpaCoinRepository,
                                     alg.coyote001.repository.ExchangeRepository jpaExchangeRepository,
                                     PriceHistoryMapper priceHistoryMapper) {
        this.jpaPriceHistoryRepository = jpaPriceHistoryRepository;
        this.jpaCoinRepository = jpaCoinRepository;
        this.jpaExchangeRepository = jpaExchangeRepository;
        this.priceHistoryMapper = priceHistoryMapper;
    }
    
    @Override
    @Cacheable(
        cacheNames = CachingConfig.CacheNames.LATEST_PRICES, 
        key = "#coinSymbol.toLowerCase() + ':' + #exchangeName.toLowerCase()",
        unless = "#result.isEmpty()"
    )
    public Optional<PriceHistory> findLatestPrice(String coinSymbol, String exchangeName) {
        log.debug("Finding latest price for {} on {}", coinSymbol, exchangeName);
        
        var coin = jpaCoinRepository.findBySymbolIgnoreCase(coinSymbol).orElse(null);
        var exchange = jpaExchangeRepository.findByNameIgnoreCase(exchangeName).orElse(null);
        
        if (coin == null || exchange == null) {
            return Optional.empty();
        }
        
        return jpaPriceHistoryRepository.findLatestPrice(coin, exchange)
                .map(priceHistoryMapper::toDomain);
    }
    
    @Override
    public List<PriceHistory> findLatestPricesForCoin(String coinSymbol) {
        var coin = jpaCoinRepository.findBySymbolIgnoreCase(coinSymbol).orElse(null);
        if (coin == null) {
            return List.of();
        }
        
        return priceHistoryMapper.toDomainList(
                jpaPriceHistoryRepository.findRecentPricesForCoin(coin.getId())
        );
    }
    
    @Override
    public List<PriceHistory> findRecentPrices(String coinSymbol, int hours) {
        var coin = jpaCoinRepository.findBySymbolIgnoreCase(coinSymbol).orElse(null);
        if (coin == null) {
            return List.of();
        }
        
        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
        return priceHistoryMapper.toDomainList(
                jpaPriceHistoryRepository.findRecentPricesForCoin(coin.getId(), threshold)
        );
    }
    
    @Override
    public List<PriceHistory> findRecentPricesOnExchange(String coinSymbol, String exchangeName, int hours) {
        var coin = jpaCoinRepository.findBySymbolIgnoreCase(coinSymbol).orElse(null);
        var exchange = jpaExchangeRepository.findByNameIgnoreCase(exchangeName).orElse(null);
        
        if (coin == null || exchange == null) {
            return List.of();
        }
        
        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
        return priceHistoryMapper.toDomainList(
                jpaPriceHistoryRepository.findByCoinAndExchangeAndTimestampAfter(coin, exchange, threshold)
        );
    }
    
    @Override
    public Page<PriceHistory> findHistoricalData(String coinSymbol, String exchangeName, 
                                                LocalDateTime from, LocalDateTime to, 
                                                Pageable pageable) {
        var coin = jpaCoinRepository.findBySymbolIgnoreCase(coinSymbol).orElse(null);
        if (coin == null) {
            return Page.empty(pageable);
        }
        
        Page<alg.coyote001.entity.PriceHistory> entityPage;
        
        if (exchangeName != null) {
            var exchange = jpaExchangeRepository.findByNameIgnoreCase(exchangeName).orElse(null);
            if (exchange == null) {
                return Page.empty(pageable);
            }
            
            if (from != null && to != null) {
                entityPage = jpaPriceHistoryRepository.findByCoinAndExchangeAndTimestampBetween(
                        coin, exchange, from, to, pageable);
            } else if (from != null) {
                entityPage = jpaPriceHistoryRepository.findByCoinAndExchangeAndTimestampAfter(
                        coin, exchange, from, pageable);
            } else if (to != null) {
                entityPage = jpaPriceHistoryRepository.findByCoinAndExchangeAndTimestampBefore(
                        coin, exchange, to, pageable);
            } else {
                entityPage = jpaPriceHistoryRepository.findByCoinAndExchange(coin, exchange, pageable);
            }
        } else {
            if (from != null && to != null) {
                entityPage = jpaPriceHistoryRepository.findByCoinAndTimestampBetween(coin, from, to, pageable);
            } else if (from != null) {
                entityPage = jpaPriceHistoryRepository.findByCoinAndTimestampAfter(coin, from, pageable);
            } else if (to != null) {
                entityPage = jpaPriceHistoryRepository.findByCoinAndTimestampBefore(coin, to, pageable);
            } else {
                entityPage = jpaPriceHistoryRepository.findByCoin(coin, pageable);
            }
        }
        
        var domainHistory = priceHistoryMapper.toDomainList(entityPage.getContent());
        return new PageImpl<>(domainHistory, pageable, entityPage.getTotalElements());
    }
    
    @Override
    public List<PriceHistory> findByTradingPair(String tradingPair, LocalDateTime from, LocalDateTime to) {
        return priceHistoryMapper.toDomainList(
                jpaPriceHistoryRepository.findByTradingPairAndTimestampBetween(tradingPair, from, to)
        );
    }
    
    @Override
    public List<PriceHistory> findFreshData(int maxAgeMinutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(maxAgeMinutes);
        return priceHistoryMapper.toDomainList(
                jpaPriceHistoryRepository.findRecentTickers(threshold)
        );
    }
    
    @Override
    public List<PriceHistory> findByPriceType(String priceType) {
        return priceHistoryMapper.toDomainList(
                jpaPriceHistoryRepository.findByPriceType(priceType)
        );
    }
    
    @Override
    public List<String> findTradingPairsByExchange(String exchangeName) {
        var exchange = jpaExchangeRepository.findByNameIgnoreCase(exchangeName).orElse(null);
        if (exchange == null) {
            return List.of();
        }
        
        return jpaPriceHistoryRepository.findDistinctTradingPairsByExchange(exchange.getId());
    }
    
    @Override
    public List<String> findDistinctQuoteCurrencies() {
        return jpaPriceHistoryRepository.findDistinctQuoteCurrencies();
    }
    
    @Override
    public PriceHistory save(PriceHistory priceHistory) {
        // Найти соответствующие Coin и Exchange entities
        var coin = jpaCoinRepository.findBySymbolIgnoreCase(priceHistory.getCoinSymbol()).orElse(null);
        var exchange = jpaExchangeRepository.findByNameIgnoreCase(priceHistory.getExchangeName()).orElse(null);
        
        if (coin == null || exchange == null) {
            throw new IllegalArgumentException("Coin or Exchange not found for price history");
        }
        
        var entity = priceHistoryMapper.toEntity(priceHistory, coin, exchange);
        var savedEntity = jpaPriceHistoryRepository.save(entity);
        return priceHistoryMapper.toDomain(savedEntity);
    }
    
    @Override
    public List<PriceHistory> saveAll(List<PriceHistory> priceHistories) {
        return priceHistories.stream()
                .map(this::save)
                .toList();
    }
    
    @Override
    public void deleteOldData(LocalDateTime cutoffDate) {
        jpaPriceHistoryRepository.deleteByTimestampBefore(cutoffDate);
    }
    
    @Override
    public long countByExchange(String exchangeName) {
        var exchange = jpaExchangeRepository.findByNameIgnoreCase(exchangeName).orElse(null);
        if (exchange == null) {
            return 0;
        }
        
        return jpaPriceHistoryRepository.countByExchange(exchange);
    }
    
    @Override
    public long countByTimestampBetween(LocalDateTime from, LocalDateTime to) {
        return jpaPriceHistoryRepository.countByTimestampBetween(from, to);
    }
    
    @Override
    public boolean existsByTradingPair(String tradingPair) {
        return jpaPriceHistoryRepository.existsByTradingPair(tradingPair);
    }
} 
