# Руководство пользователя системы подписок

## Введение

Добро пожаловать в систему управления подписками! Эта документация поможет вам интегрировать и использовать функциональность подписок в вашем приложении.

## Быстрый старт

### 1. Конфигурация

Создайте файл `subscription-limits.json` в директории `scripts/`:

```json
{
  "plans": {
    "FREE": {
      "name": "FREE",
      "displayName": "Бесплатный",
      "description": "Базовый функционал",
      "price": 0,
      "currency": "USD",
      "billingCycle": "monthly",
      "trialDays": 0,
      "features": ["Базовое отслеживание", "До 5 уведомлений в день"],
      "limits": {
        "twitterTracker": {
          "maxTrackedAccounts": 3,
          "maxTweetsPerDay": 100,
          "resetPeriod": "daily"
        },
        "telegramTracker": {
          "maxTrackedChannels": 2,
          "maxMessagesPerDay": 50,
          "resetPeriod": "daily"
        }
      }
    },
    "PREMIUM": {
      "name": "PREMIUM",
      "displayName": "Премиум",
      "description": "Расширенный функционал",
      "price": 9.99,
      "currency": "USD", 
      "billingCycle": "monthly",
      "trialDays": 7,
      "features": ["Безлимитное отслеживание", "Аналитика", "Приоритетная поддержка"],
      "limits": {
        "twitterTracker": {
          "maxTrackedAccounts": -1,
          "maxTweetsPerDay": -1,
          "resetPeriod": "daily"
        },
        "telegramTracker": {
          "maxTrackedChannels": -1,
          "maxMessagesPerDay": -1,
          "resetPeriod": "daily"
        },
        "marketAnalytics": {
          "maxReportsPerMonth": -1,
          "resetPeriod": "monthly"
        }
      }
    }
  },
  "settings": {
    "defaultPlan": "FREE",
    "trialEnabled": true,
    "autoRenewal": true,
    "gracePeriodDays": 3
  }
}
```

### 2. Инициализация

Система автоматически инициализируется при запуске приложения. Проверьте логи:

```
[INFO] SubscriptionLimitsConfiguration - Loaded subscription configuration with 2 plans
[INFO] SubscriptionRoleInitializer - Initialized subscription roles: USER, SUBSCRIPTION_FREE, SUBSCRIPTION_PREMIUM
```

## Использование API

### Получение доступных планов

```bash
curl -X GET "http://localhost:8080/api/subscription/plans" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Ответ:**
```json
[
  {
    "name": "FREE",
    "displayName": "Бесплатный",
    "description": "Базовый функционал",
    "price": 0,
    "currency": "USD",
    "billingCycle": "monthly",
    "trialDays": 0,
    "features": ["Базовое отслеживание", "До 5 уведомлений в день"]
  },
  {
    "name": "PREMIUM", 
    "displayName": "Премиум",
    "description": "Расширенный функционал",
    "price": 9.99,
    "currency": "USD",
    "billingCycle": "monthly",
    "trialDays": 7,
    "features": ["Безлимитное отслеживание", "Аналитика", "Приоритетная поддержка"]
  }
]
```

### Получение статуса подписки

```bash
curl -X GET "http://localhost:8080/api/subscription/status" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Ответ:**
```json
{
  "planType": "FREE",
  "status": "ACTIVE",
  "isActive": true,
  "startsAt": "2024-01-01T00:00:00",
  "expiresAt": null,
  "autoRenewal": false,
  "price": null,
  "currency": null,
  "paymentTransactionId": null,
  "cancellationReason": null
}
```

### Обновление подписки

```bash
curl -X POST "http://localhost:8080/api/subscription/upgrade" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "planType": "PREMIUM",
    "billingCycle": "monthly", 
    "price": 9.99,
    "currency": "USD",
    "paymentTransactionId": "tx_1234567890",
    "paymentProvider": "stripe"
  }'
```

