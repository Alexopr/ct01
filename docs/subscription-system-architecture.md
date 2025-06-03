# Subscription System Architecture

## Обзор

Система подписок представляет собой комплексное решение для управления пользовательскими подписками в приложении cryptocurrency admin dashboard. Система обеспечивает гибкое управление планами подписок, контроль лимитов ресурсов, интеграцию с ролевой системой и обработку платежей.

## Архитектурные компоненты

### 1. Конфигурационный слой

#### SubscriptionLimitsConfiguration
- **Назначение**: Централизованное управление конфигурацией лимитов подписок
- **Особенности**:
  - Hot-reload поддержка через FileWatcher
  - JSON-based конфигурация в `subscription-limits.json`
  - Thread-safe операции чтения
  - Валидация конфигурации при загрузке

```java
@Component
public class SubscriptionLimitsConfiguration {
    // Автоматическая перезагрузка при изменении файла
    // Кэширование конфигурации для производительности
}
```

#### Структура конфигурации
```json
{
  "plans": {
    "FREE": {
      "name": "FREE",
      "displayName": "Бесплатный",
      "limits": {
        "twitterTracker": {
          "maxTrackedAccounts": 3,
          "maxTweetsPerDay": 100
        }
      }
    },
    "PREMIUM": {
      "name": "PREMIUM", 
      "displayName": "Премиум",
      "limits": {
        "twitterTracker": {
          "maxTrackedAccounts": -1,
          "maxTweetsPerDay": -1
        }
      }
    }
  }
}
```

### 2. Модель данных

#### Основные сущности

**UserSubscription**
- Хранит информацию о подписке пользователя
- Поддерживает историю изменений
- Отслеживает платежную информацию
- Управляет автопродлением

**SubscriptionUsage**
- Отслеживает использование ресурсов
- Поддерживает различные периоды сброса (daily/weekly/monthly)
- Thread-safe операции инкремента
- Автоматический сброс по расписанию

#### Database Schema
```sql
-- Таблица подписок
CREATE TABLE user_subscriptions (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    starts_at TIMESTAMP,
    expires_at TIMESTAMP,
    auto_renewal BOOLEAN DEFAULT false,
    price DECIMAL(10,2),
    currency VARCHAR(3),
    -- Индексы для производительности
    INDEX idx_user_active (user_id, status, expires_at),
    INDEX idx_expiring (expires_at, status),
    INDEX idx_plan_type (plan_type)
);

-- Таблица использования ресурсов
CREATE TABLE subscription_usage (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    usage_count INTEGER DEFAULT 0,
    limit_value INTEGER NOT NULL,
    reset_period VARCHAR(20) NOT NULL,
    last_reset_at TIMESTAMP,
    -- Уникальный индекс для предотвращения дублирования
    UNIQUE KEY uk_user_resource (user_id, resource_type),
    INDEX idx_reset_period (reset_period, last_reset_at)
);
```

### 3. Бизнес-логика (Services)

#### SubscriptionService
**Основные функции**:
- Создание и управление подписками
- Обработка upgrade/downgrade операций
- Управление жизненным циклом подписки
- Публикация событий изменения подписки

**Ключевые методы**:
```java
public class SubscriptionService {
    // Создание подписки
    UserSubscription createSubscription(Long userId, PlanType planType, BigDecimal price, String currency);
    
    // Обновление подписки
    UserSubscription upgradeToPremium(Long userId, BigDecimal price, String currency, String transactionId, String provider);
    
    // Отмена подписки
    UserSubscription cancelSubscription(Long subscriptionId, String reason);
    
    // Получение активной подписки
    Optional<UserSubscription> getActiveSubscription(Long userId);
}
```

#### SubscriptionLimitService
**Основные функции**:
- Проверка лимитов ресурсов
- Инкремент использования ресурсов
- Сброс лимитов по расписанию
- Кэширование через Redis

**Thread Safety**:
```java
@Service
public class SubscriptionLimitService {
    private final ReentrantLock lock = new ReentrantLock();
    
    public void incrementUsage(Long userId, String resourceType) {
        lock.lock();
        try {
            // Thread-safe операции с базой данных
        } finally {
            lock.unlock();
        }
    }
}
```

### 4. Event-Driven Architecture

#### События подписки
- `SubscriptionUpgradedEvent`
- `SubscriptionCancelledEvent` 
- `SubscriptionExpiredEvent`

#### Обработчики событий
```java
@EventListener
public void handleSubscriptionUpgraded(SubscriptionUpgradedEvent event) {
    // Автоматическое обновление ролей пользователя
    updateUserRoles(event.getUserId(), event.getNewPlan());
}
```

### 5. Интеграция с ролевой системой

#### SubscriptionRoleIntegrationService
- Автоматическое назначение ролей при изменении подписки
- Сохранение не-подписочных ролей
- Инициализация стандартных ролей при запуске

#### Роли подписки
- `USER` - базовая роль пользователя
- `SUBSCRIPTION_FREE` - роль бесплатной подписки
- `SUBSCRIPTION_PREMIUM` - роль премиум подписки

### 6. AOP-based Access Control

#### @RequireSubscription аннотация
```java
@RequireSubscription(plan = "PREMIUM", resourceType = "twitter_tracking")
public void trackTwitterAccount(String account) {
    // Метод доступен только пользователям с Premium подпиской
}
```

#### SubscriptionAspect
- Перехватывает вызовы аннотированных методов
- Проверяет права доступа пользователя
- Интегрируется с Spring Security

### 7. REST API

