# 🚀 API Migration Guide - Legacy to DDD Architecture

## 📋 Обзор

Этот guide поможет разработчикам мигрировать с legacy API (alg.coyote001) на новую DDD архитектуру (com.ct01).

## 🆚 Основные изменения

### 1. Изменения в путях endpoints

| Legacy Endpoint | Новый Endpoint | Статус |
|----------------|---------------|--------|
| `/api/v1/coins` | `/api/v1/crypto/coins` | ✅ Готов |
| `/api/v1/tracked-coins` | `/api/v1/crypto/tracked-coins` | ✅ Готов |
| `/api/v1/prices` | `/api/v1/crypto/prices` | ✅ Готов |
| `/api/v1/exchanges` | `/api/v1/crypto/exchanges` | ✅ Готов |
| `/api/v1/system` | `/api/v1/system` | ✅ Готов |
| `/api/v1/subscriptions` | `/api/v1/subscriptions` | ✅ Готов |
| `/api/v1/users` | `/api/v1/users` | ✅ Готов |
| `/api/v1/analysis` | `/api/v1/admin/analysis` | ✅ Готов |

### 2. Структурные изменения

#### Unified Response Format
Все новые endpoints возвращают единый формат ответа:

```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed successfully",
  "timestamp": "2024-01-15T10:30:00Z",
  "traceId": "abc123-def456-ghi789"
}
```

#### Error Response Format
```json
{
  "success": false,
  "error": "VALIDATION_ERROR",
  "message": "Invalid input parameters",
  "details": ["Field 'symbol' is required"],
  "timestamp": "2024-01-15T10:30:00Z",
  "traceId": "abc123-def456-ghi789"
}
```

## 🔧 Практические примеры миграции

### 1. Работа с монетами

#### Legacy
```bash
# Получить все монеты
GET /api/v1/coins

# Получить монету по символу
GET /api/v1/coins/BTC

# Получить цену монеты
GET /api/v1/coins/BTC/price
```

#### Новый API
```bash
# Получить все монеты
GET /api/v1/crypto/coins

# Получить монету по символу
GET /api/v1/crypto/coins/BTC

# Получить текущую цену
GET /api/v1/crypto/prices/current/BTC
```

#### Код миграции

**JavaScript/TypeScript:**

```typescript
// Legacy
class LegacyCoinService {
  async getCoin(symbol: string) {
    const response = await fetch(`/api/v1/coins/${symbol}`);
    return response.json(); // Прямой объект
  }
  
  async getCoinPrice(symbol: string) {
    const response = await fetch(`/api/v1/coins/${symbol}/price`);
    return response.json(); // Прямой объект с ценами
  }
}

// Новый API
class CoinService {
  async getCoin(symbol: string) {
    const response = await fetch(`/api/v1/crypto/coins/${symbol}`);
    const result = await response.json();
    return result.data; // Извлекаем data из unified response
  }
  
  async getCoinPrice(symbol: string) {
    const response = await fetch(`/api/v1/crypto/prices/current/${symbol}`);
    const result = await response.json();
    return result.data; // Извлекаем data из unified response
  }
}
```

**Java:**

```java
// Legacy
@Service
public class LegacyCoinService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public CoinDto getCoin(String symbol) {
        return restTemplate.getForObject("/api/v1/coins/" + symbol, CoinDto.class);
    }
    
    public Map<String, Object> getCoinPrice(String symbol) {
        return restTemplate.getForObject("/api/v1/coins/" + symbol + "/price", Map.class);
    }
}

// Новый API
@Service
public class CoinService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public CoinDto getCoin(String symbol) {
        ApiResponse<CoinDto> response = restTemplate.getForObject(
            "/api/v1/crypto/coins/" + symbol, 
            new ParameterizedTypeReference<ApiResponse<CoinDto>>() {}
        );
        return response.getData();
    }
    
    public PriceData getCoinPrice(String symbol) {
        ApiResponse<PriceData> response = restTemplate.getForObject(
            "/api/v1/crypto/prices/current/" + symbol, 
            new ParameterizedTypeReference<ApiResponse<PriceData>>() {}
        );
        return response.getData();
    }
}
```

**Python:**

```python
# Legacy
class LegacyCoinService:
    def __init__(self, base_url):
        self.base_url = base_url
    
    def get_coin(self, symbol):
        response = requests.get(f"{self.base_url}/api/v1/coins/{symbol}")
        return response.json()  # Прямой объект
    
    def get_coin_price(self, symbol):
        response = requests.get(f"{self.base_url}/api/v1/coins/{symbol}/price")
        return response.json()  # Прямой объект

# Новый API
class CoinService:
    def __init__(self, base_url):
        self.base_url = base_url
    
    def get_coin(self, symbol):
        response = requests.get(f"{self.base_url}/api/v1/crypto/coins/{symbol}")
        result = response.json()
        return result['data']  # Извлекаем data из unified response
    
    def get_coin_price(self, symbol):
        response = requests.get(f"{self.base_url}/api/v1/crypto/prices/current/{symbol}")
        result = response.json()
        return result['data']  # Извлекаем data из unified response
```

### 2. Работа с отслеживаемыми монетами

#### Legacy vs Новый API

```typescript
// Legacy
interface LegacyTrackedCoin {
  id: number;
  symbol: string;
  exchanges: string[];
  isActive: boolean;
}

// Новый API
interface TrackedCoin {
  id: string;
  symbol: string;
  exchanges: string[];
  active: boolean;
  priority: number;
  pollingInterval: number;
  createdAt: string;
  updatedAt: string;
}

// Миграция
class TrackedCoinMigration {
  migrateFromLegacy(legacy: LegacyTrackedCoin): TrackedCoin {
    return {
      id: legacy.id.toString(),
      symbol: legacy.symbol,
      exchanges: legacy.exchanges,
      active: legacy.isActive,
      priority: 1, // Значение по умолчанию
      pollingInterval: 60, // Значение по умолчанию
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
  }
}
```

