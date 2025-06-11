package com.ct01.notification.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Событие создания уведомления
 */
public class NotificationCreatedEvent implements DomainEvent {
    
    private final NotificationId notificationId;
    private final UserId recipientId;
    private final NotificationChannel channel;
    private final NotificationPriority priority;
    private final LocalDateTime occurredAt;
    
    public NotificationCreatedEvent(
            NotificationId notificationId, 
            UserId recipientId, 
            NotificationChannel channel, 
            NotificationPriority priority) {
        
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.channel = channel;
        this.priority = priority;
        this.occurredAt = LocalDateTime.now();
    }
    
    public NotificationId getNotificationId() {
        return notificationId;
    }
    
    public UserId getRecipientId() {
        return recipientId;
    }
    
    public NotificationChannel getChannel() {
        return channel;
    }
    
    public NotificationPriority getPriority() {
        return priority;
    }
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotificationCreatedEvent that = (NotificationCreatedEvent) obj;
        return Objects.equals(notificationId, that.notificationId) &&
               Objects.equals(occurredAt, that.occurredAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(notificationId, occurredAt);
    }
    
    @Override
    public String toString() {
        return String.format("NotificationCreatedEvent{notificationId=%s, recipientId=%s, channel=%s, priority=%s}", 
                           notificationId, recipientId, channel, priority);
    }
} 
