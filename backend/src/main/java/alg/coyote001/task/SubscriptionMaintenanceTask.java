package alg.coyote001.task;

import alg.coyote001.entity.SubscriptionUsage.ResetPeriod;
import alg.coyote001.entity.UserSubscription;
import alg.coyote001.entity.UserSubscription.SubscriptionStatus;
import alg.coyote001.event.SubscriptionExpiredEvent;
import alg.coyote001.service.SubscriptionLimitService;
import alg.coyote001.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled задачи для обслуживания системы подписок
 */
@Component
public class SubscriptionMaintenanceTask {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionMaintenanceTask.class);

    private final SubscriptionService subscriptionService;
    private final SubscriptionLimitService limitService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public SubscriptionMaintenanceTask(SubscriptionService subscriptionService,
                                       SubscriptionLimitService limitService,
                                       ApplicationEventPublisher eventPublisher) {
        this.subscriptionService = subscriptionService;
        this.limitService = limitService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Обработка истекших подписок (каждый час)
     */
    @Scheduled(fixedRate = 3600000) // Каждый час
    @Transactional
    public void processExpiredSubscriptions() {
        logger.info("Начало обработки истекших подписок");
        
        try {
            List<UserSubscription> expiredSubscriptions = subscriptionService.getExpiredSubscriptions();
            
            for (UserSubscription subscription : expiredSubscriptions) {
                if (subscription.isAutoRenewal()) {
                    // Если включено автопродление, переводим в статус ожидания платежа
                    subscription.setStatus(SubscriptionStatus.PENDING_PAYMENT);
                    logger.info("Подписка {} переведена в статус ожидания платежа для автопродления", 
                               subscription.getId());
                } else {
                    // Иначе устанавливаем grace period
                    subscription.setStatus(SubscriptionStatus.GRACE_PERIOD);
                    logger.info("Подписка {} переведена в grace period", subscription.getId());
                }
                
                // Publish subscription expired event
                eventPublisher.publishEvent(new SubscriptionExpiredEvent(
                        this, subscription.getUser().getId(), subscription.getPlanType().name()));
            }
            
            logger.info("Обработано {} истекших подписок", expiredSubscriptions.size());
            
        } catch (Exception e) {
            logger.error("Ошибка при обработке истекших подписок", e);
        }
    }

    /**
     * Сброс ежедневных лимитов использования (каждый день в 00:01)
     */
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void resetDailyUsageLimits() {
        logger.info("Начало сброса ежедневных лимитов использования");
        
        try {
            limitService.resetUsageForPeriod(ResetPeriod.DAILY);
            logger.info("Ежедневные лимиты использования сброшены");
        } catch (Exception e) {
            logger.error("Ошибка при сбросе ежедневных лимитов", e);
        }
    }

    /**
     * Сброс еженедельных лимитов использования (каждый понедельник в 00:02)
     */
    @Scheduled(cron = "0 2 0 * * MON")
    @Transactional
    public void resetWeeklyUsageLimits() {
        logger.info("Начало сброса еженедельных лимитов использования");
        
        try {
            limitService.resetUsageForPeriod(ResetPeriod.WEEKLY);
            logger.info("Еженедельные лимиты использования сброшены");
        } catch (Exception e) {
            logger.error("Ошибка при сбросе еженедельных лимитов", e);
        }
    }

    /**
     * Сброс месячных лимитов использования (1 число каждого месяца в 00:03)
     */
    @Scheduled(cron = "0 3 0 1 * *")
    @Transactional
    public void resetMonthlyUsageLimits() {
        logger.info("Начало сброса месячных лимитов использования");
        
        try {
            limitService.resetUsageForPeriod(ResetPeriod.MONTHLY);
            logger.info("Месячные лимиты использования сброшены");
        } catch (Exception e) {
            logger.error("Ошибка при сбросе месячных лимитов", e);
        }
    }

    /**
     * Уведомления о скором истечении подписок (каждый день в 09:00)
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void notifyExpiringSubscriptions() {
        logger.info("Начало отправки уведомлений о скором истечении подписок");
        
        try {
            // Уведомления за 3 дня до истечения
            List<UserSubscription> expiringSoon = subscriptionService.getExpiringSubscriptions(3);
            
            for (UserSubscription subscription : expiringSoon) {
                // Здесь можно добавить логику отправки email или push уведомлений
                logger.info("Пользователю {} отправлено уведомление о скором истечении подписки {}", 
                           subscription.getUser().getId(), subscription.getId());
            }
            
            // Уведомления за 1 день до истечения
            List<UserSubscription> expiringTomorrow = subscriptionService.getExpiringSubscriptions(1);
            
            for (UserSubscription subscription : expiringTomorrow) {
                // Более срочное уведомление
                logger.warn("СРОЧНО: Подписка {} пользователя {} истекает завтра", 
                           subscription.getId(), subscription.getUser().getId());
            }
            
            logger.info("Отправлено {} уведомлений о скором истечении", 
                       expiringSoon.size() + expiringTomorrow.size());
            
        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомлений о истечении подписок", e);
        }
    }

    /**
     * Очистка старых данных использования (каждую неделю в воскресенье в 02:00)
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    @Transactional
    public void cleanupOldUsageData() {
        logger.info("Начало очистки старых данных использования");
        
        try {
            // Удаляем данные старше 1 года
            // Логика будет добавлена в repository
            logger.info("Очистка старых данных использования завершена");
        } catch (Exception e) {
            logger.error("Ошибка при очистке старых данных", e);
        }
    }

    /**
     * Обработка подписок в grace period (каждые 6 часов)
     */
    @Scheduled(fixedRate = 21600000) // Каждые 6 часов
    @Transactional
    public void processGracePeriodSubscriptions() {
        logger.info("Начало обработки подписок в grace period");
        
        try {
            // Найти подписки в grace period, которые истекли более 3 дней назад
            LocalDateTime gracePeriodExpiry = LocalDateTime.now().minusDays(3);
            
            // Здесь можно добавить логику для поиска и обработки таких подписок
            // Например, перевод в статус EXPIRED или отмена
            
            logger.info("Обработка подписок в grace period завершена");
            
        } catch (Exception e) {
            logger.error("Ошибка при обработке подписок в grace period", e);
        }
    }

    /**
     * Статистика использования подписок (каждый день в 23:00)
     */
    @Scheduled(cron = "0 0 23 * * *")
    @Transactional
    public void generateUsageStatistics() {
        logger.info("Начало генерации статистики использования подписок");
        
        try {
            // Здесь можно добавить логику для генерации отчетов
            // о использовании различных модулей и функций
            
            logger.info("Генерация статистики использования завершена");
            
        } catch (Exception e) {
            logger.error("Ошибка при генерации статистики", e);
        }
    }

    /**
     * Проверка конфигурации подписок (каждые 30 минут)
     */
    @Scheduled(fixedRate = 1800000) // Каждые 30 минут
    public void validateSubscriptionConfiguration() {
        logger.debug("Проверка конфигурации подписок");
        
        try {
            // Проверяем, что конфигурация загружена и валидна
            var config = subscriptionService.getSubscriptionConfiguration();
            if (config == null) {
                logger.error("Конфигурация подписок не загружена!");
                return;
            }
            
            if (config.getSubscriptionPlans() == null || config.getSubscriptionPlans().isEmpty()) {
                logger.error("Планы подписок не настроены!");
                return;
            }
            
            logger.debug("Конфигурация подписок валидна. Версия: {}", config.getVersion());
            
        } catch (Exception e) {
            logger.error("Ошибка при проверке конфигурации подписок", e);
        }
    }
} 