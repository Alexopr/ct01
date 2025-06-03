package alg.coyote001.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Base class for subscription-related events
 */
@Getter
public abstract class SubscriptionEvent extends ApplicationEvent {
    
    private final Long userId;
    private final String planId;
    private final LocalDateTime timestamp;
    
    public SubscriptionEvent(Object source, Long userId, String planId) {
        super(source);
        this.userId = userId;
        this.planId = planId;
        this.timestamp = LocalDateTime.now();
    }
} 