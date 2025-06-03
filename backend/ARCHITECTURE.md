# Backend Architecture Documentation

## Overview

This document describes the architecture of the CT.01 backend application - a cryptocurrency tracking and analysis system built with Spring Boot.

## Technology Stack

### Core Framework
- **Spring Boot 3.4.5** - Main application framework
- **Java 17** - Programming language
- **Maven** - Build and dependency management

### Data Layer
- **PostgreSQL** - Primary database for persistent data
- **Redis** - Caching and session storage
- **Spring Data JPA** - ORM and data access
- **Liquibase** - Database migration management

### Security
- **Spring Security** - Authentication and authorization
- **Telegram Bot API** - Alternative authentication method
- **Rate Limiting** - Bucket4j for API rate limiting

### External Integrations
- **WebSocket** - Real-time data streaming
- **WebFlux** - Reactive HTTP client for exchange APIs
- **OkHttp** - HTTP client for external API calls

### Testing
- **JUnit 5** - Unit testing framework
- **Testcontainers** - Integration testing with real databases
- **Mockito** - Mocking framework

## Project Structure

```
src/main/java/alg/coyote001/
├── config/              # Configuration classes
├── controller/          # REST API controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA entities
├── exception/           # Custom exceptions
├── mapper/              # Object mapping utilities
├── model/               # Domain models
├── repository/          # Data access layer
├── security/            # Security configuration and services
├── service/             # Business logic layer
│   └── exchange/        # Exchange adapter implementations
├── websocket/           # WebSocket controllers
└── Coyote001Application.java  # Main application class
```

## Architecture Patterns

### Layered Architecture
The application follows a classic layered architecture:

1. **Presentation Layer** (`controller/`, `websocket/`)
   - REST API endpoints
   - WebSocket endpoints
   - Request/response handling

2. **Business Logic Layer** (`service/`)
   - Core business logic
   - Exchange integrations
   - Data processing

3. **Data Access Layer** (`repository/`, `entity/`)
   - Database operations
   - Entity management
   - Query optimization

4. **Cross-cutting Concerns** (`config/`, `security/`, `exception/`)
   - Security configuration
   - Global exception handling
   - Application configuration

### Adapter Pattern
Exchange integrations use the Adapter pattern:

```
AbstractExchangeAdapter (base class)
├── BybitAdapter
├── BinanceAdapter
└── OkxAdapter
```

Each adapter implements:
- Ticker data retrieval
- Health checks
- WebSocket connections
- Rate limiting

### Dependency Injection
- **Constructor Injection** - Preferred method for all dependencies
- **No Field Injection** - Avoided for better testability
- **Service Layer** - All business logic in services

## Key Components

### Authentication System
- **Traditional Login** - Username/password with Spring Security
- **Telegram Auth** - OAuth-like flow with Telegram Bot API
- **Session Management** - Redis-backed sessions
- **Rate Limiting** - IP-based rate limiting for auth endpoints

### Exchange Integration
- **Multi-Exchange Support** - Binance, Bybit, OKX
- **Real-time Data** - WebSocket connections for live prices
- **Caching** - Redis caching for API responses
- **Rate Limiting** - Per-exchange rate limiting

### Data Management
- **User Preferences** - Customizable coin tracking
- **Price History** - Historical price data storage
- **Settings** - User-specific configuration

### WebSocket Architecture
- **Real-time Updates** - Live price streaming
- **Connection Management** - Automatic reconnection
- **Message Broadcasting** - Multi-client support

## Database Schema

### Core Entities
- **User** - User accounts and profiles
- **Role/Permission** - RBAC security model
- **Coin** - Cryptocurrency definitions
- **Exchange** - Exchange configurations
- **PriceHistory** - Historical price data
- **Settings** - Application settings

### Relationships
- User ↔ Role (Many-to-Many)
- Role ↔ Permission (Many-to-Many)
- User ↔ UserCoinPreference (One-to-Many)
- Exchange ↔ Coin (Many-to-Many)

## Security Architecture

### Authentication Flow
1. User submits credentials (traditional or Telegram)
2. Rate limiting check
3. Authentication validation
4. Session creation
5. Security context establishment

### Authorization
- **Role-based Access Control (RBAC)**
- **Method-level Security** - `@PreAuthorize` annotations
- **URL-based Security** - Path-specific access rules

### Rate Limiting
- **Authentication Endpoints** - 5 attempts per minute per IP
- **API Endpoints** - Configurable per endpoint
- **Exchange APIs** - Respect exchange rate limits

## Configuration Management

### Profiles
- **Development** (`application-dev.properties`)
  - Verbose logging
  - Extended timeouts
  - Debug endpoints enabled

- **Production** (`application-prod.properties`)
  - Minimal logging
  - Optimized timeouts
  - Security hardening

### Environment Variables
- **Database Configuration** - Connection strings, credentials
- **Redis Configuration** - Connection details
- **API Keys** - Exchange API credentials
- **Security Settings** - JWT secrets, encryption keys

## Monitoring and Observability

### Logging
- **SLF4J + Logback** - Structured logging
- **Environment-specific Levels** - Debug in dev, warn in prod
- **Request Tracing** - Correlation IDs for request tracking

### Health Checks
- **Spring Actuator** - Application health endpoints
- **Database Health** - Connection and query checks
- **Redis Health** - Cache connectivity
- **Exchange Health** - External API availability

## Performance Considerations

### Caching Strategy
- **Redis Caching** - API responses, user sessions
- **TTL Management** - Appropriate cache expiration
- **Cache Warming** - Preload frequently accessed data

### Database Optimization
- **Connection Pooling** - HikariCP for efficient connections
- **Query Optimization** - Indexed queries, pagination
- **Lazy Loading** - JPA lazy loading for large datasets

### Async Processing
- **WebFlux** - Non-blocking HTTP clients
- **WebSocket** - Async real-time communication
- **Scheduled Tasks** - Background data processing

## Deployment Architecture

### Application Packaging
- **Spring Boot JAR** - Executable JAR with embedded Tomcat
- **Docker Support** - Containerization ready
- **Profile-based Configuration** - Environment-specific settings

### External Dependencies
- **PostgreSQL Database** - Primary data store
- **Redis Cache** - Session and cache store
- **Exchange APIs** - External cryptocurrency data

## Development Guidelines

### Code Quality
- **Constructor Injection** - Mandatory for all dependencies
- **JavaDoc Documentation** - Public methods and classes
- **Unit Testing** - Minimum 80% coverage target
- **Integration Testing** - Testcontainers for database tests

### Best Practices
- **Exception Handling** - Global exception handlers
- **Validation** - Bean validation annotations
- **Logging** - Structured logging with appropriate levels
- **Security** - Input validation, output encoding

## Future Considerations

### Scalability
- **Microservices** - Potential service decomposition
- **Load Balancing** - Horizontal scaling support
- **Database Sharding** - Data partitioning strategies

### Monitoring
- **Metrics Collection** - Application and business metrics
- **Distributed Tracing** - Request flow tracking
- **Alerting** - Proactive issue detection

### Security Enhancements
- **OAuth2/OIDC** - Standard authentication protocols
- **API Versioning** - Backward compatibility
- **Audit Logging** - Security event tracking 