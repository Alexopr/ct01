package com.ct01.notification.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Событие отправки уведомления
 */
public class NotificationSentEvent implements DomainEvent {
    
    private final NotificationId notificationId;
    private final UserId recipientId;
    private final NotificationChannel channel;
    private final LocalDateTime sentAt;
    private final LocalDateTime occurredAt;
    
    public NotificationSentEvent(NotificationId notificationId, UserId recipientId, NotificationChannel channel, LocalDateTime sentAt) {
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.channel = channel;
        this.sentAt = sentAt;
        this.occurredAt = LocalDateTime.now();
    }
    
    public NotificationId getNotificationId() { return notificationId; }
    public UserId getRecipientId() { return recipientId; }
    public NotificationChannel getChannel() { return channel; }
    public LocalDateTime getSentAt() { return sentAt; }
    
    @Override
    public LocalDateTime getOccurredAt() { return occurredAt; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotificationSentEvent that = (NotificationSentEvent) obj;
        return Objects.equals(notificationId, that.notificationId) && Objects.equals(occurredAt, that.occurredAt);
    }
    
    @Override
    public int hashCode() { return Objects.hash(notificationId, occurredAt); }
    
    @Override
    public String toString() {
        return String.format("NotificationSentEvent{notificationId=%s, channel=%s, sentAt=%s}", notificationId, channel, sentAt);
    }
} 
