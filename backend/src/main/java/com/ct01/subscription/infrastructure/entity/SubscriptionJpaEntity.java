package com.ct01.subscription.infrastructure.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity для Subscription
 */
@Entity
@Table(name = "subscriptions",
    indexes = {
        @Index(columnList = "user_id"),
        @Index(columnList = "plan_type"),
        @Index(columnList = "status"),
        @Index(columnList = "expires_at"),
        @Index(columnList = "next_billing_date")
    }
)
public class SubscriptionJpaEntity {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 20)
    private PlanTypeJpaEnum planType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatusJpaEnum status;
    
    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;
    
    @Column(name = "auto_renewal", nullable = false)
    private boolean autoRenewal;
    
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "currency", length = 10)
    private String currency;
    
    @Column(name = "billing_period", length = 20)
    private String billingPeriod;
    
    @Column(name = "payment_transaction_id", length = 255)
    private String paymentTransactionId;
    
    @Column(name = "payment_provider", length = 50)
    private String paymentProvider;
    
    @Column(name = "payment_metadata", columnDefinition = "TEXT")
    private String paymentMetadata;
    
    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Конструкторы
    protected SubscriptionJpaEntity() {}
    
    public SubscriptionJpaEntity(String id, String userId, PlanTypeJpaEnum planType, 
                               SubscriptionStatusJpaEnum status, LocalDateTime startsAt,
                               boolean autoRenewal, BigDecimal price, String currency,
                               String billingPeriod, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.planType = planType;
        this.status = status;
        this.startsAt = startsAt;
        this.autoRenewal = autoRenewal;
        this.price = price;
        this.currency = currency;
        this.billingPeriod = billingPeriod;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }
    
    // Getters и Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public PlanTypeJpaEnum getPlanType() { return planType; }
    public void setPlanType(PlanTypeJpaEnum planType) { this.planType = planType; }
    
    public SubscriptionStatusJpaEnum getStatus() { return status; }
    public void setStatus(SubscriptionStatusJpaEnum status) { this.status = status; }
    
    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getTrialEndsAt() { return trialEndsAt; }
    public void setTrialEndsAt(LocalDateTime trialEndsAt) { this.trialEndsAt = trialEndsAt; }
    
    public boolean isAutoRenewal() { return autoRenewal; }
    public void setAutoRenewal(boolean autoRenewal) { this.autoRenewal = autoRenewal; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getBillingPeriod() { return billingPeriod; }
    public void setBillingPeriod(String billingPeriod) { this.billingPeriod = billingPeriod; }
    
    public String getPaymentTransactionId() { return paymentTransactionId; }
    public void setPaymentTransactionId(String paymentTransactionId) { this.paymentTransactionId = paymentTransactionId; }
    
    public String getPaymentProvider() { return paymentProvider; }
    public void setPaymentProvider(String paymentProvider) { this.paymentProvider = paymentProvider; }
    
    public String getPaymentMetadata() { return paymentMetadata; }
    public void setPaymentMetadata(String paymentMetadata) { this.paymentMetadata = paymentMetadata; }
    
    public LocalDateTime getNextBillingDate() { return nextBillingDate; }
    public void setNextBillingDate(LocalDateTime nextBillingDate) { this.nextBillingDate = nextBillingDate; }
    
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
} 
