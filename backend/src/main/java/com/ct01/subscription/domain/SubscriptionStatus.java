package com.ct01.subscription.domain;

import com.ct01.core.domain.ValueObject;

/**
 * Value Object для статуса подписки
 */
public enum SubscriptionStatus implements ValueObject {
    ACTIVE("active", "Активная"),
    TRIAL("trial", "Пробный период"),
    EXPIRED("expired", "Истекшая"),
    CANCELLED("cancelled", "Отмененная"),
    PENDING_PAYMENT("pending_payment", "Ожидает оплаты"),
    SUSPENDED("suspended", "Приостановлена"),
    GRACE_PERIOD("grace_period", "Льготный период");
    
    private final String code;
    private final String displayName;
    
    SubscriptionStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isActive() {
        return this == ACTIVE || this == TRIAL;
    }
    
    public boolean isInactive() {
        return this == EXPIRED || this == CANCELLED || this == SUSPENDED;
    }
    
    public boolean canBeUpgraded() {
        return this == ACTIVE || this == TRIAL;
    }
    
    public boolean canBeCancelled() {
        return this == ACTIVE || this == TRIAL || this == PENDING_PAYMENT;
    }
    
    public static SubscriptionStatus fromCode(String code) {
        for (SubscriptionStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус подписки: " + code);
    }
} 
