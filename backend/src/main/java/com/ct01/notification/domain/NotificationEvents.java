package com.ct01.notification.domain;

import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Доменные события для Notification Domain
 */
public class NotificationEvents {
    
    /**
     * Событие отправки уведомления
     */
    public static class NotificationSentEvent implements DomainEvent {
        
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
    
    /**
     * Событие доставки уведомления
     */
    public static class NotificationDeliveredEvent implements DomainEvent {
        
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
    
    /**
     * Событие прочтения уведомления
     */
    public static class NotificationReadEvent implements DomainEvent {
        
        private final NotificationId notificationId;
        private final UserId recipientId;
        private final LocalDateTime readAt;
        private final LocalDateTime occurredAt;
        
        public NotificationReadEvent(NotificationId notificationId, UserId recipientId, LocalDateTime readAt) {
            this.notificationId = notificationId;
            this.recipientId = recipientId;
            this.readAt = readAt;
            this.occurredAt = LocalDateTime.now();
        }
        
        public NotificationId getNotificationId() { return notificationId; }
        public UserId getRecipientId() { return recipientId; }
        public LocalDateTime getReadAt() { return readAt; }
        
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            NotificationReadEvent that = (NotificationReadEvent) obj;
            return Objects.equals(notificationId, that.notificationId) && Objects.equals(occurredAt, that.occurredAt);
        }
        
        @Override
        public int hashCode() { return Objects.hash(notificationId, occurredAt); }
        
        @Override
        public String toString() {
            return String.format("NotificationReadEvent{notificationId=%s, readAt=%s}", notificationId, readAt);
        }
    }
    
    /**
     * Событие неудачной отправки уведомления
     */
    public static class NotificationFailedEvent implements DomainEvent {
        
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
    
    /**
     * Событие повтора отправки уведомления
     */
    public static class NotificationRetryEvent implements DomainEvent {
        
        private final NotificationId notificationId;
        private final UserId recipientId;
        private final int retryCount;
        private final LocalDateTime occurredAt;
        
        public NotificationRetryEvent(NotificationId notificationId, UserId recipientId, int retryCount) {
            this.notificationId = notificationId;
            this.recipientId = recipientId;
            this.retryCount = retryCount;
            this.occurredAt = LocalDateTime.now();
        }
        
        public NotificationId getNotificationId() { return notificationId; }
        public UserId getRecipientId() { return recipientId; }
        public int getRetryCount() { return retryCount; }
        
        @Override
        public LocalDateTime getOccurredAt() { return occurredAt; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            NotificationRetryEvent that = (NotificationRetryEvent) obj;
            return Objects.equals(notificationId, that.notificationId) && Objects.equals(occurredAt, that.occurredAt);
        }
        
        @Override
        public int hashCode() { return Objects.hash(notificationId, occurredAt); }
        
        @Override
        public String toString() {
            return String.format("NotificationRetryEvent{notificationId=%s, retryCount=%d}", notificationId, retryCount);
        }
    }
}
