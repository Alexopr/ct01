package alg.coyote001.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    
    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();
    
    // Максимум 5 попыток авторизации в минуту на IP
    private static final int AUTH_ATTEMPTS_LIMIT = 5;
    private static final Duration AUTH_WINDOW = Duration.ofMinutes(1);
    
    // Максимум 100 обычных запросов в минуту на IP
    private static final int GENERAL_REQUESTS_LIMIT = 100;
    private static final Duration GENERAL_WINDOW = Duration.ofMinutes(1);
    
    public Bucket createAuthBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(AUTH_ATTEMPTS_LIMIT, Refill.intervally(AUTH_ATTEMPTS_LIMIT, AUTH_WINDOW)))
            .build();
    }
    
    public Bucket createGeneralBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(GENERAL_REQUESTS_LIMIT, Refill.intervally(GENERAL_REQUESTS_LIMIT, GENERAL_WINDOW)))
            .build();
    }
    
    public boolean tryConsumeAuth(String key) {
        return getBucket(key, "auth").tryConsume(1);
    }
    
    public boolean tryConsumeGeneral(String key) {
        return getBucket(key, "general").tryConsume(1);
    }
    
    private Bucket getBucket(String key, String type) {
        String bucketKey = key + ":" + type;
        return cache.computeIfAbsent(bucketKey, k -> {
            if ("auth".equals(type)) {
                return createAuthBucket();
            } else {
                return createGeneralBucket();
            }
        });
    }
    
    public void resetAuthLimit(String key) {
        cache.remove(key + ":auth");
    }
} 