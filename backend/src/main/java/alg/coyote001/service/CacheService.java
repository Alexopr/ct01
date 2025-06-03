package alg.coyote001.service;

import alg.coyote001.dto.TickerData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Cache management service with improved structure and separated concerns
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheMetricsService metricsService;
    
    private static final String TICKERS_CACHE = "tickers";
    private static final String SYMBOLS_CACHE = "symbols";
    private static final String HEALTH_CHECK_CACHE = "healthCheck";
    private static final String LAST_WARMED_PREFIX = "cache_warmed:";
    
    // ===== Generic Cache Operations =====
    
    /**
     * Generic method to get value from cache with automatic metrics tracking
     * @param cacheName name of the cache
     * @param key cache key
     * @param type expected return type
     * @return cached value or empty optional if not found
     */
    private <T> Optional<T> getCachedValue(String cacheName, Object key, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            metricsService.recordCacheMiss();
            log.debug("Cache '{}' not found", cacheName);
            return Optional.empty();
        }

        Cache.ValueWrapper wrapper = cache.get(key);
        if (wrapper != null && wrapper.get() != null) {
            metricsService.recordCacheHit();
            log.debug("Cache hit for {}:{} in cache '{}'", key, type.getSimpleName(), cacheName);
            return Optional.of(type.cast(wrapper.get()));
        }

        metricsService.recordCacheMiss();
        log.debug("Cache miss for {}:{} in cache '{}'", key, type.getSimpleName(), cacheName);
        return Optional.empty();
    }

    /**
     * Generic method to put value into cache
     * @param cacheName name of the cache
     * @param key cache key
     * @param value value to cache
     */
    private void putCachedValue(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
            log.debug("Cached value for key '{}' in cache '{}'", key, cacheName);
        } else {
            log.warn("Cache '{}' not found, unable to cache value for key '{}'", cacheName, key);
        }
    }

    /**
     * Generic method to evict value from cache
     * @param cacheName name of the cache
     * @param key cache key
     */
    private void evictCachedValue(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.debug("Evicted key '{}' from cache '{}'", key, cacheName);
        }
    }
    
    // ===== Ticker Cache Operations =====
    
    /**
     * Cache ticker data
     * @param exchange Exchange name
     * @param symbol Trading pair symbol
     * @param tickerData Ticker data to cache
     */
    public void cacheTicker(String exchange, String symbol, TickerData tickerData) {
        String cacheKey = generateTickerKey(exchange, symbol);
        putCachedValue(TICKERS_CACHE, cacheKey, tickerData);
    }
    
    /**
     * Get cached ticker data
     * @param exchange Exchange name
     * @param symbol Trading pair symbol
     * @return cached ticker data or null if not found
     */
    public TickerData getCachedTicker(String exchange, String symbol) {
        String cacheKey = generateTickerKey(exchange, symbol);
        return getCachedValue(TICKERS_CACHE, cacheKey, TickerData.class).orElse(null);
    }

    /**
     * Evict cache for specific ticker
     * @param exchange Exchange name
     * @param symbol Trading symbol
     */
    public void evictTickerCache(String exchange, String symbol) {
        String cacheKey = generateTickerKey(exchange, symbol);
        evictCachedValue(TICKERS_CACHE, cacheKey);
    }
    
    // ===== Symbols Cache Operations =====
    
    /**
     * Cache exchange symbols list
     * @param exchange Exchange name
     * @param symbols List of trading symbols
     */
    public void cacheSymbols(String exchange, List<String> symbols) {
        putCachedValue(SYMBOLS_CACHE, exchange, symbols);
        log.info("Cached {} symbols for exchange {}", symbols.size(), exchange);
    }
    
    /**
     * Get cached symbols for exchange
     * @param exchange Exchange name
     * @return cached symbols list or null if not found
     */
    @SuppressWarnings("unchecked")
    public List<String> getCachedSymbols(String exchange) {
        return (List<String>) getCachedValue(SYMBOLS_CACHE, exchange, List.class).orElse(null);
    }
    
    // ===== Health Check Cache Operations =====
    
    /**
     * Cache health check result
     * @param exchange Exchange name
     * @param isHealthy Health status
     */
    public void cacheHealthCheck(String exchange, boolean isHealthy) {
        putCachedValue(HEALTH_CHECK_CACHE, exchange, isHealthy);
    }
    
    /**
     * Get cached health check result
     * @param exchange Exchange name
     * @return cached health status or null if not found
     */
    public Boolean getCachedHealthCheck(String exchange) {
        return getCachedValue(HEALTH_CHECK_CACHE, exchange, Boolean.class).orElse(null);
    }
    
    // ===== Cache Management Operations =====
    
    /**
     * Warm cache with critical data for an exchange
     * @param exchange Exchange name
     */
    public void warmCache(String exchange) {
        try {
            log.info("Starting cache warming for exchange: {}", exchange);
            
            markCacheWarmedTime(exchange);
            
            // Note: Actual cache warming would involve calling exchange adapters
            // This is a placeholder for the warming logic
            log.info("Cache warming completed for exchange: {}", exchange);
            
        } catch (Exception e) {
            log.error("Error warming cache for exchange: {}", exchange, e);
        }
    }

    /**
     * Mark cache warming time for exchange
     * @param exchange Exchange name
     */
    private void markCacheWarmedTime(String exchange) {
        String warmedKey = LAST_WARMED_PREFIX + exchange;
        redisTemplate.opsForValue().set(warmedKey, LocalDateTime.now().toString());
    }
    
    /**
     * Get last cache warming time for exchange
     * @param exchange Exchange name
     * @return last warming time or null if never warmed
     */
    public LocalDateTime getLastWarmedTime(String exchange) {
        String warmedKey = LAST_WARMED_PREFIX + exchange;
        try {
            String timeStr = (String) redisTemplate.opsForValue().get(warmedKey);
            return timeStr != null ? LocalDateTime.parse(timeStr) : null;
        } catch (Exception e) {
            log.error("Error getting last warmed time for exchange: {}", exchange, e);
            return null;
        }
    }
    
    /**
     * Evict all cached data for an exchange
     * @param exchange Exchange name
     */
    @CacheEvict(value = {TICKERS_CACHE, SYMBOLS_CACHE, HEALTH_CHECK_CACHE}, allEntries = true)
    public void evictExchangeCache(String exchange) {
        log.info("Evicted all cache data for exchange: {}", exchange);
        
        clearRedisKeysForExchange(exchange);
    }

    /**
     * Clear Redis-specific keys for an exchange
     * @param exchange Exchange name
     */
    private void clearRedisKeysForExchange(String exchange) {
        try {
            redisTemplate.delete("rate_limit:" + exchange);
            redisTemplate.delete("backoff:" + exchange);
            redisTemplate.delete(LAST_WARMED_PREFIX + exchange);
            
            log.debug("Cleared Redis keys for exchange: {}", exchange);
        } catch (Exception e) {
            log.error("Error clearing Redis keys for exchange: {}", exchange, e);
        }
    }
    
    // ===== Cache Health & Metrics =====
    
    /**
     * Check if cache is healthy (Redis connectivity)
     * @return true if cache is operational
     */
    public boolean isCacheHealthy() {
        try {
            String testKey = "health_check_" + System.currentTimeMillis();
            String testValue = "test";
            
            redisTemplate.opsForValue().set(testKey, testValue);
            String result = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);
            
            return testValue.equals(result);
        } catch (Exception e) {
            log.error("Cache health check failed", e);
            return false;
        }
    }

    /**
     * Get cache hit rate from metrics service
     * @return cache hit rate (0.0 to 1.0)
     */
    public double getCacheHitRate() {
        return metricsService.getCacheHitRate();
    }

    /**
     * Get cache statistics from metrics service
     * @return formatted cache statistics string
     */
    public String getCacheStatistics() {
        return metricsService.getCacheStatistics();
    }

    /**
     * Reset cache metrics
     */
    public void resetCacheMetrics() {
        metricsService.resetMetrics();
    }
    
    // ===== Private Utility Methods =====
    
    /**
     * Generate cache key for ticker data
     * @param exchange Exchange name
     * @param symbol Trading symbol
     * @return formatted cache key
     */
    private String generateTickerKey(String exchange, String symbol) {
        return exchange + ":" + symbol;
    }
} 