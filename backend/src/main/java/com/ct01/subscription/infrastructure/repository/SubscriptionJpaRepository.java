package com.ct01.subscription.infrastructure.repository;

import com.ct01.subscription.infrastructure.entity.PlanTypeJpaEnum;
import com.ct01.subscription.infrastructure.entity.SubscriptionJpaEntity;
import com.ct01.subscription.infrastructure.entity.SubscriptionStatusJpaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository для Subscription
 */
@Repository
public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionJpaEntity, String> {
    
    Optional<SubscriptionJpaEntity> findByUserId(String userId);
    
    @Query("SELECT s FROM SubscriptionJpaEntity s WHERE s.userId = :userId AND s.status IN ('ACTIVE', 'TRIAL')")
    Optional<SubscriptionJpaEntity> findActiveByUserId(@Param("userId") String userId);
    
    List<SubscriptionJpaEntity> findByStatus(SubscriptionStatusJpaEnum status);
    
    List<SubscriptionJpaEntity> findByPlanType(PlanTypeJpaEnum planType);
    
    @Query("SELECT s FROM SubscriptionJpaEntity s WHERE s.expiresAt BETWEEN :startDate AND :endDate")
    List<SubscriptionJpaEntity> findByExpiresAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM SubscriptionJpaEntity s WHERE s.autoRenewal = true AND s.nextBillingDate <= :beforeDate AND s.status = 'ACTIVE'")
    List<SubscriptionJpaEntity> findForAutoRenewal(@Param("beforeDate") LocalDateTime beforeDate);
    
    @Query("SELECT s FROM SubscriptionJpaEntity s WHERE s.expiresAt < CURRENT_TIMESTAMP AND s.status NOT IN ('EXPIRED', 'CANCELLED')")
    List<SubscriptionJpaEntity> findExpiredSubscriptions();
    
    @Query("SELECT s FROM SubscriptionJpaEntity s WHERE s.status = 'TRIAL' AND s.trialEndsAt > CURRENT_TIMESTAMP")
    List<SubscriptionJpaEntity> findTrialSubscriptions();
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SubscriptionJpaEntity s WHERE s.userId = :userId AND s.status IN ('ACTIVE', 'TRIAL')")
    boolean existsActiveByUserId(@Param("userId") String userId);
    
    long countByStatus(SubscriptionStatusJpaEnum status);
    
    long countByPlanType(PlanTypeJpaEnum planType);
} 