### 3. Обработка ошибок

#### Legacy
```typescript
try {
  const coin = await fetch('/api/v1/coins/INVALID');
  // Legacy возвращал разные форматы ошибок
  if (!coin.ok) {
    throw new Error('Coin not found');
  }
} catch (error) {
  console.error('Error:', error.message);
}
```

#### Новый API
```typescript
try {
  const response = await fetch('/api/v1/crypto/coins/INVALID');
  const result = await response.json();
  
  if (!result.success) {
    throw new Error(result.message);
  }
  
  const coin = result.data;
} catch (error) {
  console.error('Error:', error.message);
}
```

## 🔍 Мониторинг миграции

### Отслеживание использования legacy API

Новая система автоматически отслеживает использование legacy endpoints:

```bash
# Получить статистику legacy API
GET /api/v1/admin/deprecation/stats

# Получить статистику конкретного endpoint
GET /api/v1/admin/deprecation/stats/endpoint?endpoint=/api/v1/coins&method=GET

# Экспортировать статистику в CSV
GET /api/v1/admin/deprecation/export/csv
```

### HTTP заголовки депрекации

Legacy endpoints автоматически добавляют заголовки:

```http
X-API-Deprecated: true
X-API-Deprecation-Date: 2024-01-01T00:00:00Z
X-API-Removal-Date: 2024-06-01T00:00:00Z
X-API-New-Endpoint: /api/v1/crypto/coins
Warning: 299 - "This API is deprecated. Use /api/v1/crypto/coins instead."
Sunset: 2024-06-01T00:00:00Z
```

## 📚 Adapter Pattern для плавной миграции

Используйте adapter pattern для постепенной миграции:

```typescript
class ApiAdapter {
  private useNewApi: boolean = true;
  
  async getCoin(symbol: string) {
    if (this.useNewApi) {
      try {
        return await this.newCoinService.getCoin(symbol);
      } catch (error) {
        console.warn('New API failed, falling back to legacy');
        return await this.legacyCoinService.getCoin(symbol);
      }
    } else {
      return await this.legacyCoinService.getCoin(symbol);
    }
  }
  
  enableNewApi() {
    this.useNewApi = true;
  }
  
  disableNewApi() {
    this.useNewApi = false;
  }
}
```

## ⚡ Feature Flags

Используйте feature flags для управления миграцией:

```typescript
class FeatureFlags {
  static USE_NEW_COIN_API = 'use_new_coin_api';
  static USE_NEW_PRICE_API = 'use_new_price_api';
  
  static isEnabled(flag: string): boolean {
    return localStorage.getItem(flag) === 'true';
  }
}

// Использование
if (FeatureFlags.isEnabled(FeatureFlags.USE_NEW_COIN_API)) {
  // Используем новый API
} else {
  // Используем legacy API
}
```

## 🧪 Тестирование миграции

### Параллельное тестирование

```typescript
class MigrationTester {
  async testEndpoint(symbol: string) {
    const [legacyResult, newResult] = await Promise.all([
      this.legacyService.getCoin(symbol),
      this.newService.getCoin(symbol)
    ]);
    
    // Сравниваем результаты
    const diff = this.compareResults(legacyResult, newResult.data);
    if (diff.length > 0) {
      console.warn('API migration differences:', diff);
    }
    
    return newResult.data;
  }
  
  private compareResults(legacy: any, newApi: any): string[] {
    const differences: string[] = [];
    
    // Сравниваем ключевые поля
    if (legacy.symbol !== newApi.symbol) {
      differences.push(`symbol: ${legacy.symbol} vs ${newApi.symbol}`);
    }
    
    return differences;
  }
}
```

## 📅 Timeline миграции

### Фаза 1: Подготовка (Текущая)
- [x] Новые endpoints доступны
- [x] Deprecation заголовки добавлены
- [x] Мониторинг внедрен
- [ ] Клиенты уведомлены

### Фаза 2: Активная миграция (1-3 месяца)
- [ ] Массовые уведомления клиентов
- [ ] Техническая поддержка миграции
- [ ] Документация и примеры обновлены

### Фаза 3: Принудительная миграция (3-6 месяцев)
- [ ] Rate limiting для legacy endpoints
- [ ] HTTP 410 Gone для неактивных endpoints
- [ ] Специальная конфигурация для legacy access

### Фаза 4: Полное удаление (6-12 месяцев)
- [ ] Удаление legacy controllers
- [ ] Очистка legacy кода
- [ ] Обновление документации

## 🆘 Поддержка и помощь

### Контакты
- **Email:** api-support@company.com
- **Slack:** #api-migration
- **Documentation:** https://docs.api.company.com/migration

### FAQ

**Q: Когда legacy API будет полностью отключен?**
A: Поэтапно в течение 12 месяцев. Точные даты указаны в HTTP заголовках `X-API-Removal-Date`.

**Q: Совместимы ли новые endpoints с legacy форматом данных?**
A: Данные совместимы, но формат ответа изменился на unified response format.

**Q: Можно ли использовать оба API одновременно?**
A: Да, в период миграции оба API работают параллельно.

**Q: Как отслеживать прогресс миграции?**
A: Используйте endpoints мониторинга `/api/v1/admin/deprecation/stats`.

**Q: Что делать если новый API работает некорректно?**
A: Сообщите в поддержку, временно можно использовать legacy API.

---

*Этот guide будет обновляться по мере продвижения миграции.* 