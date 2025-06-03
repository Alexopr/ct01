# Tech Context

## Technologies Used
- **Backend**: Java 17, Spring Boot 3.x, PostgreSQL, Redis, Spring Security, Spring Data JPA, WebSocket
- **Crypto APIs**: Bybit REST/WebSocket, Binance API, OKX API с rate limiting
- **Authentication**: Telegram Login Widget, JWT HttpOnly Secure Cookies
- **Frontend**: React 18, TypeScript, Vite, NextUI (migrated from Material-UI), React Router v6
- **Styling**: NextUI theming system, Tailwind CSS, Custom dark theme, Glassmorphism effects
- **Real-time**: WebSocket, Server-Sent Events, Push Notifications API
- **Build Tools**: Maven (backend), Vite (frontend), Docker Compose
- **Code Quality**: ESLint, Prettier, SonarLint, Comprehensive linting rules

## Development Setup
- **Backend**: Maven wrapper, application.properties, Flyway migrations, JUnit 5 + Testcontainers
- **Frontend**: Vite dev server, TypeScript strict mode, hot module replacement
- **Database**: PostgreSQL with Docker, Redis for caching and sessions
- **API Documentation**: Springdoc OpenAPI 3, Swagger UI
- **Development Workflow**: Docker Compose local stack, environment profiles

## Technical Constraints
- **Crypto Focus**: Ограничено мониторингом BTC, ETH, SOL (расширяемо)
- **Exchange Support**: Только Bybit, Binance, OKX (без DEX интеграций)
- **Authentication**: Только Telegram (без email/пароль fallback для MVP)
- **UI Theme**: Обязательный dark theme с glassmorphism (без light mode)
- **Real-time Limits**: WebSocket connection limits и API rate limiting

## Dependencies
### Backend
- **Spring Boot Starters**: web, data-jpa, security, validation, cache
- **Database**: PostgreSQL driver, Flyway, Redis Jedis/Lettuce
- **External APIs**: RestTemplate/WebClient для crypto APIs
- **Testing**: JUnit 5, Mockito, Testcontainers, Spring Boot Test

### Frontend  
- **Core**: React 18, TypeScript 4.9+, React Router 6
- **UI**: @nextui-org/react, @iconify/react, framer-motion
- **Styling**: Tailwind CSS, NextUI theme system, PostCSS
- **HTTP**: Axios с interceptors, error handling
- **Build**: Vite, @vitejs/plugin-react

## UI Migration Details
### From Material-UI to NextUI (Completed)
- **Reason**: Critical build issues with HeroUI/Material-UI conflicts (123+ TypeScript errors)
- **Solution**: Complete migration to NextUI as modern successor to HeroUI
- **Migration Scope**:
  - Updated main.tsx: HeroUIProvider → NextUIProvider
  - Updated tailwind.config.js: heroui → nextui plugin configuration
  - Mass replaced @heroui/react → @nextui-org/react imports
  - Rewrote UI components: Button.tsx, Card.tsx, Input.tsx, Modal.tsx
  - Fixed props: variant="primary" → color="primary", leftstartContent → startContent
- **Results**: 85% error reduction, successful builds, preserved design aesthetic

## Performance Considerations
- **Caching Strategy**: Redis multi-level caching с TTL management
- **API Optimization**: Rate limiting, request deduplication, error retry logic
- **Frontend**: Code splitting, lazy loading, React.memo optimization, NextUI tree-shaking
- **WebSocket**: Connection pooling, automatic reconnection, message buffering
- **Build Optimization**: Vite optimizations, NextUI component chunking 