#### Endpoint'ы
```
GET /api/subscription/plans         - Получить доступные планы
GET /api/subscription/status        - Статус текущей подписки
POST /api/subscription/upgrade      - Обновить подписку
POST /api/subscription/cancel       - Отменить подписку
GET /api/subscription/limits        - Получить лимиты использования
GET /api/subscription/check/{resource} - Проверить доступ к ресурсу
```

#### DTO классы
- `SubscriptionPlanDto` - информация о плане
- `SubscriptionStatusDto` - статус подписки
- `UsageLimitDto` - лимиты использования
- `SubscriptionUpgradeRequest` - запрос на обновление

### 8. Frontend Components

#### React/NextUI компоненты
- `SubscriptionDashboard` - главная панель управления
- `SubscriptionPlans` - отображение доступных планов
- `SubscriptionStatus` - текущий статус подписки
- `LimitIndicator` - индикатор использования лимитов
- `UpgradeModal` - модальное окно обновления

#### Интеграция с API
```typescript
// Сервис для работы с API подписок
export const subscriptionService = {
  async getPlans(): Promise<SubscriptionPlan[]> {
    return api.get('/subscription/plans');
  },
  
  async getStatus(): Promise<SubscriptionStatus> {
    return api.get('/subscription/status');
  },
  
  async upgrade(request: UpgradeRequest): Promise<SubscriptionStatus> {
    return api.post('/subscription/upgrade', request);
  }
};
```

### 9. Автоматизация и задачи

#### SubscriptionMaintenanceTask
**Scheduled задачи**:
- Обработка истекающих подписок
- Сброс лимитов использования
- Отправка уведомлений
- Очистка устаревших данных
- Валидация целостности данных

```java
@Scheduled(fixedRate = 3600000) // Каждый час
public void processExpiringSubscriptions() {
    List<UserSubscription> expiring = subscriptionService.getExpiringSubscriptions(24);
    expiring.forEach(this::sendExpirationNotification);
}
```

## Паттерны проектирования

### 1. Strategy Pattern
Используется для различных алгоритмов расчета лимитов в зависимости от типа подписки.

### 2. Observer Pattern
Реализован через Spring Events для уведомления о изменениях подписки.

### 3. Repository Pattern
Инкапсулирует логику доступа к данным для подписок и использования ресурсов.

### 4. Command Pattern
Используется в операциях изменения подписки для возможности отмены и аудита.

## Производительность и масштабирование

### Оптимизации базы данных
- Составные индексы для частых запросов
- Партиционирование по дате для исторических данных
- Batch операции для массовых обновлений

### Кэширование
- Redis кэш для лимитов пользователей
- Application-level кэш для конфигурации
- TTL-based кэширование для редко изменяемых данных

### Асинхронная обработка
- Event-driven архитектура для decoupling
- Асинхронная обработка уведомлений
- Background jobs для maintenance задач

## Безопасность

### Аутентификация и авторизация
- Интеграция с Spring Security
- Role-based access control
- Method-level security через AOP

### Валидация данных
- Bean Validation для входных данных
- Custom validators для бизнес-правил
- SQL injection protection через JPA

### Аудит и логирование
- Логирование всех операций с подписками
- Audit trail для изменений лимитов
- Security events для подозрительной активности

## Мониторинг и метрики

### Ключевые метрики
- Количество активных подписок по планам
- Использование лимитов ресурсов
- Частота upgrade/downgrade операций
- Performance метрики API endpoint'ов

### Health Checks
- Доступность конфигурационного файла
- Состояние базы данных подписок
- Работоспособность Redis кэша
- Статус background задач

## Развертывание и конфигурация

### Environment Variables
```properties
# Database configuration
SUBSCRIPTION_DB_URL=jdbc:postgresql://localhost:5432/subscriptions
SUBSCRIPTION_DB_USERNAME=subscription_user
SUBSCRIPTION_DB_PASSWORD=subscription_pass

# Redis configuration  
SUBSCRIPTION_REDIS_HOST=localhost
SUBSCRIPTION_REDIS_PORT=6379

# File paths
SUBSCRIPTION_CONFIG_PATH=/config/subscription-limits.json
```

### Docker Configuration
```yaml
subscription-service:
  image: crypto-admin/subscription-service:latest
  environment:
    - SPRING_PROFILES_ACTIVE=production
    - SUBSCRIPTION_CONFIG_PATH=/app/config/subscription-limits.json
  volumes:
    - ./config:/app/config
  depends_on:
    - postgres
    - redis
```

## Тестирование

### Типы тестов
- **Unit Tests**: Отдельные компоненты и сервисы
- **Integration Tests**: Взаимодействие между компонентами
- **Performance Tests**: Нагрузочное тестирование
- **API Tests**: REST endpoint'ы
- **End-to-End Tests**: Полные пользовательские сценарии

### Test Coverage
- Минимальное покрытие: 80%
- Critical paths: 95%
- Performance tests для ключевых операций

## Развитие и расширение

### Планы развития
1. **Multi-currency support**: Поддержка множественных валют
2. **Promo codes**: Система промокодов и скидок
3. **Team subscriptions**: Корпоративные подписки
4. **Usage analytics**: Детальная аналитика использования
5. **A/B testing**: Тестирование различных планов

### Расширение функциональности
- Plugin архитектура для новых типов лимитов
- Webhook поддержка для внешних систем
- GraphQL API для гибких запросов
- Real-time уведомления через WebSocket

## Заключение

Система подписок представляет собой масштабируемое и гибкое решение, которое может адаптироваться к растущим потребностям бизнеса. Архитектура обеспечивает высокую производительность, безопасность и простоту сопровождения, следуя лучшим практикам разработки enterprise приложений. 