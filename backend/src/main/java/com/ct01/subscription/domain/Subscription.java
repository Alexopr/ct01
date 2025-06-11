package com.ct01.subscription.domain;

import com.ct01.core.domain.AggregateRoot;
import com.ct01.core.domain.Money;
import com.ct01.core.domain.UserId;
import com.ct01.shared.exception.DomainException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Subscription Aggregate Root
 */
public class Subscription extends AggregateRoot<SubscriptionId> {
    
    private final UserId userId;
    private final PlanType planType;
    private SubscriptionStatus status;
    private final LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private LocalDateTime trialEndsAt;
    private boolean autoRenewal;
    private final Money price;
    private final BillingPeriod billingPeriod;
    private String paymentTransactionId;
    private String paymentProvider;
    private String paymentMetadata;
    private LocalDateTime nextBillingDate;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Конструктор для создания новой подписки
    private Subscription(SubscriptionId id, UserId userId, PlanType planType, Money price, 
                        BillingPeriod billingPeriod, LocalDateTime startsAt) {
        super(id);
        this.userId = userId;
        this.planType = planType;
        this.price = price;
        this.billingPeriod = billingPeriod;
        this.startsAt = startsAt;
        this.status = SubscriptionStatus.ACTIVE;
        this.autoRenewal = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Вычисляем дату истечения и следующего биллинга
        this.expiresAt = billingPeriod.calculateNextBillingDate(startsAt);
        this.nextBillingDate = this.expiresAt;
        
        // Добавляем событие создания подписки
        addDomainEvent(new SubscriptionCreatedEvent(id, userId, planType, startsAt));
    }
    
    // Конструктор для восстановления из базы данных
    public Subscription(SubscriptionId id, UserId userId, PlanType planType, SubscriptionStatus status,
                       LocalDateTime startsAt, LocalDateTime expiresAt, LocalDateTime trialEndsAt,
                       boolean autoRenewal, Money price, BillingPeriod billingPeriod,
                       String paymentTransactionId, String paymentProvider, String paymentMetadata,
                       LocalDateTime nextBillingDate, LocalDateTime cancelledAt, String cancellationReason,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id);
        this.userId = userId;
        this.planType = planType;
        this.status = status;
        this.startsAt = startsAt;
        this.expiresAt = expiresAt;
        this.trialEndsAt = trialEndsAt;
        this.autoRenewal = autoRenewal;
        this.price = price;
        this.billingPeriod = billingPeriod;
        this.paymentTransactionId = paymentTransactionId;
        this.paymentProvider = paymentProvider;
        this.paymentMetadata = paymentMetadata;
        this.nextBillingDate = nextBillingDate;
        this.cancelledAt = cancelledAt;
        this.cancellationReason = cancellationReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Создает новую подписку
     */
    public static Subscription create(UserId userId, PlanType planType, Money price, 
                                    BillingPeriod billingPeriod) {
        if (userId == null) {
            throw DomainException.invalidArgument("UserId не может быть null");
        }
        if (planType == null) {
            throw DomainException.invalidArgument("PlanType не может быть null");
        }
        if (price == null) {
            throw DomainException.invalidArgument("Price не может быть null");
        }
        if (billingPeriod == null) {
            throw DomainException.invalidArgument("BillingPeriod не может быть null");
        }
        
        return new Subscription(
            SubscriptionId.generate(),
            userId,
            planType,
            price,
            billingPeriod,
            LocalDateTime.now()
        );
    }
    
    /**
     * Создает trial подписку
     */
    public static Subscription createTrial(UserId userId, PlanType planType, int trialDays) {
        if (trialDays <= 0) {
            throw DomainException.invalidArgument("Trial дни должны быть больше 0");
        }
        
        Subscription subscription = new Subscription(
            SubscriptionId.generate(),
            userId,
            planType,
            Money.zero("USD"),
            BillingPeriod.MONTHLY,
            LocalDateTime.now()
        );
        
        subscription.status = SubscriptionStatus.TRIAL;
        subscription.trialEndsAt = LocalDateTime.now().plusDays(trialDays);
        subscription.expiresAt = subscription.trialEndsAt;
        
        subscription.addDomainEvent(new SubscriptionTrialStartedEvent(
            subscription.getId(), userId, planType, subscription.trialEndsAt));
        
        return subscription;
    }
    