**Ответ:**
```json
{
  "planType": "PREMIUM",
  "status": "ACTIVE", 
  "isActive": true,
  "startsAt": "2024-01-15T10:30:00",
  "expiresAt": "2024-02-15T10:30:00",
  "autoRenewal": true,
  "price": 9.99,
  "currency": "USD",
  "paymentTransactionId": "tx_1234567890"
}
```

### Отмена подписки

```bash
curl -X POST "http://localhost:8080/api/subscription/cancel" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Пользователь больше не нуждается в услуге"
  }'
```

### Проверка лимитов использования

```bash
curl -X GET "http://localhost:8080/api/subscription/limits" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Ответ:**
```json
[
  {
    "resourceType": "TWITTER_TRACK",
    "currentUsage": 2,
    "limit": 3,
    "resetPeriod": "daily",
    "nextResetAt": "2024-01-16T00:00:00",
    "isUnlimited": false
  },
  {
    "resourceType": "TELEGRAM_TRACK", 
    "currentUsage": 1,
    "limit": 2,
    "resetPeriod": "daily",
    "nextResetAt": "2024-01-16T00:00:00",
    "isUnlimited": false
  }
]
```

### Проверка доступа к ресурсу

```bash
curl -X GET "http://localhost:8080/api/subscription/check/twitter_tracking" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Ответ (доступ разрешен):**
```json
{
  "hasAccess": true,
  "resource": "twitter_tracking",
  "message": "Доступ разрешен"
}
```

**Ответ (доступ запрещен):**
```json
{
  "hasAccess": false,
  "resource": "twitter_tracking", 
  "message": "Требуется обновление до Premium подписки для доступа к этому ресурсу"
}
```

## Интеграция в код

### Проверка лимитов в сервисах

```java
@Service
public class TwitterService {
    
    @Autowired
    private SubscriptionLimitService limitService;
    
    public void trackAccount(Long userId, String account) {
        // Проверяем лимит перед выполнением операции
        if (!limitService.checkLimit(userId, "TWITTER_TRACK")) {
            throw new LimitExceededException("Достигнут лимит отслеживаемых аккаунтов");
        }
        
        // Выполняем операцию
        addAccountToTracking(account);
        
        // Увеличиваем счетчик использования
        limitService.incrementUsage(userId, "TWITTER_TRACK");
    }
    
    // Альтернативный подход: atomic check and increment
    public void trackAccountAtomic(Long userId, String account) {
        if (limitService.checkAndIncrementUsage(userId, "TWITTER_TRACK")) {
            addAccountToTracking(account);
        } else {
            throw new LimitExceededException("Достигнут лимит отслеживаемых аккаунтов");
        }
    }
}
```

### Использование аннотации @RequireSubscription

```java
@RestController
@RequestMapping("/api/twitter")
public class TwitterController {
    
    @PostMapping("/track")
    @RequireSubscription(plan = "PREMIUM", resourceType = "twitter_tracking")
    public ResponseEntity<?> trackAccount(@RequestBody TrackRequest request) {
        // Этот метод доступен только пользователям с Premium подпиской
        twitterService.trackAccount(getCurrentUserId(), request.getAccount());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/analytics")
    @RequireSubscription(plan = "PREMIUM")
    public ResponseEntity<AnalyticsData> getAnalytics() {
        // Доступно только Premium пользователям
        return ResponseEntity.ok(analyticsService.getTwitterAnalytics());
    }
}
```

### Обработка событий подписки

