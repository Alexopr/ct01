package alg.coyote001.service;

import alg.coyote001.config.SubscriptionLimitsConfiguration;
import alg.coyote001.config.SubscriptionPlan;
import alg.coyote001.entity.User;
import alg.coyote001.entity.UserSubscription;
import alg.coyote001.entity.UserSubscription.PlanType;
import alg.coyote001.entity.UserSubscription.SubscriptionStatus;
import alg.coyote001.event.SubscriptionCancelledEvent;
import alg.coyote001.event.SubscriptionUpgradedEvent;
import alg.coyote001.repository.UserRepository;
import alg.coyote001.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private UserSubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionLimitsConfiguration subscriptionConfig;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User testUser;
    private UserSubscription testSubscription;
    private SubscriptionPlan premiumPlan;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        testSubscription = UserSubscription.builder()
                .id(1L)
                .user(testUser)
                .planType(PlanType.FREE)
                .status(SubscriptionStatus.ACTIVE)
                .startsAt(LocalDateTime.now())
                .autoRenewal(true)
                .build();

        premiumPlan = new SubscriptionPlan();
        premiumPlan.setName("PREMIUM");
        premiumPlan.setBillingCycle("monthly");
        premiumPlan.setTrialDays(7);
    }

    @Test
    void testGetActiveSubscription() {
        // Given
        when(subscriptionRepository.findActiveSubscriptionByUserId(1L))
                .thenReturn(Optional.of(testSubscription));

        // When
        Optional<UserSubscription> result = subscriptionService.getActiveSubscription(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testSubscription, result.get());
        verify(subscriptionRepository).findActiveSubscriptionByUserId(1L);
    }

    @Test
    void testGetUserPlanType() {
        // Given
        when(subscriptionRepository.findActiveSubscriptionByUserId(1L))
                .thenReturn(Optional.of(testSubscription));

        // When
        PlanType result = subscriptionService.getUserPlanType(1L);

        // Then
        assertEquals(PlanType.FREE, result);
    }

    @Test
    void testGetUserPlanType_NoActiveSubscription() {
        // Given
        when(subscriptionRepository.findActiveSubscriptionByUserId(1L))
                .thenReturn(Optional.empty());

        // When
        PlanType result = subscriptionService.getUserPlanType(1L);

        // Then
        assertEquals(PlanType.FREE, result);
    }

    @Test
    void testHasActivePremiumSubscription() {
        // Given
        when(subscriptionRepository.hasActivePremiumSubscription(1L))
                .thenReturn(true);

        // When
        boolean result = subscriptionService.hasActivePremiumSubscription(1L);

        // Then
        assertTrue(result);
        verify(subscriptionRepository).hasActivePremiumSubscription(1L);
    }

    @Test
    void testCreateSubscription() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionConfig.getSubscriptionPlan("PREMIUM")).thenReturn(premiumPlan);
        when(subscriptionRepository.findActiveSubscriptionByUserId(1L))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(UserSubscription.class)))
                .thenReturn(testSubscription);

        // When
        UserSubscription result = subscriptionService.createSubscription(
                1L, PlanType.PREMIUM, BigDecimal.valueOf(9.99), "USD");

        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(subscriptionRepository).save(any(UserSubscription.class));
        verify(eventPublisher).publishEvent(any(SubscriptionUpgradedEvent.class));
    }

    @Test
    void testCreateSubscription_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                subscriptionService.createSubscription(1L, PlanType.PREMIUM, BigDecimal.valueOf(9.99), "USD"));
    }

    @Test
    void testCreateFreeSubscription() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionConfig.getSubscriptionPlan("FREE")).thenReturn(new SubscriptionPlan());
        when(subscriptionRepository.findActiveSubscriptionByUserId(1L))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(UserSubscription.class)))
                .thenReturn(testSubscription);

        // When
        UserSubscription result = subscriptionService.createFreeSubscription(1L);

        // Then
        assertNotNull(result);
        verify(subscriptionRepository).save(any(UserSubscription.class));
    }

    @Test
    void testUpgradeToPremium() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionConfig.getSubscriptionPlan("PREMIUM")).thenReturn(premiumPlan);
        when(subscriptionRepository.findActiveSubscriptionByUserId(1L))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(UserSubscription.class)))
                .thenReturn(testSubscription);

        // When
        UserSubscription result = subscriptionService.upgradeToPremium(
                1L, BigDecimal.valueOf(9.99), "USD", "txn123", "stripe");

        // Then
        assertNotNull(result);
        verify(subscriptionRepository, times(2)).save(any(UserSubscription.class));
    }

    @Test
    void testCancelSubscription() {
        // Given
        testSubscription.setStatus(SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(UserSubscription.class))).thenReturn(testSubscription);

        // When
        UserSubscription result = subscriptionService.cancelSubscription(1L, "User request");

        // Then
        assertEquals(SubscriptionStatus.CANCELLED, result.getStatus());
        assertNotNull(result.getCancelledAt());
        assertEquals("User request", result.getCancellationReason());
        assertFalse(result.isAutoRenewal());
        
        verify(subscriptionRepository).save(testSubscription);
        verify(eventPublisher).publishEvent(any(SubscriptionCancelledEvent.class));
    }

    @Test
    void testCancelSubscription_NotFound() {
        // Given
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                subscriptionService.cancelSubscription(1L, "User request"));
    }

    @Test
    void testRenewSubscription() {
        // Given
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionConfig.getSubscriptionPlan("FREE")).thenReturn(new SubscriptionPlan());
        when(subscriptionRepository.save(any(UserSubscription.class))).thenReturn(testSubscription);

        // When
        UserSubscription result = subscriptionService.renewSubscription(
                1L, BigDecimal.valueOf(9.99), "txn456");

        // Then
        assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getExpiresAt());
        assertEquals("txn456", result.getPaymentTransactionId());
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    void testActivateFromTrial() {
        // Given
        testSubscription.setStatus(SubscriptionStatus.TRIAL);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionConfig.getSubscriptionPlan("FREE")).thenReturn(new SubscriptionPlan());
        when(subscriptionRepository.save(any(UserSubscription.class))).thenReturn(testSubscription);

        // When
        UserSubscription result = subscriptionService.activateFromTrial(
                1L, BigDecimal.valueOf(9.99), "txn789", "stripe");

        // Then
        assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getExpiresAt());
        assertEquals("txn789", result.getPaymentTransactionId());
        assertEquals("stripe", result.getPaymentProvider());
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    void testActivateFromTrial_NotInTrial() {
        // Given
        testSubscription.setStatus(SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                subscriptionService.activateFromTrial(1L, BigDecimal.valueOf(9.99), "txn789", "stripe"));
    }

    @Test
    void testGetUserSubscriptionHistory() {
        // Given
        List<UserSubscription> history = List.of(testSubscription);
        when(subscriptionRepository.findAllByUserId(1L)).thenReturn(history);

        // When
        List<UserSubscription> result = subscriptionService.getUserSubscriptionHistory(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals(testSubscription, result.get(0));
        verify(subscriptionRepository).findAllByUserId(1L);
    }

    @Test
    void testGetExpiringSubscriptions() {
        // Given
        List<UserSubscription> expiring = List.of(testSubscription);
        when(subscriptionRepository.findExpiringSubscriptions(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expiring);

        // When
        List<UserSubscription> result = subscriptionService.getExpiringSubscriptions(7);

        // Then
        assertEquals(1, result.size());
        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(subscriptionRepository).findExpiringSubscriptions(startCaptor.capture(), endCaptor.capture());
        
        LocalDateTime start = startCaptor.getValue();
        LocalDateTime end = endCaptor.getValue();
        assertTrue(end.isAfter(start));
    }

    @Test
    void testGetExpiredSubscriptions() {
        // Given
        List<UserSubscription> expired = List.of(testSubscription);
        when(subscriptionRepository.findExpiredSubscriptions(any(LocalDateTime.class)))
                .thenReturn(expired);

        // When
        List<UserSubscription> result = subscriptionService.getExpiredSubscriptions();

        // Then
        assertEquals(1, result.size());
        verify(subscriptionRepository).findExpiredSubscriptions(any(LocalDateTime.class));
    }

    @Test
    void testGetSubscriptionsForRenewal() {
        // Given
        List<UserSubscription> renewals = List.of(testSubscription);
        when(subscriptionRepository.findSubscriptionsForRenewal(any(LocalDateTime.class)))
                .thenReturn(renewals);

        // When
        List<UserSubscription> result = subscriptionService.getSubscriptionsForRenewal();

        // Then
        assertEquals(1, result.size());
        verify(subscriptionRepository).findSubscriptionsForRenewal(any(LocalDateTime.class));
    }

    @Test
    void testGetPlanConfiguration() {
        // Given
        when(subscriptionConfig.getSubscriptionPlan("PREMIUM")).thenReturn(premiumPlan);

        // When
        SubscriptionPlan result = subscriptionService.getPlanConfiguration(PlanType.PREMIUM);

        // Then
        assertEquals(premiumPlan, result);
        verify(subscriptionConfig).getSubscriptionPlan("PREMIUM");
    }
} 