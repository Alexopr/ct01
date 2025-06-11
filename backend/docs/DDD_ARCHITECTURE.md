# CT.01 - Domain-Driven Design Architecture

## Обзор архитектуры

CT.01 теперь использует Domain-Driven Design (DDD) архитектуру с четким разделением на домены и слои. Это обеспечивает лучшую организацию кода, масштабируемость и поддержку бизнес-логики.

## Структура доменов

Приложение разделено на следующие домены:

### 🏗️ Core Domain
**Пакет:** `com.ct01.core`
**Описание:** Базовые компоненты, shared kernel, общие Value Objects и конфигурации

**Компоненты:**
- `ValueObject<T>` - базовый интерфейс для Value Objects
- `Entity<ID>` - базовый интерфейс для сущностей
- `AggregateRoot<ID>` - базовый интерфейс для агрегатных корней
- `DomainEvent` - интерфейс для доменных событий
- `Repository<T, ID>` - базовый интерфейс репозитория
- `Money` - Value Object для денежных значений
- `Email` - Value Object для email адресов
- `UserId` - Value Object для идентификаторов пользователей

### 👤 User Domain
**Пакет:** `com.ct01.user`
**Описание:** Управление пользователями, аутентификация, авторизация

**Ключевые агрегаты:**
- `User` - основной агрегат пользователя
- `Role` - роли пользователей
- `Permission` - права доступа

**Value Objects:**
- `Username`, `Password`, `UserStatus`

**Domain Events:**
- `UserCreatedEvent`, `UserActivatedEvent`, `UserBlockedEvent`, и др.

### 💰 Crypto Domain
**Пакет:** `com.ct01.crypto`
**Описание:** Управление криптовалютными данными, трекинг монет

**Ключевые агрегаты:**
- `Coin` - информация о криптовалютах
- `TrackedCoin` - отслеживаемые пользователем монеты
- `PriceHistory` - история цен

**Value Objects:**
- `CoinSymbol`, `CoinName`, `PriceHistory`

**Интеграции:**
- Exchange Adapters (Binance, OKX, Bybit)
- Price Update Services

### 📊 Market Domain
**Пакет:** `com.ct01.market`
**Описание:** Рыночные данные, цены, объемы торгов

**Ключевые агрегаты:**
- `MarketData` - рыночные данные

**Value Objects:**
- `Price`, `Volume`, `PriceChange`, `MarketDataId`

**Domain Services:**
- `MarketDataDomainService` - межагрегатная логика

### 📋 Subscription Domain
**Пакет:** `com.ct01.subscription`
**Описание:** Управление подписками и лимитами

**Ключевые агрегаты:**
- `Subscription` - подписки пользователей
- `SubscriptionUsage` - использование лимитов

**Value Objects:**
- `SubscriptionPlan`, `UsageLimit`, `SubscriptionPeriod`

### 🔔 Notification Domain
**Пакет:** `com.ct01.notification`
**Описание:** Система уведомлений, WebSocket, Telegram

**Ключевые агрегаты:**
- `Notification` - уведомления

**Value Objects:**
- `NotificationContent`, `NotificationChannel`

**Интеграции:**
- WebSocket для real-time уведомлений
- Telegram Bot API

### ⚙️ Admin Domain
**Пакет:** `com.ct01.admin`
**Описание:** Административные функции, системные операции

**Ключевые агрегаты:**
- `Admin` - административные операции

**Value Objects:**
- `AdminAction`, `AdminPermissionLevel`, `AdminStatus`

**Domain Services:**
- `AdminDomainService` - проверка прав и операций

## Архитектурные слои

Каждый домен организован по следующим слоям:

### 1. Domain Layer (`domain/`)
- **Entities** - сущности с уникальной идентичностью
- **Value Objects** - неизменяемые объекты без идентичности
- **Aggregates** - кластеры связанных объектов
- **Domain Services** - логика, не принадлежащая сущностям
- **Repository Interfaces** - контракты для персистентности
- **Domain Events** - события домена
- **Domain Exceptions** - доменные исключения

### 2. Application Layer (`application/`)
- **Use Cases** - сценарии использования
- **Commands** - команды изменения состояния
- **Queries** - запросы данных
- **Application Facades** - упрощенные интерфейсы
- **DTOs** - объекты передачи данных

