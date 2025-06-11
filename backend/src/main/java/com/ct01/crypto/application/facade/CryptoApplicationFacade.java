package com.ct01.crypto.application.facade;

import com.ct01.crypto.application.dto.CoinCommand;
import com.ct01.crypto.application.dto.CoinQuery;
import com.ct01.crypto.application.dto.CoinResult;
import com.ct01.crypto.application.usecase.GetCoinDataUseCase;
import com.ct01.crypto.application.usecase.GetPriceDataUseCase;
import com.ct01.crypto.application.usecase.ManageCoinTrackingUseCase;
import com.ct01.crypto.domain.Coin;
import com.ct01.crypto.domain.PriceHistory;
import com.ct01.crypto.domain.TrackedCoin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Фасад для интеграции application services с существующими контроллерами
 * Обеспечивает обратную совместимость и упрощает миграцию
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoApplicationFacade {
    
    private final ManageCoinTrackingUseCase manageCoinTrackingUseCase;
    private final GetCoinDataUseCase getCoinDataUseCase;
    private final GetPriceDataUseCase getPriceDataUseCase;
    
    // ============ Управление отслеживаемыми монетами ============
    
    /**
     * Создать новую отслеживаемую монету
     */
    public CoinResult.TrackedCoinOperationResult createTrackedCoin(
            String symbol,
            String name,
            Set<TrackedCoin.Exchange> exchanges,
            Set<String> quoteCurrencies,
            Boolean isActive,
            Integer pollingIntervalSeconds,
            Boolean websocketEnabled,
            Integer priority,
            String notes) {
        
        CoinCommand.CreateTrackedCoinCommand command = new CoinCommand.CreateTrackedCoinCommand(
            symbol, name, exchanges, quoteCurrencies, isActive, 
            pollingIntervalSeconds, websocketEnabled, priority, notes
        );
        
        return manageCoinTrackingUseCase.createTrackedCoin(command);
    }
    
    /**
     * Обновить отслеживаемую монету
     */
    public CoinResult.TrackedCoinOperationResult updateTrackedCoin(
            Long id,
            String symbol,
            String name,
            Set<TrackedCoin.Exchange> exchanges,
            Set<String> quoteCurrencies,
            Boolean isActive,
            Integer pollingIntervalSeconds,
            Boolean websocketEnabled,
            Integer priority,
            String notes) {
        
        CoinCommand.UpdateTrackedCoinCommand command = new CoinCommand.UpdateTrackedCoinCommand(
            id, symbol, name, exchanges, quoteCurrencies, isActive,
            pollingIntervalSeconds, websocketEnabled, priority, notes
        );
        
        return manageCoinTrackingUseCase.updateTrackedCoin(command);
    }
    
    /**
     * Переключить активность монеты
     */
    public CoinResult.TrackedCoinOperationResult toggleActivation(Long id, boolean isActive) {
        CoinCommand.ToggleActivationCommand command = new CoinCommand.ToggleActivationCommand(id, isActive);
        return manageCoinTrackingUseCase.toggleActivation(command);
    }
    
    /**
     * Массовое изменение активности
     */
    public CoinResult.BulkOperationResult bulkToggleActivation(Set<String> symbols, boolean isActive) {
        CoinCommand.BulkActivationCommand command = new CoinCommand.BulkActivationCommand(symbols, isActive);
        return manageCoinTrackingUseCase.bulkToggleActivation(command);
    }
    
    /**
     * Удалить отслеживаемую монету
     */
    public CoinResult.TrackedCoinOperationResult deleteTrackedCoin(Long id) {
        return manageCoinTrackingUseCase.deleteTrackedCoin(id);
    }
    
    // ============ Получение данных о монетах ============
    
    /**
     * Получить монету по символу
     */
    public Optional<Coin> getCoinBySymbol(String symbol) {
        CoinQuery.GetCoinQuery query = new CoinQuery.GetCoinQuery(symbol);
        return getCoinDataUseCase.getCoin(query);
    }
    
    /**
     * Получить отслеживаемую монету по ID
     */
    public Optional<TrackedCoin> getTrackedCoinById(Long id) {
        CoinQuery.GetTrackedCoinQuery query = new CoinQuery.GetTrackedCoinQuery(id, null);
        return getCoinDataUseCase.getTrackedCoin(query);
    }
    
    /**
     * Получить отслеживаемую монету по символу
     */
    public Optional<TrackedCoin> getTrackedCoinBySymbol(String symbol) {
        CoinQuery.GetTrackedCoinQuery query = new CoinQuery.GetTrackedCoinQuery(null, symbol);
        return getCoinDataUseCase.getTrackedCoin(query);
    }
    
    /**
     * Получить все активные монеты
     */
    public List<TrackedCoin> getActiveTrackedCoins() {
        return getCoinDataUseCase.getActiveCoins().trackedCoins();
    }
    
    /**
     * Получить все отслеживаемые монеты с пагинацией
     */
    public CoinResult.TrackedCoinListResult getAllTrackedCoins(Integer page, Integer size) {
        return getCoinDataUseCase.getAllTrackedCoins(page, size);
    }
    
    /**
     * Поиск монет
     */
    public CoinResult.CoinSearchResult searchCoins(String searchTerm) {
        return getCoinDataUseCase.searchCoins(searchTerm);
    }
    
    // ============ Получение данных о ценах ============
    
    /**
     * Получить текущую цену монеты на бирже
     */
    public Optional<CoinResult.PriceResult> getCurrentPrice(String coinSymbol, String exchangeName) {
        CoinQuery.GetCurrentPriceQuery query = new CoinQuery.GetCurrentPriceQuery(coinSymbol, exchangeName);
        return getPriceDataUseCase.getCurrentPrice(query);
    }
    
    /**
     * Получить текущие цены монеты на всех биржах
     */
    public Optional<CoinResult.MultiExchangePriceResult> getCurrentPricesAllExchanges(String coinSymbol) {
        return getPriceDataUseCase.getCurrentPricesAllExchanges(coinSymbol);
    }
    
    /**
     * Получить исторические данные
     */
    public CoinResult.HistoricalDataResult getHistoricalData(
            String coinSymbol,
            String exchangeName,
            LocalDateTime from,
            LocalDateTime to,
            Integer pageNumber,
            Integer pageSize) {
        
        CoinQuery.GetHistoricalPriceQuery query = new CoinQuery.GetHistoricalPriceQuery(
            coinSymbol, exchangeName, from, to, pageNumber, pageSize
        );
        
        return getPriceDataUseCase.getHistoricalData(query);
    }
    
    /**
     * Получить статистику цен
     */
    public Optional<CoinResult.PriceStatisticsResult> getPriceStatistics(
            String coinSymbol,
            String exchangeName,
            Integer hours) {
        
        CoinQuery.GetPriceStatisticsQuery query = new CoinQuery.GetPriceStatisticsQuery(
            coinSymbol, exchangeName, hours
        );
        
        return getPriceDataUseCase.getPriceStatistics(query);
    }
    
    /**
     * Получить недавние цены
     */
    public List<PriceHistory> getRecentPrices(String coinSymbol, String exchangeName, Integer hours) {
        CoinQuery.GetRecentPricesQuery query = new CoinQuery.GetRecentPricesQuery(
            coinSymbol, exchangeName, hours
        );
        
        return getPriceDataUseCase.getRecentPrices(query);
    }
    
    /**
     * Получить свежие данные
     */
    public List<PriceHistory> getFreshData(int maxAgeMinutes) {
        return getPriceDataUseCase.getFreshData(maxAgeMinutes);
    }
    
    /**
     * Получить торговые пары по бирже
     */
    public List<String> getTradingPairsByExchange(String exchangeName) {
        return getPriceDataUseCase.getTradingPairsByExchange(exchangeName);
    }
    
    /**
     * Получить доступные котируемые валюты
     */
    public List<String> getAvailableQuoteCurrencies() {
        return getPriceDataUseCase.getAvailableQuoteCurrencies();
    }
    
    // ============ Методы для совместимости с существующими сервисами ============
    
    /**
     * Адаптер для совместимости с legacy API
     */
    public static class LegacyApiAdapter {
        
        /**
         * Конвертация результата операции в legacy формат
         */
        public static Map<String, Object> convertOperationResult(CoinResult.TrackedCoinOperationResult result) {
            return Map.of(
                "success", result.success(),
                "message", result.message(),
                "data", result.trackedCoin(),
                "errorCode", result.errorCode() != null ? result.errorCode() : ""
            );
        }
        
        /**
         * Конвертация результата цены в legacy формат
         */
        public static Map<String, Object> convertPriceResult(CoinResult.PriceResult result) {
            return Map.of(
                "symbol", result.coinSymbol(),
                "name", result.coinName(),
                "exchange", result.exchangeName(),
                "price", result.price(),
                "volume", result.volume(),
                "timestamp", result.timestamp()
            );
        }
        
        /**
         * Конвертация статистики цен в legacy формат
         */
        public static Map<String, Object> convertStatisticsResult(CoinResult.PriceStatisticsResult result) {
            return Map.of(
                "symbol", result.coinSymbol(),
                "exchange", result.exchangeName(),
                "period", result.periodHours() + "h",
                "high", result.highPrice(),
                "low", result.lowPrice(),
                "average", result.averagePrice(),
                "change", result.priceChange(),
                "changePercent", result.priceChangePercent(),
                "volume", result.totalVolume(),
                "dataPoints", result.dataPointsCount()
            );
        }
    }
} 
