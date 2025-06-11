package com.ct01.crypto.infrastructure.config;

import com.ct01.crypto.domain.repository.CoinRepository;
import com.ct01.crypto.domain.repository.PriceHistoryRepository;
import com.ct01.crypto.domain.repository.TrackedCoinRepository;
import com.ct01.crypto.domain.service.CoinDomainService;
import com.ct01.crypto.infrastructure.mapper.CoinMapper;
import com.ct01.crypto.infrastructure.mapper.PriceHistoryMapper;
import com.ct01.crypto.infrastructure.mapper.TrackedCoinMapper;
import com.ct01.crypto.infrastructure.persistence.CoinRepositoryImpl;
import com.ct01.crypto.infrastructure.persistence.PriceHistoryRepositoryImpl;
import com.ct01.crypto.infrastructure.persistence.TrackedCoinRepositoryImpl;
import com.ct01.crypto.infrastructure.external.BinanceExchangeAdapter;
import com.ct01.crypto.infrastructure.external.OkxExchangeAdapter;
import com.ct01.crypto.infrastructure.service.ExchangeAdapterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Spring для DDD компонентов криптовалютного модуля
 */
@Configuration
public class CryptoDomainConfig {
    
    /**
     * Доменный сервис для криптовалютной логики
     */
    @Bean
    public CoinDomainService coinDomainService(CoinRepository coinRepository,
                                             TrackedCoinRepository trackedCoinRepository,
                                             PriceHistoryRepository priceHistoryRepository) {
        return new CoinDomainService(coinRepository, trackedCoinRepository, priceHistoryRepository);
    }
    
    /**
     * Маппер для Coin
     */
    @Bean
    public CoinMapper coinMapper() {
        return new CoinMapper();
    }
    
    /**
     * Маппер для TrackedCoin
     */
    @Bean
    public TrackedCoinMapper trackedCoinMapper() {
        return new TrackedCoinMapper();
    }
    
    /**
     * Маппер для PriceHistory
     */
    @Bean
    public PriceHistoryMapper priceHistoryMapper() {
        return new PriceHistoryMapper();
    }
    
    /**
     * Реализация доменного репозитория Coin
     */
    @Bean
    public CoinRepository coinRepository(alg.coyote001.repository.CoinRepository jpaCoinRepository,
                                        CoinMapper coinMapper) {
        return new CoinRepositoryImpl(jpaCoinRepository, coinMapper);
    }
    
    /**
     * Реализация доменного репозитория TrackedCoin
     */
    @Bean
    public TrackedCoinRepository trackedCoinRepository(alg.coyote001.repository.TrackedCoinRepository jpaTrackedCoinRepository,
                                                      TrackedCoinMapper trackedCoinMapper) {
        return new TrackedCoinRepositoryImpl(jpaTrackedCoinRepository, trackedCoinMapper);
    }
    
    /**
     * Реализация доменного репозитория PriceHistory
     */
    @Bean
    public PriceHistoryRepository priceHistoryRepository(alg.coyote001.repository.PriceHistoryRepository jpaPriceHistoryRepository,
                                                        alg.coyote001.repository.CoinRepository jpaCoinRepository,
                                                        alg.coyote001.repository.ExchangeRepository jpaExchangeRepository,
                                                        PriceHistoryMapper priceHistoryMapper) {
        return new PriceHistoryRepositoryImpl(jpaPriceHistoryRepository, jpaCoinRepository, jpaExchangeRepository, priceHistoryMapper);
    }
    
    /**
     * ObjectMapper для JSON обработки в Exchange Adapters
     */
    @Bean
    public ObjectMapper cryptoObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
    
    /**
     * Binance Exchange Adapter
     */
    @Bean
    public BinanceExchangeAdapter binanceExchangeAdapter(ObjectMapper cryptoObjectMapper) {
        return new BinanceExchangeAdapter(cryptoObjectMapper);
    }
    
    /**
     * OKX Exchange Adapter
     */
    @Bean
    public OkxExchangeAdapter okxExchangeAdapter(ObjectMapper cryptoObjectMapper) {
        return new OkxExchangeAdapter(cryptoObjectMapper);
    }
    
    /**
     * Exchange Adapter Service
     */
    @Bean
    public ExchangeAdapterService exchangeAdapterService(BinanceExchangeAdapter binanceAdapter,
                                                        OkxExchangeAdapter okxAdapter) {
        return new ExchangeAdapterService(binanceAdapter, okxAdapter);
    }
} 
