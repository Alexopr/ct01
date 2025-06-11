package com.ct01.crypto.infrastructure.service;

import com.ct01.crypto.domain.PriceHistory;
import com.ct01.crypto.infrastructure.external.IExchangeAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Центральный сервис для управления Exchange Adapters
 * Предоставляет унифицированный интерфейс для работы с различными биржами
 */
@Service
@Slf4j
public class ExchangeAdapterService {
    
    private final Map<String, IExchangeAdapter> adapters = new ConcurrentHashMap<>();
    private final Map<String, Boolean> adapterStatus = new ConcurrentHashMap<>();
    
    public ExchangeAdapterService(
            @Qualifier("dddBinanceAdapter") IExchangeAdapter binanceAdapter,
            @Qualifier("dddOkxAdapter") IExchangeAdapter okxAdapter
    ) {
        // Register adapters
        adapters.put(binanceAdapter.getExchangeName(), binanceAdapter);
        adapters.put(okxAdapter.getExchangeName(), okxAdapter);
        
        log.info("Initialized ExchangeAdapterService with {} adapters: {}", 
                adapters.size(), adapters.keySet());
        
        // Initialize all adapters
        initializeAdapters();
    }
    
    /**
     * Инициализация всех адаптеров
     */
    private void initializeAdapters() {
        adapters.forEach((exchangeName, adapter) -> {
            adapter.initialize()
                    .doOnSuccess(v -> {
                        adapterStatus.put(exchangeName, true);
                        log.info("Successfully initialized adapter for {}", exchangeName);
                    })
                    .doOnError(error -> {
                        adapterStatus.put(exchangeName, false);
                        log.error("Failed to initialize adapter for {}: {}", exchangeName, error.getMessage());
                    })
                    .subscribe();
        });
    }
    
