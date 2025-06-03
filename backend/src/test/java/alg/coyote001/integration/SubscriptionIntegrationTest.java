package alg.coyote001.integration;

import alg.coyote001.entity.Role;
import alg.coyote001.entity.User;
import alg.coyote001.entity.UserSubscription;
import alg.coyote001.entity.UserSubscription.PlanType;
import alg.coyote001.entity.UserSubscription.SubscriptionStatus;
import alg.coyote001.repository.RoleRepository;
import alg.coyote001.repository.UserRepository;
import alg.coyote001.repository.UserSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.redis.host=localhost"
})
@Transactional
class SubscriptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSubscriptionRepository subscriptionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Role userRole;
    private Role freeRole;
    private Role premiumRole;

    @BeforeEach
    void setUp() {
        // Create test roles
        userRole = Role.builder()
                .name("USER")
                .description("Base user role")
                .priority(1)
                .build();
        roleRepository.save(userRole);

        freeRole = Role.builder()
                .name("SUBSCRIPTION_FREE")
                .description("Free subscription plan")
                .priority(1)
                .build();
        roleRepository.save(freeRole);

        premiumRole = Role.builder()
                .name("SUBSCRIPTION_PREMIUM")
                .description("Premium subscription plan")
                .priority(5)
                .build();
        roleRepository.save(premiumRole);

        // Create test user
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$test")
                .telegramId(12345L)
                .telegramUsername("testuser")
                .build();
        userRepository.save(testUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetSubscriptionPlans() throws Exception {
        mockMvc.perform(get("/api/subscription/plans"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpected(jsonPath("$[0].name", equalTo("FREE")))
                .andExpected(jsonPath("$[1].name", equalTo("PREMIUM")))
                .andExpected(jsonPath("$[0].features", not(empty())))
                .andExpected(jsonPath("$[1].price", notNullValue()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetSubscriptionStatus_NoSubscription() throws Exception {
        mockMvc.perform(get("/api/subscription/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpected(jsonPath("$.planType", equalTo("FREE")))
                .andExpected(jsonPath("$.status", equalTo("ACTIVE")))
                .andExpected(jsonPath("$.isActive", equalTo(true)));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetSubscriptionStatus_WithActiveSubscription() throws Exception {
        // Create active subscription
        UserSubscription subscription = UserSubscription.builder()
                .user(testUser)
                .planType(PlanType.PREMIUM)
                .status(SubscriptionStatus.ACTIVE)
                .startsAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .autoRenewal(true)
                .price(BigDecimal.valueOf(9.99))
                .currency("USD")
                .build();
        subscriptionRepository.save(subscription);

        mockMvc.perform(get("/api/subscription/status"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.planType", equalTo("PREMIUM")))
                .andExpected(jsonPath("$.status", equalTo("ACTIVE")))
                .andExpected(jsonPath("$.isActive", equalTo(true)))
                .andExpected(jsonPath("$.price", equalTo(9.99)))
                .andExpected(jsonPath("$.currency", equalTo("USD")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpgradeSubscription() throws Exception {
        Map<String, Object> upgradeRequest = Map.of(
                "planType", "PREMIUM",
                "billingCycle", "monthly",
                "price", 9.99,
                "currency", "USD",
                "paymentTransactionId", "tx_12345",
                "paymentProvider", "stripe"
        );

        mockMvc.perform(post("/api/subscription/upgrade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upgradeRequest)))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.planType", equalTo("PREMIUM")))
                .andExpected(jsonPath("$.status", equalTo("ACTIVE")))
                .andExpected(jsonPath("$.paymentTransactionId", equalTo("tx_12345")));

        // Verify subscription was created in database
        Optional<UserSubscription> subscription = subscriptionRepository.findActiveSubscriptionByUserId(testUser.getId());
        assertTrue(subscription.isPresent());
        assertEquals(PlanType.PREMIUM, subscription.get().getPlanType());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.get().getStatus());

        // Verify user roles were updated
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertTrue(updatedUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("SUBSCRIPTION_PREMIUM")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCancelSubscription() throws Exception {
        // Create active subscription first
        UserSubscription subscription = UserSubscription.builder()
                .user(testUser)
                .planType(PlanType.PREMIUM)
                .status(SubscriptionStatus.ACTIVE)
                .startsAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .autoRenewal(true)
                .build();
        UserSubscription savedSubscription = subscriptionRepository.save(subscription);

        Map<String, String> cancelRequest = Map.of(
                "reason", "User request"
        );

        mockMvc.perform(post("/api/subscription/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.status", equalTo("CANCELLED")))
                .andExpected(jsonPath("$.cancellationReason", equalTo("User request")))
                .andExpected(jsonPath("$.autoRenewal", equalTo(false)));

        // Verify subscription was cancelled in database
        UserSubscription cancelledSubscription = subscriptionRepository.findById(savedSubscription.getId()).orElseThrow();
        assertEquals(SubscriptionStatus.CANCELLED, cancelledSubscription.getStatus());
        assertNotNull(cancelledSubscription.getCancelledAt());
        assertFalse(cancelledSubscription.isAutoRenewal());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUsageLimits() throws Exception {
        mockMvc.perform(get("/api/subscription/limits"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpected(jsonPath("$", hasSize(greaterThan(0))))
                .andExpected(jsonPath("$[*].resourceType", hasItem("TWITTER_TRACK")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCheckResourceAccess_FreeUser() throws Exception {
        mockMvc.perform(get("/api/subscription/check/twitter_tracking"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.hasAccess", equalTo(false)))
                .andExpected(jsonPath("$.message", containsString("Premium")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCheckResourceAccess_PremiumUser() throws Exception {
        // Create premium subscription
        UserSubscription subscription = UserSubscription.builder()
                .user(testUser)
                .planType(PlanType.PREMIUM)
                .status(SubscriptionStatus.ACTIVE)
                .startsAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        subscriptionRepository.save(subscription);

        mockMvc.perform(get("/api/subscription/check/twitter_tracking"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.hasAccess", equalTo(true)));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testSubscriptionLifecycle() throws Exception {
        // 1. Start with no subscription (FREE plan)
        mockMvc.perform(get("/api/subscription/status"))
                .andExpected(jsonPath("$.planType", equalTo("FREE")));

        // 2. Upgrade to PREMIUM
        Map<String, Object> upgradeRequest = Map.of(
                "planType", "PREMIUM",
                "billingCycle", "monthly",
                "price", 9.99,
                "currency", "USD",
                "paymentTransactionId", "tx_upgrade",
                "paymentProvider", "stripe"
        );

        mockMvc.perform(post("/api/subscription/upgrade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upgradeRequest)))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.planType", equalTo("PREMIUM")));

        // 3. Verify premium features are accessible
        mockMvc.perform(get("/api/subscription/check/twitter_tracking"))
                .andExpected(jsonPath("$.hasAccess", equalTo(true)));

        // 4. Cancel subscription
        Map<String, String> cancelRequest = Map.of("reason", "Testing");
        mockMvc.perform(post("/api/subscription/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.status", equalTo("CANCELLED")));

        // 5. Verify subscription is cancelled but still active until expiry
        mockMvc.perform(get("/api/subscription/status"))
                .andExpected(jsonPath("$.status", equalTo("CANCELLED")))
                .andExpected(jsonPath("$.isActive", equalTo(true))); // Still active until expiry
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/subscription/status"))
                .andExpect(status().isOk()); // Should work with authenticated user
    }

    @Test
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/subscription/status"))
                .andExpected(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testInvalidUpgradeRequest() throws Exception {
        Map<String, Object> invalidRequest = Map.of(
                "planType", "INVALID_PLAN"
        );

        mockMvc.perform(post("/api/subscription/upgrade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpected(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCancelNonExistentSubscription() throws Exception {
        Map<String, String> cancelRequest = Map.of("reason", "Testing");

        mockMvc.perform(post("/api/subscription/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpected(status().isNotFound())
                .andExpected(jsonPath("$.message", containsString("No active subscription")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testConcurrentSubscriptionOperations() throws Exception {
        // This test would simulate concurrent subscription operations
        // In a real scenario, you'd use multiple threads
        
        Map<String, Object> upgradeRequest = Map.of(
                "planType", "PREMIUM",
                "billingCycle", "monthly",
                "price", 9.99,
                "currency", "USD",
                "paymentTransactionId", "tx_concurrent_1",
                "paymentProvider", "stripe"
        );

        // First upgrade should succeed
        mockMvc.perform(post("/api/subscription/upgrade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upgradeRequest)))
                .andExpect(status().isOk());

        // Second upgrade should handle existing subscription appropriately
        Map<String, Object> secondUpgradeRequest = Map.of(
                "planType", "PREMIUM",
                "billingCycle", "yearly",
                "price", 99.99,
                "currency", "USD",
                "paymentTransactionId", "tx_concurrent_2",
                "paymentProvider", "stripe"
        );

        mockMvc.perform(post("/api/subscription/upgrade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUpgradeRequest)))
                .andExpected(status().isConflict());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testSubscriptionEventHandling() throws Exception {
        // Create subscription
        Map<String, Object> upgradeRequest = Map.of(
                "planType", "PREMIUM",
                "billingCycle", "monthly",
                "price", 9.99,
                "currency", "USD",
                "paymentTransactionId", "tx_event_test",
                "paymentProvider", "stripe"
        );

        mockMvc.perform(post("/api/subscription/upgrade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upgradeRequest)))
                .andExpect(status().isOk());

        // Verify that user roles were updated due to event handling
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        boolean hasPremiumRole = updatedUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("SUBSCRIPTION_PREMIUM"));
        assertTrue(hasPremiumRole, "User should have premium role after subscription upgrade");

        boolean hasUserRole = updatedUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("USER"));
        assertTrue(hasUserRole, "User should retain base USER role");
    }
} 