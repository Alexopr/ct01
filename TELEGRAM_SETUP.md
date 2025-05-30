# Настройка Telegram авторизации

## 1. Настройка бота в BotFather

1. Откройте [@BotFather](https://t.me/botfather) в Telegram
2. Отправьте команду `/setdomain`
3. Выберите вашего бота `alg_gor_bot`
4. Укажите домен: `localhost:3000` (для разработки)

5. Получите токен бота:
   - Отправьте `/token`
   - Выберите `alg_gor_bot`
   - Скопируйте токен

## 2. Настройка бэкенда

Создайте файл `.env` в папке `backend/` и добавьте:

```bash
# Telegram Bot Configuration
TELEGRAM_BOT_TOKEN=your_actual_bot_token_here

# Database Configuration (если нужно переопределить)
DATABASE_URL=jdbc:postgresql://localhost:5432/crud_app
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=root

# Redis Configuration (если нужно переопределить)
REDIS_HOST=localhost
REDIS_PORT=6379
```

## 3. Запуск с переменными окружения

### Windows (PowerShell):
```powershell
$env:TELEGRAM_BOT_TOKEN="your_actual_bot_token_here"
./mvnw spring-boot:run
```

### Linux/Mac:
```bash
export TELEGRAM_BOT_TOKEN="your_actual_bot_token_here"
./mvnw spring-boot:run
```

### Через application.properties:
```properties
app.telegram.bot.token=your_actual_bot_token_here
```

## 4. Проверка работы

1. Запустите бэкенд
2. Откройте фронтенд `http://localhost:3000`
3. Нажмите "Войти через Telegram"
4. Должен загрузиться виджет Telegram или показаться тестовая кнопка

## Структура ответа от Telegram

```javascript
{
  "id": 123456789,
  "first_name": "John",
  "last_name": "Doe",
  "username": "johndoe",
  "photo_url": "https://...",
  "auth_date": 1640995200,
  "hash": "abc123..."
}
```

## Безопасность

- В production обязательно используйте HTTPS
- Настройте правильный домен в BotFather
- Храните токен бота в безопасном месте
- Проверяйте валидность hash от Telegram 