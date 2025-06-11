package com.ct01.notification.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public class NotificationDeliveredEvent implements DomainEvent {
    
    private final NotificationId notificationId;
    private final UserId recipientId;
    private final LocalDateTime deliveredAt;
    private final LocalDateTime occurredAt;
    
    public NotificationDeliveredEvent(NotificationId notificationId, UserId recipientId, LocalDateTime deliveredAt) {
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.deliveredAt = deliveredAt;
        this.occurredAt = LocalDateTime.now();
    }
    
    public NotificationId getNotificationId() { return notificationId; }
    public UserId getRecipientId() { return recipientId; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    
    @Override
    public LocalDateTime getOccurredAt() { return occurredAt; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotificationDeliveredEvent that = (NotificationDeliveredEvent) obj;
        return Objects.equals(notificationId, that.notificationId) && Objects.equals(occurredAt, that.occurredAt);
    }
    
    @Override
    public int hashCode() { return Objects.hash(notificationId, occurredAt); }
    
    @Override
    public String toString() {
        return String.format("NotificationDeliveredEvent{notificationId=%s, deliveredAt=%s}", notificationId, deliveredAt);
    }
} 
