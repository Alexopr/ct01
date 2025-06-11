package com.ct01.websocket.domain.session;

import com.ct01.shared.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

/**
 * WebSocket Session ID value object
 */
public final class SessionId implements ValueObject {
    
    private final String value;
    
    private SessionId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId cannot be null or empty");
        }
        this.value = value;
    }
    
    public static SessionId of(String value) {
        return new SessionId(value);
    }
    
    public static SessionId generate() {
        return new SessionId(UUID.randomUUID().toString());
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionId sessionId = (SessionId) o;
        return Objects.equals(value, sessionId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
} 