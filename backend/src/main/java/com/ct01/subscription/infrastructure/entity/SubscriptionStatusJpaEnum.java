package com.ct01.subscription.infrastructure.entity;

/**
 * JPA Enum для статуса подписки
 */
public enum SubscriptionStatusJpaEnum {
    ACTIVE,
    TRIAL,
    EXPIRED,
    CANCELLED,
    PENDING_PAYMENT,
    SUSPENDED,
    GRACE_PERIOD
} 
