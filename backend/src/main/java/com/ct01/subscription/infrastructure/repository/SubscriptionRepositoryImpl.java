package com.ct01.subscription.infrastructure.repository;

import com.ct01.core.domain.UserId;
import com.ct01.subscription.domain.*;
import com.ct01.subscription.infrastructure.entity.PlanTypeJpaEnum;
import com.ct01.subscription.infrastructure.entity.SubscriptionJpaEntity;
import com.ct01.subscription.infrastructure.entity.SubscriptionStatusJpaEnum;
import com.ct01.subscription.infrastructure.mapper.SubscriptionMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация SubscriptionRepository через JPA
 */
@Repository
public class SubscriptionRepositoryImpl implements SubscriptionRepository {
    
    private final SubscriptionJpaRepository jpaRepository;
    private final SubscriptionMapper subscriptionMapper;
    
    public SubscriptionRepositoryImpl(SubscriptionJpaRepository jpaRepository,
                                    SubscriptionMapper subscriptionMapper) {
        this.jpaRepository = jpaRepository;
        this.subscriptionMapper = subscriptionMapper;
    }
    
    @Override
    public Subscription save(Subscription subscription) {
        SubscriptionJpaEntity entity = subscriptionMapper.toJpaEntity(subscription);
        SubscriptionJpaEntity savedEntity = jpaRepository.save(entity);
        return subscriptionMapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Subscription> findById(SubscriptionId id) {
        return jpaRepository.findById(id.value())
            .map(subscriptionMapper::toDomain);
    }
    
    @Override
    public void deleteById(SubscriptionId id) {
        jpaRepository.deleteById(id.value());
    }
    
    @Override
    public boolean existsById(SubscriptionId id) {
        return jpaRepository.existsById(id.value());
    }
    
    @Override
    public List<Subscription> findAll() {
        return jpaRepository.findAll().stream()
            .map(subscriptionMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Subscription> findByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId.value())
            .map(subscriptionMapper::toDomain);
    }
    
    @Override
    public Optional<Subscription> findActiveByUserId(UserId userId) {
        return jpaRepository.findActiveByUserId(userId.value())
            .map(subscriptionMapper::toDomain);
    }
    
    @Override
    public List<Subscription> findByStatus(SubscriptionStatus status) {
        SubscriptionStatusJpaEnum statusJpa = mapStatus(status);
        return jpaRepository.findByStatus(statusJpa).stream()
            .map(subscriptionMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Subscription> findByPlanType(PlanType planType) {
        PlanTypeJpaEnum planTypeJpa = mapPlanType(planType);
        return jpaRepository.findByPlanType(planTypeJpa).stream()
            .map(subscriptionMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Subscription> findExpiringBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByExpiresAtBetween(startDate, endDate).stream()
            .map(subscriptionMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Subscription> findForAutoRenewal(LocalDateTime beforeDate) {
        return jpaRepository.findForAutoRenewal(beforeDate).stream()
            .map(subscriptionMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Subscription> findExpiredSubscriptions() {
        return jpaRepository.findExpiredSubscriptions().stream()
            .map(subscriptionMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Subscription> findTrialSubscriptions() {
        return jpaRepository.findTrialSubscriptions().stream()
            .map(subscriptionMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsActiveByUserId(UserId userId) {
        return jpaRepository.existsActiveByUserId(userId.value());
    }
    
    @Override
    public long countByStatus(SubscriptionStatus status) {
        SubscriptionStatusJpaEnum statusJpa = mapStatus(status);
        return jpaRepository.countByStatus(statusJpa);
    }
    
    @Override
    public long countByPlanType(PlanType planType) {
        PlanTypeJpaEnum planTypeJpa = mapPlanType(planType);
        return jpaRepository.countByPlanType(planTypeJpa);
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
    
    private PlanTypeJpaEnum mapPlanType(PlanType planType) {
        return switch (planType) {
            case FREE -> PlanTypeJpaEnum.FREE;
            case PREMIUM -> PlanTypeJpaEnum.PREMIUM;
        };
    }
} 
