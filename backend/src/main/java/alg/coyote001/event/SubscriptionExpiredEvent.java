package alg.coyote001.event;

/**
 * Event triggered when a user's subscription expires
 */
public class SubscriptionExpiredEvent extends SubscriptionEvent {
    
    public SubscriptionExpiredEvent(Object source, Long userId, String planId) {
        super(source, userId, planId);
    }
} 