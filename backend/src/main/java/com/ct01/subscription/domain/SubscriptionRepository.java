package com.ct01.subscription.domain;

import com.ct01.core.domain.Repository;
import com.ct01.core.domain.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository для Subscription Aggregate
 */
public interface SubscriptionRepository extends Repository<Subscription, SubscriptionId> {
    
    /**
     * Найти подписку по пользователю
     */
    Optional<Subscription> findByUserId(UserId userId);
    
    /**
     * Найти активную подписку пользователя
     */
    Optional<Subscription> findActiveByUserId(UserId userId);
    
    /**
     * Найти подписки по статусу
     */
    List<Subscription> findByStatus(SubscriptionStatus status);
    
    /**
     * Найти подписки по типу плана
     */
    List<Subscription> findByPlanType(PlanType planType);
    
    /**
     * Найти подписки, которые истекают в указанный период
     */
    List<Subscription> findExpiringBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Найти подписки для автопродления
     */
    List<Subscription> findForAutoRenewal(LocalDateTime beforeDate);
    
    /**
     * Найти истекшие подписки
     */
    List<Subscription> findExpiredSubscriptions();
    
    /**
     * Найти подписки в trial периоде
     */
    List<Subscription> findTrialSubscriptions();
    
    /**
     * Проверить существование активной подписки у пользователя
     */
    boolean existsActiveByUserId(UserId userId);
    
    /**
     * Подсчитать количество подписок по статусу
     */
    long countByStatus(SubscriptionStatus status);
    
    /**
     * Подсчитать количество подписок по типу плана
     */
    long countByPlanType(PlanType planType);
} 
