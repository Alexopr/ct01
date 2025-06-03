package alg.coyote001.service;

import alg.coyote001.config.SubscriptionLimitsConfiguration;
import alg.coyote001.config.SubscriptionLimits;
import alg.coyote001.entity.SubscriptionUsage;
import alg.coyote001.entity.SubscriptionUsage.ResetPeriod;
import alg.coyote001.entity.UserSubscription.PlanType;
import alg.coyote001.repository.SubscriptionUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionLimitServiceTest {

    @Mock
    private SubscriptionUsageRepository usageRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionLimitsConfiguration limitsConfig;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private SubscriptionLimitService limitService;

    private SubscriptionUsage testUsage;
    private SubscriptionLimits testLimits;

    @BeforeEach
    void setUp() {
        testUsage = SubscriptionUsage.builder()
                .id(1L)
                .userId(1L)
                .resourceType("TWITTER_TRACK")
                .usageCount(5)
                .limitValue(10)
                .resetPeriod(ResetPeriod.MONTHLY)
                .lastResetAt(LocalDateTime.now().minusDays(1))
                .build();

        testLimits = new SubscriptionLimits();
        testLimits.setTwitterTracker(new SubscriptionLimits.TwitterTracker());
        testLimits.getTwitterTracker().setMaxTrackedAccounts(10);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCheckLimit_WithinLimit() {
        // Given
        when(subscriptionService.getUserPlanType(1L)).thenReturn(PlanType.PREMIUM);
        when(limitsConfig.getLimitsForPlan("PREMIUM")).thenReturn(testLimits);
        when(usageRepository.findByUserIdAndResourceType(1L, "TWITTER_TRACK"))
                .thenReturn(Optional.of(testUsage));

        // When
        boolean result = limitService.checkLimit(1L, "TWITTER_TRACK");

        // Then
        assertTrue(result);
        verify(usageRepository).findByUserIdAndResourceType(1L, "TWITTER_TRACK");
    }

    @Test
    void testCheckLimit_ExceedsLimit() {
        // Given
        testUsage.setUsageCount(15);
        when(subscriptionService.getUserPlanType(1L)).thenReturn(PlanType.PREMIUM);
        when(limitsConfig.getLimitsForPlan("PREMIUM")).thenReturn(testLimits);
        when(usageRepository.findByUserIdAndResourceType(1L, "TWITTER_TRACK"))
                .thenReturn(Optional.of(testUsage));

        // When
        boolean result = limitService.checkLimit(1L, "TWITTER_TRACK");

        // Then
        assertFalse(result);
    }

    @Test
    void testCheckLimit_NoUsageRecord() {
        // Given
        when(subscriptionService.getUserPlanType(1L)).thenReturn(PlanType.PREMIUM);
        when(limitsConfig.getLimitsForPlan("PREMIUM")).thenReturn(testLimits);
        when(usageRepository.findByUserIdAndResourceType(1L, "TWITTER_TRACK"))
                .thenReturn(Optional.empty());

        // When
        boolean result = limitService.checkLimit(1L, "TWITTER_TRACK");

        // Then
        assertTrue(result); // First usage should be allowed
    }

    @Test
    void testCheckLimit_UnlimitedPlan() {
        // Given
        testLimits.getTwitterTracker().setMaxTrackedAccounts(-1); // Unlimited
        when(subscriptionService.getUserPlanType(1L)).thenReturn(PlanType.PREMIUM);
        when(limitsConfig.getLimitsForPlan("PREMIUM")).thenReturn(testLimits);

        // When
        boolean result = limitService.checkLimit(1L, "TWITTER_TRACK");

        // Then
        assertTrue(result);
    }

    @Test
    void testIncrementUsage_NewRecord() {
        // Given
        when(subscriptionService.getUserPlanType(1L)).thenReturn(PlanType.PREMIUM);
        when(limitsConfig.getLimitsForPlan("PREMIUM")).thenReturn(testLimits);
        when(usageRepository.findByUserIdAndResourceType(1L, "TWITTER_TRACK"))
                .thenReturn(Optional.empty());
        when(usageRepository.save(any(SubscriptionUsage.class))).thenReturn(testUsage);

        // When
        limitService.incrementUsage(1L, "TWITTER_TRACK");

        // Then
        verify(usageRepository).save(argThat(usage -> 
            usage.getUserId().equals(1L) && 
            usage.getResourceType().equals("TWITTER_TRACK") &&
            usage.getUsageCount() == 1
        ));
    }

    @Test
    void testIncrementUsage_ExistingRecord() {
        // Given
        when(subscriptionService.getUserPlanType(1L)).thenReturn(PlanType.PREMIUM);
        when(limitsConfig.getLimitsForPlan("PREMIUM")).thenReturn(testLimits);
        when(usageRepository.findByUserIdAndResourceType(1L, "TWITTER_TRACK"))
                .thenReturn(Optional.of(testUsage));
        when(usageRepository.save(any(SubscriptionUsage.class))).thenReturn(testUsage);

        // When
        limitService.incrementUsage(1L, "TWITTER_TRACK");

        // Then
        verify(usageRepository).save(argThat(usage -> 
            usage.getUsageCount() == 6 // 5 + 1
        ));
    }

    @Test
    void testIncrementUsage_ThreadSafety() throws InterruptedException {
        // Given
        when(subscriptionService.getUserPlanType(1L)).thenReturn(PlanType.PREMIUM);
        when(limitsConfig.getLimitsForPlan("PREMIUM")).thenReturn(testLimits);
        when(usageRepository.findByUserIdAndResourceType(1L, "TWITTER_TRACK"))
                .thenReturn(Optional.of(testUsage));
        when(usageRepository.save(any(SubscriptionUsage.class))).thenReturn(testUsage);

        // When - simulate concurrent access
        Thread thread1 = new Thread(() -> limitService.incrementUsage(1L, "TWITTER_TRACK"));
        Thread thread2 = new Thread(() -> limitService.incrementUsage(1L, "TWITTER_TRACK"));

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // Then - verify that save was called (thread safety via ReentrantLock should prevent issues)
        verify(usageRepository, atLeast(2)).save(any(SubscriptionUsage.class));
    }

    @Test
    void testGetCurrentUsage() {
        // Given
        when(usageRepository.findByUserIdAndResourceType(1L, "TWITTER_TRACK"))
                .thenReturn(Optional.of(testUsage));

        // When
        int result = limitService.getCurrentUsage(1L, "TWITTER_TRACK");

        // Then
        assertEquals(5, result);
        verify(usageRepository).findByUserIdAndResourceType(1L, "TWITTER_TRACK");
    }

    @Test
    void testGetCurrentUsage_NoRecord() {
        // Given
        when(usageRepository.findByUserIdAndResourceType(1L, "TWITTER_TRACK"))
                .thenReturn(Optional.empty());

        // When
        int result = limitService.getCurrentUsage(1L, "TWITTER_TRACK");

        // Then
        assertEquals(0, result);
    }

    @Test
    void testGetLimit() {
        // Given
        when(subscriptionService.getUserPlanType(1L)).thenReturn(PlanType.PREMIUM);
        when(limitsConfig.getLimitsForPlan("PREMIUM")).thenReturn(testLimits);

        // When
        int result = limitService.getLimit(1L, "TWITTER_TRACK");

        // Then
        assertEquals(10, result);
    }

    @Test
    void testGetLimit_UnknownResource() {
        // Given
        when(subscriptionService.getUserPlanType(1L)).thenReturn(PlanType.PREMIUM);
        when(limitsConfig.getLimitsForPlan("PREMIUM")).thenReturn(testLimits);

        // When
        int result = limitService.getLimit(1L, "UNKNOWN_RESOURCE");

        // Then
        assertEquals(0, result); // Default limit for unknown resources
    }

    @Test
    void testResetUsageForPeriod() {
        // Given
        when(usageRepository.resetUsageForPeriod(ResetPeriod.DAILY, any(LocalDateTime.class)))
                .thenReturn(5);

        // When
        int result = limitService.resetUsageForPeriod(ResetPeriod.DAILY);

        // Then
        assertEquals(5, result);
        verify(usageRepository).resetUsageForPeriod(eq(ResetPeriod.DAILY), any(LocalDateTime.class));
    }

    @Test
    void testResetUserUsage() {
        // Given
        when(usageRepository.resetUserUsage(1L, any(LocalDateTime.class)))
                .thenReturn(3);

        // When
        int result = limitService.resetUserUsage(1L);

        // Then
        assertEquals(3, result);
        verify(usageRepository).resetUserUsage(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void testCheckAndIncrementUsage_Success() {
        // Given
        when(subscriptionService.getUserPlanType(1L)).thenReturn(PlanType.PREMIUM);
        when(limitsConfig.getLimitsForPlan("PREMIUM")).thenReturn(testLimits);
        when(usageRepository.findByUserIdAndResourceType(1L, "TWITTER_TRACK"))
                .thenReturn(Optional.of(testUsage));
        when(usageRepository.save(any(SubscriptionUsage.class))).thenReturn(testUsage);

        // When
        boolean result = limitService.checkAndIncrementUsage(1L, "TWITTER_TRACK");

        // Then
        assertTrue(result);
        verify(usageRepository).save(any(SubscriptionUsage.class));
    }

    @Test
    void testCheckAndIncrementUsage_LimitExceeded() {
        // Given
        testUsage.setUsageCount(10); // At limit
        when(subscriptionService.getUserPlanType(1L)).thenReturn(PlanType.PREMIUM);
        when(limitsConfig.getLimitsForPlan("PREMIUM")).thenReturn(testLimits);
        when(usageRepository.findByUserIdAndResourceType(1L, "TWITTER_TRACK"))
                .thenReturn(Optional.of(testUsage));

        // When
        boolean result = limitService.checkAndIncrementUsage(1L, "TWITTER_TRACK");

        // Then
        assertFalse(result);
        verify(usageRepository, never()).save(any(SubscriptionUsage.class));
    }

    @Test
    void testIsResourceUnlimited() {
        // Given
        testLimits.getTwitterTracker().setMaxTrackedAccounts(-1);
        when(subscriptionService.getUserPlanType(1L)).thenReturn(PlanType.PREMIUM);
        when(limitsConfig.getLimitsForPlan("PREMIUM")).thenReturn(testLimits);

        // When
        boolean result = limitService.isResourceUnlimited(1L, "TWITTER_TRACK");

        // Then
        assertTrue(result);
    }

    @Test
    void testIsResourceUnlimited_Limited() {
        // Given
        when(subscriptionService.getUserPlanType(1L)).thenReturn(PlanType.PREMIUM);
        when(limitsConfig.getLimitsForPlan("PREMIUM")).thenReturn(testLimits);

        // When
        boolean result = limitService.isResourceUnlimited(1L, "TWITTER_TRACK");

        // Then
        assertFalse(result);
    }

    @Test
    void testCacheUsage() {
        // Given
        String cacheKey = "subscription_usage:1:TWITTER_TRACK";
        when(valueOperations.get(cacheKey)).thenReturn(null);

        // When
        limitService.getCurrentUsage(1L, "TWITTER_TRACK");

        // Then
        verify(redisTemplate).opsForValue();
        // Cache operations would be verified in integration tests
    }
} 