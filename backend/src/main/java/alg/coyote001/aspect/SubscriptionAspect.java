package alg.coyote001.aspect;

import alg.coyote001.annotation.RequireSubscription;
import alg.coyote001.service.SubscriptionRoleIntegrationService;
import alg.coyote001.service.SubscriptionService;
import alg.coyote001.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Aspect for subscription-based access control
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionAspect {
    
    private final SubscriptionService subscriptionService;
    private final SubscriptionRoleIntegrationService roleIntegrationService;
    private final UserService userService;
    
    @Around("@annotation(requireSubscription)")
    public Object checkSubscription(ProceedingJoinPoint joinPoint, RequireSubscription requireSubscription) throws Throwable {
        
        // Get current user ID from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        
        String username = authentication.getName();
        Long userId = getCurrentUserId(username);
        
        if (userId == null) {
            throw new SecurityException("User not found");
        }
        
        // Check subscription plan
        String requiredPlan = requireSubscription.plan();
        String resourceType = requireSubscription.resourceType();
        
        boolean hasAccess = false;
        
        if (!resourceType.isEmpty()) {
            // Check specific resource access
            hasAccess = roleIntegrationService.hasSubscriptionAccess(userId, resourceType);
        } else {
            // Check plan level access
            var userPlan = subscriptionService.getUserPlanType(userId);
            hasAccess = checkPlanAccess(userPlan.name(), requiredPlan);
        }
        
        if (!hasAccess) {
            log.warn("User {} denied access to method {} - insufficient subscription", 
                    userId, joinPoint.getSignature().getName());
            throw new SecurityException(requireSubscription.message());
        }
        
        log.debug("User {} granted access to method {} with subscription check", 
                userId, joinPoint.getSignature().getName());
        
        return joinPoint.proceed();
    }
    
    private boolean checkPlanAccess(String userPlan, String requiredPlan) {
        if ("FREE".equals(requiredPlan)) {
            return true; // Everyone has access to FREE features
        }
        
        if ("PREMIUM".equals(requiredPlan)) {
            return "PREMIUM".equals(userPlan);
        }
        
        return false;
    }
    
    private Long getCurrentUserId(String username) {
        try {
            return userService.findByUsername(username).getId();
        } catch (Exception e) {
            log.error("Failed to find user by username: {}", username, e);
            return null;
        }
    }
} 