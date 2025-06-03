package alg.coyote001.service.exchange;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Unit тесты для RateLimitInfo
 */
class RateLimitInfoTest {
    
    @Test
    void testCalculateUsage_NormalStatus() {
        // Given
        RateLimitInfo rateLimitInfo = RateLimitInfo.builder()
                .currentRequests(30)
                .maxRequestsPerMinute(100)
                .build();
        
        // When
        rateLimitInfo.calculateUsage();
        
        // Then
        assertEquals(70, rateLimitInfo.getRemainingRequests());
        assertEquals(30.0, rateLimitInfo.getUsagePercentage(), 0.01);
        assertEquals(RateLimitInfo.RateLimitStatus.NORMAL, rateLimitInfo.getStatus());
        assertEquals(1000L, rateLimitInfo.getRecommendedDelayMs());
    }
    
    @Test
    void testCalculateUsage_WarningStatus() {
        // Given
        RateLimitInfo rateLimitInfo = RateLimitInfo.builder()
                .currentRequests(75)
                .maxRequestsPerMinute(100)
                .build();
        
        // When
        rateLimitInfo.calculateUsage();
        
        // Then
        assertEquals(25, rateLimitInfo.getRemainingRequests());
        assertEquals(75.0, rateLimitInfo.getUsagePercentage(), 0.01);
        assertEquals(RateLimitInfo.RateLimitStatus.WARNING, rateLimitInfo.getStatus());
        assertEquals(2000L, rateLimitInfo.getRecommendedDelayMs());
    }
    
    @Test
    void testCalculateUsage_CriticalStatus() {
        // Given
        RateLimitInfo rateLimitInfo = RateLimitInfo.builder()
                .currentRequests(92)
                .maxRequestsPerMinute(100)
                .build();
        
        // When
        rateLimitInfo.calculateUsage();
        
        // Then
        assertEquals(8, rateLimitInfo.getRemainingRequests());
        assertEquals(92.0, rateLimitInfo.getUsagePercentage(), 0.01);
        assertEquals(RateLimitInfo.RateLimitStatus.CRITICAL, rateLimitInfo.getStatus());
        assertEquals(5000L, rateLimitInfo.getRecommendedDelayMs());
    }
    
    @Test
    void testCalculateUsage_ExceededStatus() {
        // Given
        RateLimitInfo rateLimitInfo = RateLimitInfo.builder()
                .currentRequests(105)
                .maxRequestsPerMinute(100)
                .build();
        
        // When
        rateLimitInfo.calculateUsage();
        
        // Then
        assertEquals(-5, rateLimitInfo.getRemainingRequests());
        assertEquals(105.0, rateLimitInfo.getUsagePercentage(), 0.01);
        assertEquals(RateLimitInfo.RateLimitStatus.EXCEEDED, rateLimitInfo.getStatus());
        assertEquals(60000L, rateLimitInfo.getRecommendedDelayMs());
    }
    
    @Test
    void testCalculateUsage_ExactlyAtLimit() {
        // Given
        RateLimitInfo rateLimitInfo = RateLimitInfo.builder()
                .currentRequests(100)
                .maxRequestsPerMinute(100)
                .build();
        
        // When
        rateLimitInfo.calculateUsage();
        
        // Then
        assertEquals(0, rateLimitInfo.getRemainingRequests());
        assertEquals(100.0, rateLimitInfo.getUsagePercentage(), 0.01);
        assertEquals(RateLimitInfo.RateLimitStatus.EXCEEDED, rateLimitInfo.getStatus());
        assertEquals(60000L, rateLimitInfo.getRecommendedDelayMs());
    }
    
    @Test
    void testCalculateUsage_EdgeCase70Percent() {
        // Given
        RateLimitInfo rateLimitInfo = RateLimitInfo.builder()
                .currentRequests(70)
                .maxRequestsPerMinute(100)
                .build();
        
        // When
        rateLimitInfo.calculateUsage();
        
        // Then
        assertEquals(30, rateLimitInfo.getRemainingRequests());
        assertEquals(70.0, rateLimitInfo.getUsagePercentage(), 0.01);
        assertEquals(RateLimitInfo.RateLimitStatus.WARNING, rateLimitInfo.getStatus());
        assertEquals(2000L, rateLimitInfo.getRecommendedDelayMs());
    }
    
