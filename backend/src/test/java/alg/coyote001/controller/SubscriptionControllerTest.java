package alg.coyote001.controller;

import alg.coyote001.dto.SubscriptionPlanDto;
import alg.coyote001.dto.SubscriptionStatusDto;
import alg.coyote001.dto.SubscriptionUpgradeRequest;
import alg.coyote001.dto.UsageLimitDto;
import alg.coyote001.entity.UserSubscription.PlanType;
import alg.coyote001.entity.UserSubscription.SubscriptionStatus;
import alg.coyote001.service.SubscriptionService;
import alg.coyote001.service.SubscriptionLimitService;
import alg.coyote001.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private SubscriptionLimitService limitService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private SubscriptionPlanDto freePlan;
    private SubscriptionPlanDto premiumPlan;
    private SubscriptionStatusDto statusDto;
    private UsageLimitDto usageLimit;

    @BeforeEach
    void setUp() {
        freePlan = SubscriptionPlanDto.builder()
                .name("FREE")
                .displayName("Бесплатный")
                .description("Базовый функционал")
                .price(BigDecimal.ZERO)
                .currency("USD")
                .billingCycle("monthly")
                .trialDays(0)
                .features(List.of("Базовый трекинг", "До 5 уведомлений"))
                .build();

        premiumPlan = SubscriptionPlanDto.builder()
                .name("PREMIUM")
                .displayName("Премиум")
                .description("Расширенный функционал")
                .price(BigDecimal.valueOf(9.99))
                .currency("USD")
                .billingCycle("monthly")
                .trialDays(7)
                .features(List.of("Трекинг Twitter", "Безлимитные уведомления", "Аналитика"))
                .build();

        statusDto = SubscriptionStatusDto.builder()
                .planType(PlanType.FREE)
                .status(SubscriptionStatus.ACTIVE)
                .isActive(true)
                .startsAt(LocalDateTime.now())
                .autoRenewal(false)
                .build();

        usageLimit = UsageLimitDto.builder()
                .resourceType("TWITTER_TRACK")
                .currentUsage(5)
                .limit(10)
                .resetPeriod("monthly")
                .nextResetAt(LocalDateTime.now().plusDays(30))
                .isUnlimited(false)
                .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetSubscriptionPlans() throws Exception {
        // Given
        when(subscriptionService.getAvailablePlans()).thenReturn(List.of(freePlan, premiumPlan));

        // When & Then
        mockMvc.perform(get("/api/subscription/plans"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", equalTo("FREE")))
                .andExpect(jsonPath("$[0].price", equalTo(0)))
                .andExpect(jsonPath("$[1].name", equalTo("PREMIUM")))
                .andExpect(jsonPath("$[1].price", equalTo(9.99)))
                .andExpect(jsonPath("$[1].features", hasSize(3)));

        verify(subscriptionService).getAvailablePlans();
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetSubscriptionStatus() throws Exception {
        // Given
        when(userService.findByUsername("testuser")).thenReturn(createMockUser(1L));
        when(subscriptionService.getSubscriptionStatus(1L)).thenReturn(statusDto);

        // When & Then
        mockMvc.perform(get("/api/subscription/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.planType", equalTo("FREE")))
                .andExpect(jsonPath("$.status", equalTo("ACTIVE")))
                .andExpect(jsonPath("$.isActive", equalTo(true)))
                .andExpect(jsonPath("$.autoRenewal", equalTo(false)));

        verify(subscriptionService).getSubscriptionStatus(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpgradeSubscription() throws Exception {
        // Given
        SubscriptionUpgradeRequest request = SubscriptionUpgradeRequest.builder()
                .planType(PlanType.PREMIUM)
                .billingCycle("monthly")
                .price(BigDecimal.valueOf(9.99))
                .currency("USD")
                .paymentTransactionId("tx_12345")
                .paymentProvider("stripe")
                .build();

        SubscriptionStatusDto upgradedStatus = SubscriptionStatusDto.builder()
                .planType(PlanType.PREMIUM)
                .status(SubscriptionStatus.ACTIVE)
                .isActive(true)
                .price(BigDecimal.valueOf(9.99))
                .currency("USD")
                .paymentTransactionId("tx_12345")
                .build();

        when(userService.findByUsername("testuser")).thenReturn(createMockUser(1L));
        when(subscriptionService.upgradeSubscription(eq(1L), any(SubscriptionUpgradeRequest.class)))
                .thenReturn(upgradedStatus);

        // When & Then
        mockMvc.perform(post("/api/subscription/upgrade")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.planType", equalTo("PREMIUM")))
                .andExpect(jsonPath("$.status", equalTo("ACTIVE")))
                .andExpect(jsonPath("$.price", equalTo(9.99)))
                .andExpect(jsonPath("$.paymentTransactionId", equalTo("tx_12345")));

        verify(subscriptionService).upgradeSubscription(eq(1L), any(SubscriptionUpgradeRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpgradeSubscription_InvalidRequest() throws Exception {
        // Given - invalid request with missing required fields
        String invalidRequest = "{\"planType\": \"INVALID\"}";

        // When & Then
        mockMvc.perform(post("/api/subscription/upgrade")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(subscriptionService, never()).upgradeSubscription(anyLong(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCancelSubscription() throws Exception {
        // Given
        String cancelRequest = "{\"reason\": \"User request\"}";
        SubscriptionStatusDto cancelledStatus = SubscriptionStatusDto.builder()
                .planType(PlanType.PREMIUM)
                .status(SubscriptionStatus.CANCELLED)
                .isActive(true) // Still active until expiry
                .cancellationReason("User request")
                .autoRenewal(false)
                .build();

        when(userService.findByUsername("testuser")).thenReturn(createMockUser(1L));
        when(subscriptionService.cancelSubscription(1L, "User request")).thenReturn(cancelledStatus);

        // When & Then
        mockMvc.perform(post("/api/subscription/cancel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cancelRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("CANCELLED")))
                .andExpect(jsonPath("$.cancellationReason", equalTo("User request")))
                .andExpect(jsonPath("$.autoRenewal", equalTo(false)));

        verify(subscriptionService).cancelSubscription(1L, "User request");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCancelSubscription_NoActiveSubscription() throws Exception {
        // Given
        String cancelRequest = "{\"reason\": \"User request\"}";
        when(userService.findByUsername("testuser")).thenReturn(createMockUser(1L));
        when(subscriptionService.cancelSubscription(1L, "User request"))
                .thenThrow(new RuntimeException("No active subscription found"));

        // When & Then
        mockMvc.perform(post("/api/subscription/cancel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cancelRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("No active subscription")));

        verify(subscriptionService).cancelSubscription(1L, "User request");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUsageLimits() throws Exception {
        // Given
        when(userService.findByUsername("testuser")).thenReturn(createMockUser(1L));
        when(limitService.getUserLimits(1L)).thenReturn(List.of(usageLimit));

        // When & Then
        mockMvc.perform(get("/api/subscription/limits"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].resourceType", equalTo("TWITTER_TRACK")))
                .andExpect(jsonPath("$[0].currentUsage", equalTo(5)))
                .andExpect(jsonPath("$[0].limit", equalTo(10)))
                .andExpect(jsonPath("$[0].resetPeriod", equalTo("monthly")))
                .andExpect(jsonPath("$[0].isUnlimited", equalTo(false)));

        verify(limitService).getUserLimits(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCheckResourceAccess_HasAccess() throws Exception {
        // Given
        when(userService.findByUsername("testuser")).thenReturn(createMockUser(1L));
        when(limitService.checkLimit(1L, "twitter_tracking")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/subscription/check/twitter_tracking"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hasAccess", equalTo(true)))
                .andExpect(jsonPath("$.resource", equalTo("twitter_tracking")));

        verify(limitService).checkLimit(1L, "twitter_tracking");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCheckResourceAccess_NoAccess() throws Exception {
        // Given
        when(userService.findByUsername("testuser")).thenReturn(createMockUser(1L));
        when(limitService.checkLimit(1L, "twitter_tracking")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/subscription/check/twitter_tracking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasAccess", equalTo(false)))
                .andExpect(jsonPath("$.resource", equalTo("twitter_tracking")))
                .andExpect(jsonPath("$.message", containsString("upgrade")));

        verify(limitService).checkLimit(1L, "twitter_tracking");
    }

    @Test
    void testUnauthenticatedAccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/subscription/status"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/subscription/plans"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/subscription/upgrade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        verify(subscriptionService, never()).getSubscriptionStatus(anyLong());
        verify(subscriptionService, never()).getAvailablePlans();
        verify(subscriptionService, never()).upgradeSubscription(anyLong(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetSubscriptionPlans_ServiceException() throws Exception {
        // Given
        when(subscriptionService.getAvailablePlans())
                .thenThrow(new RuntimeException("Configuration error"));

        // When & Then
        mockMvc.perform(get("/api/subscription/plans"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("Configuration error")));

        verify(subscriptionService).getAvailablePlans();
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpgradeSubscription_AlreadyExists() throws Exception {
        // Given
        SubscriptionUpgradeRequest request = SubscriptionUpgradeRequest.builder()
                .planType(PlanType.PREMIUM)
                .billingCycle("monthly")
                .price(BigDecimal.valueOf(9.99))
                .currency("USD")
                .paymentTransactionId("tx_12345")
                .paymentProvider("stripe")
                .build();

        when(userService.findByUsername("testuser")).thenReturn(createMockUser(1L));
        when(subscriptionService.upgradeSubscription(eq(1L), any(SubscriptionUpgradeRequest.class)))
                .thenThrow(new IllegalStateException("Active subscription already exists"));

        // When & Then
        mockMvc.perform(post("/api/subscription/upgrade")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Active subscription already exists")));

        verify(subscriptionService).upgradeSubscription(eq(1L), any(SubscriptionUpgradeRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testValidationErrors() throws Exception {
        // Test empty request body
        mockMvc.perform(post("/api/subscription/upgrade")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        // Test invalid JSON
        mockMvc.perform(post("/api/subscription/upgrade")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());

        // Test missing content type
        mockMvc.perform(post("/api/subscription/upgrade")
                        .with(csrf())
                        .content("{}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testRateLimiting() throws Exception {
        // This would test rate limiting if implemented
        // For now, just verify normal operation
        when(userService.findByUsername("testuser")).thenReturn(createMockUser(1L));
        when(subscriptionService.getSubscriptionStatus(1L)).thenReturn(statusDto);

        // Multiple rapid requests should all succeed (no rate limiting implemented yet)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/subscription/status"))
                    .andExpect(status().isOk());
        }

        verify(subscriptionService, times(5)).getSubscriptionStatus(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCsrfProtection() throws Exception {
        // CSRF token required for POST requests
        String request = "{\"planType\": \"PREMIUM\", \"price\": 9.99}";

        // Request without CSRF token should fail
        mockMvc.perform(post("/api/subscription/upgrade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());

        // Request with CSRF token should succeed (if other validation passes)
        when(userService.findByUsername("testuser")).thenReturn(createMockUser(1L));
        
        mockMvc.perform(post("/api/subscription/upgrade")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest()); // Bad request due to missing fields, but CSRF passed
    }

    private alg.coyote001.entity.User createMockUser(Long id) {
        return alg.coyote001.entity.User.builder()
                .id(id)
                .username("testuser")
                .email("test@example.com")
                .build();
    }
} 