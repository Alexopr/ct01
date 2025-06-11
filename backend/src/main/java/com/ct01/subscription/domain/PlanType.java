package com.ct01.subscription.domain;

import com.ct01.core.domain.ValueObject;

/**
 * Value Object для типа плана подписки
 */
public enum PlanType implements ValueObject {
    FREE("free", "Бесплатный план"),
    PREMIUM("premium", "Премиум план");
    
    private final String code;
    private final String displayName;
    
    PlanType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isFree() {
        return this == FREE;
    }
    
    public boolean isPremium() {
        return this == PREMIUM;
    }
    
    public static PlanType fromCode(String code) {
        for (PlanType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Неизвестный тип плана: " + code);
    }
} 
