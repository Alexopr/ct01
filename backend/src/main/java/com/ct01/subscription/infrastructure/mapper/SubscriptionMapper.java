package com.ct01.subscription.infrastructure.mapper;

import com.ct01.core.domain.Money;
import com.ct01.core.domain.UserId;
import com.ct01.subscription.domain.*;
import com.ct01.subscription.infrastructure.entity.PlanTypeJpaEnum;
import com.ct01.subscription.infrastructure.entity.SubscriptionJpaEntity;
import com.ct01.subscription.infrastructure.entity.SubscriptionStatusJpaEnum;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования между Subscription domain и JPA entities
 */
@Component
public class SubscriptionMapper {
    
    /**
     * Преобразует Subscription domain объект в JPA entity
     */
    public SubscriptionJpaEntity toJpaEntity(Subscription subscription) {
        SubscriptionJpaEntity entity = new SubscriptionJpaEntity();
        
        entity.setId(subscription.getId().value());
        entity.setUserId(subscription.getUserId().value());
        entity.setPlanType(mapPlanType(subscription.getPlanType()));
        entity.setStatus(mapStatus(subscription.getStatus()));
        entity.setStartsAt(subscription.getStartsAt());
        entity.setExpiresAt(subscription.getExpiresAt());
        entity.setTrialEndsAt(subscription.getTrialEndsAt());
        entity.setAutoRenewal(subscription.isAutoRenewal());
        
        if (subscription.getPrice() != null) {
            entity.setPrice(subscription.getPrice().amount());
            entity.setCurrency(subscription.getPrice().currency());
        }
        
        if (subscription.getBillingPeriod() != null) {
            entity.setBillingPeriod(subscription.getBillingPeriod().displayName());
        }
        
        entity.setPaymentTransactionId(subscription.getPaymentTransactionId());
        entity.setPaymentProvider(subscription.getPaymentProvider());
        entity.setPaymentMetadata(subscription.getPaymentMetadata());
        entity.setNextBillingDate(subscription.getNextBillingDate());
        entity.setCancelledAt(subscription.getCancelledAt());
        entity.setCancellationReason(subscription.getCancellationReason());
        entity.setCreatedAt(subscription.getCreatedAt());
        entity.setUpdatedAt(subscription.getUpdatedAt());
        
        return entity;
    }
    
    /**
     * Преобразует JPA entity в Subscription domain объект
     */
    public Subscription toDomain(SubscriptionJpaEntity entity) {
        Money price = entity.getPrice() != null ? 
            Money.of(entity.getPrice(), entity.getCurrency()) : null;
            
        BillingPeriod billingPeriod = entity.getBillingPeriod() != null ?
            mapBillingPeriod(entity.getBillingPeriod()) : null;
        
        return new Subscription(
            SubscriptionId.of(entity.getId()),
            UserId.of(entity.getUserId()),
            mapPlanType(entity.getPlanType()),
            mapStatus(entity.getStatus()),
            entity.getStartsAt(),
            entity.getExpiresAt(),
            entity.getTrialEndsAt(),
            entity.isAutoRenewal(),
            price,
            billingPeriod,
            entity.getPaymentTransactionId(),
            entity.getPaymentProvider(),
            entity.getPaymentMetadata(),
            entity.getNextBillingDate(),
            entity.getCancelledAt(),
            entity.getCancellationReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
    
    private PlanTypeJpaEnum mapPlanType(PlanType planType) {
        return switch (planType) {
            case FREE -> PlanTypeJpaEnum.FREE;
            case PREMIUM -> PlanTypeJpaEnum.PREMIUM;
        };
    }
    
    private PlanType mapPlanType(PlanTypeJpaEnum planTypeJpa) {
        return switch (planTypeJpa) {
            case FREE -> PlanType.FREE;
            case PREMIUM -> PlanType.PREMIUM;
        };
    }
    
    private SubscriptionStatusJpaEnum mapStatus(SubscriptionStatus status) {
        return switch (status) {
            case ACTIVE -> SubscriptionStatusJpaEnum.ACTIVE;
            case TRIAL -> SubscriptionStatusJpaEnum.TRIAL;
            case EXPIRED -> SubscriptionStatusJpaEnum.EXPIRED;
            case CANCELLED -> SubscriptionStatusJpaEnum.CANCELLED;
            case PENDING_PAYMENT -> SubscriptionStatusJpaEnum.PENDING_PAYMENT;
            case SUSPENDED -> SubscriptionStatusJpaEnum.SUSPENDED;
            case GRACE_PERIOD -> SubscriptionStatusJpaEnum.GRACE_PERIOD;
        };
    }
    
    private SubscriptionStatus mapStatus(SubscriptionStatusJpaEnum statusJpa) {
        return switch (statusJpa) {
            case ACTIVE -> SubscriptionStatus.ACTIVE;
            case TRIAL -> SubscriptionStatus.TRIAL;
            case EXPIRED -> SubscriptionStatus.EXPIRED;
            case CANCELLED -> SubscriptionStatus.CANCELLED;
            case PENDING_PAYMENT -> SubscriptionStatus.PENDING_PAYMENT;
            case SUSPENDED -> SubscriptionStatus.SUSPENDED;
            case GRACE_PERIOD -> SubscriptionStatus.GRACE_PERIOD;
        };
    }
    
    private BillingPeriod mapBillingPeriod(String billingPeriodStr) {
        return switch (billingPeriodStr.toLowerCase()) {
            case "ежемесячно" -> BillingPeriod.MONTHLY;
            case "ежегодно" -> BillingPeriod.YEARLY;
            case "еженедельно" -> BillingPeriod.WEEKLY;
            default -> BillingPeriod.MONTHLY;
        };
    }
} 
