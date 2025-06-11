# 📊 Анализ миграции API контроллеров: Legacy vs DDD

## 🎯 Цель анализа
Определить недостающие API контроллеры и endpoints в новой DDD архитектуре (com.ct01) по сравнению со старым кодом (alg.coyote001)

## 📋 Старые контроллеры (alg.coyote001)

### ✅ **Уже реализованы в DDD:**

#### 1. **CoinDataController** → **CoinApiController** ✅
- **Старый путь:** `/api/v1/coins`
- **Новый путь:** `/api/v1/crypto/coins`
- **Endpoints покрыты:**
  - ✅ `GET /` → `GET /`
  - ✅ `GET /{symbol}` → `GET /{symbol}`
  - ✅ `GET /{symbol}/price` → `GET /{symbol}/prices`
  - ✅ `GET /search` → `GET /search`
  - ✅ `GET /exchange/{exchangeName}` → `GET /exchanges/{exchange}/symbols`

#### 2. **TrackedCoinController** → **TrackedCoinApiController** ✅ 
- **Старый путь:** `/api/v1/tracked-coins`
- **Новый путь:** `/api/v1/crypto/tracked-coins`
- **Endpoints покрыты:**
  - ✅ `GET /` → `GET /`
  - ✅ `GET /{id}` → `GET /{id}`
  - ✅ `GET /symbol/{symbol}` → `GET /symbol/{symbol}`
  - ✅ `GET /exchange/{exchange}` → `GET /exchange/{exchange}`
  - ✅ `POST /` → `POST /`

#### 3. **PriceUpdateController** → **PriceApiController** ✅
- **Старый путь:** `/api/v1/prices`
- **Новый путь:** `/api/v1/crypto/prices`
- **Endpoints покрыты:**
  - ✅ `GET /{symbol}/price` → `GET /current/{symbol}`
  - ✅ `GET /{symbol}/history` → `GET /historical/{symbol}`

#### 4. **HealthController** ✅
- **Новый путь:** `/api/v1/core/health`
- ✅ `GET /ddd` - новый endpoint для DDD статуса

---

## ❌ **НЕДОСТАЮЩИЕ контроллеры в DDD:**

### 🔴 **КРИТИЧЕСКИ ВАЖНЫЕ:**

#### 1. **AuthController** ❌
- **Путь:** `/api/v1/auth`
- **Endpoints:** 5 endpoints
  - `GET /csrf`
  - `POST /login`
  - `POST /register`
  - `GET /me`
  - `POST /logout`
  - `POST /telegram`
- **Модуль:** Нужен **User Bounded Context**

#### 2. **UserManagementController** ❌
- **Путь:** `/api/v1/users`
- **Endpoints:** 14 endpoints
  - `GET /` - список пользователей
  - `GET /{userId}` - пользователь по ID
  - `POST /` - создание пользователя
  - `PUT /{userId}` - обновление пользователя
  - `DELETE /{userId}` - удаление пользователя
  - `POST /{userId}/roles/{roleId}` - назначение роли
  - `DELETE /{userId}/roles/{roleId}` - снятие роли
  - `GET /roles` - список ролей
  - `POST /roles` - создание роли
  - `PUT /roles/{roleId}` - обновление роли
  - `DELETE /roles/{roleId}` - удаление роли
  - `GET /statistics` - статистика пользователей
  - `POST /{userId}/change-password` - смена пароля
  - `PUT /{userId}/upgrade-to-premium` - апгрейд до премиум
- **Модуль:** **User Bounded Context** + **Admin Bounded Context**

#### 3. **SubscriptionController** ❌
- **Путь:** `/api/v1/subscriptions`
- **Endpoints:** 6 endpoints
  - `GET /plans` - планы подписки
  - `GET /status` - статус подписки
  - `POST /upgrade` - апгрейд подписки
  - `POST /cancel` - отмена подписки
  - `GET /limits/{moduleName}` - лимиты по модулю
  - `GET /check/{moduleName}/{resourceType}` - проверка лимитов
- **Модуль:** **Subscription Bounded Context**

### 🟡 **ВАЖНЫЕ:**

