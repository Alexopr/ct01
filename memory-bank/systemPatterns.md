# System Patterns

## System Architecture
- **Layered Architecture**: Controller → Service → Repository → Entity (Spring Boot backend)
- **Microservice Pattern**: Specialized services for different crypto exchange integrations
- **Adapter Pattern**: Unified interface (AbstractExchangeAdapter) for Bybit, Binance, OKX
- **Frontend SPA**: React with Context API, Material-UI theming, protected routes
- **Event-Driven**: WebSocket connections for real-time crypto price updates

## Key Technical Decisions
- **Backend**: Spring Boot 3.x, PostgreSQL, Redis caching, @PreAuthorize security annotations
- **Crypto Integration**: Multi-exchange API with rate limiting and error handling
- **Authentication**: Telegram Login Widget → JWT HttpOnly Secure Cookies (no email/password)
- **Frontend**: React + TypeScript, Material-UI with custom dark theme and glassmorphism
- **Real-time**: WebSocket + Server-Sent Events for push notifications
- **Containerization**: Docker Compose для всего стека

## Design Patterns in Use
- **Adapter Pattern**: AbstractExchangeAdapter для унификации разных бирж
- **Service Layer Pattern**: Business logic separation с @Transactional
- **Repository Pattern**: Spring Data JPA с custom queries
- **Strategy Pattern**: Different caching strategies для различных типов данных
- **Observer Pattern**: WebSocket listeners для real-time updates
- **Dependency Injection**: Spring IoC container управление
- **DTO Pattern**: Data Transfer Objects для API layer

## Component Relationships
- **Exchange Adapters** → Unified CryptoTrackingService → REST Controllers
- **Redis Cache Layer** → Service Methods с @Cacheable annotations
- **Frontend Context API** → Components → API Services → Backend endpoints
- **WebSocket Handlers** → Real-time price broadcasters → Frontend listeners
- **Telegram Auth Flow** → Security Context → Protected routes/endpoints
- **Docker Network**: PostgreSQL ↔ Redis ↔ Spring Boot ↔ React SPA

## Code Quality Patterns
- **Error Handling**: Centralized GlobalExceptionHandler с business exception mapping
- **Constants Management**: AppConstants.java для всех hardcoded values
- **Generic Methods**: Type-safe caching и API operations
- **Modular Components**: Separated concerns (ToolPanel, Topbar, Layout)
- **Color System**: Unified color utilities с TypeScript interfaces 