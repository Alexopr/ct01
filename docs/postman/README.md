# CT.01 API Testing with Postman

Этот каталог содержит Postman коллекции и environment'ы для тестирования CT.01 API.

## Файлы

### Коллекции
- `CT01-API-Collection.postman_collection.json` - Основная коллекция с всеми API endpoints

### Environments
- `CT01-Development-Environment.postman_environment.json` - Environment для локальной разработки

## Импорт в Postman

1. Откройте Postman
2. Кликните "Import" в верхнем левом углу
3. Перетащите файлы коллекции и environment в окно импорта
4. Убедитесь, что выбран правильный environment (CT.01 - Development Environment)

## Настройка переменных

### Базовые переменные
- `baseUrl` - URL сервера API (по умолчанию: http://localhost:8080)
- `username` - Имя пользователя для тестирования
- `password` - Пароль для тестирования
- `csrfToken` - CSRF токен (автоматически заполняется)

### Переменные аутентификации
- `sessionId` - ID сессии (автоматически заполняется)
- `authToken` - JWT токен (если используется)

## Использование

### Последовательность тестирования аутентификации

1. **Get CSRF Token** - Получение CSRF токена для защиты от CSRF атак
2. **Login** - Аутентификация пользователя
3. **Get Current User** - Проверка текущего пользователя
4. **Logout** - Выход из системы

### Другие endpoints

- **Cryptocurrency Data** - Получение данных о криптовалютах
- **Subscription Management** - Управление подписками
- **User Management** - Администрирование пользователей

## Автоматическое извлечение токенов

Некоторые requests содержат скрипты в разделе "Tests", которые автоматически извлекают и сохраняют токены в переменные environment'а для использования в последующих запросах.

## Дополнительные environments

Для тестирования на разных серверах создайте дополнительные environment файлы:

- Staging: измените `baseUrl` на staging сервер
- Production: измените `baseUrl` на production сервер (ОСТОРОЖНО!)

## Запуск коллекции через Newman

Для автоматизированного тестирования можно использовать Newman:

```bash
# Установка Newman
npm install -g newman

# Запуск коллекции
newman run CT01-API-Collection.postman_collection.json \
  -e CT01-Development-Environment.postman_environment.json \
  --reporters cli,htmlextra \
  --reporter-htmlextra-export reports/api-test-report.html
```

## Структура коллекции

```
CT.01 API Collection
├── Authentication
│   ├── Get CSRF Token
│   ├── Login
│   ├── Register User
│   ├── Get Current User
│   ├── Logout
│   └── Telegram Auth
├── Cryptocurrency Data
│   ├── Get Coin Data
│   ├── Get Tracked Coins
│   └── Get Exchange Data
├── Subscription Management
│   ├── Get Subscription Status
│   └── Get Subscription Plans
└── User Management
    └── Get All Users
```

## Troubleshooting

### CSRF Token Errors
- Убедитесь, что получили CSRF токен перед выполнением POST запросов
- Проверьте, что токен правильно установлен в переменную `csrfToken`

### Authentication Errors
- Проверьте правильность credentials в environment
- Убедитесь, что сервер запущен на правильном порту

### CORS Errors
- При тестировании с фронтенда убедитесь, что CORS настроен правильно
- Для тестирования API используйте Postman desktop приложение 