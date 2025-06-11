package com.ct01.market.application.facade;

import com.ct01.market.application.usecase.GetMarketDataUseCase;
import com.ct01.market.application.usecase.UpdateMarketDataUseCase;
import com.ct01.market.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Application Facade для Market Domain
 * Предоставляет упрощенный интерфейс для работы с рыночными данными
 */
public class MarketApplicationFacade {
    
    private final GetMarketDataUseCase getMarketDataUseCase;
    private final UpdateMarketDataUseCase updateMarketDataUseCase;
    private final MarketDataDomainService marketDataDomainService;
    private final MarketDataRepository marketDataRepository;
    
    public MarketApplicationFacade(GetMarketDataUseCase getMarketDataUseCase,
                                 UpdateMarketDataUseCase updateMarketDataUseCase,
                                 MarketDataDomainService marketDataDomainService,
                                 MarketDataRepository marketDataRepository) {
        this.getMarketDataUseCase = getMarketDataUseCase;
        this.updateMarketDataUseCase = updateMarketDataUseCase;
        this.marketDataDomainService = marketDataDomainService;
        this.marketDataRepository = marketDataRepository;
    }
    
    /**
     * Получить рыночные данные для конкретной пары символ-биржа
     */
    public Optional<MarketData> getMarketData(String symbol, String exchange) {
        GetMarketDataUseCase.Query query = GetMarketDataUseCase.Query.bySymbolAndExchange(symbol, exchange);
        GetMarketDataUseCase.Result result = getMarketDataUseCase.execute(query);
        return result.getSingle();
    }
    
    /**
     * Получить все рыночные данные для символа
     */
    public List<MarketData> getMarketDataBySymbol(String symbol) {
        GetMarketDataUseCase.Query query = GetMarketDataUseCase.Query.bySymbol(symbol);
        GetMarketDataUseCase.Result result = getMarketDataUseCase.execute(query);
        return result.getData();
    }
    
    /**
     * Получить все рыночные данные для биржи
     */
    public List<MarketData> getMarketDataByExchange(String exchange) {
        GetMarketDataUseCase.Query query = GetMarketDataUseCase.Query.byExchange(exchange);
        GetMarketDataUseCase.Result result = getMarketDataUseCase.execute(query);
        return result.getData();
    }
    
    /**
     * Получить все активные рыночные данные
     */
    public List<MarketData> getActiveMarketData() {
        GetMarketDataUseCase.Query query = GetMarketDataUseCase.Query.activeData();
        GetMarketDataUseCase.Result result = getMarketDataUseCase.execute(query);
        return result.getData();
    }
    
    /**
     * Получить устаревшие данные
     */
    public List<MarketData> getStaleMarketData(LocalDateTime olderThan) {
        GetMarketDataUseCase.Query query = GetMarketDataUseCase.Query.staleData(olderThan);
        GetMarketDataUseCase.Result result = getMarketDataUseCase.execute(query);
        return result.getData();
    }
    
    /**
     * Получить данные по статусу
     */
    public List<MarketData> getMarketDataByStatus(MarketDataStatus status) {
        GetMarketDataUseCase.Query query = GetMarketDataUseCase.Query.byStatus(status);
        GetMarketDataUseCase.Result result = getMarketDataUseCase.execute(query);
        return result.getData();
    }
    
    /**
     * Создать или обновить рыночные данные
     */
    public MarketDataUpdateResult createOrUpdateMarketData(String symbol, String exchange,
                                                          Price currentPrice, Price bidPrice, Price askPrice,
                                                          Volume volume24h, PriceChange change24h) {
        UpdateMarketDataUseCase.Command command = UpdateMarketDataUseCase.Command.createOrUpdate(
            symbol, exchange, currentPrice, bidPrice, askPrice, volume24h, change24h);
        
        UpdateMarketDataUseCase.Result result = updateMarketDataUseCase.execute(command);
        
        return new MarketDataUpdateResult(
            result.isSuccess(),
            result.getData(),
            result.getMessage(),
            result.getErrorMessage()
        );
    }
    
    /**
     * Обновить цены для существующих данных
     */
    public MarketDataUpdateResult updatePrices(String symbol, String exchange,
                                             Price currentPrice, Price bidPrice, Price askPrice,
                                             Volume volume24h, PriceChange change24h) {
        UpdateMarketDataUseCase.Command command = UpdateMarketDataUseCase.Command.updatePrices(
            symbol, exchange, currentPrice, bidPrice, askPrice, volume24h, change24h);
        
        UpdateMarketDataUseCase.Result result = updateMarketDataUseCase.execute(command);
        
        return new MarketDataUpdateResult(
            result.isSuccess(),
            result.getData(),
            result.getMessage(),
            result.getErrorMessage()
        );
    }
    
