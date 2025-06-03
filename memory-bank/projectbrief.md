# Project Brief

## Purpose
Полнофункциональная административная панель для отслеживания криптовалют на различных биржах с поддержкой Telegram авторизации. Система предоставляет real-time мониторинг цен, управление пользователями, специализированные инструменты для криптоанализа и современный UI в стиле Crypto Dashboard.

## Core Requirements
- **Backend**: Spring Boot 3.x (Java 17), PostgreSQL, Redis, Spring Security с @PreAuthorize аннотациями
- **Криптоинтеграция**: Мультибиржевая интеграция (Bybit, Binance, OKX) с WebSocket real-time обновлениями
- **Авторизация**: Telegram Login Widget + JWT через HttpOnly Secure Cookies
- **Frontend**: React + TypeScript, Material-UI с glassmorphism dark theme
- **Архитектура**: Docker-окружение, микросервисная архитектура с unified adapter pattern

## Goals
- Real-time отслеживание криптовалют на всех основных биржах
- Безопасная авторизация через Telegram без email/пароль fallback
- Масштабируемая система с кэшированием и rate limiting
- Современный UI/UX с dark theme и glassmorphism эффектами
- Инструменты для криптоанализа: тикер, scanner, PumpFun интеграция

## Scope
- **Основные функции**: Мониторинг BTC, ETH, SOL (с возможностью расширения)
- **Специализированные инструменты**: Real-time ticker, PumpFun scanner, уведомления
- **Администрирование**: Управление пользователями, мониторинг системы, логирование
- **Интеграции**: Telegram авторизация, push-уведомления, WebSocket connections
- **Ограничения**: Фокус на криптовалютный мониторинг, без сложных trading функций 