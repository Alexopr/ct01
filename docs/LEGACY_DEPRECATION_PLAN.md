# 📋 План депрекации Legacy API endpoints

## 🎯 Цель

Постепенная и контролируемая депрекация legacy API endpoints (alg.coyote001) с переходом на новую DDD архитектуру (com.ct01).

## 📊 Анализ текущего состояния

### Legacy controllers для депрекации:

1. **CoinDataController** (`/api/v1/coins`) ✅ *Частично мигрирован*
2. **TrackedCoinController** (`/api/v1/tracked-coins`) ✅ *Частично мигрирован*
3. **PriceUpdateController** (`/api/v1/prices`) ✅ *Мигрирован*
4. **ExchangeController** (`/api/v1/exchanges`) ❌ *Не мигрирован*
5. **SystemController** (`/api/v1/system`) ❌ *Не мигрирован*
6. **SubscriptionController** (`/api/v1/subscriptions`) ❌ *Не мигрирован*
7. **UserManagementController** (`/api/v1/users`) ❌ *Не мигрирован*
8. **ApiAnalysisController** (`/api/v1/analysis`) ❌ *Не мигрирован*

### Целевые новые endpoints:

1. `/api/v1/crypto/coins` → замена `/api/v1/coins`
2. `/api/v1/crypto/tracked-coins` → замена `/api/v1/tracked-coins`
3. `/api/v1/crypto/prices` → замена `/api/v1/prices`
4. `/api/v1/crypto/exchanges` → замена `/api/v1/exchanges`
5. `/api/v1/system` → новая реализация
6. `/api/v1/subscriptions` → новая реализация
7. `/api/v1/users` → новая реализация
8. `/api/v1/admin/analysis` → замена `/api/v1/analysis`

## 🕐 Временные рамки депрекации

### Фаза 1: Предупреждения (Текущая - 3 месяца)
- ✅ Добавить deprecation заголовки во все legacy endpoints
- ✅ Добавить логирование использования legacy API
- ✅ Обновить документацию с указанием новых endpoints
- ✅ Уведомить потребителей API о предстоящих изменениях

### Фаза 2: Мягкая депрекация (3-6 месяцев)
- ⏳ Добавить HTTP-заголовки с предупреждениями
- ⏳ Внедрить rate limiting для legacy endpoints
- ⏳ Реализовать автоматические редиректы там, где возможно
- ⏳ Создать миграционные руководства

### Фаза 3: Жесткая депрекация (6-9 месяцев)
- ⏳ Отключить legacy endpoints по умолчанию
- ⏳ Требовать специальную конфигурацию для их включения
- ⏳ Возвращать HTTP 410 Gone для деактивированных endpoints
- ⏳ Интенсивное уведомление пользователей

### Фаза 4: Полное удаление (9-12 месяцев)
- ⏳ Полное удаление legacy controllers
- ⏳ Очистка legacy кода и зависимостей
- ⏳ Обновление тестов и документации

## 🔧 Технические компоненты

### 1. Система мониторинга использования

```java
@Component
public class LegacyEndpointMonitor {
    // Отслеживание использования legacy endpoints
    // Метрики и статистика для принятия решений
}
```

### 2. Middleware для депрекации

```java
@Component
public class DeprecationMiddleware {
    // Добавление заголовков депрекации
    // Логирование и метрики
    // Условные редиректы
}
```

### 3. Конфигурация управления

```yaml
legacy:
  endpoints:
    enabled: true
    deprecation-warnings: true
    rate-limiting: false
    redirect-enabled: false
```

## 📋 Детальный план по endpoints

### CoinDataController (`/api/v1/coins`)

**Статус:** Частично мигрирован  
**Новый endpoint:** `/api/v1/crypto/coins`

| Legacy Endpoint | Новый Endpoint | Статус | Срок депрекации |
|----------------|---------------|--------|-----------------|
| `GET /api/v1/coins` | `GET /api/v1/crypto/coins` | ✅ Мигрирован | Фаза 2 |
| `GET /api/v1/coins/{symbol}` | `GET /api/v1/crypto/coins/{symbol}` | ✅ Мигрирован | Фаза 2 |
| `GET /api/v1/coins/{symbol}/price` | `GET /api/v1/crypto/prices/current/{symbol}` | ✅ Мигрирован | Фаза 2 |
| `GET /api/v1/coins/search` | `GET /api/v1/crypto/coins/search` | ✅ Мигрирован | Фаза 2 |

### TrackedCoinController (`/api/v1/tracked-coins`)

**Статус:** Частично мигрирован  
**Новый endpoint:** `/api/v1/crypto/tracked-coins`

| Legacy Endpoint | Новый Endpoint | Статус | Срок депрекации |
|----------------|---------------|--------|-----------------|
| `GET /api/v1/tracked-coins` | `GET /api/v1/crypto/tracked-coins` | ✅ Мигрирован | Фаза 2 |
| `POST /api/v1/tracked-coins` | `POST /api/v1/crypto/tracked-coins` | ✅ Мигрирован | Фаза 2 |
| `GET /api/v1/tracked-coins/{id}` | `GET /api/v1/crypto/tracked-coins/{id}` | ✅ Мигрирован | Фаза 2 |
| `GET /api/v1/tracked-coins/symbol/{symbol}` | `GET /api/v1/crypto/tracked-coins?symbol={symbol}` | ✅ Мигрирован | Фаза 2 |

