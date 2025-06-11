package com.ct01.subscription.domain;

import com.ct01.core.domain.ValueObject;

import java.util.UUID;

/**
 * Value Object для идентификатора подписки
 */
public record SubscriptionId(String value) implements ValueObject {
    
    public SubscriptionId {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("SubscriptionId не может быть пустым");
        }
    }
    
    public static SubscriptionId generate() {
        return new SubscriptionId(UUID.randomUUID().toString());
    }
    
    public static SubscriptionId of(String value) {
        return new SubscriptionId(value);
    }
    
    public static SubscriptionId of(Long id) {
        return new SubscriptionId(id.toString());
    }
} 
