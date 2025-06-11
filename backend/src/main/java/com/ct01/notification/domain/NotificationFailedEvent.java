package com.ct01.notification.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public class NotificationFailedEvent implements DomainEvent {
    
    private final NotificationId notificationId;
    private final UserId recipientId;
    private final String reason;
    private final LocalDateTime occurredAt;
    
    public NotificationFailedEvent(NotificationId notificationId, UserId recipientId, String reason) {
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.reason = reason;
        this.occurredAt = LocalDateTime.now();
    }
    
    public NotificationId getNotificationId() { return notificationId; }
    public UserId getRecipientId() { return recipientId; }
    public String getReason() { return reason; }
    
    @Override
    public LocalDateTime getOccurredAt() { return occurredAt; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotificationFailedEvent that = (NotificationFailedEvent) obj;
        return Objects.equals(notificationId, that.notificationId) && Objects.equals(occurredAt, that.occurredAt);
    }
    
    @Override
    public int hashCode() { return Objects.hash(notificationId, occurredAt); }
    
    @Override
    public String toString() {
        return String.format("NotificationFailedEvent{notificationId=%s, reason='%s'}", notificationId, reason);
    }
} 