### ExchangeController (`/api/v1/exchanges`)

**Статус:** Требует миграции  
**Новый endpoint:** `/api/v1/crypto/exchanges`

| Legacy Endpoint | Новый Endpoint | Статус | Срок депрекации |
|----------------|---------------|--------|-----------------|
| `GET /api/v1/exchanges` | `GET /api/v1/crypto/exchanges` | ❌ Не мигрирован | Фаза 3 |
| `GET /api/v1/exchanges/{name}` | `GET /api/v1/crypto/exchanges/{name}` | ❌ Не мигрирован | Фаза 3 |
| `GET /api/v1/exchanges/{name}/status` | `GET /api/v1/crypto/exchanges/{name}/status` | ❌ Не мигрирован | Фаза 3 |

## 📊 Мониторинг и метрики

### Ключевые метрики для отслеживания:

1. **Использование Legacy API:**
   - Количество запросов к legacy endpoints
   - Уникальные пользователи legacy API
   - Популярные legacy endpoints

2. **Миграция пользователей:**
   - Скорость перехода на новые endpoints
   - Процент трафика на legacy vs новых endpoints
   - Ошибки при использовании новых endpoints

3. **Производительность:**
   - Время ответа legacy vs новых endpoints
   - Нагрузка на сервер от legacy endpoints
   - Количество ошибок

### Инструменты мониторинга:

- **Micrometer + Prometheus** для метрик
- **ELK Stack** для логирования
- **Grafana** для визуализации
- **Spring Boot Actuator** для health checks

## 📢 Коммуникационная стратегия

### 1. Внутренние команды

- **Уведомления в Slack/Teams** о планах депрекации
- **Техническая документация** с примерами миграции
- **Воркшопы** по новой архитектуре DDD

### 2. Внешние потребители API

- **Email рассылки** с уведомлениями о депрекации
- **Обновление API документации** с deprecation notices
- **Migration guides** с пошаговыми инструкциями
- **Changelog** с детальным описанием изменений

### 3. Документация

- **API Reference** с указанием deprecated endpoints
- **Migration Guide** с примерами кода
- **FAQ** по часто задаваемым вопросам
- **Breaking Changes** документ

## ⚙️ Конфигурация управления депрекацией

### application.yml конфигурация:

```yaml
legacy:
  api:
    enabled: true  # Общий флаг включения legacy API
    deprecation:
      warnings: true  # Включить предупреждения
      headers: true   # Добавлять HTTP заголовки
      logging: true   # Логировать использование
    endpoints:
      coins:
        enabled: true
        redirect-to: "/api/v1/crypto/coins"
        removal-date: "2024-06-01"
      tracked-coins:
        enabled: true  
        redirect-to: "/api/v1/crypto/tracked-coins"
        removal-date: "2024-06-01"
      exchanges:
        enabled: false  # Уже отключен
        redirect-to: "/api/v1/crypto/exchanges"
        removal-date: "2024-03-01"
```

### Feature flags:

```java
@ConditionalOnProperty(
    name = "legacy.api.endpoints.coins.enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
```

## 🚀 План реализации

### Неделя 1-2: Подготовка инфраструктуры
- [ ] Создать систему мониторинга legacy endpoints
- [ ] Внедрить middleware для добавления deprecation заголовков
- [ ] Настроить метрики и дашборды

### Неделя 3-4: Мягкая депрекация
- [ ] Добавить deprecation заголовки во все legacy endpoints
- [ ] Запустить логирование использования
- [ ] Отправить уведомления потребителям API

### Месяц 2-3: Интенсивные предупреждения
- [ ] Добавить rate limiting для legacy endpoints
- [ ] Внедрить автоматические редиректы
- [ ] Усилить коммуникацию с пользователями

### Месяц 4-6: Жесткая депрекация
- [ ] Отключить legacy endpoints по умолчанию
- [ ] Возвращать HTTP 410 Gone для неактивных endpoints
- [ ] Требовать специальную конфигурацию для активации

### Месяц 7-12: Полное удаление
- [ ] Удалить legacy controllers из кода
- [ ] Очистить зависимости и тесты
- [ ] Обновить документацию

## ✅ Критерии успеха

1. **Снижение использования legacy API на 90%** в течение 6 месяцев
2. **Нулевые critical ошибки** при миграции на новые endpoints
3. **Положительная обратная связь** от потребителей API
4. **Улучшение производительности** системы на 20%
5. **Сокращение технического долга** на 50%

## 📞 Контакты

- **Technical Lead:** Команда разработки DDD архитектуры
- **Product Owner:** API Product Team
- **Support:** api-support@company.com
- **Documentation:** docs.api.company.com

---

*Документ будет обновляться по мере выполнения плана депрекации.* 