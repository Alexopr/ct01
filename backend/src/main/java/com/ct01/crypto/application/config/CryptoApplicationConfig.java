package com.ct01.crypto.application.config;

import com.ct01.crypto.application.facade.CryptoApplicationFacade;
import com.ct01.crypto.application.usecase.GetCoinDataUseCase;
import com.ct01.crypto.application.usecase.GetPriceDataUseCase;
import com.ct01.crypto.application.usecase.ManageCoinTrackingUseCase;
import com.ct01.crypto.domain.repository.CoinRepository;
import com.ct01.crypto.domain.repository.PriceHistoryRepository;
import com.ct01.crypto.domain.repository.TrackedCoinRepository;
import com.ct01.crypto.domain.service.CoinDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для Application Services криптовалютного модуля
 */
@Configuration
public class CryptoApplicationConfig {
    
    /**
     * Use Case для управления отслеживаемыми монетами
     */
    @Bean
    public ManageCoinTrackingUseCase manageCoinTrackingUseCase(
            TrackedCoinRepository trackedCoinRepository,
            CoinDomainService coinDomainService) {
        return new ManageCoinTrackingUseCase(trackedCoinRepository, coinDomainService);
    }
    
    /**
     * Use Case для получения данных о монетах
     */
    @Bean
    public GetCoinDataUseCase getCoinDataUseCase(
            CoinRepository coinRepository,
            TrackedCoinRepository trackedCoinRepository,
            CoinDomainService coinDomainService) {
        return new GetCoinDataUseCase(coinRepository, trackedCoinRepository, coinDomainService);
    }
    
    /**
     * Use Case для получения данных о ценах
     */
    @Bean
    public GetPriceDataUseCase getPriceDataUseCase(
            PriceHistoryRepository priceHistoryRepository) {
        return new GetPriceDataUseCase(priceHistoryRepository);
    }
    
    /**
     * Фасад для интеграции с контроллерами
     */
    @Bean
    public CryptoApplicationFacade cryptoApplicationFacade(
            ManageCoinTrackingUseCase manageCoinTrackingUseCase,
            GetCoinDataUseCase getCoinDataUseCase,
            GetPriceDataUseCase getPriceDataUseCase) {
        return new CryptoApplicationFacade(manageCoinTrackingUseCase, getCoinDataUseCase, getPriceDataUseCase);
    }
} 