    /**
     * Пометить данные как устаревшие
     */
    public MarketDataUpdateResult markAsStale(String symbol, String exchange) {
        UpdateMarketDataUseCase.Command command = UpdateMarketDataUseCase.Command.markAsStale(symbol, exchange);
        UpdateMarketDataUseCase.Result result = updateMarketDataUseCase.execute(command);
        
        return new MarketDataUpdateResult(
            result.isSuccess(),
            result.getData(),
            result.getMessage(),
            result.getErrorMessage()
        );
    }
    
    /**
     * Пометить данные как ошибочные
     */
    public MarketDataUpdateResult markAsError(String symbol, String exchange, String errorMessage) {
        UpdateMarketDataUseCase.Command command = UpdateMarketDataUseCase.Command.markAsError(
            symbol, exchange, errorMessage);
        UpdateMarketDataUseCase.Result result = updateMarketDataUseCase.execute(command);
        
        return new MarketDataUpdateResult(
            result.isSuccess(),
            result.getData(),
            result.getMessage(),
            result.getErrorMessage()
        );
    }
    
    /**
     * Найти лучшую цену покупки для символа
     */
    public Optional<Price> getBestBidPrice(String symbol) {
        return marketDataDomainService.findBestBidPrice(symbol);
    }
    
    /**
     * Найти лучшую цену продажи для символа
     */
    public Optional<Price> getBestAskPrice(String symbol) {
        return marketDataDomainService.findBestAskPrice(symbol);
    }
    
    /**
     * Вычислить средневзвешенную цену по объему
     */
    public Optional<Price> getVolumeWeightedAveragePrice(String symbol) {
        return marketDataDomainService.calculateVolumeWeightedAveragePrice(symbol);
    }
    
    /**
     * Найти биржу с лучшим спредом
     */
    public Optional<String> getExchangeWithBestSpread(String symbol) {
        return marketDataDomainService.findExchangeWithBestSpread(symbol);
    }
    
    /**
     * Найти биржу с наибольшим объемом
     */
    public Optional<String> getExchangeWithHighestVolume(String symbol) {
        return marketDataDomainService.findExchangeWithHighestVolume(symbol);
    }
    
    /**
     * Получить статистику рынка для символа
     */
    public MarketDataDomainService.MarketStatistics getMarketStatistics(String symbol) {
        return marketDataDomainService.getMarketStatistics(symbol);
    }
    
    /**
     * Получить все поддерживаемые символы
     */
    public List<String> getAllSupportedSymbols() {
        return marketDataRepository.findAllSupportedSymbols();
    }
    
    /**
     * Получить все активные биржи
     */
    public List<String> getAllActiveExchanges() {
        return marketDataRepository.findAllActiveExchanges();
    }
    
    /**
     * Проверить существование данных
     */
    public boolean existsMarketData(String symbol, String exchange) {
        return marketDataRepository.existsBySymbolAndExchange(symbol, exchange);
    }
    
    /**
     * Получить количество записей по статусу
     */
    public long countByStatus(MarketDataStatus status) {
        return marketDataRepository.countByStatus(status);
    }
    
    /**
     * Получить количество записей для биржи
     */
    public long countByExchange(String exchange) {
        return marketDataRepository.countByExchange(exchange);
    }
    
    /**
     * Очистить устаревшие данные
     */
    public void cleanupStaleData(LocalDateTime olderThan) {
        marketDataRepository.deleteStaleData(olderThan);
    }
    
    /**
     * Очистить данные с ошибками
     */
    public void cleanupErrorData(LocalDateTime olderThan) {
        marketDataRepository.deleteErrorData(olderThan);
    }
    
    /**
     * Результат обновления рыночных данных
     */
    public static class MarketDataUpdateResult {
        private final boolean success;
        private final MarketData data;
        private final String message;
        private final String errorMessage;
        
        public MarketDataUpdateResult(boolean success, MarketData data, String message, String errorMessage) {
            this.success = success;
            this.data = data;
            this.message = message;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() { return success; }
        public boolean isFailure() { return !success; }
        public MarketData getData() { return data; }
        public String getMessage() { return message; }
        public String getErrorMessage() { return errorMessage; }
    }
} 
