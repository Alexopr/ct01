package alg.coyote001.service.exchange;

import alg.coyote001.dto.TickerData;
import alg.coyote001.service.CacheService;
import alg.coyote001.service.RateLimitingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Базовый абстрактный класс для адаптеров бирж
 * Содержит общую функциональность для всех реализаций с Redis caching и rate limiting
 */
@Slf4j
public abstract class AbstractExchangeAdapter implements IExchangeAdapter {
    
    protected final ObjectMapper objectMapper;
    protected final CacheService cacheService;
    protected final RateLimitingService rateLimitingService;
    
    protected final WebClient webClient;
    protected final String baseUrl;
    protected final int maxRequestsPerMinute;
    
    // Rate limiting (fallback if Redis is down)
    protected final AtomicInteger requestCounter = new AtomicInteger(0);
    protected LocalDateTime lastResetTime = LocalDateTime.now();
    
    /**
     * Constructor with dependency injection for better testability
     * This constructor should be used by Spring when creating beans
     */
    protected AbstractExchangeAdapter(String baseUrl, int maxRequestsPerMinute,
                                    ObjectMapper objectMapper, 
                                    CacheService cacheService, 
                                    RateLimitingService rateLimitingService) {
        this.baseUrl = baseUrl;
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.objectMapper = objectMapper;
        this.cacheService = cacheService;
        this.rateLimitingService = rateLimitingService;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
    
    // ===== Common Request Execution Methods =====
    
    /**
     * Execute a request with standardized retry logic and error handling
     * @param request the request to execute
     * @param operation descriptive name for logging
     * @param <T> response type
     * @return Mono with response or error
     */
    protected <T> Mono<T> executeWithRetry(Mono<T> request, String operation) {
        return executeWithRetry(request, operation, getDefaultValue());
    }

    /**
     * Execute a request with retry logic and fallback value
     * @param request the request to execute
     * @param operation descriptive name for logging
     * @param defaultValue fallback value on error
     * @param <T> response type
     * @return Mono with response or default value
     */
    protected <T> Mono<T> executeWithRetry(Mono<T> request, String operation, T defaultValue) {
        return request
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableError))
                .doOnError(error -> logError(operation, error))
                .onErrorReturn(defaultValue);
    }

    /**
     * Execute a request with rate limiting and error handling
     * @param requestBuilder function to build the request
     * @param operation descriptive name for logging
     * @param <T> response type
     * @return Mono with response
     */
    protected <T> Mono<T> executeWithRateLimit(Function<WebClient, Mono<T>> requestBuilder, String operation) {
        return checkRateLimit()
                .then(requestBuilder.apply(webClient))
                .doOnSubscribe(s -> incrementRequestCounter())
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableError))
                .doOnError(error -> handleRequestError(operation, error));
    }

    /**
     * Execute a cached request with standardized caching logic
     * @param cacheKey key for caching
     * @param request the request to execute if cache miss
     * @param operation operation name for logging
     * @param <T> response type
     * @return Mono with cached or fresh response
     */
    protected <T> Mono<T> executeWithCache(String cacheKey, Mono<T> request, String operation) {
        // Note: This is a template method. Specific implementations would need to handle their own cache logic
        return request
                .doOnNext(result -> log.debug("Completed {} for cache key: {}", operation, cacheKey))
                .doOnError(error -> logError(operation, error));
    }
    
    // ===== Error Handling Methods =====
    
    /**
     * Handle request errors with standardized logging and backoff logic
     * @param operation descriptive name of the operation
     * @param error the error that occurred
     */
    protected void handleRequestError(String operation, Throwable error) {
        String exchangeName = getExchangeName();
        logError(operation, error);
        
        if (isRateLimitError(error)) {
            rateLimitingService.setBackoff(exchangeName, 60); // 1 minute backoff
            log.warn("Set backoff for {} due to rate limit error", exchangeName);
        } else if (isTemporaryError(error)) {
            rateLimitingService.setBackoff(exchangeName, 30); // 30 second backoff
            log.warn("Set backoff for {} due to temporary error", exchangeName);
        }
    }

    /**
     * Log errors in a standardized format
     * @param operation descriptive name of the operation
     * @param error the error that occurred
     */
    protected void logError(String operation, Throwable error) {
        String exchangeName = getExchangeName();
        if (error instanceof WebClientResponseException webClientError) {
            log.error("Failed {} for {}: HTTP {} - {}", 
                operation, exchangeName, webClientError.getStatusCode(), webClientError.getMessage());
        } else {
            log.error("Failed {} for {}: {}", operation, exchangeName, error.getMessage());
        }
    }

    /**
     * Check if error indicates a temporary issue that should trigger backoff
     * @param error the error to check
     * @return true if temporary error
     */
    protected boolean isTemporaryError(Throwable error) {
        if (error instanceof WebClientResponseException webClientError) {
            int status = webClientError.getStatusCode().value();
            return status >= 500 || status == 429 || status == 503;
        }
        return error.getMessage() != null && 
               (error.getMessage().contains("timeout") || 
                error.getMessage().contains("connection"));
    }
    
    // ===== Cache Helper Methods =====
    
    /**
     * Get cached value with fallback to fresh request
     * @param cacheGetter function to get cached value
     * @param freshRequest request to execute if cache miss
     * @param cacheSetter function to cache the fresh value
     * @param operation operation name for logging
     * @param <T> response type
     * @return Mono with cached or fresh value
     */
    protected <T> Mono<T> getCachedOrFresh(
            java.util.function.Supplier<T> cacheGetter,
            Mono<T> freshRequest,
            java.util.function.Consumer<T> cacheSetter,
            String operation) {
        
        T cached = cacheGetter.get();
        if (cached != null) {
            log.debug("Cache hit for {}", operation);
            return Mono.just(cached);
        }
        
        return freshRequest
                .doOnNext(result -> {
                    cacheSetter.accept(result);
                    log.debug("Cached result for {}", operation);
                })
                .doOnError(error -> logError(operation, error));
    }
    
    // ===== Default Implementation Methods =====
    
    @Override
    public Mono<Boolean> isHealthy() {
        String exchangeName = getExchangeName();
        
        return getCachedOrFresh(
                () -> cacheService.getCachedHealthCheck(exchangeName),
                executeWithRetry(
                        webClient.get()
                                .uri(getHealthCheckEndpoint())
                                .retrieve()
                                .bodyToMono(String.class)
                                .map(response -> !response.isEmpty())
                                .timeout(Duration.ofSeconds(5)),
                        "health check",
                        false
                ),
                isHealthy -> cacheService.cacheHealthCheck(exchangeName, isHealthy),
                "health check"
        );
    }

    @Override
    public Mono<RateLimitInfo> getRateLimitInfo() {
        String exchangeName = getExchangeName();
        String rateLimitKey = "exchange:" + exchangeName;
        
        // Check Redis-based rate limiting first
        if (cacheService.isCacheHealthy()) {
            int currentCount = rateLimitingService.getCurrentCount(rateLimitKey);
            Duration timeUntilReset = rateLimitingService.getTimeUntilReset(rateLimitKey);
            long recommendedDelay = rateLimitingService.getRecommendedDelay(
                rateLimitKey, maxRequestsPerMinute, 60);
            
            RateLimitInfo info = RateLimitInfo.builder()
                    .currentRequests(currentCount)
                    .maxRequestsPerMinute(maxRequestsPerMinute)
                    .resetTime(LocalDateTime.now().plus(timeUntilReset != null ? timeUntilReset : Duration.ZERO))
                    .recommendedDelayMs(recommendedDelay)
                    .build();
            
            info.calculateUsage();
            return Mono.just(info);
        }
        
        // Fallback to in-memory rate limiting
        resetCounterIfNeeded();
        
        RateLimitInfo info = RateLimitInfo.builder()
                .currentRequests(requestCounter.get())
                .maxRequestsPerMinute(maxRequestsPerMinute)
                .resetTime(lastResetTime.plusMinutes(1))
                .build();
        
        info.calculateUsage();
        return Mono.just(info);
    }

    /**
     * Fetch ticker with caching and rate limiting
     */
    @Override
    public Mono<TickerData> fetchTicker(String symbol) {
        String exchangeName = getExchangeName();
        String normalizedSymbol = normalizeSymbol(symbol);
        
        return getCachedOrFresh(
                () -> cacheService.getCachedTicker(exchangeName, normalizedSymbol),
                checkBackoffAndExecute(() -> fetchTickerFromExchange(normalizedSymbol)),
                ticker -> cacheService.cacheTicker(exchangeName, normalizedSymbol, ticker),
                "fetch ticker " + normalizedSymbol
        );
    }

    /**
     * Check if exchange is in backoff before executing request
     * @param request request to execute
     * @param <T> response type
     * @return Mono with result or error if in backoff
     */
    protected <T> Mono<T> checkBackoffAndExecute(java.util.function.Supplier<Mono<T>> request) {
        String exchangeName = getExchangeName();
        
        if (rateLimitingService.isInBackoff(exchangeName)) {
            Duration backoffTime = rateLimitingService.getBackoffTime(exchangeName);
            log.warn("Exchange {} is in backoff for {} more seconds", 
                exchangeName, backoffTime != null ? backoffTime.getSeconds() : 0);
            return Mono.error(new RuntimeException("Exchange in backoff state"));
        }
        
        return request.get()
                .doOnError(error -> handleRequestError("request execution", error));
    }
    
    /**
     * Выполнить HTTP запрос с учетом rate limiting
     */
    protected <T> Mono<T> executeRequest(Mono<T> request, Class<T> responseType) {
        return checkRateLimit()
                .then(request)
                .doOnSubscribe(s -> incrementRequestCounter())
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableError))
                .doOnError(error -> log.error("Request failed for {}: {}", 
                        getExchangeName(), error.getMessage()));
    }
    
    /**
     * Проверить лимиты запросов с использованием Redis
     */
    private Mono<Void> checkRateLimit() {
        String exchangeName = getExchangeName();
        String rateLimitKey = "exchange:" + exchangeName;
        
        // Check Redis-based rate limiting
        if (cacheService.isCacheHealthy()) {
            boolean allowed = rateLimitingService.isRequestAllowed(rateLimitKey, maxRequestsPerMinute, 60);
            if (!allowed) {
                long delay = rateLimitingService.getRecommendedDelay(rateLimitKey, maxRequestsPerMinute, 60);
                log.warn("Rate limit exceeded for {}, waiting {} ms", exchangeName, delay);
                return Mono.delay(Duration.ofMillis(delay)).then();
            }
            return Mono.empty();
        }
        
        // Fallback to in-memory rate limiting
        return getRateLimitInfo()
                .flatMap(info -> {
                    if (info.getStatus() == RateLimitInfo.RateLimitStatus.EXCEEDED) {
                        log.warn("Rate limit exceeded for {}, waiting...", exchangeName);
                        return Mono.delay(Duration.ofMillis(info.getRecommendedDelayMs())).then();
                    }
                    if (info.getStatus() == RateLimitInfo.RateLimitStatus.CRITICAL) {
                        return Mono.delay(Duration.ofMillis(info.getRecommendedDelayMs())).then();
                    }
                    return Mono.empty();
                });
    }
    
    /**
     * Увеличить счетчик запросов (fallback)
     */
    private void incrementRequestCounter() {
        resetCounterIfNeeded();
        requestCounter.incrementAndGet();
    }
    
    /**
     * Сбросить счетчик если прошла минута (fallback)
     */
    private void resetCounterIfNeeded() {
        LocalDateTime now = LocalDateTime.now();
        if (Duration.between(lastResetTime, now).toMinutes() >= 1) {
            requestCounter.set(0);
            lastResetTime = now;
        }
    }
    
    /**
     * Проверить, является ли ошибка повторяемой
     */
    private boolean isRetryableError(Throwable error) {
        if (error instanceof WebClientResponseException webClientError) {
            int status = webClientError.getStatusCode().value();
            // Retry on server errors and rate limits, but not on client errors
            return status >= 500 || status == 429 || status == 503;
        }
        
        // Retry on network issues
        return error.getMessage() != null && 
               (error.getMessage().contains("timeout") || 
                error.getMessage().contains("connection"));
    }
    
    /**
     * Проверить, является ли ошибка связанной с лимитами запросов
     */
    private boolean isRateLimitError(Throwable error) {
        if (error instanceof WebClientResponseException webClientError) {
            return webClientError.getStatusCode().value() == 429;
        }
        
        return error.getMessage() != null && 
               error.getMessage().toLowerCase().contains("rate limit");
    }
    
    /**
     * Создать TickerData для ошибки
     */
    protected TickerData createErrorTicker(String symbol, String error) {
        return TickerData.builder()
                .symbol(symbol)
                .exchange(getExchangeName())
                .price(BigDecimal.ZERO)
                .timestamp(LocalDateTime.now())
                .status(TickerData.TickerStatus.ERROR)
                .error(error)
                .build();
    }
    
    // ===== Abstract Methods =====
    
    protected abstract Mono<TickerData> fetchTickerFromExchange(String symbol);
    
    protected abstract String getHealthCheckEndpoint();
    
    protected abstract String normalizeSymbol(String symbol);
    
    /**
     * Get default value for the response type - subclasses should override if needed
     * @param <T> response type
     * @return default value
     */
    @SuppressWarnings("unchecked")
    protected <T> T getDefaultValue() {
        return (T) createErrorTicker("", "Service unavailable");
    }
} 