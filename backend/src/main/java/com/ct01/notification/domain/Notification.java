package com.ct01.notification.domain;

import com.ct01.core.domain.AggregateRoot;
import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Notification Aggregate Root
 * Основная сущность домена уведомлений с полной бизнес-логикой
 */
public class Notification extends AggregateRoot<NotificationId> {
    
    private final NotificationId id;
    private final NotificationContent content;
    private final NotificationChannel channel;
    private final UserId recipientId;
    private final NotificationPriority priority;
    private final LocalDateTime createdAt;
    
    private NotificationStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private LocalDateTime expiresAt;
    private String failureReason;
    private int retryCount;
    
    // Domain Events
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    // Конструктор для создания нового уведомления
    public Notification(
            NotificationId id,
            NotificationContent content,
            NotificationChannel channel,
            UserId recipientId,
            NotificationPriority priority,
            LocalDateTime expiresAt) {
        super(id);
        
        this.id = validateNotNull(id, "NotificationId не может быть null");
        this.content = validateNotNull(content, "NotificationContent не может быть null");
        this.channel = validateNotNull(channel, "NotificationChannel не может быть null");
        this.recipientId = validateNotNull(recipientId, "UserId не может быть null");
        this.priority = validateNotNull(priority, "NotificationPriority не может быть null");
        this.expiresAt = expiresAt;
        
        this.status = NotificationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.retryCount = 0;
        
        // Добавляем доменное событие
        addDomainEvent(new NotificationCreatedEvent(id, recipientId, channel, priority));
    }
    
    // Конструктор для восстановления из хранилища
    public Notification(
            NotificationId id,
            NotificationContent content,
            NotificationChannel channel,
            UserId recipientId,
            NotificationPriority priority,
            NotificationStatus status,
            LocalDateTime createdAt,
            LocalDateTime sentAt,
            LocalDateTime deliveredAt,
            LocalDateTime readAt,
            LocalDateTime expiresAt,
            String failureReason,
            int retryCount) {
        super(id);
        
        this.id = id;
        this.content = content;
        this.channel = channel;
        this.recipientId = recipientId;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.deliveredAt = deliveredAt;
        this.readAt = readAt;
        this.expiresAt = expiresAt;
        this.failureReason = failureReason;
        this.retryCount = retryCount;
    }
    
    /**
     * Пометить уведомление как отправленное
     */
    public void markAsSent() {
        validateNotExpired();
        
        if (status != NotificationStatus.PENDING) {
            throw new IllegalStateException("Только pending уведомления могут быть отправлены");
        }
        
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
        
        addDomainEvent(new NotificationSentEvent(id, recipientId, channel, sentAt));
    }
    
    /**
     * Пометить уведомление как доставленное
     */
    public void markAsDelivered() {
        if (status != NotificationStatus.SENT) {
            throw new IllegalStateException("Только отправленные уведомления могут быть доставлены");
        }
        
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        
        addDomainEvent(new NotificationDeliveredEvent(id, recipientId, deliveredAt));
    }
    
    /**
     * Пометить уведомление как прочитанное
     */
    public void markAsRead() {
        if (status != NotificationStatus.DELIVERED) {
            throw new IllegalStateException("Только доставленные уведомления могут быть прочитаны");
        }
        
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
        
        addDomainEvent(new NotificationReadEvent(id, recipientId, readAt));
    }
    
    /**
     * Пометить уведомление как неудачное
     */
    public void markAsFailed(String reason) {
        validateNotNull(reason, "Причина неудачи не может быть null");
        
        if (status == NotificationStatus.DELIVERED || status == NotificationStatus.READ) {
            throw new IllegalStateException("Доставленные или прочитанные уведомления не могут быть помечены как неудачные");
        }
        
        this.status = NotificationStatus.FAILED;
        this.failureReason = reason;
        
        addDomainEvent(new NotificationFailedEvent(id, recipientId, reason));
    }
    
    /**
     * Увеличить счетчик повторов
     */
    public void incrementRetryCount() {
        this.retryCount++;
        
        addDomainEvent(new NotificationRetryEvent(id, recipientId, retryCount));
    }
    
    /**
     * Проверить, можно ли повторить отправку
     */
    public boolean canRetry(int maxRetries) {
        return status == NotificationStatus.FAILED && 
               retryCount < maxRetries && 
               !isExpired();
    }
    
    /**
     * Проверить, истекло ли уведомление
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Проверить, высокий ли приоритет
     */
    public boolean isHighPriority() {
        return priority == NotificationPriority.HIGH || priority == NotificationPriority.URGENT;
    }
    
    /**
     * Проверить, требуется ли подтверждение доставки
     */
    public boolean requiresDeliveryConfirmation() {
        return channel.requiresDeliveryConfirmation();
    }
    
    /**
     * Проверить, можно ли отправить уведомление
     */
    public boolean canBeSent() {
        return status == NotificationStatus.PENDING && !isExpired();
    }
    
    /**
     * Получить время ожидания до следующей попытки
     */
    public long getRetryDelayMinutes() {
        // Экспоненциальная задержка: 1, 2, 4, 8, 16 минут
        return (long) Math.pow(2, Math.min(retryCount, 4));
    }
    
    private void validateNotExpired() {
        if (isExpired()) {
            throw new IllegalStateException("Уведомление истекло");
        }
    }
    
    private void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }
    
    private <T> T validateNotNull(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
    
    @Override
    public NotificationId getId() {
        return id;
    }
    
    @Override
    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }
    
    @Override
    public void clearDomainEvents() {
        domainEvents.clear();
    }
    
    // Геттеры
    public NotificationContent getContent() { return content; }
    public NotificationChannel getChannel() { return channel; }
    public UserId getRecipientId() { return recipientId; }
    public NotificationPriority getPriority() { return priority; }
    public NotificationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public String getFailureReason() { return failureReason; }
    public int getRetryCount() { return retryCount; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Notification that = (Notification) obj;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Notification{id=%s, status=%s, channel=%s, priority=%s}", 
                           id, status, channel, priority);
    }
}
