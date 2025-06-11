package com.ct01.crypto.infrastructure.external;

import com.ct01.crypto.domain.PriceHistory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Базовый класс для Exchange Adapters
 * Содержит общую функциональность для работы с биржами
 */
@Slf4j
public abstract class AbstractExchangeAdapter implements IExchangeAdapter {
    
    protected final WebClient webClient;
    protected final ObjectMapper objectMapper;
    protected final String baseUrl;
    protected final int rateLimitPerMinute;
    
    // Rate limiting
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private final ConcurrentHashMap<String, Long> lastRequestTime = new ConcurrentHashMap<>();
    private volatile long lastResetTime = System.currentTimeMillis();
    
    protected AbstractExchangeAdapter(String baseUrl, int rateLimitPerMinute, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
        
        log.info("Initialized {} with base URL: {}, rate limit: {} req/min", 
                getExchangeName(), baseUrl, rateLimitPerMinute);
    }
    
    /**
     * Проверить и применить rate limiting
     */
    protected void checkRateLimit() {
        long currentTime = System.currentTimeMillis();
        
        // Reset counter every minute
        if (currentTime - lastResetTime > 60_000) {
            requestCounter.set(0);
            lastResetTime = currentTime;
        }
        
        if (requestCounter.get() >= rateLimitPerMinute) {
            long waitTime = 60_000 - (currentTime - lastResetTime);
            if (waitTime > 0) {
                log.warn("Rate limit reached for {}, waiting {} ms", getExchangeName(), waitTime);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                requestCounter.set(0);
                lastResetTime = System.currentTimeMillis();
            }
        }
        
        requestCounter.incrementAndGet();
    }
    
    /**
     * Выполнить HTTP запрос с rate limiting
     */
    protected <T> Mono<T> executeRequest(Mono<T> request, Class<T> responseType) {
        return Mono.fromRunnable(this::checkRateLimit)
                .then(request)
                .doOnError(error -> log.error("Request to {} failed: {}", getExchangeName(), error.getMessage()))
                .onErrorResume(error -> {
                    log.error("Exchange {} request failed", getExchangeName(), error);
                    return Mono.empty();
                });
    }
    
    /**
     * Нормализовать символ торговой пары для конкретной биржи
     */
    protected abstract String normalizeSymbol(String symbol);
    
    /**
     * Восстановить оригинальный формат символа
     */
    protected String restoreSymbolFormat(String normalizedSymbol) {
        // Default implementation - можно переопределить в наследниках
        return normalizedSymbol.replace("-", "/");
    }
    
    /**
     * Создать PriceHistory из данных биржи
     */
    protected PriceHistory createPriceHistory(String symbol, String exchange, BigDecimal price, 
                                            BigDecimal volume, BigDecimal change24h, LocalDateTime timestamp) {
        return new PriceHistory(
                null, // ID будет установлен при сохранении
                symbol,
                exchange,
                symbol + "/USDT", // trading pair - можно улучшить
                "USDT", // quote currency - можно улучшить
                timestamp != null ? timestamp : LocalDateTime.now(),
                price, // open price
                price, // high price  
                price, // low price
                price, // close price (current)
                volume != null ? volume : BigDecimal.ZERO,
                null, // volume USD - можно рассчитать
                null, // trades count
                "TICKER" // price type
        );
    }
    
    /**
     * Создать объект ошибки в случае неудачного запроса
     */
    protected PriceHistory createErrorPriceHistory(String symbol, String errorMessage) {
        log.warn("Creating error PriceHistory for {} on {}: {}", symbol, getExchangeName(), errorMessage);
        return new PriceHistory(
                null,
                symbol,
                getExchangeName(),
                symbol + "/USDT",
                "USDT",
                LocalDateTime.now(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null,
                "ERROR"
        );
    }
    
    @Override
    public Mono<Boolean> isHealthy() {
        return executeRequest(
                webClient.get()
                        .uri(getHealthCheckEndpoint())
                        .retrieve()
                        .bodyToMono(String.class)
                        .map(response -> true),
                Boolean.class
        ).onErrorReturn(false);
    }
    
    @Override
    public Mono<ExchangeRateLimitInfo> getRateLimitInfo() {
        long currentTime = System.currentTimeMillis();
        int remaining = Math.max(0, rateLimitPerMinute - requestCounter.get());
        long resetTime = lastResetTime + 60_000;
        boolean isLimited = requestCounter.get() >= rateLimitPerMinute;
        
        return Mono.just(new ExchangeRateLimitInfo(
                rateLimitPerMinute,
                remaining,
                resetTime,
                isLimited
        ));
    }
    
    /**
     * Получить endpoint для проверки здоровья биржи
     */
    protected abstract String getHealthCheckEndpoint();
} 
