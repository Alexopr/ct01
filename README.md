# Cryptocurrency Admin Dashboard

Полнофункциональная административная панель для отслеживания криптовалют на различных биржах с поддержкой Telegram авторизации.

## 🚀 Особенности

### Backend (Spring Boot)
- **Безопасность**: Spring Security с @PreAuthorize аннотациями
- **Авторизация**: Поддержка Telegram и стандартной авторизации
- **API**: RESTful API с полной документацией
- **Кэширование**: Redis для высокой производительности
- **Мониторинг**: Отслеживание цен в реальном времени
- **WebSocket**: Обновления цен в реальном времени
- **Базы данных**: PostgreSQL с миграциями Flyway

### Frontend (React + TypeScript)
- **UI**: Material-UI для современного интерфейса
- **Типизация**: Полная типизация TypeScript
- **Состояние**: Контекстное управление состоянием
- **Роутинг**: React Router с защищенными маршрутами
- **API**: Централизованный API слой с обработкой ошибок

## 📋 Требования

- **Java**: 17+
- **Node.js**: 18+
- **PostgreSQL**: 13+
- **Redis**: 6+
- **Docker**: (опционально)

## 🛠 Установка и запуск

### Backend

1. **Клонирование репозитория**
```bash
git clone <repository-url>
cd ct.01/backend
```

2. **Настройка базы данных**
```sql
-- PostgreSQL
CREATE DATABASE crypto_admin;
CREATE USER crypto_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE crypto_admin TO crypto_user;
```

3. **Конфигурация**
```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/crypto_admin
spring.datasource.username=crypto_user
spring.datasource.password=your_password

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# CORS
app.cors.allowed-origins=http://localhost:3000,http://localhost:5173

# Telegram Bot (опционально)
telegram.bot.token=your_telegram_bot_token
```

4. **Запуск**
```bash
./mvnw spring-boot:run
```

### Frontend

1. **Установка зависимостей**
```bash
cd ../frontend
npm install
```

2. **Конфигурация**
```env
# .env
VITE_API_BASE_URL=http://localhost:8080/api
```

3. **Запуск**
```bash
npm run dev
```

## 🏗 Архитектура

### Backend Architecture

```
src/main/java/alg/coyote001/
├── controller/          # REST контроллеры
│   ├── UserManagementController.java
│   ├── SettingsController.java
│   └── ...
├── service/            # Бизнес логика
│   ├── UserService.java
│   ├── CoinTrackingService.java
│   └── ...
├── repository/         # Доступ к данным
├── entity/            # JPA сущности
├── dto/               # Data Transfer Objects
├── security/          # Конфигурация безопасности
├── config/            # Конфигурации Spring
├── exception/         # Обработка исключений
└── websocket/         # WebSocket контроллеры
```

### Frontend Architecture

```
src/
├── components/        # React компоненты
│   ├── ui/           # Переиспользуемые UI компоненты
│   └── auth/         # Компоненты авторизации
├── pages/            # Страницы приложения
├── services/         # API сервисы
│   ├── api.ts        # Основной API клиент
│   └── userService.ts # Сервис пользователей
├── types/            # TypeScript типы
├── contexts/         # React контексты
└── utils/            # Утилиты
```

## 🔐 Безопасность

### Backend Security Features

- **Spring Security**: Конфигурация с @PreAuthorize
- **CORS**: Ограниченные домены вместо "*"
- **CSRF**: Защита с токенами
- **Сессии**: Управление сессиями с ограничениями
- **Пароли**: BCrypt с увеличенной сложностью (12)
- **Централизованная обработка ошибок**: GlobalExceptionHandler

### Роли и разрешения

```java
// Примеры разрешений
USER_READ, USER_WRITE, USER_DELETE
COIN_READ, COIN_WRITE, COIN_DELETE
EXCHANGE_READ, EXCHANGE_WRITE, EXCHANGE_DELETE
TRADING_READ, TRADING_WRITE
SYSTEM_READ, SYSTEM_WRITE
```

### Использование @PreAuthorize

```java
@PreAuthorize("hasAuthority('USER_READ') or hasRole('ADMIN')")
public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
    // Метод выполнится только если у пользователя есть права
}

@PreAuthorize("@userService.isCurrentUser(#userId, authentication.name) or hasAuthority('USER_WRITE')")
public ResponseEntity<UserDto> updateUser(@PathVariable Long userId, @RequestBody UserDto userDto) {
    // Пользователь может редактировать свой профиль или иметь права USER_WRITE
}
```

