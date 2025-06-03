package alg.coyote001.event;

/**
 * Event triggered when a user cancels their subscription
 */
public class SubscriptionCancelledEvent extends SubscriptionEvent {
    
    public SubscriptionCancelledEvent(Object source, Long userId, String planId) {
        super(source, userId, planId);
    }
} 