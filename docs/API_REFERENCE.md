# API Reference

<div align="center">

![API Version](https://img.shields.io/badge/API-v1.0-blue)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0-green)
![Status](https://img.shields.io/badge/Status-Production-success)

**Полная документация REST API для платформы CT.01**

</div>

## 📖 Содержание

- [Аутентификация](#-аутентификация)
- [Управление пользователями](#-управление-пользователями)
- [Система подписок](#-система-подписок)
- [Криптовалютные данные](#-криптовалютные-данные)
- [Биржи и обмены](#-биржи-и-обмены)
- [Отслеживание монет](#-отслеживание-монет)
- [Настройки](#-настройки)
- [WebSocket API](#-websocket-api)
- [Администрирование](#-администрирование)
- [Коды ответов](#-коды-ответов)
- [Примеры использования](#-примеры-использования)

## 🔐 Аутентификация

### Base URL
```
Production: https://ct01.herokuapp.com/api/v1
Development: http://localhost:8080/api/v1
```

### Типы аутентификации
1. **JWT Bearer Token** - для API запросов
2. **Telegram Auth** - для входа через Telegram
3. **Session Cookie** - для web интерфейса

### Получение токена

#### `POST /auth/telegram`
Аутентификация через Telegram

**Request Body:**
```json
{
  "id": "123456789",
  "first_name": "John",
  "username": "john_doe",
  "photo_url": "https://t.me/i/userpic/320/photo.jpg",
  "auth_date": "1640995200",
  "hash": "abc123def456"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "john_doe",
      "firstName": "John",
      "telegramId": "123456789",
      "roles": ["USER"],
      "subscription": {
        "planType": "FREE",
        "status": "ACTIVE"
      }
    }
  }
}
```

#### `POST /auth/refresh`
Обновление токена

**Headers:**
```
Authorization: Bearer <refresh_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

#### `POST /auth/logout`
Выход из системы

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Successfully logged out"
}
```

---

## 👥 Управление пользователями

### `GET /users/profile`
Получение профиля текущего пользователя

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "john_doe",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "telegramId": "123456789",
    "photoUrl": "https://t.me/i/userpic/320/photo.jpg",
    "roles": ["USER"],
    "subscription": {
      "planType": "PREMIUM",
      "status": "ACTIVE",
      "expiresAt": "2024-12-31T23:59:59Z",
      "usage": {
        "apiCalls": 150,
        "apiLimit": 1000,
        "trackedCoins": 25,
        "trackedCoinsLimit": 50
      }
    },
    "preferences": {
      "language": "ru",
      "notifications": true,
      "theme": "dark"
    },
    "createdAt": "2024-01-01T00:00:00Z",
    "lastLoginAt": "2024-01-15T10:30:00Z"
  }
}
```

### `PUT /users/profile`
Обновление профиля пользователя

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "preferences": {
    "language": "en",
    "notifications": false,
    "theme": "light"
  }
}
```

### `GET /users/stats`
Статистика использования для текущего пользователя

**Response:**
```json
{
  "success": true,
  "data": {
    "currentPeriod": {
      "apiCalls": 150,
      "trackedCoins": 25,
      "resetDate": "2024-02-01T00:00:00Z"
    },
    "limits": {
      "apiCallsLimit": 1000,
      "trackedCoinsLimit": 50,
      "realTimeUpdates": true,
      "advancedAnalytics": true
    },
    "history": {
      "totalApiCalls": 5280,
      "averageDaily": 42,
      "peakDaily": 156
    }
  }
}
```

---

## 💳 Система подписок

### `GET /subscriptions/plans`
Получение доступных планов подписки

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "planType": "FREE",
      "name": "Бесплатный",
      "description": "Базовый функционал для начинающих",
      "price": 0,
      "currency": "USD",
      "duration": "UNLIMITED",
      "features": {
        "apiCallsLimit": 100,
        "trackedCoinsLimit": 5,
        "realTimeUpdates": false,
        "advancedAnalytics": false,
        "prioritySupport": false
      }
    },
    {
      "planType": "PREMIUM",
      "name": "Премиум",
      "description": "Расширенные возможности для профессионалов",
      "price": 29.99,
      "currency": "USD",
      "duration": "MONTHLY",
      "features": {
        "apiCallsLimit": 10000,
        "trackedCoinsLimit": 100,
        "realTimeUpdates": true,
        "advancedAnalytics": true,
        "prioritySupport": true
      }
    }
  ]
}
```

### `GET /subscriptions/status`
Статус текущей подписки

**Response:**
```json
{
  "success": true,
  "data": {
    "planType": "PREMIUM",
    "status": "ACTIVE",
    "startDate": "2024-01-01T00:00:00Z",
    "expiresAt": "2024-02-01T00:00:00Z",
    "autoRenewal": true,
    "usage": {
      "apiCalls": 150,
      "apiLimit": 10000,
      "trackedCoins": 25,
      "trackedCoinsLimit": 100,
      "resetDate": "2024-02-01T00:00:00Z"
    },
    "nextBilling": {
      "date": "2024-02-01T00:00:00Z",
      "amount": 29.99,
      "currency": "USD"
    }
  }
}
```

### `POST /subscriptions/upgrade`
Обновление подписки

**Request Body:**
```json
{
  "planType": "PREMIUM",
  "paymentMethod": "CARD",
  "paymentToken": "tok_1234567890"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "subscriptionId": "sub_abc123def456",
    "planType": "PREMIUM",
    "status": "ACTIVE",
    "startDate": "2024-01-15T10:30:00Z",
    "expiresAt": "2024-02-15T10:30:00Z",
    "invoice": {
      "id": "inv_789xyz",
      "amount": 29.99,
      "currency": "USD",
      "downloadUrl": "https://ct01.com/invoices/inv_789xyz.pdf"
    }
  }
}
```

### `POST /subscriptions/cancel`
Отмена подписки

**Response:**
```json
{
  "success": true,
  "message": "Subscription cancelled successfully",
  "data": {
    "cancelledAt": "2024-01-15T10:30:00Z",
    "expiresAt": "2024-02-01T00:00:00Z",
    "refund": {
      "amount": 15.00,
      "currency": "USD",
      "processedAt": "2024-01-15T10:35:00Z"
    }
  }
}
```

---

## 💰 Криптовалютные данные

### `GET /coins`
Получение списка поддерживаемых криптовалют

**Query Parameters:**
- `search` (string) - поиск по названию или символу
- `limit` (integer) - количество результатов (по умолчанию 50)
- `offset` (integer) - смещение для пагинации

**Response:**
```json
{
  "success": true,
  "data": {
    "coins": [
      {
        "id": 1,
        "symbol": "BTC",
        "name": "Bitcoin",
        "logoUrl": "https://cryptoicons.org/api/color/btc/200",
        "marketCap": 850000000000,
        "volume24h": 25000000000,
        "price": 45000.50,
        "priceChange24h": 2.5,
        "priceChangePercent24h": 5.88,
        "rank": 1,
        "isTracked": false
      }
    ],
    "pagination": {
      "total": 250,
      "limit": 50,
      "offset": 0,
      "hasNext": true
    }
  }
}
```

### `GET /coins/{symbol}/price`
Получение цены конкретной криптовалюты

**Path Parameters:**
- `symbol` (string) - символ криптовалюты (например, BTC)

**Query Parameters:**
- `exchanges` (string) - список бирж через запятую (по умолчанию все)

**Response:**
```json
{
  "success": true,
  "data": {
    "symbol": "BTC",
    "prices": [
      {
        "exchange": "BINANCE",
        "price": 45000.50,
        "volume": 1250000000,
        "lastUpdate": "2024-01-15T10:30:00Z",
        "bid": 44999.00,
        "ask": 45001.00,
        "spread": 0.004
      },
      {
        "exchange": "OKEX",
        "price": 45005.20,
        "volume": 980000000,
        "lastUpdate": "2024-01-15T10:30:05Z",
        "bid": 45003.50,
        "ask": 45006.80,
        "spread": 0.007
      }
    ],
    "arbitrage": {
      "bestBuy": {
        "exchange": "BINANCE",
        "price": 44999.00
      },
      "bestSell": {
        "exchange": "OKEX", 
        "price": 45006.80
      },
      "profit": 7.80,
      "profitPercent": 0.017
    }
  }
}
```

### `GET /coins/{symbol}/history`
Исторические данные по цене

**Path Parameters:**
- `symbol` (string) - символ криптовалюты

**Query Parameters:**
- `period` (string) - период: 1h, 4h, 1d, 1w, 1m (по умолчанию 1d)
- `interval` (string) - интервал: 1m, 5m, 15m, 1h, 4h, 1d (по умолчанию 1h)
- `exchange` (string) - биржа (по умолчанию BINANCE)

**Response:**
```json
{
  "success": true,
  "data": {
    "symbol": "BTC",
    "exchange": "BINANCE",
    "period": "1d",
    "interval": "1h",
    "data": [
      {
        "timestamp": "2024-01-15T00:00:00Z",
        "open": 44800.00,
        "high": 45200.00,
        "low": 44600.00,
        "close": 45000.50,
        "volume": 125000000
      }
    ]
  }
}
```

---

## 🏛️ Биржи и обмены

### `GET /exchanges`
Получение списка поддерживаемых бирж

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Binance",
      "code": "BINANCE",
      "logoUrl": "https://public.bnbstatic.com/image/cms/blog/20201110/87c748c0-6c50-4c74-bb2e-c1194a681a7f.png",
      "website": "https://binance.com",
      "isActive": true,
      "supportedPairs": 1250,
      "fees": {
        "maker": 0.001,
        "taker": 0.001,
        "withdrawal": {
          "BTC": 0.0005,
          "ETH": 0.005
        }
      },
      "features": {
        "spot": true,
        "futures": true,
        "margin": true,
        "webSocket": true
      },
      "lastUpdate": "2024-01-15T10:30:00Z"
    }
  ]
}
```

### `GET /exchanges/{exchange}/pairs`
Получение торговых пар для биржи

**Path Parameters:**
- `exchange` (string) - код биржи

**Query Parameters:**
- `base` (string) - базовая валюта
- `quote` (string) - котируемая валюта

**Response:**
```json
{
  "success": true,
  "data": {
    "exchange": "BINANCE",
    "pairs": [
      {
        "symbol": "BTCUSDT",
        "baseAsset": "BTC",
        "quoteAsset": "USDT",
        "status": "TRADING",
        "price": 45000.50,
        "volume24h": 1250000000,
        "priceChange24h": 2.5,
        "priceChangePercent24h": 5.88,
        "high24h": 45500.00,
        "low24h": 44200.00,
        "fees": {
          "maker": 0.001,
          "taker": 0.001
        }
      }
    ]
  }
}
```

### `GET /exchanges/compare`
Сравнение цен между биржами

**Query Parameters:**
- `symbol` (string, required) - символ для сравнения
- `exchanges` (string) - список бирж через запятую

**Response:**
```json
{
  "success": true,
  "data": {
    "symbol": "BTC",
    "comparison": [
      {
        "exchange": "BINANCE",
        "price": 45000.50,
        "volume": 1250000000,
        "rank": 1
      },
      {
        "exchange": "OKEX",
        "price": 45005.20,
        "volume": 980000000,
        "rank": 2
      }
    ],
    "arbitrage": {
      "maxProfit": 7.80,
      "maxProfitPercent": 0.017,
      "bestBuyExchange": "BINANCE",
      "bestSellExchange": "OKEX"
    }
  }
}
```

---

## 📊 Отслеживание монет

### `GET /tracked-coins`
Получение отслеживаемых монет

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "coinId": 1,
      "coin": {
        "symbol": "BTC",
        "name": "Bitcoin",
        "logoUrl": "https://cryptoicons.org/api/color/btc/200"
      },
      "targetPrice": 50000.00,
      "priceAlert": "ABOVE",
      "isActive": true,
      "createdAt": "2024-01-01T00:00:00Z",
      "currentPrice": 45000.50,
      "priceChange": -10.0,
      "lastAlert": "2024-01-10T15:30:00Z"
    }
  ]
}
```

### `POST /tracked-coins`
Добавление монеты в отслеживание

**Request Body:**
```json
{
  "coinId": 1,
  "targetPrice": 50000.00,
  "priceAlert": "ABOVE",
  "notifications": {
    "telegram": true,
    "email": false,
    "push": true
  }
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "coinId": 1,
    "targetPrice": 50000.00,
    "priceAlert": "ABOVE",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

### `PUT /tracked-coins/{id}`
Обновление настроек отслеживания

**Path Parameters:**
- `id` (integer) - ID отслеживаемой монеты

**Request Body:**
```json
{
  "targetPrice": 52000.00,
  "priceAlert": "BELOW",
  "isActive": false
}
```

### `DELETE /tracked-coins/{id}`
Удаление из отслеживания

**Path Parameters:**
- `id` (integer) - ID отслеживаемой монеты

**Response:**
```json
{
  "success": true,
  "message": "Tracked coin removed successfully"
}
```

---

## ⚙️ Настройки

### `GET /settings`
Получение настроек системы

**Response:**
```json
{
  "success": true,
  "data": {
    "general": {
      "siteName": "CT.01",
      "defaultLanguage": "ru",
      "maintenanceMode": false
    },
    "features": {
      "telegramAuth": true,
      "subscriptions": true,
      "realTimeData": true
    },
    "limits": {
      "maxTrackedCoins": 100,
      "maxApiCallsPerHour": 1000,
      "maxConcurrentConnections": 50
    },
    "exchanges": {
      "enabled": ["BINANCE", "OKEX", "BYBIT"],
      "updateInterval": 5000
    }
  }
}
```

### `PUT /settings` 🔒
Обновление настроек (только для админов)

**Request Body:**
```json
{
  "general": {
    "siteName": "CT.01 Pro",
    "maintenanceMode": false
  },
  "limits": {
    "maxApiCallsPerHour": 2000
  }
}
```

---

## 🔄 WebSocket API

### Подключение
```
wss://ct01.herokuapp.com/ws/price-updates
```

### Аутентификация
```javascript
const ws = new WebSocket('wss://ct01.herokuapp.com/ws/price-updates');
ws.send(JSON.stringify({
  type: 'auth',
  token: 'Bearer eyJhbGciOiJIUzI1NiJ9...'
}));
```

### Подписка на обновления цен
```javascript
ws.send(JSON.stringify({
  type: 'subscribe',
  symbols: ['BTC', 'ETH', 'ADA'],
  exchanges: ['BINANCE', 'OKEX']
}));
```

### Пример сообщения о цене
```json
{
  "type": "price_update",
  "data": {
    "symbol": "BTC",
    "exchange": "BINANCE",
    "price": 45000.50,
    "volume": 1250000000,
    "timestamp": "2024-01-15T10:30:00Z",
    "change24h": 2.5,
    "changePercent24h": 5.88
  }
}
```

---

## 🛡️ Администрирование

### `GET /admin/users` 🔒
Получение списка пользователей (только для админов)

**Query Parameters:**
- `search` (string) - поиск по имени/email
- `role` (string) - фильтр по роли
- `status` (string) - фильтр по статусу
- `limit` (integer) - лимит результатов
- `offset` (integer) - смещение

**Response:**
```json
{
  "success": true,
  "data": {
    "users": [
      {
        "id": 1,
        "username": "john_doe",
        "firstName": "John",
        "email": "john@example.com",
        "roles": ["USER"],
        "status": "ACTIVE",
        "subscription": {
          "planType": "PREMIUM",
          "status": "ACTIVE"
        },
        "lastLoginAt": "2024-01-15T10:30:00Z",
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "pagination": {
      "total": 1250,
      "limit": 50,
      "offset": 0
    }
  }
}
```

### `POST /admin/users/{id}/roles` 🔒
Изменение ролей пользователя

**Path Parameters:**
- `id` (integer) - ID пользователя

**Request Body:**
```json
{
  "roles": ["USER", "ADMIN"]
}
```

### `GET /admin/stats` 🔒
Статистика системы

**Response:**
```json
{
  "success": true,
  "data": {
    "users": {
      "total": 1250,
      "active": 987,
      "premium": 234,
      "newToday": 12
    },
    "subscriptions": {
      "free": 1016,
      "premium": 234,
      "revenue": 6986.66
    },
    "system": {
      "uptime": "7d 12h 45m",
      "apiCalls": 125000,
      "activeConnections": 45,
      "cacheHitRate": 87.5
    }
  }
}
```

---

## 📋 Коды ответов

### Успешные ответы
- `200 OK` - Запрос выполнен успешно
- `201 Created` - Ресурс создан успешно
- `204 No Content` - Запрос выполнен, содержимое отсутствует

### Ошибки клиента (4xx)
- `400 Bad Request` - Некорректный запрос
- `401 Unauthorized` - Необходима аутентификация
- `403 Forbidden` - Доступ запрещен
- `404 Not Found` - Ресурс не найден
- `422 Unprocessable Entity` - Ошибка валидации
- `429 Too Many Requests` - Превышен лимит запросов

### Ошибки сервера (5xx)
- `500 Internal Server Error` - Внутренняя ошибка сервера
- `502 Bad Gateway` - Ошибка шлюза
- `503 Service Unavailable` - Сервис недоступен

### Формат ошибок
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "email",
        "message": "Email is required"
      }
    ],
    "timestamp": "2024-01-15T10:30:00Z",
    "path": "/api/v1/users/profile"
  }
}
```

---

## 💡 Примеры использования

### Получение цен и создание alert-а

```javascript
// 1. Аутентификация
const authResponse = await fetch('/api/v1/auth/telegram', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(telegramAuthData)
});
const { token } = await authResponse.json();