    /**
     * Получить тикер с указанной биржи
     */
    public Mono<PriceHistory> fetchTicker(String exchange, String symbol) {
        IExchangeAdapter adapter = adapters.get(exchange.toUpperCase());
        if (adapter == null) {
            log.warn("No adapter found for exchange: {}", exchange);
            return Mono.empty();
        }
        
        if (!Boolean.TRUE.equals(adapterStatus.get(exchange.toUpperCase()))) {
            log.warn("Adapter for {} is not healthy, skipping", exchange);
            return Mono.empty();
        }
        
        return adapter.fetchTicker(symbol)
                .timeout(Duration.ofSeconds(10))
                .doOnError(error -> log.error("Error fetching ticker from {}: {}", exchange, error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }
    
    /**
     * Получить тикеры со всех доступных бирж для указанного символа
     */
    public Flux<PriceHistory> fetchTickerFromAllExchanges(String symbol) {
        return Flux.fromIterable(adapters.entrySet())
                .filter(entry -> Boolean.TRUE.equals(adapterStatus.get(entry.getKey())))
                .flatMap(entry -> entry.getValue().fetchTicker(symbol)
                        .timeout(Duration.ofSeconds(10))
                        .doOnError(error -> log.debug("Failed to fetch {} from {}: {}", 
                                symbol, entry.getKey(), error.getMessage()))
                        .onErrorResume(error -> Mono.empty())
                )
                .doOnComplete(() -> log.debug("Completed fetching {} from all exchanges", symbol));
    }
    
    /**
     * Получить тикеры для нескольких символов с указанной биржи
     */
    public Flux<PriceHistory> fetchTickers(String exchange, List<String> symbols) {
        IExchangeAdapter adapter = adapters.get(exchange.toUpperCase());
        if (adapter == null) {
            log.warn("No adapter found for exchange: {}", exchange);
            return Flux.empty();
        }
        
        if (!Boolean.TRUE.equals(adapterStatus.get(exchange.toUpperCase()))) {
            log.warn("Adapter for {} is not healthy, skipping", exchange);
            return Flux.empty();
        }
        
        return adapter.fetchTickers(symbols)
                .timeout(Duration.ofSeconds(30))
                .doOnError(error -> log.error("Error fetching tickers from {}: {}", exchange, error.getMessage()))
                .onErrorResume(error -> Flux.empty());
    }
    
    /**
     * Получить лучшую цену для символа среди всех бирж
     */
    public Mono<PriceHistory> getBestPrice(String symbol) {
        return fetchTickerFromAllExchanges(symbol)
                .collectList()
                .map(priceHistories -> {
                    if (priceHistories.isEmpty()) {
                        log.warn("No price data found for symbol: {}", symbol);
                        return null;
                    }
                    
                    // Find the highest price (could also be lowest depending on use case)
                    return priceHistories.stream()
                            .filter(ph -> ph.closePrice() != null)
                            .max((p1, p2) -> p1.closePrice().compareTo(p2.closePrice()))
                            .orElse(priceHistories.get(0));
                })
                .filter(priceHistory -> priceHistory != null);
    }
    
    /**
     * Подписаться на обновления тикера в реальном времени
     */
    public Mono<Void> subscribeToTicker(String exchange, String symbol, Consumer<PriceHistory> callback) {
        IExchangeAdapter adapter = adapters.get(exchange.toUpperCase());
        if (adapter == null) {
            log.warn("No adapter found for exchange: {}", exchange);
            return Mono.empty();
        }
        
        return adapter.subscribeToTicker(symbol, callback);
    }
    
    /**
     * Отписаться от обновлений тикера
     */
    public Mono<Void> unsubscribeFromTicker(String exchange, String symbol) {
        IExchangeAdapter adapter = adapters.get(exchange.toUpperCase());
        if (adapter == null) {
            log.warn("No adapter found for exchange: {}", exchange);
            return Mono.empty();
        }
        
        return adapter.unsubscribeFromTicker(symbol);
    }
    
    /**
     * Получить статус всех бирж
     */
    public Mono<Map<String, Boolean>> getExchangeStatus() {
        return Flux.fromIterable(adapters.entrySet())
                .flatMap(entry -> 
                        entry.getValue().isHealthy()
                                .map(healthy -> Map.entry(entry.getKey(), healthy))
                                .onErrorReturn(Map.entry(entry.getKey(), false))
                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .doOnNext(statusMap -> {
                    // Update internal status
                    adapterStatus.putAll(statusMap);
                    log.debug("Updated exchange status: {}", statusMap);
                });
    }
    
    /**
     * Получить поддерживаемые символы для указанной биржи
     */
    public Mono<List<String>> getSupportedSymbols(String exchange) {
        IExchangeAdapter adapter = adapters.get(exchange.toUpperCase());
        if (adapter == null) {
            log.warn("No adapter found for exchange: {}", exchange);
            return Mono.just(List.of());
        }
        
        return adapter.getSupportedSymbols()
                .timeout(Duration.ofSeconds(30))
                .doOnError(error -> log.error("Error fetching symbols from {}: {}", exchange, error.getMessage()))
                .onErrorReturn(List.of());
    }
    
    /**
     * Получить все поддерживаемые символы со всех бирж
     */
    public Mono<Map<String, List<String>>> getAllSupportedSymbols() {
        return Flux.fromIterable(adapters.entrySet())
                .filter(entry -> Boolean.TRUE.equals(adapterStatus.get(entry.getKey())))
                .flatMap(entry -> 
                        entry.getValue().getSupportedSymbols()
                                .map(symbols -> Map.entry(entry.getKey(), symbols))
                                .onErrorReturn(Map.entry(entry.getKey(), List.of()))
                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }
    
    /**
     * Получить информацию о лимитах всех бирж
     */
    public Mono<Map<String, IExchangeAdapter.ExchangeRateLimitInfo>> getRateLimitInfo() {
        return Flux.fromIterable(adapters.entrySet())
                .flatMap(entry -> 
                        entry.getValue().getRateLimitInfo()
                                .map(info -> Map.entry(entry.getKey(), info))
                                .onErrorReturn(Map.entry(entry.getKey(), 
                                        new IExchangeAdapter.ExchangeRateLimitInfo(0, 0, 0, true)))
                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }
    
    /**
     * Получить список доступных бирж
     */
    public List<String> getAvailableExchanges() {
        return adapters.keySet().stream()
                .filter(exchange -> Boolean.TRUE.equals(adapterStatus.get(exchange)))
                .sorted()
                .toList();
    }
    
    /**
     * Проверить доступность конкретной биржи
     */
    public boolean isExchangeAvailable(String exchange) {
        return Boolean.TRUE.equals(adapterStatus.get(exchange.toUpperCase()));
    }
    
    /**
     * Перезапустить адаптер для указанной биржи
     */
    public Mono<Void> restartAdapter(String exchange) {
        IExchangeAdapter adapter = adapters.get(exchange.toUpperCase());
        if (adapter == null) {
            log.warn("No adapter found for exchange: {}", exchange);
            return Mono.empty();
        }
        
        log.info("Restarting adapter for {}", exchange);
        return adapter.disconnect()
                .then(adapter.initialize())
                .doOnSuccess(v -> {
                    adapterStatus.put(exchange.toUpperCase(), true);
                    log.info("Successfully restarted adapter for {}", exchange);
                })
                .doOnError(error -> {
                    adapterStatus.put(exchange.toUpperCase(), false);
                    log.error("Failed to restart adapter for {}: {}", exchange, error.getMessage());
                });
    }
    
    /**
     * Закрыть все адаптеры при завершении работы
     */
    public Mono<Void> shutdown() {
        log.info("Shutting down all exchange adapters");
        return Flux.fromIterable(adapters.values())
                .flatMap(IExchangeAdapter::disconnect)
                .then()
                .doOnSuccess(v -> log.info("All exchange adapters shut down"))
                .doOnError(error -> log.error("Error during shutdown: {}", error.getMessage()));
    }
} 
