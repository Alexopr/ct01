package alg.coyote001.event;

/**
 * Event triggered when a user upgrades their subscription
 */
public class SubscriptionUpgradedEvent extends SubscriptionEvent {
    
    private final String previousPlanId;
    
    public SubscriptionUpgradedEvent(Object source, Long userId, String newPlanId, String previousPlanId) {
        super(source, userId, newPlanId);
        this.previousPlanId = previousPlanId;
    }
    
    public String getPreviousPlanId() {
        return previousPlanId;
    }
} 