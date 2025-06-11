# 📊 Анализ текущей API структуры CT.01

## 🎯 Обзор проекта

**Общая статистика:**
- **Контроллеров:** 12 активных REST контроллеров
- **Java классов:** ~111 классов
- **API версия:** v1 (единообразная)
- **Архитектура:** Монолитная слоевая
- **Технологии:** Java 17 + Spring Boot 3.4.5

## 📋 Детальная карта контроллеров и эндпоинтов

### 🔐 Аутентификация и безопасность
| Контроллер | Базовый путь | Эндпоинты | Статус | Целевой модуль |
|------------|--------------|-----------|--------|----------------|
| `AuthController` | `/api/v1/auth` | 4 | ✅ Активен | `user` |

**Эндпоинты:**
- `POST /api/v1/auth/login` - Аутентификация
- `POST /api/v1/auth/logout` - Выход
- `POST /api/v1/auth/register` - Регистрация  
- `GET /api/v1/auth/csrf-token` - CSRF токен
- `POST /api/v1/auth/telegram` - Telegram авторизация

### 👥 Управление пользователями
| Контроллер | Базовый путь | Эндпоинты | Статус | Целевой модуль |
|------------|--------------|-----------|--------|----------------|
| `UserManagementController` | `/api/v1/users` | 6 | ✅ Активен | `user` |
| `SettingsController` | `/api/v1/settings` | 4 | ✅ Активен | `user` |

**UserManagementController эндпоинты:**
- `GET /api/v1/users` - Список пользователей (admin)
- `POST /api/v1/users` - Создать пользователя (admin)
- `GET /api/v1/users/{userId}` - Профиль пользователя
- `PUT /api/v1/users/{userId}` - Обновить пользователя
- `DELETE /api/v1/users/{userId}` - Удалить пользователя (admin)
- `POST /api/v1/users/{userId}/roles` - Управление ролями (admin)

**SettingsController эндпоинты:**
- `GET /api/v1/settings` - Получить настройки
- `PUT /api/v1/settings` - Обновить настройки
- `GET /api/v1/settings/user/{userId}` - Настройки пользователя (admin)
- `PUT /api/v1/settings/user/{userId}` - Обновить настройки пользователя (admin)

### 💳 Подписки и биллинг
| Контроллер | Базовый путь | Эндпоинты | Статус | Целевой модуль |
|------------|--------------|-----------|--------|----------------|
| `SubscriptionController` | `/api/v1/subscriptions` | 8 | ✅ Активен | `subscription` |

**Эндпоинты:**
- `GET /api/v1/subscriptions/plans` - Доступные планы
- `GET /api/v1/subscriptions/current` - Текущая подписка
- `POST /api/v1/subscriptions/upgrade` - Обновить подписку
- `GET /api/v1/subscriptions/limits/{moduleName}` - Лимиты модуля
- `GET /api/v1/subscriptions/check/{moduleName}/{resourceType}` - Проверить доступность
- `GET /api/v1/subscriptions/usage` - Использование лимитов
- `POST /api/v1/subscriptions/usage/reset` - Сброс использования (admin)
- `GET /api/v1/subscriptions/history` - История подписок

### 🪙 Криптовалюты (основной домен)
| Контроллер | Базовый путь | Эндпоинты | Статус | Целевой модуль |
|------------|--------------|-----------|--------|----------------|
| `CoinDataController` | `/api/v1/coins` | 6 | ✅ Активен | `crypto` |
| `TrackedCoinController` | `/api/v1/tracked-coins` | 5 | ⚠️ Конфликт | `crypto` |

**CoinDataController эндпоинты:**
- `GET /api/v1/coins` - Список всех монет (с пагинацией)
- `GET /api/v1/coins/{symbol}` - Информация о монете
- `GET /api/v1/coins/{symbol}/history` - История цен
- `POST /api/v1/coins/bulk-prices` - Групповое получение цен
- `GET /api/v1/coins/popular` - Популярные монеты
- `GET /api/v1/coins/search` - Поиск монет

**TrackedCoinController эндпоинты (конфликтует):**
- `GET /api/v1/tracked-coins` - Отслеживаемые монеты
- `POST /api/v1/tracked-coins` - Добавить в отслеживание
- `GET /api/v1/tracked-coins/{id}` - Детали отслеживания
- `PUT /api/v1/tracked-coins/{id}` - Обновить отслеживание
- `DELETE /api/v1/tracked-coins/{id}` - Удалить из отслеживания

### 🏪 Биржи и рыночные данные
| Контроллер | Базовый путь | Эндпоинты | Статус | Целевой модуль |
|------------|--------------|-----------|--------|----------------|
| `ExchangeController` | `/api/v1/exchanges` | 6 | ✅ Активен | `market` |
| `PriceUpdateController` | `/api/v1/prices` | 3 | ✅ Активен | `market` |

**ExchangeController эндпоинты:**
- `GET /api/v1/exchanges` - Список бирж (с пагинацией)
- `GET /api/v1/exchanges/active` - Активные биржи
- `GET /api/v1/exchanges/{name}` - Информация о бирже
- `GET /api/v1/exchanges/{name}/status` - Статус биржи
- `GET /api/v1/exchanges/{name}/pairs` - Торговые пары
- `GET /api/v1/exchanges/{name}/stats` - Статистика биржи

