# API Documentation

## Overview

This document describes the REST API endpoints for the CT.01 cryptocurrency tracking application.

## Base URL

```
http://localhost:8080/api
```

## Authentication

The API supports two authentication methods:

1. **Traditional Login** - Username/password authentication
2. **Telegram Authentication** - OAuth-like flow with Telegram Bot API

All authenticated endpoints require a valid session.

## Rate Limiting

- **Authentication endpoints**: 5 attempts per minute per IP
- **General API endpoints**: Configurable per endpoint
- **Exchange data endpoints**: Respect external API rate limits

## Common Response Formats

### Success Response
```json
{
  "data": { ... },
  "message": "Success message",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### Error Response
```json
{
  "error": "Error description",
  "message": "Detailed error message",
  "timestamp": "2024-01-01T12:00:00Z",
  "path": "/api/endpoint"
}
```

## Authentication Endpoints

### Get CSRF Token
```http
GET /api/auth/csrf
```

**Description**: Retrieve CSRF token for form submissions.

**Response**:
```json
{
  "token": "csrf-token-value",
  "headerName": "X-XSRF-TOKEN"
}
```

### Login
```http
POST /api/auth/login
```

**Description**: Authenticate user with username and password.

**Request Body**:
```json
{
  "username": "user@example.com",
  "password": "password123"
}
```

**Response**: 
- `200 OK` - Authentication successful
- `401 Unauthorized` - Invalid credentials
- `429 Too Many Requests` - Rate limit exceeded

### Get Current User
```http
GET /api/auth/me
```

**Description**: Get information about the currently authenticated user.

**Response**:
```json
{
  "username": "user@example.com",
  "email": "user@example.com",
  "roles": ["USER", "ADMIN"]
}
```

### Logout
```http
POST /api/auth/logout
```

**Description**: Invalidate current session and log out user.

**Response**: `200 OK`

### Telegram Authentication
```http
POST /api/auth/telegram
```

**Description**: Authenticate user via Telegram Bot API.

**Request Body**:
```json
{
  "id": 123456789,
  "first_name": "John",
  "last_name": "Doe",
  "username": "johndoe",
  "photo_url": "https://...",
  "auth_date": 1640995200,
  "hash": "telegram-auth-hash"
}
```

**Response**:
```json
{
  "user": {
    "username": "johndoe",
    "email": null,
    "roles": ["USER"]
  },
  "message": "Успешная авторизация через Telegram"
}
```

## User Management Endpoints

### Get User Profile
```http
GET /api/users/profile
```

**Description**: Get current user's profile information.

**Authentication**: Required

**Response**:
```json
{
  "id": 1,
  "username": "user@example.com",
  "email": "user@example.com",
  "telegramId": 123456789,
  "createdAt": "2024-01-01T12:00:00Z",
  "roles": ["USER"]
}
```

### Update User Profile
```http
PUT /api/users/profile
```

**Description**: Update current user's profile.

**Authentication**: Required

**Request Body**:
```json
{
  "email": "newemail@example.com",
  "username": "newusername"
}
```

## Coin Management Endpoints

### Get All Coins
```http
GET /api/coins
```

**Description**: Retrieve list of all supported cryptocurrencies.

**Query Parameters**:
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort field (default: name)

**Response**:
```json
{
  "content": [
    {
      "id": 1,
      "symbol": "BTC",
      "name": "Bitcoin",
      "description": "The first cryptocurrency",
      "isActive": true,
      "createdAt": "2024-01-01T12:00:00Z"
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

### Get Coin by ID
```http
GET /api/coins/{id}
```

**Description**: Get detailed information about a specific coin.

**Path Parameters**:
- `id`: Coin ID

**Response**:
```json
{
  "id": 1,
  "symbol": "BTC",
  "name": "Bitcoin",
  "description": "The first cryptocurrency",
  "isActive": true,
  "exchanges": [
    {
      "id": 1,
      "name": "Binance",
      "isActive": true
    }
  ],
  "createdAt": "2024-01-01T12:00:00Z"
}
```

### Create Coin
```http
POST /api/coins
```

**Description**: Create a new cryptocurrency entry.

**Authentication**: Required (ADMIN role)

**Request Body**:
```json
{
  "symbol": "ETH",
  "name": "Ethereum",
  "description": "Smart contract platform"
}
```

## Exchange Endpoints

### Get All Exchanges
```http
GET /api/exchanges
```

**Description**: Get list of all supported exchanges.

**Response**:
```json
[
  {
    "id": 1,
    "name": "Binance",
    "apiUrl": "https://api.binance.com",
    "isActive": true,
    "supportedFeatures": ["SPOT", "FUTURES", "WEBSOCKET"],
    "rateLimits": {
      "requestsPerMinute": 1200,
      "requestsPerSecond": 20
    }
  }
]
```

### Get Exchange Health
```http
GET /api/exchanges/{exchangeId}/health
```

**Description**: Check health status of a specific exchange.

**Path Parameters**:
- `exchangeId`: Exchange ID

**Response**:
```json
{
  "exchangeId": 1,
  "exchangeName": "Binance",
  "status": "HEALTHY",
  "responseTime": 150,
  "lastChecked": "2024-01-01T12:00:00Z",
  "details": {
    "apiStatus": "OK",
    "websocketStatus": "CONNECTED"
  }
}
```

## Price Data Endpoints

### Get Current Prices
```http
GET /api/prices/current
```

**Description**: Get current prices for specified coins and exchanges.

**Query Parameters**:
- `coins`: Comma-separated list of coin symbols (e.g., "BTC,ETH")
- `exchanges`: Comma-separated list of exchange names (optional)

**Response**:
```json
{
  "BTC": {
    "Binance": {
      "price": 45000.50,
      "volume": 1234567.89,
      "change24h": 2.5,
      "timestamp": "2024-01-01T12:00:00Z"
    },
    "Bybit": {
      "price": 45010.25,
      "volume": 987654.32,
      "change24h": 2.6,
      "timestamp": "2024-01-01T12:00:00Z"
    }
  }
}
```

### Get Price History
```http
GET /api/prices/history
```

**Description**: Get historical price data for a coin.

**Query Parameters**:
- `coin`: Coin symbol (required)
- `exchange`: Exchange name (required)
- `from`: Start date (ISO 8601 format)
- `to`: End date (ISO 8601 format)
- `interval`: Time interval (1m, 5m, 1h, 1d, etc.)

**Response**:
```json
{
  "coin": "BTC",
  "exchange": "Binance",
  "interval": "1h",
  "data": [
    {
      "timestamp": "2024-01-01T12:00:00Z",
      "open": 44900.00,
      "high": 45100.00,
      "low": 44800.00,
      "close": 45000.50,
      "volume": 123.45
    }
  ]
}
```

## User Preferences Endpoints

### Get User Coin Preferences
```http
GET /api/preferences/coins
```

**Description**: Get current user's coin tracking preferences.

**Authentication**: Required

**Response**:
```json
[
  {
    "id": 1,
    "coin": {
      "id": 1,
      "symbol": "BTC",
      "name": "Bitcoin"
    },
    "exchanges": ["Binance", "Bybit"],
    "alertSettings": {
      "priceAlerts": true,
      "volumeAlerts": false,
      "thresholds": {
        "priceUp": 5.0,
        "priceDown": -5.0
      }
    },
    "isActive": true
  }
]
```

### Update Coin Preferences
```http
PUT /api/preferences/coins/{coinId}
```

**Description**: Update tracking preferences for a specific coin.

**Authentication**: Required

**Path Parameters**:
- `coinId`: Coin ID

**Request Body**:
```json
{
  "exchanges": ["Binance", "OKX"],
  "alertSettings": {
    "priceAlerts": true,
    "volumeAlerts": true,
    "thresholds": {
      "priceUp": 10.0,
      "priceDown": -10.0
    }
  },
  "isActive": true
}
```

## Settings Endpoints

### Get Application Settings
```http
GET /api/settings
```

**Description**: Get application-wide settings.

**Authentication**: Required (ADMIN role)

**Response**:
```json
{
  "id": 1,
  "key": "default_refresh_interval",
  "value": "30",
  "description": "Default refresh interval in seconds",
  "category": "SYSTEM",
  "isActive": true
}
```

### Update Setting
```http
PUT /api/settings/{settingId}
```

**Description**: Update a specific application setting.

**Authentication**: Required (ADMIN role)

**Path Parameters**:
- `settingId`: Setting ID

**Request Body**:
```json
{
  "value": "60",
  "description": "Updated refresh interval"
}
```

## WebSocket Endpoints

### Real-time Price Updates
```
ws://localhost:8080/ws/prices
```

**Description**: WebSocket endpoint for real-time price updates.

**Authentication**: Required

**Message Format**:
```json
{
  "type": "PRICE_UPDATE",
  "data": {
    "coin": "BTC",
    "exchange": "Binance",
    "price": 45000.50,
    "volume": 1234567.89,
    "change24h": 2.5,
    "timestamp": "2024-01-01T12:00:00Z"
  }
}
```

**Subscription Message**:
```json
{
  "action": "SUBSCRIBE",
  "coins": ["BTC", "ETH"],
  "exchanges": ["Binance", "Bybit"]
}
```

**Unsubscription Message**:
```json
{
  "action": "UNSUBSCRIBE",
  "coins": ["BTC"],
  "exchanges": ["Binance"]
}
```

## Error Codes

| Code | Description |
|------|-------------|
| 400 | Bad Request - Invalid request format or parameters |
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 429 | Too Many Requests - Rate limit exceeded |
| 500 | Internal Server Error - Server error |

## Validation Rules

### User Input Validation
- **Email**: Valid email format, max 255 characters
- **Username**: 3-50 characters, alphanumeric and underscore only
- **Password**: Minimum 8 characters, at least one uppercase, lowercase, and number
- **Coin Symbol**: 2-10 characters, uppercase letters only
- **Coin Name**: 1-100 characters

### Request Limits
- **Maximum page size**: 100 items
- **Maximum coins per request**: 50
- **Maximum exchanges per request**: 10

## OpenAPI/Swagger Documentation

Interactive API documentation is available at:
```
http://localhost:8080/swagger-ui.html
```

OpenAPI specification:
```
http://localhost:8080/v3/api-docs
``` 