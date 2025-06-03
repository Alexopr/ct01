# Environment Variables Configuration

This document describes the required environment variables for the application.

## Required Variables

### Telegram Bot Configuration
- `TELEGRAM_BOT_TOKEN` - **Required** - Your Telegram bot token from BotFather
- `TELEGRAM_BOT_NAME` - Optional - The name of your bot (defaults to 'alg_gor_bot')

### Database Configuration (Optional - for overriding defaults)
- `SPRING_DATASOURCE_URL` - PostgreSQL connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

### Redis Configuration (Optional - for overriding defaults)
- `SPRING_REDIS_HOST` - Redis server host
- `SPRING_REDIS_PORT` - Redis server port
- `SPRING_REDIS_PASSWORD` - Redis password (if authentication is enabled)

### Application Configuration
- `SPRING_PROFILES_ACTIVE` - Active Spring profile (dev, test, prod)

## Example Configuration

For development:
```bash
export TELEGRAM_BOT_TOKEN="your_telegram_bot_token_here"
export TELEGRAM_BOT_NAME="your_bot_name"
export SPRING_PROFILES_ACTIVE="dev"
```

For production, make sure to set secure passwords for database and Redis connections. 