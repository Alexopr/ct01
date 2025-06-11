package com.ct01.subscription.application.command;

import com.ct01.core.domain.UserId;
import com.ct01.subscription.domain.PlanType;

import java.math.BigDecimal;

/**
 * Команда для создания подписки
 */
public record CreateSubscriptionCommand(
    UserId userId,
    PlanType planType,
    BigDecimal price,
    String currency,
    String billingCycle
) {
    
    public CreateSubscriptionCommand {
        if (userId == null) {
            throw new IllegalArgumentException("UserId не может быть null");
        }
        if (planType == null) {
            throw new IllegalArgumentException("PlanType не может быть null");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price должна быть больше или равна 0");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency не может быть пустой");
        }
        if (billingCycle == null || billingCycle.trim().isEmpty()) {
            throw new IllegalArgumentException("BillingCycle не может быть пустым");
        }
    }
} 
