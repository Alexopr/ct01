package com.ct01.notification.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * JPA Entity для уведомлений
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
    @Index(name = "idx_notification_status", columnList = "status"),
    @Index(name = "idx_notification_created", columnList = "created_at"),
    @Index(name = "idx_notification_category", columnList = "category"),
    @Index(name = "idx_notification_channel", columnList = "channel_type"),
    @Index(name = "idx_notification_priority", columnList = "priority"),
    @Index(name = "idx_notification_expires", columnList = "expires_at")
})
public class NotificationJpaEntity {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "title", nullable = false, length = 500)
    private String title;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private NotificationCategoryJpa category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 50)
    private NotificationChannelTypeJpa channelType;
    
    @Column(name = "channel_address", nullable = false, length = 500)
    private String channelAddress;
    
    @ElementCollection
    @CollectionTable(
        name = "notification_channel_config", 
        joinColumns = @JoinColumn(name = "notification_id")
    )
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value")
    private Map<String, String> channelConfiguration;
    
    @Column(name = "recipient_id", nullable = false, length = 36)
    private String recipientId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private NotificationPriorityJpa priority;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatusJpa status;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;
    
    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;
    
    @Column(name = "failure_reason", length = 1000)
    private String failureReason;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @ElementCollection
    @CollectionTable(
        name = "notification_metadata", 
        joinColumns = @JoinColumn(name = "notification_id")
    )
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    private Map<String, String> metadata;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Column(name = "created_by", length = 36)
    private String createdBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_by", length = 36)
    private String updatedBy;
    
    // Конструкторы
    
    public NotificationJpaEntity() {}
    
    public NotificationJpaEntity(String id, String title, String message, NotificationCategoryJpa category,
                                NotificationChannelTypeJpa channelType, String channelAddress, 
                                String recipientId, NotificationPriorityJpa priority, LocalDateTime expiresAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.category = category;
        this.channelType = channelType;
        this.channelAddress = channelAddress;
        this.recipientId = recipientId;
        this.priority = priority;
        this.status = NotificationStatusJpa.PENDING;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }
    
    // Lifecycle callbacks
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public NotificationCategoryJpa getCategory() { return category; }
    public void setCategory(NotificationCategoryJpa category) { this.category = category; }
    
    public NotificationChannelTypeJpa getChannelType() { return channelType; }
    public void setChannelType(NotificationChannelTypeJpa channelType) { this.channelType = channelType; }
    
    public String getChannelAddress() { return channelAddress; }
    public void setChannelAddress(String channelAddress) { this.channelAddress = channelAddress; }
    
    public Map<String, String> getChannelConfiguration() { return channelConfiguration; }
    public void setChannelConfiguration(Map<String, String> channelConfiguration) { this.channelConfiguration = channelConfiguration; }
    
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    
    public NotificationPriorityJpa getPriority() { return priority; }
    public void setPriority(NotificationPriorityJpa priority) { this.priority = priority; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public NotificationStatusJpa getStatus() { return status; }
    public void setStatus(NotificationStatusJpa status) { this.status = status; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    
    public LocalDateTime getLastRetryAt() { return lastRetryAt; }
    public void setLastRetryAt(LocalDateTime lastRetryAt) { this.lastRetryAt = lastRetryAt; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
} 
