package alg.coyote001.service;

import alg.coyote001.entity.Role;
import alg.coyote001.entity.User;
import alg.coyote001.event.SubscriptionCancelledEvent;
import alg.coyote001.event.SubscriptionExpiredEvent;
import alg.coyote001.event.SubscriptionUpgradedEvent;
import alg.coyote001.repository.RoleRepository;
import alg.coyote001.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Service for integrating subscription system with role-based access control
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionRoleIntegrationService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    // Standard role names for subscription plans
    private static final String FREE_ROLE = "SUBSCRIPTION_FREE";
    private static final String PREMIUM_ROLE = "SUBSCRIPTION_PREMIUM";
    private static final String USER_ROLE = "USER"; // Base role for all users
    
    /**
     * Handle subscription upgrade events
     */
    @EventListener
    public void handleSubscriptionUpgraded(SubscriptionUpgradedEvent event) {
        log.info("Processing subscription upgrade for user {} to plan {}", 
                 event.getUserId(), event.getPlanId());
        
        try {
            updateUserRolesForPlan(event.getUserId(), event.getPlanId());
            log.info("Successfully updated roles for user {} subscription upgrade", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to update roles for user {} subscription upgrade: {}", 
                     event.getUserId(), e.getMessage(), e);
        }
    }
    
    /**
     * Handle subscription cancellation events
     */
    @EventListener
    public void handleSubscriptionCancelled(SubscriptionCancelledEvent event) {
        log.info("Processing subscription cancellation for user {}", event.getUserId());
        
        try {
            updateUserRolesForPlan(event.getUserId(), "FREE");
            log.info("Successfully updated roles for user {} subscription cancellation", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to update roles for user {} subscription cancellation: {}", 
                     event.getUserId(), e.getMessage(), e);
        }
    }
    
    /**
     * Handle subscription expiration events
     */
    @EventListener
    public void handleSubscriptionExpired(SubscriptionExpiredEvent event) {
        log.info("Processing subscription expiration for user {}", event.getUserId());
        
        try {
            updateUserRolesForPlan(event.getUserId(), "FREE");
            log.info("Successfully updated roles for user {} subscription expiration", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to update roles for user {} subscription expiration: {}", 
                     event.getUserId(), e.getMessage(), e);
        }
    }
    
    /**
     * Update user roles based on subscription plan
     */
    private void updateUserRolesForPlan(Long userId, String planId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        Set<Role> newRoles = new HashSet<>();
        
        // Always add base USER role
        Optional<Role> userRole = roleRepository.findByName(USER_ROLE);
        userRole.ifPresent(newRoles::add);
        
        // Add subscription-specific role
        String subscriptionRoleName = getSubscriptionRoleName(planId);
        Optional<Role> subscriptionRole = roleRepository.findByName(subscriptionRoleName);
        
        if (subscriptionRole.isPresent()) {
            newRoles.add(subscriptionRole.get());
        } else {
            log.warn("Subscription role not found: {}", subscriptionRoleName);
            // Create the role if it doesn't exist
            createSubscriptionRole(subscriptionRoleName, planId);
            subscriptionRole = roleRepository.findByName(subscriptionRoleName);
            subscriptionRole.ifPresent(newRoles::add);
        }
        
        // Remove old subscription roles but keep other roles
        Set<Role> currentRoles = user.getRoles();
        if (currentRoles != null) {
            currentRoles.removeIf(role -> 
                role.getName().equals(FREE_ROLE) || role.getName().equals(PREMIUM_ROLE));
            newRoles.addAll(currentRoles);
        }
        
        user.setRoles(newRoles);
        userRepository.save(user);
        
        log.info("Updated user {} roles to include: {}", userId, 
                newRoles.stream().map(Role::getName).toList());
    }
    
    /**
     * Get subscription role name based on plan ID
     */
    private String getSubscriptionRoleName(String planId) {
        return switch (planId.toUpperCase()) {
            case "FREE" -> FREE_ROLE;
            case "PREMIUM" -> PREMIUM_ROLE;
            default -> FREE_ROLE; // Default to free
        };
    }
    
    /**
     * Create subscription role if it doesn't exist
     */
    private void createSubscriptionRole(String roleName, String planId) {
        String description = switch (roleName) {
            case FREE_ROLE -> "Free subscription plan with basic features";
            case PREMIUM_ROLE -> "Premium subscription plan with advanced features";
            default -> "Subscription role for plan: " + planId;
        };
        
        Role role = Role.builder()
                .name(roleName)
                .description(description)
                .priority(roleName.equals(PREMIUM_ROLE) ? 5 : 1)
                .isActive(true)
                .build();
        
        roleRepository.save(role);
        log.info("Created subscription role: {}", roleName);
    }
    
    /**
     * Initialize subscription roles if they don't exist
     */
    @Transactional
    public void initializeSubscriptionRoles() {
        log.info("Initializing subscription roles...");
        
        // Create base roles if they don't exist
        if (roleRepository.findByName(USER_ROLE).isEmpty()) {
            createSubscriptionRole(USER_ROLE, "BASE");
        }
        
        if (roleRepository.findByName(FREE_ROLE).isEmpty()) {
            createSubscriptionRole(FREE_ROLE, "FREE");
        }
        
        if (roleRepository.findByName(PREMIUM_ROLE).isEmpty()) {
            createSubscriptionRole(PREMIUM_ROLE, "PREMIUM");
        }
        
        log.info("Subscription roles initialization completed");
    }
    
    /**
     * Check if user has subscription-based access to resource
     */
    public boolean hasSubscriptionAccess(Long userId, String resourceType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Check if user has premium role for advanced features
        if (user.hasRole(PREMIUM_ROLE)) {
            return true;
        }
        
        // Free users have limited access
        if (user.hasRole(FREE_ROLE)) {
            return isResourceAvailableForFreeUsers(resourceType);
        }
        
        return false;
    }
    
    /**
     * Check if resource is available for free users
     */
    private boolean isResourceAvailableForFreeUsers(String resourceType) {
        return switch (resourceType.toLowerCase()) {
            case "basic_tracking" -> true;
            case "price_alerts" -> true;
            case "twitter_tracking" -> false; // Premium only
            case "advanced_analytics" -> false; // Premium only
            case "smart_money_tracking" -> false; // Premium only
            default -> false;
        };
    }
} 