    /**
     * Активирует подписку после оплаты
     */
    public void activate(String transactionId, String provider, String metadata) {
        if (!status.equals(SubscriptionStatus.PENDING_PAYMENT) && 
            !status.equals(SubscriptionStatus.TRIAL)) {
            throw DomainException.businessRule("Нельзя активировать подписку в статусе: " + status);
        }
        
        this.status = SubscriptionStatus.ACTIVE;
        this.paymentTransactionId = transactionId;
        this.paymentProvider = provider;
        this.paymentMetadata = metadata;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new SubscriptionActivatedEvent(getId(), userId, planType));
    }
    
    /**
     * Отменяет подписку
     */
    public void cancel(String reason) {
        if (!status.canBeCancelled()) {
            throw DomainException.businessRule("Нельзя отменить подписку в статусе: " + status);
        }
        
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
        this.autoRenewal = false;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new SubscriptionCancelledEvent(getId(), userId, reason));
    }
    
    /**
     * Приостанавливает подписку
     */
    public void suspend(String reason) {
        if (!status.isActive()) {
            throw DomainException.businessRule("Нельзя приостановить неактивную подписку");
        }
        
        this.status = SubscriptionStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new SubscriptionSuspendedEvent(getId(), userId, reason));
    }
    
    /**
     * Возобновляет приостановленную подписку
     */
    public void resume() {
        if (!status.equals(SubscriptionStatus.SUSPENDED)) {
            throw DomainException.businessRule("Можно возобновить только приостановленную подписку");
        }
        
        this.status = SubscriptionStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new SubscriptionResumedEvent(getId(), userId));
    }
    
    /**
     * Продлевает подписку
     */
    public void renew() {
        if (!autoRenewal) {
            throw DomainException.businessRule("Автопродление отключено");
        }
        
        this.expiresAt = billingPeriod.calculateNextBillingDate(expiresAt);
        this.nextBillingDate = this.expiresAt;
        this.status = SubscriptionStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new SubscriptionRenewedEvent(getId(), userId, expiresAt));
    }
    
    /**
     * Помечает подписку как истекшую
     */
    public void markAsExpired() {
        if (status.isInactive()) {
            return; // Уже неактивна
        }
        
        this.status = SubscriptionStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new SubscriptionExpiredEvent(getId(), userId));
    }
    
    /**
     * Проверяет, активна ли подписка
     */
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return status.isActive() && (expiresAt == null || expiresAt.isAfter(now));
    }
    
    /**
     * Проверяет, находится ли подписка в trial периоде
     */
    public boolean isInTrial() {
        LocalDateTime now = LocalDateTime.now();
        return status == SubscriptionStatus.TRIAL &&
               trialEndsAt != null && trialEndsAt.isAfter(now);
    }
    
    /**
     * Проверяет, истекла ли подписка
     */
    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        return expiresAt != null && expiresAt.isBefore(now) && 
               !status.equals(SubscriptionStatus.CANCELLED);
    }
    
    /**
     * Получает количество дней до истечения подписки
     */
    public long getDaysUntilExpiry() {
        if (expiresAt == null) return Long.MAX_VALUE;
        LocalDateTime now = LocalDateTime.now();
        return java.time.Duration.between(now, expiresAt).toDays();
    }
    
    // Getters
    public UserId getUserId() { return userId; }
    public PlanType getPlanType() { return planType; }
    public SubscriptionStatus getStatus() { return status; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getTrialEndsAt() { return trialEndsAt; }
    public boolean isAutoRenewal() { return autoRenewal; }
    public Money getPrice() { return price; }
    public BillingPeriod getBillingPeriod() { return billingPeriod; }
    public String getPaymentTransactionId() { return paymentTransactionId; }
    public String getPaymentProvider() { return paymentProvider; }
    public String getPaymentMetadata() { return paymentMetadata; }
    public LocalDateTime getNextBillingDate() { return nextBillingDate; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public String getCancellationReason() { return cancellationReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
} 