```java
@Component
public class SubscriptionEventHandler {
    
    @EventListener
    public void handleSubscriptionUpgraded(SubscriptionUpgradedEvent event) {
        log.info("User {} upgraded from {} to {}", 
                event.getUserId(), event.getOldPlan(), event.getNewPlan());
        
        // Отправляем welcome email для Premium пользователей
        if ("PREMIUM".equals(event.getNewPlan())) {
            emailService.sendPremiumWelcomeEmail(event.getUserId());
        }
        
        // Обновляем кэш пользователя
        userCacheService.invalidateUser(event.getUserId());
    }
    
    @EventListener
    public void handleSubscriptionCancelled(SubscriptionCancelledEvent event) {
        log.info("User {} cancelled {} subscription", 
                event.getUserId(), event.getPlanType());
        
        // Отправляем feedback email
        emailService.sendCancellationFeedback(event.getUserId());
    }
    
    @EventListener 
    public void handleSubscriptionExpired(SubscriptionExpiredEvent event) {
        log.warn("Subscription expired for user {} (plan: {})", 
                event.getUserId(), event.getPlanType());
        
        // Отключаем Premium функции
        featureToggleService.disablePremiumFeatures(event.getUserId());
    }
}
```

## Frontend интеграция

### React компоненты

```typescript
import React from 'react';
import { SubscriptionDashboard } from '@/components/subscription';

export default function SubscriptionPage() {
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">Управление подпиской</h1>
      <SubscriptionDashboard />
    </div>
  );
}
```

### Использование API сервиса

```typescript
import { subscriptionService } from '@/services/api';

// Получение статуса подписки
const getSubscriptionStatus = async () => {
  try {
    const status = await subscriptionService.getStatus();
    console.log('Current plan:', status.planType);
    console.log('Is active:', status.isActive);
  } catch (error) {
    console.error('Failed to get subscription status:', error);
  }
};

// Обновление подписки
const upgradeToPremium = async () => {
  try {
    const upgradeRequest = {
      planType: 'PREMIUM',
      billingCycle: 'monthly',
      price: 9.99,
      currency: 'USD',
      paymentTransactionId: 'stripe_tx_123',
      paymentProvider: 'stripe'
    };
    
    const result = await subscriptionService.upgrade(upgradeRequest);
    console.log('Upgrade successful:', result);
  } catch (error) {
    console.error('Upgrade failed:', error);
  }
};

// Проверка доступа к функции
const checkFeatureAccess = async (feature: string) => {
  try {
    const access = await subscriptionService.checkAccess(feature);
    if (access.hasAccess) {
      // Показываем функцию
      showFeature(feature);
    } else {
      // Показываем upgrade предложение
      showUpgradeModal(access.message);
    }
  } catch (error) {
    console.error('Access check failed:', error);
  }
};
```

### Хук для подписки

```typescript
import { useState, useEffect } from 'react';
import { subscriptionService } from '@/services/api';

export const useSubscription = () => {
  const [subscription, setSubscription] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadSubscription();
  }, []);

  const loadSubscription = async () => {
    try {
      setLoading(true);
      const status = await subscriptionService.getStatus();
      setSubscription(status);
      setError(null);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  const upgrade = async (request) => {
    try {
      const result = await subscriptionService.upgrade(request);
      setSubscription(result);
      return result;
    } catch (err) {
      setError(err);
      throw err;
    }
  };

  const cancel = async (reason) => {
    try {
      const result = await subscriptionService.cancel({ reason });
      setSubscription(result);
      return result;
    } catch (err) {
      setError(err);
      throw err;
    }
  };

  return {
    subscription,
    loading,
    error,
    loadSubscription,
    upgrade,
    cancel,
    isPremium: subscription?.planType === 'PREMIUM',
    isActive: subscription?.isActive ?? false
  };
};
```

## Конфигурация и настройка

### Переменные окружения

```properties
# Основные настройки
SUBSCRIPTION_CONFIG_PATH=/app/config/subscription-limits.json
SUBSCRIPTION_DEFAULT_PLAN=FREE
SUBSCRIPTION_TRIAL_ENABLED=true

# База данных
SUBSCRIPTION_DB_URL=jdbc:postgresql://localhost:5432/crypto_admin
SUBSCRIPTION_DB_USERNAME=crypto_user
SUBSCRIPTION_DB_PASSWORD=crypto_password

# Redis для кэширования
SUBSCRIPTION_REDIS_HOST=localhost
SUBSCRIPTION_REDIS_PORT=6379
SUBSCRIPTION_REDIS_PASSWORD=redis_password

# Уведомления
SUBSCRIPTION_EMAIL_ENABLED=true
SUBSCRIPTION_EMAIL_FROM=noreply@example.com

# Maintenance
SUBSCRIPTION_CLEANUP_ENABLED=true
SUBSCRIPTION_CLEANUP_RETENTION_DAYS=365
```

