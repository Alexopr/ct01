package alg.coyote001.service;

import alg.coyote001.config.SubscriptionLimits;
import alg.coyote001.config.SubscriptionLimitsConfiguration;
import alg.coyote001.config.SubscriptionPlan;
import alg.coyote001.entity.SubscriptionUsage;
import alg.coyote001.entity.SubscriptionUsage.ResetPeriod;
import alg.coyote001.entity.User;
import alg.coyote001.entity.UserSubscription;
import alg.coyote001.entity.UserSubscription.PlanType;
import alg.coyote001.repository.SubscriptionUsageRepository;
import alg.coyote001.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Сервис для управления лимитами использования подписок
 */
@Service
@Transactional
public class SubscriptionLimitService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionLimitService.class);

    private final SubscriptionUsageRepository usageRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final SubscriptionLimitsConfiguration subscriptionConfig;
    
    // Thread-safe locks для конкурентных операций
    private final ReentrantLock usageLock = new ReentrantLock();

    @Autowired
    public SubscriptionLimitService(SubscriptionUsageRepository usageRepository,
                                    UserRepository userRepository,
                                    SubscriptionService subscriptionService,
                                    SubscriptionLimitsConfiguration subscriptionConfig) {
        this.usageRepository = usageRepository;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
        this.subscriptionConfig = subscriptionConfig;
    }

    /**
     * Проверить, можно ли использовать ресурс (не превышен ли лимит)
     */
    @Cacheable(value = "usageLimits", key = "#userId + '_' + #moduleName + '_' + #resourceType")
    public boolean canUseResource(Long userId, String moduleName, String resourceType) {
        return canUseResource(userId, moduleName, resourceType, 1);
    }

    /**
     * Проверить, можно ли использовать определенное количество ресурса
     */
    public boolean canUseResource(Long userId, String moduleName, String resourceType, int requestedAmount) {
        PlanType planType = subscriptionService.getUserPlanType(userId);
        Integer limit = getResourceLimit(planType, moduleName, resourceType);
        
        if (limit == null || limit == 0) {
            return false; // Ресурс недоступен для данного плана
        }
        
        if (limit == -1) {
            return true; // Неограниченное использование
        }

        LocalDate today = LocalDate.now();
        Integer currentUsage = usageRepository.getTodayUsage(userId, moduleName, resourceType, today);
        if (currentUsage == null) {
            currentUsage = 0;
        }

        return (currentUsage + requestedAmount) <= limit;
    }

    /**
     * Использовать ресурс (увеличить счетчик)
     */
    @CacheEvict(value = "usageLimits", key = "#userId + '_' + #moduleName + '_' + #resourceType")
    public boolean useResource(Long userId, String moduleName, String resourceType) {
        return useResource(userId, moduleName, resourceType, 1);
    }

    /**
     * Использовать определенное количество ресурса
     */
    @CacheEvict(value = "usageLimits", key = "#userId + '_' + #moduleName + '_' + #resourceType")
    public boolean useResource(Long userId, String moduleName, String resourceType, int amount) {
        usageLock.lock();
        try {
            // Проверить, можно ли использовать ресурс
            if (!canUseResource(userId, moduleName, resourceType, amount)) {
                return false;
            }

            // Получить или создать запись использования
            LocalDate today = LocalDate.now();
            Optional<SubscriptionUsage> usageOpt = usageRepository.findByUserAndModuleAndResourceAndDate(
                    userId, moduleName, resourceType, today);

            if (usageOpt.isPresent()) {
                // Обновить существующую запись
                int updated = usageRepository.incrementUsage(userId, moduleName, resourceType, today, amount);
                if (updated == 0) {
                    // Если update не сработал, создаем новую запись
                    createUsageRecord(userId, moduleName, resourceType, amount);
                }
            } else {
                // Создать новую запись
                createUsageRecord(userId, moduleName, resourceType, amount);
            }

            logger.debug("Использован ресурс {}.{} пользователем {} в количестве {}", 
                        moduleName, resourceType, userId, amount);
            return true;
            
        } finally {
            usageLock.unlock();
        }
    }

    /**
     * Получить текущее использование ресурса
     */
    @Cacheable(value = "currentUsage", key = "#userId + '_' + #moduleName + '_' + #resourceType")
    public int getCurrentUsage(Long userId, String moduleName, String resourceType) {
        LocalDate today = LocalDate.now();
        Integer usage = usageRepository.getTodayUsage(userId, moduleName, resourceType, today);
        return usage != null ? usage : 0;
    }

    /**
     * Получить оставшееся количество ресурса
     */
    public int getRemainingUsage(Long userId, String moduleName, String resourceType) {
        PlanType planType = subscriptionService.getUserPlanType(userId);
        Integer limit = getResourceLimit(planType, moduleName, resourceType);
        
        if (limit == null || limit == 0) {
            return 0; // Ресурс недоступен
        }
        
        if (limit == -1) {
            return Integer.MAX_VALUE; // Неограниченное использование
        }

        int currentUsage = getCurrentUsage(userId, moduleName, resourceType);
        return Math.max(0, limit - currentUsage);
    }

    /**
     * Получить лимит ресурса для плана
     */
    public Integer getResourceLimit(PlanType planType, String moduleName, String resourceType) {
        SubscriptionPlan plan = subscriptionConfig.getSubscriptionPlan(planType.name());
        if (plan == null || plan.getLimits() == null) {
            return 0;
        }

        SubscriptionLimits limits = plan.getLimits();
        
        switch (moduleName.toLowerCase()) {
            case "twitter_tracker":
                return getTwitterTrackerLimit(limits.getTwitterTracker(), resourceType);
            case "telegram_tracker":
                return getTelegramTrackerLimit(limits.getTelegramTracker(), resourceType);
            case "market_analytics":
                return getMarketAnalyticsLimit(limits.getMarketAnalytics(), resourceType);
            case "smart_money_tracking":
                return getSmartMoneyLimit(limits.getSmartMoneyTracking(), resourceType);
            case "general":
                return getGeneralLimit(limits.getGeneral(), resourceType);
            default:
                return 0;
        }
    }

    /**
     * Проверить, превышен ли лимит
     */
    public boolean isLimitExceeded(Long userId, String moduleName, String resourceType) {
        return !canUseResource(userId, moduleName, resourceType);
    }

    /**
     * Получить статистику использования по модулю для пользователя
     */
    public List<SubscriptionUsage> getUserModuleUsage(Long userId, String moduleName) {
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        return usageRepository.findCurrentUsageByUserAndModule(userId, moduleName, weekAgo);
    }

    /**
     * Сбросить использование для определенного периода
     */
    @CacheEvict(value = {"usageLimits", "currentUsage"}, allEntries = true)
    public void resetUsageForPeriod(ResetPeriod period) {
        LocalDate cutoffDate = calculateCutoffDate(period);
        int resetCount = usageRepository.resetUsageForPeriod(period, cutoffDate);
        logger.info("Сброшено {} записей использования для периода {}", resetCount, period);
    }

    /**
     * Создать запись использования
     */
    private void createUsageRecord(Long userId, String moduleName, String resourceType, int amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + userId));

        PlanType planType = subscriptionService.getUserPlanType(userId);
        Integer limit = getResourceLimit(planType, moduleName, resourceType);
        
        if (limit == null) {
            limit = 0;
        }

        SubscriptionUsage usage = SubscriptionUsage.builder()
                .user(user)
                .moduleName(moduleName)
                .resourceType(resourceType)
                .usageDate(LocalDate.now())
                .usedCount(amount)
                .limitCount(limit)
                .resetPeriod(ResetPeriod.DAILY) // По умолчанию ежедневный сброс
                .build();

        usageRepository.save(usage);
    }

    /**
     * Вычислить дату отсечения для сброса
     */
    private LocalDate calculateCutoffDate(ResetPeriod period) {
        LocalDate now = LocalDate.now();
        switch (period) {
            case DAILY:
                return now;
            case WEEKLY:
                return now.minusDays(now.getDayOfWeek().getValue() - 1);
            case MONTHLY:
                return now.withDayOfMonth(1);
            case YEARLY:
                return now.withDayOfYear(1);
            default:
                return now;
        }
    }

    // Методы для получения специфичных лимитов модулей

    private Integer getTwitterTrackerLimit(SubscriptionLimits.TwitterTrackerLimits limits, String resourceType) {
        if (limits == null) return 0;
        
        switch (resourceType.toLowerCase()) {
            case "max_accounts": return limits.getMaxAccounts();
            case "max_alerts_per_day": return limits.getMaxAlertsPerDay();
            case "historical_data_days": return limits.getHistoricalDataDays();
            default: return 0;
        }
    }

    private Integer getTelegramTrackerLimit(SubscriptionLimits.TelegramTrackerLimits limits, String resourceType) {
        if (limits == null) return 0;
        
        switch (resourceType.toLowerCase()) {
            case "max_channels": return limits.getMaxChannels();
            case "max_alerts_per_day": return limits.getMaxAlertsPerDay();
            case "historical_data_days": return limits.getHistoricalDataDays();
            default: return 0;
        }
    }

    private Integer getMarketAnalyticsLimit(SubscriptionLimits.MarketAnalyticsLimits limits, String resourceType) {
        if (limits == null) return 0;
        
        switch (resourceType.toLowerCase()) {
            case "max_tracked_contracts": return limits.getMaxTrackedContracts();
            case "alert_frequency_minutes": return limits.getAlertFrequencyMinutes();
            case "historical_data_days": return limits.getHistoricalDataDays();
            default: return 0;
        }
    }

    private Integer getSmartMoneyLimit(SubscriptionLimits.SmartMoneyTrackingLimits limits, String resourceType) {
        if (limits == null) return 0;
        
        switch (resourceType.toLowerCase()) {
            case "max_wallets": return limits.getMaxWallets();
            case "historical_data_days": return limits.getHistoricalDataDays();
            default: return 0;
        }
    }

    private Integer getGeneralLimit(SubscriptionLimits.GeneralLimits limits, String resourceType) {
        if (limits == null) return 0;
        
        switch (resourceType.toLowerCase()) {
            case "api_requests_per_day": return limits.getApiRequestsPerDay();
            case "historical_data_retention_days": return limits.getHistoricalDataRetentionDays();
            case "concurrent_connections": return limits.getConcurrentConnections();
            default: return 0;
        }
    }
} 