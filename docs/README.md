# CT.01 - Cryptocurrency Trading Dashboard

<div align="center">

![Project Status](https://img.shields.io/badge/Status-In%20Development-yellow)
![API Version](https://img.shields.io/badge/API-v1.0-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![React](https://img.shields.io/badge/React-18.x-blue)

**Профессиональная платформа для анализа криптовалютных рынков с системой подписок**

</div>

## 📋 Обзор проекта

CT.01 - это комплексная веб-платформа для анализа криптовалютных рынков, включающая:

- **Мониторинг цен** с нескольких бирж в реальном времени
- **Арбитражные возможности** между биржами  
- **Система подписок** с ограничениями по функциональности
- **Административная панель** для управления пользователями и системой
- **WebSocket интеграции** для получения данных в реальном времени
- **Telegram авторизация** для удобного входа пользователей

## 🏗️ Архитектура

```
├── Backend (Spring Boot 3.x)
│   ├── REST API + WebSocket
│   ├── PostgreSQL Database
│   ├── Redis Caching
│   └── Liquibase Migrations
│
├── Frontend (React 18 + TypeScript)
│   ├── HeroUI Components
│   ├── Real-time Data Updates
│   └── Responsive Design
│
└── Infrastructure
    ├── Docker Support
    ├── Environment Configurations
    └── CI/CD Ready
```

## 📚 Документация

### 🔗 Быстрые ссылки
- **[API Documentation](./API_REFERENCE.md)** - Полная документация всех эндпоинтов
- **[Architecture Guide](./ARCHITECTURE.md)** - Архитектура системы и компонентов
- **[Setup Guide](./SETUP.md)** - Инструкции по установке и настройке
- **[Development Guide](./DEVELOPMENT.md)** - Руководство для разработчиков
- **[Testing Guide](./TESTING.md)** - Стратегии тестирования и примеры

### 📖 Специализированная документация
- **[Subscription System](./subscription-system-architecture.md)** - Система подписок
- **[User Management](./ROLE_MANAGEMENT_API.md)** - Управление пользователями и ролями
- **[Real-time Data](./WEBSOCKET_API.md)** - WebSocket API и real-time данные
- **[Database Schema](./DATABASE.md)** - Схема базы данных и миграции
- **[Security](./SECURITY.md)** - Безопасность и аутентификация

## 🚀 Быстрый старт

### Предварительные требования
- Java 17+
- Node.js 18+
- PostgreSQL 13+
- Redis 6+
- Docker (опционально)

### Установка

1. **Клонирование репозитория**
```bash
git clone https://github.com/Alexopr/ct01.git
cd ct01
```

2. **Backend Setup**
```bash
cd backend
./mvnw spring-boot:run
```

3. **Frontend Setup**
```bash
cd frontend
npm install
npm run dev
```

4. **База данных**
```bash
# Создание базы данных PostgreSQL
# Настройка Redis
# Автоматические миграции через Liquibase
```

Подробные инструкции: **[Setup Guide](./SETUP.md)**

## 🔧 Основные функции

### Для пользователей
- ✅ Мониторинг цен криптовалют
- ✅ Анализ арбитражных возможностей
- ✅ Сравнение бирж по комиссиям
- ✅ Персональные уведомления
- ✅ Система подписок (FREE/PREMIUM)

### Для администраторов
- ✅ Управление пользователями и ролями
- ✅ Мониторинг системы
- ✅ Конфигурация лимитов подписок
- ✅ Логирование и аналитика

### Технические возможности
- ✅ REST API с OpenAPI документацией
- ✅ WebSocket для real-time данных
- ✅ Redis кэширование
- ✅ Telegram авторизация
- ✅ Role-based access control (RBAC)
- ✅ Автоматическое тестирование

## 📊 Статус разработки

### Завершенные модули
- [x] **Система аутентификации** (Telegram + JWT)
- [x] **Управление пользователями** и ролями
- [x] **Система подписок** с лимитами
- [x] **API для данных бирж** (Binance, OKX, Bybit)
- [x] **WebSocket интеграции**
- [x] **Административная панель**
- [x] **Базовый frontend** с React

### В разработке
- [ ] **UI Redesign** (Crypto Dashboard Style)
- [ ] **Twitter Tracker** функциональность
- [ ] **Advanced Analytics**
- [ ] **Mobile приложение**

### Планируется
- [ ] **Telegram бот** интеграция
- [ ] **Push уведомления**
- [ ] **API для внешних разработчиков**
- [ ] **Machine Learning** алгоритмы

## 🛠️ Технологический стек

### Backend
- **Spring Boot 3.x** - основной фреймворк
- **PostgreSQL** - основная база данных
- **Redis** - кэширование и сессии
- **Liquibase** - миграции базы данных
- **Spring Security** - аутентификация и авторизация
- **WebSocket** - real-time коммуникация
- **Maven** - сборка проекта

### Frontend
- **React 18** - UI библиотека
- **TypeScript** - типизация
- **HeroUI** - компонентная библиотека
- **Vite** - сборщик и dev server
- **React Router** - роутинг
- **Axios** - HTTP клиент

### DevOps & Инфраструктура
- **Docker** - контейнеризация
- **GitHub Actions** - CI/CD
- **Nginx** - reverse proxy
- **Let's Encrypt** - SSL сертификаты

## 🤝 Участие в разработке

1. **Fork** репозитория
2. Создайте **feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Commit** изменения (`git commit -m 'Add some AmazingFeature'`)
4. **Push** в branch (`git push origin feature/AmazingFeature`)
5. Откройте **Pull Request**

См. **[Development Guide](./DEVELOPMENT.md)** для подробностей.

## 📝 Лицензия

Этот проект распространяется под лицензией MIT. См. `LICENSE` файл для подробностей.

## 📞 Контакты

- **Автор**: Alexey
- **GitHub**: [@Alexopr](https://github.com/Alexopr)
- **Проект**: [CT.01](https://github.com/Alexopr/ct01)

## 🙏 Благодарности

- Spring Boot community
- React и TypeScript сообщества
- Все участники открытого ПО

---

<div align="center">
Made with ❤️ for the crypto community
</div>

# Project Documentation

This directory contains comprehensive documentation for the role-based user management system and other project components.

## 📚 Documentation Index

### Role Management System
- **[Role Management API](./ROLE_MANAGEMENT_API.md)** - Complete API documentation with examples
- **[Quick Start Guide](./ROLE_MANAGEMENT_QUICKSTART.md)** - Essential patterns for developers

### Other Documentation
- Add other documentation files here as the project grows

## 🚀 Quick Start

New to the project? Start here:

1. **Read** the [Quick Start Guide](./ROLE_MANAGEMENT_QUICKSTART.md) for essential patterns
2. **Reference** the [Full API Documentation](./ROLE_MANAGEMENT_API.md) for detailed implementation
3. **Test** your implementation using the provided examples

## 🔑 Key Concepts

### Role Hierarchy
```
ADMIN (4) → Full system access
    ↓
MODERATOR (3) → User management + premium tools
    ↓  
PREMIUM (2) → Basic tools + premium features
    ↓
USER (1) → Basic access (auto-assigned)
```

### Permission Inheritance
Higher roles automatically inherit all permissions from lower roles.

### Security Model
- **Frontend:** Role-based UI rendering with TypeScript safety
- **Backend:** Permission-based endpoint protection with Spring Security
- **Registration:** Automatic USER role assignment, no self-service role selection

## 🛠️ Development Guidelines

### When to Use Each Type

| Type | Use Case | Has Roles? |
|------|----------|------------|
| `UserRegistrationData` | User registration | ❌ No |
| `UserFormData` | Admin user creation | ✅ Yes |
| `User` | Display user info | ✅ Yes (as objects) |

### Security Best Practices

1. **Use permissions over roles** when possible
2. **Validate on backend** - never trust frontend-only checks
3. **Follow least privilege** - grant minimum required permissions
4. **Audit regularly** - review user roles and permissions

### Code Quality

- Use TypeScript types strictly
- Implement proper error handling
- Add logging for security events
- Write tests for role-based logic

## 📖 API Reference

### Quick Endpoint Reference

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/auth/register` | User registration | No |
| `POST` | `/api/auth/telegram` | Telegram auth | No |
| `GET` | `/api/auth/me` | Current user | Yes |
| `GET` | `/api/v1/users` | List users | USER_READ |
| `PUT` | `/api/v1/users/{id}/upgrade-to-premium` | Upgrade to premium | ADMIN |
| `GET` | `/api/v1/roles` | List roles | ROLE_READ |

## 🧪 Testing

### Test Categories

1. **Unit Tests** - Role assignment, permission checks
2. **Integration Tests** - API endpoint security
3. **UI Tests** - Role-based component rendering
4. **Security Tests** - Authorization bypass attempts

### Testing Checklist

- [ ] Registration assigns USER role automatically
- [ ] Admin can manage user roles
- [ ] Premium upgrade works correctly
- [ ] UI renders based on permissions
- [ ] Protected routes block unauthorized access

## 🐛 Troubleshooting

### Common Issues

| Error | Solution |
|-------|----------|
| Type conflicts between User interfaces | Use User from `types/api.ts` |
| Role assignment failures | Verify USER role exists with ID 1 |
| Permission denied errors | Check permission vs role requirements |
| Registration without roles | Use UserRegistrationData type |

### Debug Steps

1. Check browser console for auth errors
2. Verify API responses include role structure
3. Inspect user context state in React DevTools
4. Review backend logs for authorization failures

## 🔄 Migration Guide

### From String Roles to Object Roles

```typescript
// Before
user.roles.includes('ADMIN')

// After  
user.roles.some(role => role.name === 'ADMIN')
// Or: hasRole('ADMIN')
```

### Registration Form Updates

1. Remove role selection components
2. Change `UserFormData` → `UserRegistrationData`
3. Remove roles from form state
4. Update API calls to registration endpoint

## 📞 Support

For questions about the role management system:

1. **Check** this documentation first
2. **Search** existing issues in the project repository
3. **Review** code examples in the documentation
4. **Contact** the development team if needed

---

## 📋 Documentation Maintenance

This documentation should be updated when:

- New roles or permissions are added
- API endpoints change
- Security model is modified  
- New frontend patterns are established

**Last Updated:** December 2024
**Version:** 1.0.0 