### Кастомизация лимитов

Добавьте новый тип ресурса в конфигурацию:

```json
{
  "plans": {
    "PREMIUM": {
      "limits": {
        "customFeature": {
          "maxOperationsPerHour": 1000,
          "resetPeriod": "hourly",
          "description": "Максимальное количество операций в час"
        }
      }
    }
  }
}
```

Используйте в коде:

```java
@Service
public class CustomFeatureService {
    
    public void performOperation(Long userId) {
        if (!limitService.checkAndIncrementUsage(userId, "CUSTOM_FEATURE")) {
            throw new LimitExceededException("Достигнут лимит операций");
        }
        
        // Выполняем операцию
        executeCustomOperation();
    }
}
```

## Мониторинг и отладка

### Логирование

Включите debug логирование для подписок:

```properties
logging.level.alg.coyote001.service.SubscriptionService=DEBUG
logging.level.alg.coyote001.service.SubscriptionLimitService=DEBUG
logging.level.alg.coyote001.aspect.SubscriptionAspect=DEBUG
```

### Метрики

Основные метрики для мониторинга:

```java
// Custom metrics (если используете Micrometer)
@Component
public class SubscriptionMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter upgradeCounter;
    private final Counter cancellationCounter;
    private final Gauge activeSubscriptionsGauge;
    
    public SubscriptionMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.upgradeCounter = Counter.builder("subscription.upgrades")
            .description("Number of subscription upgrades")
            .register(meterRegistry);
        this.cancellationCounter = Counter.builder("subscription.cancellations")
            .description("Number of subscription cancellations")
            .register(meterRegistry);
    }
    
    public void recordUpgrade(String fromPlan, String toPlan) {
        upgradeCounter.increment(
            Tags.of("from", fromPlan, "to", toPlan)
        );
    }
}
```

### Health Checks

```java
@Component
public class SubscriptionHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // Проверяем доступность конфигурации
            subscriptionConfig.getPlans();
            
            // Проверяем подключение к БД
            subscriptionRepository.count();
            
            return Health.up()
                .withDetail("configStatus", "OK")
                .withDetail("databaseStatus", "OK")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## Частые вопросы (FAQ)

### Q: Как добавить новый план подписки?

A: Добавьте план в `subscription-limits.json` и перезапустите приложение. Конфигурация будет автоматически загружена.

### Q: Можно ли изменить лимиты без перезапуска?

A: Да, система поддерживает hot-reload. Просто отредактируйте файл конфигурации.

### Q: Как обработать неудачную оплату?

A: Используйте webhook'и от платежной системы для обновления статуса подписки через API.

### Q: Как настроить пробный период?

A: Установите `trialDays > 0` в конфигурации плана. Система автоматически создаст подписку со статусом TRIAL.

### Q: Что происходит при достижении лимита?

A: Система возвращает `false` при проверке лимита. Обработка зависит от вашей бизнес-логики.

## Лучшие практики

1. **Всегда проверяйте лимиты** перед выполнением операций
2. **Используйте atomic операции** `checkAndIncrementUsage()` для критических ресурсов
3. **Обрабатывайте события подписки** для синхронизации состояния
4. **Мониторьте использование лимитов** для оптимизации планов
5. **Тестируйте upgrade/downgrade сценарии** в различных состояниях
6. **Настройте proper индексы** для производительности БД
7. **Используйте кэширование** для часто проверяемых лимитов

## Поддержка

Если у вас возникли вопросы или проблемы:

1. Проверьте логи приложения
2. Убедитесь в корректности конфигурации
3. Проверьте health checks
4. Обратитесь к разработчикам с детальным описанием проблемы 