package alg.coyote001.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing API rate limiting using Redis
 * Optimized with smaller, focused methods
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitingService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String BACKOFF_PREFIX = "backoff:";
    
    // ===== Rate Limiting Core Methods =====
    
    /**
     * Check if a request is allowed within rate limits
     * @param key Rate limit key
     * @param maxRequests Maximum requests allowed
     * @param windowSeconds Time window in seconds
     * @return true if request is allowed, false if rate limited
     */
    public boolean isRequestAllowed(String key, int maxRequests, int windowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        
        try {
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);
            
            if (currentCount == null) {
                log.warn("Failed to increment rate limit counter for key: {}", key);
                return false;
            }
            
            handleFirstRequest(redisKey, currentCount, windowSeconds);
            
            boolean allowed = currentCount <= maxRequests;
            logRateLimitDecision(key, currentCount, maxRequests, allowed);
            
            return allowed;
            
        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            return false; // Fail closed for safety
        }
    }
    
    /**
     * Handle TTL setting for first request
     */
    private void handleFirstRequest(String redisKey, Long currentCount, int windowSeconds) {
        if (currentCount == 1) {
            redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
        }
    }
    
    /**
     * Log rate limiting decision
     */
    private void logRateLimitDecision(String key, Long currentCount, int maxRequests, boolean allowed) {
        if (allowed) {
            log.debug("Rate limit check passed for {}: {}/{}", key, currentCount, maxRequests);
        } else {
            log.warn("Rate limit exceeded for {}: {}/{}", key, currentCount, maxRequests);
        }
    }
    
    /**
     * Get current request count for a key
     * @param key Rate limit key
     * @return current count, 0 if not found
     */
    public int getCurrentCount(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        try {
            Integer count = (Integer) redisTemplate.opsForValue().get(redisKey);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Error getting current count for key: {}", key, e);
            return 0;
        }
    }
    
    // ===== Time and Reset Methods =====
    
    /**
     * Get time until rate limit resets
     * @param key Rate limit key
     * @return Duration until reset, or null if no limit active
     */
    public Duration getTimeUntilReset(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        try {
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            return (ttl != null && ttl > 0) ? Duration.ofSeconds(ttl) : null;
        } catch (Exception e) {
            log.error("Error getting TTL for key: {}", key, e);
            return null;
        }
    }
    
    /**
     * Calculate recommended delay before next request
     * @param key Rate limit key
     * @param maxRequests Maximum requests allowed
     * @param windowSeconds Time window in seconds
     * @return recommended delay in milliseconds
     */
    public long getRecommendedDelay(String key, int maxRequests, int windowSeconds) {
        int currentCount = getCurrentCount(key);
        Duration timeUntilReset = getTimeUntilReset(key);
        
        if (currentCount < maxRequests) {
            return 0; // No delay needed
        }
        
        return calculateDelayForExceededLimit(timeUntilReset, maxRequests, windowSeconds);
    }
    
    /**
     * Calculate delay when rate limit is exceeded
     */
    private long calculateDelayForExceededLimit(Duration timeUntilReset, int maxRequests, int windowSeconds) {
        if (timeUntilReset != null) {
            // If rate limited, wait until reset + small buffer
            return timeUntilReset.toMillis() + 100;
        }
        
        // Default calculation: spread requests evenly across window
        return (windowSeconds * 1000L) / maxRequests;
    }
    
    // ===== Backoff Management =====
    
    /**
     * Set backoff state for an exchange (when approaching limits)
     * @param exchangeName Name of the exchange
     * @param backoffDurationSeconds Duration of backoff in seconds
     */
    public void setBackoff(String exchangeName, int backoffDurationSeconds) {
        String backoffKey = BACKOFF_PREFIX + exchangeName;
        LocalDateTime backoffUntil = LocalDateTime.now().plusSeconds(backoffDurationSeconds);
        
        try {
            redisTemplate.opsForValue().set(backoffKey, backoffUntil.toString(), 
                backoffDurationSeconds, TimeUnit.SECONDS);
            log.info("Set backoff for {} until {}", exchangeName, backoffUntil);
        } catch (Exception e) {
            log.error("Error setting backoff for exchange: {}", exchangeName, e);
        }
    }
    
    /**
     * Check if an exchange is currently in backoff
     * @param exchangeName Name of the exchange
     * @return true if in backoff state
     */
    public boolean isInBackoff(String exchangeName) {
        Duration backoffTime = getBackoffTime(exchangeName);
        return backoffTime != null && backoffTime.getSeconds() > 0;
    }
    
    /**
     * Get remaining backoff time
     * @param exchangeName Name of the exchange
     * @return Duration until backoff ends, or null if not in backoff
     */
    public Duration getBackoffTime(String exchangeName) {
        String backoffKey = BACKOFF_PREFIX + exchangeName;
        try {
            String backoffUntilStr = (String) redisTemplate.opsForValue().get(backoffKey);
            if (backoffUntilStr != null) {
                LocalDateTime backoffUntil = LocalDateTime.parse(backoffUntilStr);
                LocalDateTime now = LocalDateTime.now();
                
                return now.isBefore(backoffUntil) ? Duration.between(now, backoffUntil) : null;
            }
        } catch (Exception e) {
            log.error("Error getting backoff time for exchange: {}", exchangeName, e);
        }
        return null;
    }
    
    // ===== Utility and Admin Methods =====
    
    /**
     * Reset rate limit for a key (for admin/testing purposes)
     * @param key Rate limit key
     */
    public void resetRateLimit(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        redisTemplate.delete(redisKey);
        log.info("Reset rate limit for key: {}", key);
    }
    
    /**
     * Check if a key is currently rate limited with default limits
     * @param key Rate limit key
     * @return true if rate limited, false otherwise
     */
    public boolean isRateLimited(String key) {
        RateLimitConfig config = getDefaultRateLimitConfig(key);
        return !isRequestAllowed(key, config.maxRequests, config.windowSeconds);
    }
    
    /**
     * Get remaining requests for a key with default limits
     * @param key Rate limit key
     * @return number of remaining requests
     */
    public int getRemainingRequests(String key) {
        RateLimitConfig config = getDefaultRateLimitConfig(key);
        int currentCount = getCurrentCount(key);
        return Math.max(0, config.maxRequests - currentCount);
    }
    
    /**
     * Get reset time for a key
     * @param key Rate limit key
     * @return LocalDateTime when the rate limit resets
     */
    public LocalDateTime getResetTime(String key) {
        Duration timeUntilReset = getTimeUntilReset(key);
        return timeUntilReset != null ? 
            LocalDateTime.now().plus(timeUntilReset) : 
            LocalDateTime.now();
    }
    
    // ===== Configuration Helpers =====
    
    /**
     * Get default rate limit configuration based on key pattern
     */
    private RateLimitConfig getDefaultRateLimitConfig(String key) {
        // Default rate limits based on exchange name
        int maxRequests = 100; // Default
        int windowSeconds = 60; // 1 minute window
        
        // Adjust based on key pattern
        if (key.contains("binance")) {
            maxRequests = 1200;
        } else if (key.contains("bybit")) {
            maxRequests = 600;
        } else if (key.contains("okx")) {
            maxRequests = 300;
        }
        
        return new RateLimitConfig(maxRequests, windowSeconds);
    }
    
    /**
     * Internal configuration class
     */
    private static class RateLimitConfig {
        final int maxRequests;
        final int windowSeconds;
        
        RateLimitConfig(int maxRequests, int windowSeconds) {
            this.maxRequests = maxRequests;
            this.windowSeconds = windowSeconds;
        }
    }
} 