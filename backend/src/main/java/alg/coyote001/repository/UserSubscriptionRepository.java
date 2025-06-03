package alg.coyote001.repository;

import alg.coyote001.entity.User;
import alg.coyote001.entity.UserSubscription;
import alg.coyote001.entity.UserSubscription.PlanType;
import alg.coyote001.entity.UserSubscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с подписками пользователей
 */
@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    
    /**
     * Найти активную подписку пользователя
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.user.id = :userId " +
           "AND us.status IN ('ACTIVE', 'TRIAL') " +
           "AND (us.expiresAt IS NULL OR us.expiresAt > :now) " +
           "ORDER BY us.createdAt DESC")
    Optional<UserSubscription> findActiveSubscriptionByUserId(@Param("userId") Long userId, 
                                                               @Param("now") LocalDateTime now);
    
    /**
     * Найти активную подписку пользователя (упрощенный вариант)
     */
    default Optional<UserSubscription> findActiveSubscriptionByUserId(Long userId) {
        return findActiveSubscriptionByUserId(userId, LocalDateTime.now());
    }
    
    /**
     * Найти текущую подписку пользователя (включая истекшие)
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.user.id = :userId " +
           "ORDER BY us.createdAt DESC")
    Optional<UserSubscription> findCurrentSubscriptionByUserId(@Param("userId") Long userId);
    
    /**
     * Найти все подписки пользователя
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.user.id = :userId " +
           "ORDER BY us.createdAt DESC")
    List<UserSubscription> findAllByUserId(@Param("userId") Long userId);
    
    /**
     * Найти подписки по статусу
     */
    List<UserSubscription> findByStatus(SubscriptionStatus status);
    
    /**
     * Найти подписки по типу плана
     */
    List<UserSubscription> findByPlanType(PlanType planType);
    
    /**
     * Найти истекающие подписки
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.expiresAt BETWEEN :start AND :end " +
           "AND us.status IN ('ACTIVE', 'TRIAL')")
    List<UserSubscription> findExpiringSubscriptions(@Param("start") LocalDateTime start, 
                                                      @Param("end") LocalDateTime end);
    
    /**
     * Найти истекшие подписки, которые нужно обновить
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.expiresAt < :now " +
           "AND us.status IN ('ACTIVE', 'TRIAL', 'GRACE_PERIOD')")
    List<UserSubscription> findExpiredSubscriptions(@Param("now") LocalDateTime now);
    
    /**
     * Найти подписки для автоматического продления
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.autoRenewal = true " +
           "AND us.nextBillingDate <= :now " +
           "AND us.status = 'ACTIVE'")
    List<UserSubscription> findSubscriptionsForRenewal(@Param("now") LocalDateTime now);
    
    /**
     * Подсчитать количество активных подписок по типу
     */
    @Query("SELECT COUNT(us) FROM UserSubscription us WHERE us.planType = :planType " +
           "AND us.status IN ('ACTIVE', 'TRIAL') " +
           "AND (us.expiresAt IS NULL OR us.expiresAt > :now)")
    long countActiveSubscriptionsByPlanType(@Param("planType") PlanType planType, 
                                            @Param("now") LocalDateTime now);
    
    /**
     * Найти подписки по провайдеру платежей
     */
    List<UserSubscription> findByPaymentProvider(String paymentProvider);
    
    /**
     * Найти подписки по ID транзакции
     */
    Optional<UserSubscription> findByPaymentTransactionId(String transactionId);
    
    /**
     * Проверить, есть ли у пользователя активная Premium подписка
     */
    @Query("SELECT COUNT(us) > 0 FROM UserSubscription us WHERE us.user.id = :userId " +
           "AND us.planType = 'PREMIUM' " +
           "AND us.status IN ('ACTIVE', 'TRIAL') " +
           "AND (us.expiresAt IS NULL OR us.expiresAt > :now)")
    boolean hasActivePremiumSubscription(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    /**
     * Упрощенная проверка Premium подписки
     */
    default boolean hasActivePremiumSubscription(Long userId) {
        return hasActivePremiumSubscription(userId, LocalDateTime.now());
    }
    
    /**
     * Найти подписки в trial периоде
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.status = 'TRIAL' " +
           "AND us.trialEndsAt > :now")
    List<UserSubscription> findActiveTrialSubscriptions(@Param("now") LocalDateTime now);
    
    /**
     * Найти завершающиеся trial подписки
     */
    @Query("SELECT us FROM UserSubscription us WHERE us.status = 'TRIAL' " +
           "AND us.trialEndsAt BETWEEN :start AND :end")
    List<UserSubscription> findEndingTrialSubscriptions(@Param("start") LocalDateTime start, 
                                                         @Param("end") LocalDateTime end);
} 