## 📊 API Документация

### Authentication Endpoints

```http
POST /api/auth/login
POST /api/auth/telegram
POST /api/auth/logout
GET  /api/auth/me
```

### User Management

```http
GET    /api/v1/users              # Получить всех пользователей
GET    /api/v1/users/{id}         # Получить пользователя по ID
POST   /api/v1/users              # Создать пользователя
PUT    /api/v1/users/{id}         # Обновить пользователя
DELETE /api/v1/users/{id}         # Удалить пользователя
POST   /api/v1/users/{id}/roles/{roleId}    # Назначить роль
DELETE /api/v1/users/{id}/roles/{roleId}    # Убрать роль
```

### Coin Management

```http
GET    /api/v1/coins              # Получить монеты
GET    /api/v1/coins/search       # Поиск монет
GET    /api/v1/coins/tracked      # Отслеживаемые монеты
POST   /api/v1/coins/{id}/track   # Начать отслеживание
DELETE /api/v1/coins/{id}/track   # Прекратить отслеживание
```

### Settings Management

```http
GET /api/v1/settings                    # Все настройки
GET /api/v1/settings/{category}         # Настройки категории
PUT /api/v1/settings/{category}         # Обновить настройки
POST /api/v1/settings/{category}/reset  # Сбросить к умолчанию
```

## 🔄 WebSocket

### Price Updates

```javascript
// Подключение к WebSocket
const ws = new WebSocket('ws://localhost:8080/websocket/prices');

// Получение обновлений цен
ws.onmessage = (event) => {
  const priceUpdate = JSON.parse(event.data);
  console.log('Price update:', priceUpdate);
};
```

## 🧪 Тестирование

### Backend Tests

```bash
# Запуск всех тестов
./mvnw test

# Запуск конкретного теста
./mvnw test -Dtest=UserServiceTest
```

### Frontend Tests

```bash
# Запуск тестов
npm test

# Проверка типов
npm run type-check
```

## 📦 Развертывание

### Docker Deployment

```yaml
# docker-compose.yml
version: '3.8'
services:
  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: crypto_admin
      POSTGRES_USER: crypto_user
      POSTGRES_PASSWORD: your_password
    ports:
      - "5432:5432"

  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis

  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
    depends_on:
      - backend
```

### Production Configuration

```properties
# application-prod.properties
spring.profiles.active=prod
spring.datasource.url=${DATABASE_URL}
spring.data.redis.url=${REDIS_URL}
app.cors.allowed-origins=${FRONTEND_URL}
logging.level.root=WARN
```

## 🔧 Конфигурация

### Environment Variables

```bash
# Backend
DATABASE_URL=jdbc:postgresql://localhost:5432/crypto_admin
REDIS_URL=redis://localhost:6379
TELEGRAM_BOT_TOKEN=your_telegram_bot_token

# Frontend
VITE_API_BASE_URL=http://localhost:8080/api
```

## 📈 Мониторинг

### Health Checks

```http
GET /actuator/health
GET /actuator/metrics
GET /actuator/info
```

### Logging

```properties
# Настройка логирования
logging.level.alg.coyote001=DEBUG
logging.level.org.springframework.security=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

## 🤝 Вклад в проект

1. Fork репозитория
2. Создайте feature branch (`git checkout -b feature/amazing-feature`)
3. Commit изменения (`git commit -m 'Add amazing feature'`)
4. Push в branch (`git push origin feature/amazing-feature`)
5. Создайте Pull Request

## 📝 Лицензия

Этот проект лицензирован под MIT License - см. файл [LICENSE](LICENSE) для деталей.

## 🆘 Поддержка

Если у вас есть вопросы или проблемы:

1. Проверьте [Issues](../../issues)
2. Создайте новый Issue с подробным описанием
3. Укажите версии используемого ПО

## 🔄 Changelog

### v1.0.0 (Текущая версия)
- ✅ Базовая функциональность отслеживания криптовалют
- ✅ Telegram авторизация
- ✅ Административная панель
- ✅ WebSocket обновления
- ✅ Улучшенная безопасность
- ✅ Полная типизация TypeScript
- ✅ Централизованная обработка ошибок

## 🎯 Roadmap

- [ ] Мобильное приложение
- [ ] Расширенная аналитика
- [ ] Уведомления по email/SMS
- [ ] API для внешних интеграций
- [ ] Поддержка больше бирж
- [ ] Автоматическая торговля 