    @Test
    void testCalculateUsage_EdgeCase90Percent() {
        // Given
        RateLimitInfo rateLimitInfo = RateLimitInfo.builder()
                .currentRequests(90)
                .maxRequestsPerMinute(100)
                .build();
        
        // When
        rateLimitInfo.calculateUsage();
        
        // Then
        assertEquals(10, rateLimitInfo.getRemainingRequests());
        assertEquals(90.0, rateLimitInfo.getUsagePercentage(), 0.01);
        assertEquals(RateLimitInfo.RateLimitStatus.CRITICAL, rateLimitInfo.getStatus());
        assertEquals(5000L, rateLimitInfo.getRecommendedDelayMs());
    }
    
    @Test
    void testBuilder() {
        // Given
        LocalDateTime resetTime = LocalDateTime.now().plusMinutes(1);
        
        // When
        RateLimitInfo rateLimitInfo = RateLimitInfo.builder()
                .currentRequests(50)
                .maxRequestsPerMinute(120)
                .resetTime(resetTime)
                .remainingRequests(70)
                .usagePercentage(41.67)
                .recommendedDelayMs(1000L)
                .status(RateLimitInfo.RateLimitStatus.NORMAL)
                .build();
        
        // Then
        assertEquals(50, rateLimitInfo.getCurrentRequests());
        assertEquals(120, rateLimitInfo.getMaxRequestsPerMinute());
        assertEquals(resetTime, rateLimitInfo.getResetTime());
        assertEquals(70, rateLimitInfo.getRemainingRequests());
        assertEquals(41.67, rateLimitInfo.getUsagePercentage(), 0.01);
        assertEquals(1000L, rateLimitInfo.getRecommendedDelayMs());
        assertEquals(RateLimitInfo.RateLimitStatus.NORMAL, rateLimitInfo.getStatus());
    }
    
    @Test
    void testDefaultConstructor() {
        // When
        RateLimitInfo rateLimitInfo = new RateLimitInfo();
        
        // Then
        assertEquals(0, rateLimitInfo.getCurrentRequests());
        assertEquals(0, rateLimitInfo.getMaxRequestsPerMinute());
        assertNull(rateLimitInfo.getResetTime());
        assertEquals(0, rateLimitInfo.getRemainingRequests());
        assertEquals(0.0, rateLimitInfo.getUsagePercentage());
        assertEquals(0L, rateLimitInfo.getRecommendedDelayMs());
        assertNull(rateLimitInfo.getStatus());
    }
    
    @Test
    void testRateLimitStatusEnum() {
        // Test all enum values
        assertEquals(4, RateLimitInfo.RateLimitStatus.values().length);
        
        assertEquals(RateLimitInfo.RateLimitStatus.NORMAL, 
                RateLimitInfo.RateLimitStatus.valueOf("NORMAL"));
        assertEquals(RateLimitInfo.RateLimitStatus.WARNING, 
                RateLimitInfo.RateLimitStatus.valueOf("WARNING"));
        assertEquals(RateLimitInfo.RateLimitStatus.CRITICAL, 
                RateLimitInfo.RateLimitStatus.valueOf("CRITICAL"));
        assertEquals(RateLimitInfo.RateLimitStatus.EXCEEDED, 
                RateLimitInfo.RateLimitStatus.valueOf("EXCEEDED"));
    }
    
    @Test
    void testCalculateUsage_ZeroRequests() {
        // Given
        RateLimitInfo rateLimitInfo = RateLimitInfo.builder()
                .currentRequests(0)
                .maxRequestsPerMinute(60)
                .build();
        
        // When
        rateLimitInfo.calculateUsage();
        
        // Then
        assertEquals(60, rateLimitInfo.getRemainingRequests());
        assertEquals(0.0, rateLimitInfo.getUsagePercentage(), 0.01);
        assertEquals(RateLimitInfo.RateLimitStatus.NORMAL, rateLimitInfo.getStatus());
        assertEquals(1000L, rateLimitInfo.getRecommendedDelayMs());
    }
} 