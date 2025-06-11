package com.ct01.subscription.application.command;

import com.ct01.subscription.domain.SubscriptionId;

/**
 * Команда для отмены подписки
 */
public record CancelSubscriptionCommand(
    SubscriptionId subscriptionId,
    String reason
) {
    
    public CancelSubscriptionCommand {
        if (subscriptionId == null) {
            throw new IllegalArgumentException("SubscriptionId не может быть null");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason не может быть пустой");
        }
    }
} 
