# API Endpoints Analysis

## Активные контроллеры и их эндпоинты

### 1. AuthController
**Base path:** `/api/v1/auth`

### 2. CoinDataController  
**Base path:** `/api/v1/coins`

### 3. ExchangeController
**Base path:** `/api/v1/exchanges`

### 4. SubscriptionController
**Base path:** `/api/v1/subscriptions`

### 5. UserManagementController
**Base path:** `/api/v1/users`

### 6. SettingsController
**Base path:** `/api/v1/settings`

### 7. SystemController
**Base path:** `/api/v1/system`

### 8. ApiAnalysisController
**Base path:** `/api/v1/analysis`

### 9. PriceUpdateController
**Base path:** `/api/v1/price-updates`

### 10. WebSocketStatsController
**Base path:** `/api/v1/websocket`

### 11. TestController
**Base path:** `/api/v1/test`

### 12. TrackedCoinController (ОТКЛЮЧЕН)
**Base path:** `/api/v1/tracked-coins` - КОНФЛИКТУЕТ С КОЙНДАТАКОНТРОЛЛЕРОМ

## Анализ конфликтов

### Подробное сравнение проблемных контроллеров:

**CoinDataController:**
- Base path: `/api/v1/coins`
- Метод: `getAllCoins(Pageable pageable)` 
- Full path: `GET /api/v1/coins`

**TrackedCoinController (отключен):**
- Base path: `/api/v1/tracked-coins`
- Метод: `getAllTrackedCoins(Pageable pageable)`
- Full path: `GET /api/v1/tracked-coins` (должен быть)

**ПРОБЛЕМА:** В логах Spring утверждает, что TrackedCoinController пытается занять `/api/v1/coins`, но в коде видно `/api/v1/tracked-coins`.

**Возможные причины:**
1. Кэш Spring не обновился после изменений
2. Где-то есть дубликат кода или аннотации
3. Spring все еще видит отключенный контроллер как активный
4. Ошибка в компиляции - старая версия класса в classpath

## РЕШЕНИЕ ✅

**Проблема была решена полной очисткой и пересборкой:**

1. Удалили папку `target/` в backend для очистки скомпилированных классов
2. Выполнили `docker-compose down` 
3. Пересобрали с `docker-compose build --no-cache`
4. Запустили `docker-compose up`

**Результат:** Все контейнеры работают нормально!

**Причина проблемы:** Spring загружал старую скомпилированную версию `TrackedCoinController` из кэша, где видимо была другая конфигурация маршрутов.

**Вывод:** При изменении аннотаций Spring контроллеров всегда нужно делать полную очистку и пересборку. 