#### 4. **SettingsController** ❌
- **Путь:** `/api/v1/settings`
- **Endpoints:** 11 endpoints
  - `GET /` - все настройки
  - `GET /{category}` - настройки категории
  - `PUT /{category}` - обновление категории
  - `GET /trading` - торговые настройки
  - `PUT /trading` - обновление торговых настроек
  - `GET /exchanges` - настройки бирж
  - `PUT /exchanges` - обновление настроек бирж
  - `GET /notifications` - настройки уведомлений
  - `PUT /notifications` - обновление уведомлений
  - `GET /system` - системные настройки
  - `PUT /system` - обновление системных настроек
  - `POST /{category}/reset` - сброс категории
  - `GET /permissions/{category}` - права доступа
- **Модуль:** **User Bounded Context** + **Core**

#### 5. **ExchangeController** ❌
- **Путь:** `/api/v1/exchanges`
- **Endpoints:** 8 endpoints
  - `GET /` - список бирж
  - `GET /active` - активные биржи
  - `GET /{name}` - информация о бирже
  - `GET /{name}/status` - статус биржи
  - `GET /{name}/pairs` - торговые пары биржи
  - `GET /{name}/stats` - статистика биржи
  - `GET /{name}/rate-limits` - лимиты API биржи
- **Модуль:** **Market Bounded Context**

#### 6. **SystemController** ❌
- **Путь:** `/api/v1/system`
- **Endpoints:** 1 endpoint
  - `GET /statistics` - системная статистика
- **Модуль:** **Core** или **Admin**

### 🟢 **ВСПОМОГАТЕЛЬНЫЕ:**

#### 7. **ApiAnalysisController** ❌
- **Путь:** `/api/v1/analysis`
- **Endpoints:** 6 endpoints
  - `GET /discover` - обнаружение API
  - `POST /discover/save` - сохранение обнаруженных API
  - `GET /discover/json` - JSON обнаруженных API
  - `GET /summary` - сводка анализа
  - `GET /health` - здоровье анализа
  - `GET /discover/test` - тестирование обнаружения
- **Модуль:** **Analysis Bounded Context**

#### 8. **WebSocketStatsController** ❌
- **Путь:** `/api/v1/websocket`
- **Endpoints:** 2 endpoints
  - `GET /stats` - статистика WebSocket
  - `POST /broadcast-test` - тест широковещания
- **Модуль:** **Core** (Infrastructure)

#### 9. **TestController** ❌
- **Путь:** `/api/test`
- **Endpoints:** 2 endpoints
  - `GET /password-check` - проверка пароля
  - `GET /encode-password` - кодирование пароля
- **Модуль:** **Core** (только для dev)

---

## 📊 **Статистика покрытия:**

### **Общие цифры:**
- **Всего старых контроллеров:** 12
- **Уже реализовано в DDD:** 4 (33%)
- **Недостающих:** 8 (67%)

### **По endpoints:**
- **Всего старых endpoints:** ~75
- **Реализовано в DDD:** ~20 (27%)
- **Недостающих:** ~55 (73%)

### **По приоритету:**
- **🔴 Критически важные:** 3 контроллера (25 endpoints)
- **🟡 Важные:** 3 контроллера (20 endpoints)  
- **🟢 Вспомогательные:** 3 контроллера (10 endpoints)

---

## 🎯 **Рекомендации по приоритизации:**

### **Фаза 1 - Критически важные (1-2 недели):**
1. **AuthController** - аутентификация
2. **UserManagementController** - управление пользователями
3. **SubscriptionController** - подписки

### **Фаза 2 - Важные (1 неделя):**
4. **SettingsController** - настройки
5. **ExchangeController** - биржи
6. **SystemController** - система

### **Фаза 3 - Вспомогательные (3-5 дней):**
7. **ApiAnalysisController** - анализ
8. **WebSocketStatsController** - WebSocket
9. **TestController** - только если нужен в production

---

## 📝 **Следующие шаги:**
1. ✅ Создать этот анализ
2. ⏭️ **Начать с AuthController** в User Bounded Context
3. ⏭️ Реализовать User API endpoints
4. ⏭️ Создать Subscription API endpoints
5. ⏭️ Постепенно мигрировать остальные контроллеры 