**PriceUpdateController эндпоинты:**
- `POST /api/v1/prices/update` - Обновить цены (admin)
- `GET /api/v1/prices/status` - Статус обновлений
- `GET /api/v1/prices/errors` - Ошибки обновления

### 🔔 Уведомления и реальное время
| Контроллер | Базовый путь | Эндпоинты | Статус | Целевой модуль |
|------------|--------------|-----------|--------|----------------|
| `WebSocketStatsController` | `/api/v1/websocket` | 3 | ✅ Активен | `notification` |

**Эндпоинты:**
- `GET /api/v1/websocket/stats` - Статистика WebSocket
- `GET /api/v1/websocket/connections` - Активные соединения
- `POST /api/v1/websocket/broadcast` - Отправить сообщение (admin)

### ⚙️ Администрирование
| Контроллер | Базовый путь | Эндпоинты | Статус | Целевой модуль |
|------------|--------------|-----------|--------|----------------|
| `SystemController` | `/api/v1/system` | 2 | ✅ Активен | `admin` |
| `ApiAnalysisController` | `/api/v1/analysis` | 3 | ✅ Активен | `admin` |

**SystemController эндпоинты:**
- `GET /api/v1/system/info` - Информация о системе
- `GET /api/v1/system/health` - Состояние здоровья

**ApiAnalysisController эндпоинты:**
- `GET /api/v1/analysis/discover` - Обнаружение API (admin)
- `POST /api/v1/analysis/discover/save` - Сохранить анализ (admin)  
- `GET /api/v1/analysis/report` - Отчет анализа (admin)

## 🔍 Анализ проблем и возможностей

### ❌ Выявленные проблемы

1. **Конфликт маршрутов:**
   - `TrackedCoinController` конфликтует с `CoinDataController`
   - Неясное разделение между "coins" и "tracked-coins"

2. **Неоднородное именование:**
   - `/coins` vs `/tracked-coins` vs `/prices` - нет единой концепции
   - `/websocket` vs `/analysis` - разные стили именования

3. **Смешение доменов:**
   - Пользователи и настройки разделены на разные контроллеры без причины
   - Цены отделены от общего market контекста

4. **Отсутствие иерархии:**
   - Все на одном уровне `/api/v1/` без группировки по доменам

### ✅ Сильные стороны

1. **Единообразная версионность:** Все используют `/api/v1/`
2. **RESTful методы:** Правильное использование HTTP методов
3. **Аннотации безопасности:** Правильно настроены `@PreAuthorize`
4. **Swagger документация:** Хорошо аннотированы OpenAPI
5. **Пагинация:** Используется `Pageable` где нужно

## 📈 Карта миграции по доменам

### 🎯 Целевая структура модулей

| Модуль | Текущие контроллеры | Новые маршруты |
|--------|-------------------|----------------|
| **user** | `AuthController`, `UserManagementController`, `SettingsController` | `/api/v1/auth/*`, `/api/v1/users/*` |
| **subscription** | `SubscriptionController` | `/api/v1/subscriptions/*` |
| **crypto** | `CoinDataController`, `TrackedCoinController` | `/api/v1/crypto/coins/*`, `/api/v1/crypto/tracking/*` |
| **market** | `ExchangeController`, `PriceUpdateController` | `/api/v1/exchanges/*`, `/api/v1/market/*` |
| **notification** | `WebSocketStatsController` | `/api/v1/realtime/*` |
| **admin** | `SystemController`, `ApiAnalysisController` | `/api/v1/admin/*` |

### 🔄 План миграции API маршрутов

#### Приоритет 1: Crypto модуль (пилотный)
```
Старые → Новые
/api/v1/coins → /api/v1/crypto/coins
/api/v1/tracked-coins → /api/v1/crypto/tracking  
/api/v1/prices → /api/v1/market/prices
```

#### Приоритет 2: Market модуль
```
/api/v1/exchanges → /api/v1/exchanges (без изменений)
/api/v1/prices → /api/v1/market/prices
```

#### Приоритет 3: Admin модуль
```
/api/v1/system → /api/v1/admin/system
/api/v1/analysis → /api/v1/admin/analysis
/api/v1/websocket → /api/v1/realtime/websocket
```

## 📊 Статистика зависимостей

### Межмодульные зависимости (ожидаемые):
- **crypto** ↔ **market** (цены и биржи)
- **user** ↔ **subscription** (лимиты пользователей)
- **crypto** ↔ **notification** (уведомления о ценах)
- **admin** → все модули (мониторинг)

### Разделяемые компоненты:
- Security (JWT, аутентификация)
- Exception handling
- Logging и monitoring
- Validation
- Cache management

## 🚀 Следующие шаги

1. ✅ **Анализ завершен** - документированы все контроллеры и эндпоинты
2. ⏭️ **Создание модульной структуры** - определить пакеты и границы
3. ⏭️ **Пилотная реализация crypto модуля** - проверить подход
4. ⏭️ **Итеративная миграция** остальных модулей

---
*Этот анализ служит основой для планирования детальной модуляризации проекта CT.01* 