### 3. Infrastructure Layer (`infrastructure/`)
- **Repository Implementations** - реализации репозиториев
- **JPA Entities** - сущности для персистентности
- **External Service Adapters** - адаптеры внешних сервисов
- **Configuration** - конфигурации Spring

### 4. API Layer (`api/`)
- **Controllers** - REST контроллеры
- **DTOs** - объекты передачи данных API
- **Mappers** - мапперы между слоями

## Принципы архитектуры

### Dependency Rule
```
API Layer → Application Layer → Domain Layer
Infrastructure Layer → Application Layer → Domain Layer
```

- Domain Layer не зависит ни от чего
- Application Layer зависит только от Domain Layer
- Infrastructure и API зависят от Application и Domain

### Domain Events
Для интеграции между доменами используются Domain Events:

```java
@EventListener
public void handleUserCreated(UserCreatedEvent event) {
    // Создание начальных настроек уведомлений
    notificationService.createDefaultSettings(event.getUserId());
}
```

### Repository Pattern
Репозитории определены в Domain Layer как интерфейсы:

```java
public interface UserRepository extends Repository<User, UserId> {
    Optional<User> findByUsername(Username username);
    List<User> findByStatus(UserStatus status);
}
```

Реализации находятся в Infrastructure Layer.

### CQRS
Разделение команд и запросов на уровне Application Layer:

- **Commands** - изменяют состояние системы
- **Queries** - только читают данные

## Конфигурация

### Главный класс приложения
```java
@SpringBootApplication(scanBasePackages = {"com.ct01", "alg.coyote001.config"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Профили Spring
- `ddd` - основной профиль для DDD конфигураций
- `dev` - разработка
- `prod` - продуктивная среда

### База данных
- PostgreSQL для основного хранения
- Redis для кэширования
- Liquibase для миграций

## Миграция с Legacy

Поддерживается параллельная работа с legacy кодом:

1. **Двойной scanning** - сканируются пакеты `com.ct01` и `alg.coyote001`
2. **Legacy Adapters** - для плавной миграции
3. **Feature Toggles** - переключение между реализациями

## API Endpoints

### Health Check
```
GET /api/v1/core/health/ddd
```
Проверка работы DDD архитектуры.

### Domain APIs
- `/api/v1/users/**` - User Domain
- `/api/v1/crypto/**` - Crypto Domain  
- `/api/v1/market/**` - Market Domain
- `/api/v1/notifications/**` - Notification Domain
- `/api/v1/subscriptions/**` - Subscription Domain
- `/api/v1/admin/**` - Admin Domain

## Мониторинг

### Логирование
Настроено детальное логирование для каждого домена:

```properties
logging.level.com.ct01.user.domain=DEBUG
logging.level.com.ct01.crypto.domain=DEBUG
```

### Метрики
- Spring Boot Actuator endpoints
- Cache metrics для Redis
- Custom metrics для доменных операций

## Тестирование

### Unit Tests
- Тестирование доменной логики без инфраструктуры
- Мокирование зависимостей

### Integration Tests
- Тестирование взаимодействия слоев
- Тестирование с реальными базами данных

### Architecture Tests
- Проверка соблюдения правил архитектуры
- Тестирование зависимостей между слоями

## Производительность

### Кэширование
- Redis для кэширования данных
- Application-level кэш для часто используемых объектов

### Асинхронность
- Асинхронная обработка Domain Events
- Background jobs для тяжелых операций

### Оптимизация запросов
- Batch processing для массовых операций
- Оптимизированные JPA запросы

## Безопасность

### Аутентификация
- JWT токены
- Integration с User Domain

### Авторизация
- Role-based access control
- Permission checks в Domain Services

### Валидация
- Domain-level валидация в Value Objects
- API-level валидация в Controllers

## Следующие шаги

1. ✅ Миграция всех доменов на DDD
2. ⏳ Создание Infrastructure Layer для всех доменов
3. ⏳ Создание API Layer для всех доменов
4. ⏳ Полное удаление legacy кода
5. ⏳ Performance тестирование
6. ⏳ Документация API

---

**Версия документации:** 1.0  
**Дата обновления:** 09.06.2025  
**Автор:** CT.01 Development Team 