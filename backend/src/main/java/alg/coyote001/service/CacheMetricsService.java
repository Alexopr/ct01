package alg.coyote001.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for tracking cache metrics and performance statistics
 */
@Service
@Slf4j
public class CacheMetricsService {

    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    /**
     * Record a cache hit
     */
    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }

    /**
     * Record a cache miss
     */
    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }

    /**
     * Get cache hit rate
     * @return cache hit rate (0.0 to 1.0)
     */
    public double getCacheHitRate() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;

        if (total == 0) {
            return 0.0;
        }

        return (double) hits / total;
    }

    /**
     * Get cache statistics
     * @return formatted cache statistics string
     */
    public String getCacheStatistics() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        double hitRate = getCacheHitRate();

        return String.format(
            "Cache Stats: Hits=%d, Misses=%d, Total=%d, Hit Rate=%.2f%%",
            hits, misses, total, hitRate * 100
        );
    }

    /**
     * Get total cache hits
     * @return number of cache hits
     */
    public long getCacheHits() {
        return cacheHits.get();
    }

    /**
     * Get total cache misses
     * @return number of cache misses
     */
    public long getCacheMisses() {
        return cacheMisses.get();
    }

    /**
     * Get total cache operations
     * @return total number of cache operations
     */
    public long getTotalCacheOperations() {
        return cacheHits.get() + cacheMisses.get();
    }

    /**
     * Reset cache metrics to zero
     */
    public void resetMetrics() {
        cacheHits.set(0);
        cacheMisses.set(0);
        log.info("Cache metrics reset");
    }
} 