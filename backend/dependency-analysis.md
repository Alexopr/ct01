# 📊 Анализ зависимостей проекта CT.01

## 🎯 Обзор проекта

**Проект:** CT.01 Cryptocurrency Tracker  
**Технологии:** Java 17 + Spring Boot 3.4.5  
**Размер:** 111 Java классов  
**Архитектура:** Слоевая (Controller/Service/Repository)

## 📦 Основные зависимости

### Core Framework
- **Spring Boot 3.4.5** - Основной фреймворк
- **Spring Security 6.4.5** - Аутентификация и авторизация
- **Spring Data JPA 3.4.5** - ORM и доступ к данным
- **Spring WebMVC 6.2.6** - REST API
- **Spring WebFlux 6.2.6** - Реактивный HTTP клиент

### База данных
- **PostgreSQL 42.7.5** - Основная БД
- **Redis (Lettuce 6.4.2)** - Кеширование и сессии  
- **HikariCP 5.1.0** - Connection Pool
- **Liquibase 4.29.2** - Миграции БД
- **H2 2.3.232** - Тестовая БД

### Внешние интеграции
- **OkHttp 4.12.0** - HTTP клиент для бирж
- **WebSocket** - Реальное время
- **Jackson 2.18.3** - JSON обработка

### Инфраструктура
- **Quartz 2.3.2** - Планировщик задач
- **Bucket4j 8.10.1** - Rate Limiting
- **Micrometer** - Метрики
- **SpringDoc OpenAPI 2.7.0** - Документация API

### Тестирование
- **JUnit 5.11.4** - Unit тесты
- **Testcontainers 1.20.6** - Интеграционные тесты
- **Mockito 5.14.2** - Моки

## 🏗️ Архитектурный анализ

### Паттерны
✅ **Слоевая архитектура** - Controller → Service → Repository  
✅ **Dependency Injection** - Spring IoC  
✅ **Repository Pattern** - Spring Data JPA  
✅ **Adapter Pattern** - Exchange адаптеры  

### Зависимости по слоям

**Presentation Layer:**
- Spring WebMVC (REST API)
- Spring Security (Auth)
- OpenAPI (Docs)

**Business Layer:**
- Spring Core (DI)
- WebFlux (External API)
- Quartz (Scheduling)
- Bucket4j (Rate Limiting)

**Data Layer:**
- Spring Data JPA
- PostgreSQL
- Redis
- Liquibase

**Integration Layer:**
- OkHttp (Exchange APIs)
- WebSocket (Real-time)
- Jackson (JSON)

## ⚠️ Потенциальные проблемы

### Высокая связанность
- **111 классов** - большая кодовая база
- **Множественные адаптеры** - возможное дублирование
- **Сложные зависимости** - между биржами

### Предупреждения компиляции
- **31 warning** - в основном @Builder с Lombok
- **Deprecated API** - SecurityConfig и RateLimitService
- **Unchecked operations** - ApiDiscoveryService

## 📈 Рекомендации

### Краткосрочные
1. **Исправить warnings** - добавить @Builder.Default
2. **Обновить deprecated** - SecurityConfig методы
3. **Типизировать** - ApiDiscoveryService операции

### Среднесрочные  
1. **Добавить ArchUnit** - тесты архитектуры
2. **Выделить общие** - адаптеры для бирж
3. **Увеличить покрытие** - тестами (сейчас ~15 тестов)

### Долгосрочные
1. **Модуляризация** - разделить на модули
2. **Микросервисы** - при росте сложности
3. **Async processing** - для высоких нагрузок

## 🔍 Метрики качества

```
Классов: 111
Packages: 15+
Dependencies: 80+
Warnings: 31
Test Coverage: ~15%
Architecture: ⭐⭐⭐⭐⭐ (отличная)
Dependencies: ⭐⭐⭐⭐ (хорошие)
Maintainability: ⭐⭐⭐⭐ (хорошая)
```

## 🎨 Архитектурная диаграмма

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controllers   │────│    Services     │────│  Repositories   │
│                 │    │                 │    │                 │
│ ▪ AuthController│    │ ▪ UserService   │    │ ▪ UserRepo      │
│ ▪ CoinController│    │ ▪ CoinService   │    │ ▪ CoinRepo      │
│ ▪ ExchangeCtrl  │    │ ▪ ExchangeSvc   │    │ ▪ ExchangeRepo  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   DTO Layer     │    │ Exchange Adapts │    │   Database      │
│                 │    │                 │    │                 │
│ ▪ UserDto       │    │ ▪ BinanceAdpt   │    │ ▪ PostgreSQL    │
│ ▪ CoinDto       │    │ ▪ BybitAdpt     │    │ ▪ Redis         │
│ ▪ TickerData    │    │ ▪ OkxAdpt       │    │ ▪ Liquibase     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 Следующие шаги

1. **Запустить SonarQube** - для детального анализа
2. **Добавить ArchUnit** - проверки архитектуры  
3. **Создать PlantUML** - диаграммы (когда исправят Java 17)
4. **Интегрировать в CI/CD** - автоматические проверки 