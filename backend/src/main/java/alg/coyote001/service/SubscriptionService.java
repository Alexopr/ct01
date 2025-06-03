package alg.coyote001.service;

import alg.coyote001.config.SubscriptionConfig;
import alg.coyote001.config.SubscriptionLimitsConfiguration;
import alg.coyote001.config.SubscriptionPlan;
import alg.coyote001.entity.User;
import alg.coyote001.entity.UserSubscription;
import alg.coyote001.entity.UserSubscription.PlanType;
import alg.coyote001.entity.UserSubscription.SubscriptionStatus;
import alg.coyote001.repository.UserRepository;
import alg.coyote001.repository.UserSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления подписками пользователей
 */
@Service
@Transactional
public class SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    private final UserSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionLimitsConfiguration subscriptionConfig;

    @Autowired
    public SubscriptionService(UserSubscriptionRepository subscriptionRepository,
                               UserRepository userRepository,
                               SubscriptionLimitsConfiguration subscriptionConfig) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.subscriptionConfig = subscriptionConfig;
    }

    /**
     * Получить активную подписку пользователя
     */
    @Cacheable(value = "activeSubscriptions", key = "#userId")
    public Optional<UserSubscription> getActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveSubscriptionByUserId(userId);
    }

    /**
     * Получить тип плана пользователя
     */
    @Cacheable(value = "userPlanTypes", key = "#userId")
    public PlanType getUserPlanType(Long userId) {
        return getActiveSubscription(userId)
                .map(UserSubscription::getPlanType)
                .orElse(PlanType.FREE);
    }

    /**
     * Проверить, есть ли у пользователя Premium подписка
     */
    @Cacheable(value = "premiumStatus", key = "#userId")
    public boolean hasActivePremiumSubscription(Long userId) {
        return subscriptionRepository.hasActivePremiumSubscription(userId);
    }

    /**
     * Создать новую подписку для пользователя
     */
    @CacheEvict(value = {"activeSubscriptions", "userPlanTypes", "premiumStatus"}, key = "#userId")
    public UserSubscription createSubscription(Long userId, PlanType planType, 
                                               BigDecimal price, String currency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + userId));

        // Завершить текущую активную подписку
        Optional<UserSubscription> currentSubscription = getActiveSubscription(userId);
        if (currentSubscription.isPresent()) {
            cancelSubscription(currentSubscription.get().getId(), "Обновление плана");
        }

        // Получить конфигурацию плана
        SubscriptionPlan planConfig = subscriptionConfig.getSubscriptionPlan(planType.name());
        if (planConfig == null) {
            throw new RuntimeException("Конфигурация плана не найдена: " + planType);
        }

        LocalDateTime now = LocalDateTime.now();
        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .planType(planType)
                .status(planConfig.hasTrialPeriod() ? SubscriptionStatus.TRIAL : SubscriptionStatus.ACTIVE)
                .startsAt(now)
                .price(price)
                .currency(currency)
                .autoRenewal(true)
                .build();

        // Настроить trial период
        if (planConfig.hasTrialPeriod() && planType != PlanType.FREE) {
            subscription.setTrialEndsAt(now.plusDays(planConfig.getTrialDays()));
            subscription.setExpiresAt(now.plusDays(planConfig.getTrialDays()));
        } else if (planType == PlanType.PREMIUM) {
            // Установить дату истечения на основе billing cycle
            subscription.setExpiresAt(calculateExpiryDate(now, planConfig.getBillingCycle()));
            subscription.setNextBillingDate(subscription.getExpiresAt());
        }

        UserSubscription savedSubscription = subscriptionRepository.save(subscription);
        
        logger.info("Создана новая подписка {} для пользователя {}", planType, userId);
        return savedSubscription;
    }

    /**
     * Создать FREE подписку для нового пользователя
     */
    @CacheEvict(value = {"activeSubscriptions", "userPlanTypes", "premiumStatus"}, key = "#userId")
    public UserSubscription createFreeSubscription(Long userId) {
        return createSubscription(userId, PlanType.FREE, BigDecimal.ZERO, "USD");
    }

    /**
     * Обновить подписку до Premium
     */
    @CacheEvict(value = {"activeSubscriptions", "userPlanTypes", "premiumStatus"}, key = "#userId")
    public UserSubscription upgradeToPremium(Long userId, BigDecimal price, String currency, 
                                             String paymentTransactionId, String paymentProvider) {
        UserSubscription subscription = createSubscription(userId, PlanType.PREMIUM, price, currency);
        subscription.setPaymentTransactionId(paymentTransactionId);
        subscription.setPaymentProvider(paymentProvider);
        
        return subscriptionRepository.save(subscription);
    }

    /**
     * Понизить подписку до FREE
     */
    @CacheEvict(value = {"activeSubscriptions", "userPlanTypes", "premiumStatus"}, key = "#userId")
    public UserSubscription downgradeToFree(Long userId, String reason) {
        Optional<UserSubscription> currentSubscription = getActiveSubscription(userId);
        if (currentSubscription.isPresent()) {
            cancelSubscription(currentSubscription.get().getId(), reason);
        }
        
        return createFreeSubscription(userId);
    }

    /**
     * Отменить подписку
     */
    @CacheEvict(value = {"activeSubscriptions", "userPlanTypes", "premiumStatus"}, key = "#subscription.user.id")
    public UserSubscription cancelSubscription(Long subscriptionId, String reason) {
        UserSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Подписка не найдена: " + subscriptionId));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setCancellationReason(reason);
        subscription.setAutoRenewal(false);

        UserSubscription cancelledSubscription = subscriptionRepository.save(subscription);
        
        logger.info("Отменена подписка {} для пользователя {}. Причина: {}", 
                   subscriptionId, subscription.getUser().getId(), reason);
        
        return cancelledSubscription;
    }

    /**
     * Продлить подписку
     */
    @CacheEvict(value = {"activeSubscriptions", "userPlanTypes", "premiumStatus"}, key = "#subscription.user.id")
    public UserSubscription renewSubscription(Long subscriptionId, BigDecimal price, 
                                              String paymentTransactionId) {
        UserSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Подписка не найдена: " + subscriptionId));

        SubscriptionPlan planConfig = subscriptionConfig.getSubscriptionPlan(subscription.getPlanType().name());
        if (planConfig == null) {
            throw new RuntimeException("Конфигурация плана не найдена: " + subscription.getPlanType());
        }

        LocalDateTime now = LocalDateTime.now();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setExpiresAt(calculateExpiryDate(now, planConfig.getBillingCycle()));
        subscription.setNextBillingDate(subscription.getExpiresAt());
        subscription.setPaymentTransactionId(paymentTransactionId);
        subscription.setPrice(price);

        UserSubscription renewedSubscription = subscriptionRepository.save(subscription);
        
        logger.info("Продлена подписка {} для пользователя {}", 
                   subscriptionId, subscription.getUser().getId());
        
        return renewedSubscription;
    }

    /**
     * Завершить trial период и активировать подписку
     */
    @CacheEvict(value = {"activeSubscriptions", "userPlanTypes", "premiumStatus"}, key = "#subscription.user.id")
    public UserSubscription activateFromTrial(Long subscriptionId, BigDecimal price, 
                                              String paymentTransactionId, String paymentProvider) {
        UserSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Подписка не найдена: " + subscriptionId));

        if (subscription.getStatus() != SubscriptionStatus.TRIAL) {
            throw new RuntimeException("Подписка не находится в trial периоде");
        }

        SubscriptionPlan planConfig = subscriptionConfig.getSubscriptionPlan(subscription.getPlanType().name());
        LocalDateTime now = LocalDateTime.now();
        
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setExpiresAt(calculateExpiryDate(now, planConfig.getBillingCycle()));
        subscription.setNextBillingDate(subscription.getExpiresAt());
        subscription.setPaymentTransactionId(paymentTransactionId);
        subscription.setPaymentProvider(paymentProvider);
        subscription.setPrice(price);

        UserSubscription activatedSubscription = subscriptionRepository.save(subscription);
        
        logger.info("Активирована подписка из trial {} для пользователя {}", 
                   subscriptionId, subscription.getUser().getId());
        
        return activatedSubscription;
    }

    /**
     * Получить историю подписок пользователя
     */
    public List<UserSubscription> getUserSubscriptionHistory(Long userId) {
        return subscriptionRepository.findAllByUserId(userId);
    }

    /**
     * Получить истекающие подписки
     */
    public List<UserSubscription> getExpiringSubscriptions(int daysAhead) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(daysAhead);
        return subscriptionRepository.findExpiringSubscriptions(start, end);
    }

    /**
     * Получить истекшие подписки, которые нужно обновить
     */
    public List<UserSubscription> getExpiredSubscriptions() {
        return subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());
    }

    /**
     * Получить подписки для автоматического продления
     */
    public List<UserSubscription> getSubscriptionsForRenewal() {
        return subscriptionRepository.findSubscriptionsForRenewal(LocalDateTime.now());
    }

    /**
     * Вычислить дату истечения подписки
     */
    private LocalDateTime calculateExpiryDate(LocalDateTime startDate, String billingCycle) {
        switch (billingCycle.toLowerCase()) {
            case "monthly":
                return startDate.plusMonths(1);
            case "yearly":
                return startDate.plusYears(1);
            case "weekly":
                return startDate.plusWeeks(1);
            default:
                return startDate.plusMonths(1); // По умолчанию месячная подписка
        }
    }

    /**
     * Получить конфигурацию плана
     */
    public SubscriptionPlan getPlanConfiguration(PlanType planType) {
        return subscriptionConfig.getSubscriptionPlan(planType.name());
    }

    /**
     * Получить текущую конфигурацию подписок
     */
    public SubscriptionConfig getSubscriptionConfiguration() {
        return subscriptionConfig.getCurrentConfig();
    }
} 