// 2. Получение цены Bitcoin
const priceResponse = await fetch('/api/v1/coins/BTC/price', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const priceData = await priceResponse.json();

// 3. Создание alert-а
const alertResponse = await fetch('/api/v1/tracked-coins', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    coinId: 1,
    targetPrice: 50000,
    priceAlert: 'ABOVE'
  })
});
```

### Подписка на real-time обновления

```javascript
const ws = new WebSocket('wss://ct01.herokuapp.com/ws/price-updates');

ws.onopen = () => {
  // Аутентификация
  ws.send(JSON.stringify({
    type: 'auth',
    token: `Bearer ${token}`
  }));
  
  // Подписка на BTC и ETH
  ws.send(JSON.stringify({
    type: 'subscribe',
    symbols: ['BTC', 'ETH']
  }));
};

ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  if (data.type === 'price_update') {
    console.log(`${data.data.symbol}: $${data.data.price}`);
  }
};
```

### Сравнение цен между биржами

```javascript
const compareResponse = await fetch('/api/v1/exchanges/compare?symbol=BTC', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const comparison = await compareResponse.json();

// Найти лучшую арбитражную возможность
if (comparison.data.arbitrage.maxProfitPercent > 0.5) {
  console.log(`Arbitrage opportunity: ${comparison.data.arbitrage.maxProfitPercent}%`);
}
```

---

## 🔗 Дополнительные ресурсы

- **[Postman Collection](./postman_collection.json)** - коллекция для тестирования API
- **[OpenAPI Specification](./openapi.yaml)** - полная спецификация OpenAPI 3.0
- **[SDK документация](./SDK.md)** - официальные SDK для популярных языков
- **[Changelog](./CHANGELOG.md)** - история изменений API

---

<div align="center">

**[⬆️ Вернуться к началу](#api-reference)**

Made with ❤️ by CT.01 Team

</div> 