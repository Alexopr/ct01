# System Architecture

<div align="center">

![Architecture](https://img.shields.io/badge/Architecture-Microservices-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![React](https://img.shields.io/badge/React-18.x-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13+-blue)

**Comprehensive Architecture Guide для платформы CT.01**

</div>

## 📖 Содержание

- [Общий обзор архитектуры](#-общий-обзор-архитектуры)
- [Компоненты системы](#-компоненты-системы)
- [Backend архитектура](#-backend-архитектура)
- [Frontend архитектура](#-frontend-архитектура)
- [База данных](#-база-данных)
- [Система безопасности](#-система-безопасности)
- [Интеграции](#-интеграции)
- [Развертывание](#-развертывание)
- [Мониторинг и логирование](#-мониторинг-и-логирование)
- [Scalability Strategy](#-scalability-strategy)

---

## 🏗️ Общий обзор архитектуры

### High-Level Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        WEB[Web Browser]
        MOB[Mobile App]
        API_CLIENT[API Clients]
    end
    
    subgraph "Load Balancer"
        LB[Nginx Load Balancer]
    end
    
    subgraph "Application Layer"
        BACKEND[Spring Boot API]
        WS[WebSocket Server]
    end
    
    subgraph "Caching Layer"
        REDIS[Redis Cache]
    end
    
    subgraph "Database Layer"
        POSTGRES[(PostgreSQL)]
        REPLICA[(Read Replica)]
    end
    
    subgraph "External Services"
        BINANCE[Binance API]
        OKEX[OKX API]
        BYBIT[Bybit API]
        TELEGRAM[Telegram API]
    end
    
    subgraph "Infrastructure"
        DOCKER[Docker Containers]
        MONITORING[Monitoring Stack]
    end
    
    WEB --> LB
    MOB --> LB
    API_CLIENT --> LB
    
    LB --> BACKEND
    LB --> WS
    
    BACKEND --> REDIS
    BACKEND --> POSTGRES
    BACKEND --> REPLICA
    
    WS --> REDIS
    
    BACKEND --> BINANCE
    BACKEND --> OKEX
    BACKEND --> BYBIT
    BACKEND --> TELEGRAM
    
    BACKEND -.-> MONITORING
    WS -.-> MONITORING
```

### Technology Stack

```mermaid
graph LR
    subgraph "Frontend"
        React[React 18]
        TS[TypeScript]
        HEROUI[HeroUI]
        VITE[Vite]
    end
    
    subgraph "Backend"
        SPRING[Spring Boot 3.x]
        SECURITY[Spring Security]
        JPA[Spring Data JPA]
        WEB[Spring Web]
    end
    
    subgraph "Database"
        PG[PostgreSQL 13+]
        LIQUIBASE[Liquibase]
        R[Redis 6+]
    end
    
    subgraph "DevOps"
        DOCKER_TECH[Docker]
        GITHUB[GitHub Actions]
        NGINX[Nginx]
    end
    
    React --> SPRING
    SPRING --> PG
    SPRING --> R
    DOCKER_TECH --> NGINX
```

---

## 🎯 Компоненты системы

### Core Services Architecture

```mermaid
graph TB
    subgraph "API Gateway Layer"
        GATEWAY[API Gateway]
        AUTH[Authentication Service]
        RATE_LIMIT[Rate Limiting]
    end
    
    subgraph "Business Logic Layer"
        USER_SVC[User Management Service]
        SUB_SVC[Subscription Service]
        COIN_SVC[Coin Data Service]
        TRACKING_SVC[Tracking Service]
        EXCHANGE_SVC[Exchange Service]
        NOTIFICATION_SVC[Notification Service]
    end
    
    subgraph "Data Access Layer"
        USER_REPO[User Repository]
        SUB_REPO[Subscription Repository]
        COIN_REPO[Coin Repository]
        TRACKING_REPO[Tracking Repository]
        CACHE_MGR[Cache Manager]
    end
    
    subgraph "External Integration Layer"
        EXCHANGE_CLIENT[Exchange API Client]
        TELEGRAM_CLIENT[Telegram Client]
        PAYMENT_CLIENT[Payment Gateway]
    end
    
    GATEWAY --> AUTH
    GATEWAY --> RATE_LIMIT
    
    AUTH --> USER_SVC
    
    USER_SVC --> USER_REPO
    SUB_SVC --> SUB_REPO
    COIN_SVC --> COIN_REPO
    TRACKING_SVC --> TRACKING_REPO
    
    COIN_SVC --> CACHE_MGR
    EXCHANGE_SVC --> CACHE_MGR
    
    EXCHANGE_SVC --> EXCHANGE_CLIENT
    USER_SVC --> TELEGRAM_CLIENT
    SUB_SVC --> PAYMENT_CLIENT
```

### Component Responsibilities

| Component | Responsibility | Technologies |
|-----------|----------------|--------------|
| **API Gateway** | Request routing, authentication, rate limiting | Spring Boot, Spring Security |
| **User Management** | User CRUD, profiles, roles | Spring Data JPA, PostgreSQL |
| **Subscription Service** | Plans, billing, usage tracking | Spring Boot, Payment APIs |
| **Coin Data Service** | Price data, market data, caching | Redis, External APIs |
| **Exchange Service** | Multi-exchange integration, arbitrage | WebClient, Circuit Breaker |
| **Tracking Service** | Price alerts, user preferences | Spring Events, Async Processing |
| **Notification Service** | Real-time notifications | WebSocket, Telegram Bot API |

---

## 🚀 Backend архитектура

### Layered Architecture Pattern

```mermaid
graph TB
    subgraph "Presentation Layer"
        CONTROLLERS[REST Controllers]
        WS_CONTROLLERS[WebSocket Controllers]
        EXCEPTION_HANDLERS[Exception Handlers]
    end
    
    subgraph "Business Layer"
        SERVICES[Business Services]
        VALIDATORS[Data Validators]
        MAPPERS[Entity Mappers]
    end
    
    subgraph "Data Access Layer"
        REPOSITORIES[JPA Repositories]
        ENTITIES[JPA Entities]
        LIQUIBASE_MIGRATIONS[Liquibase Migrations]
    end
    
    subgraph "Infrastructure Layer"
        CONFIGS[Configuration Classes]
        SECURITY_CONFIG[Security Configuration]
        CACHE_CONFIG[Cache Configuration]
        ASYNC_CONFIG[Async Configuration]
    end
    
    CONTROLLERS --> SERVICES
    WS_CONTROLLERS --> SERVICES
    SERVICES --> REPOSITORIES
    SERVICES --> VALIDATORS
    SERVICES --> MAPPERS
    
    REPOSITORIES --> ENTITIES
    ENTITIES --> LIQUIBASE_MIGRATIONS
    
    SERVICES -.-> CONFIGS
    CONTROLLERS -.-> SECURITY_CONFIG
    SERVICES -.-> CACHE_CONFIG
    SERVICES -.-> ASYNC_CONFIG
```

### Package Structure

```
backend/src/main/java/alg/coyote001/
├── controller/           # REST API Controllers
│   ├── AuthController.java
│   ├── UserManagementController.java
│   ├── SubscriptionController.java
│   └── CoinDataController.java
├── service/             # Business Logic
│   ├── impl/           # Service Implementations
│   ├── UserService.java
│   ├── SubscriptionService.java
│   └── CoinDataService.java
├── repository/          # Data Access
│   ├── UserRepository.java
│   ├── SubscriptionRepository.java
│   └── CoinDataRepository.java
├── entity/             # JPA Entities
│   ├── User.java
│   ├── Subscription.java
│   └── CoinData.java
├── dto/                # Data Transfer Objects
│   ├── request/
│   ├── response/
│   └── mapper/
├── config/             # Configuration
│   ├── SecurityConfig.java
│   ├── RedisConfig.java
│   └── WebSocketConfig.java
├── exception/          # Exception Handling
│   ├── GlobalExceptionHandler.java
│   └── custom/
└── util/              # Utilities
    ├── JwtUtil.java
    └── ValidationUtil.java
```

### Request Processing Flow

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant Service
    participant Repository
    participant Database
    participant Cache
    
    Client->>Controller: HTTP Request
    Controller->>Controller: Validation & Authentication
    Controller->>Service: Business Logic Call
    
    Service->>Cache: Check Cache
    alt Cache Hit
        Cache->>Service: Return Cached Data
    else Cache Miss
        Service->>Repository: Data Access
        Repository->>Database: SQL Query
        Database->>Repository: Result Set
        Repository->>Service: Entity Objects
        Service->>Cache: Store in Cache
    end
    
    Service->>Controller: Business Result
    Controller->>Client: HTTP Response
```

---

## 🎨 Frontend архитектура

### Component Architecture

```mermaid
graph TB
    subgraph "Application Shell"
        APP[App.tsx]
        ROUTER[React Router]
        LAYOUT[Layout Components]
    end
    
    subgraph "Feature Components"
        AUTH[Authentication]
        DASHBOARD[Dashboard]
        COINS[Coin Tracking]
        SUBSCRIPTION[Subscription]
        PROFILE[User Profile]
    end
    
    subgraph "Shared Components"
        UI_KIT[HeroUI Components]
        CHARTS[Chart Components]
        FORMS[Form Components]
        MODALS[Modal Components]
    end
    
    subgraph "State Management"
        CONTEXT[React Context]
        HOOKS[Custom Hooks]
        API[API Layer]
    end
    
    subgraph "Services"
        AUTH_SVC[Auth Service]
        API_CLIENT[API Client]
        WS_CLIENT[WebSocket Client]
        STORAGE[Local Storage]
    end
    
    APP --> ROUTER
    ROUTER --> LAYOUT
    LAYOUT --> AUTH
    LAYOUT --> DASHBOARD
    LAYOUT --> COINS
    LAYOUT --> SUBSCRIPTION
    LAYOUT --> PROFILE
    
    AUTH --> UI_KIT
    DASHBOARD --> CHARTS
    COINS --> FORMS
    SUBSCRIPTION --> MODALS
    
    AUTH --> CONTEXT
    DASHBOARD --> HOOKS
    COINS --> API
    
    CONTEXT --> AUTH_SVC
    HOOKS --> API_CLIENT
    API --> WS_CLIENT
    AUTH_SVC --> STORAGE
```

### Folder Structure

```
frontend/src/
├── components/          # Reusable Components
│   ├── ui/             # Basic UI Components
│   ├── charts/         # Chart Components
│   ├── forms/          # Form Components
│   └── layout/         # Layout Components
├── pages/              # Page Components
│   ├── auth/           # Authentication Pages
│   ├── dashboard/      # Dashboard Pages
│   ├── coins/          # Coin Tracking Pages
│   └── subscription/   # Subscription Pages
├── hooks/              # Custom React Hooks
│   ├── useAuth.ts
│   ├── useWebSocket.ts
│   └── useSubscription.ts
├── services/           # API Services
│   ├── authService.ts
│   ├── coinService.ts
│   └── subscriptionService.ts
├── context/            # React Context
│   ├── AuthContext.tsx
│   └── ThemeContext.tsx
├── types/              # TypeScript Types
│   ├── auth.ts
│   ├── coin.ts
│   └── subscription.ts
├── utils/              # Utility Functions
│   ├── validation.ts
│   ├── formatting.ts
│   └── constants.ts
└── assets/             # Static Assets
    ├── images/
    └── icons/
```

### Data Flow Architecture

```mermaid
graph LR
    subgraph "Component Tree"
        APP_COMP[App Component]
        PAGE_COMP[Page Component]
        FEATURE_COMP[Feature Component]
        UI_COMP[UI Component]
    end
    
    subgraph "State Layer"
        CONTEXT_STATE[Context State]
        LOCAL_STATE[Local State]
        FORM_STATE[Form State]
    end
    
    subgraph "API Layer"
        HTTP_CLIENT[HTTP Client]
        WS_CLIENT_FE[WebSocket Client]
        CACHE_FE[Frontend Cache]
    end
    
    subgraph "External"
        REST_API[REST API]
        WS_SERVER[WebSocket Server]
    end
    
    APP_COMP --> PAGE_COMP
    PAGE_COMP --> FEATURE_COMP
    FEATURE_COMP --> UI_COMP
    
    FEATURE_COMP <--> CONTEXT_STATE
    UI_COMP <--> LOCAL_STATE
    FEATURE_COMP <--> FORM_STATE
    
    CONTEXT_STATE --> HTTP_CLIENT
    LOCAL_STATE --> HTTP_CLIENT
    CONTEXT_STATE --> WS_CLIENT_FE
    
    HTTP_CLIENT --> CACHE_FE
    WS_CLIENT_FE --> CACHE_FE
    
    HTTP_CLIENT --> REST_API
    WS_CLIENT_FE --> WS_SERVER
```

---

## 🗄️ База данных

### Database Schema Overview

```mermaid
erDiagram
    User ||--o{ Subscription : has
    User ||--o{ TrackedCoin : owns
    User ||--o{ UserRole : has
    
    Subscription ||--o{ SubscriptionUsage : tracks
    Subscription ||--|| SubscriptionPlan : follows
    
    TrackedCoin }|--|| Coin : references
    
    Coin ||--o{ CoinPrice : has
    Exchange ||--o{ CoinPrice : provides
    
    User {
        bigint id PK
        varchar username UK
        varchar first_name
        varchar last_name
        varchar email
        varchar telegram_id UK
        varchar photo_url
        timestamp created_at
        timestamp last_login_at
        boolean is_active
    }
    
    Subscription {
        bigint id PK
        bigint user_id FK
        varchar plan_type
        varchar status
        timestamp start_date
        timestamp expires_at
        boolean auto_renewal
        timestamp created_at
    }
    
    TrackedCoin {
        bigint id PK
        bigint user_id FK
        bigint coin_id FK
        decimal target_price
        varchar price_alert
        boolean is_active
        timestamp created_at
    }
    
    Coin {
        bigint id PK
        varchar symbol UK
        varchar name
        varchar logo_url
        bigint market_cap
        decimal price
        timestamp last_updated
    }
    
    CoinPrice {
        bigint id PK
        bigint coin_id FK
        bigint exchange_id FK
        decimal price
        decimal volume
        timestamp timestamp
    }
```

### Database Performance Strategy

```mermaid
graph TB
    subgraph "Read Performance"
        READ_REPLICA[(Read Replica)]
        QUERY_CACHE[Query Result Cache]
        INDEX_OPT[Index Optimization]
    end
    
    subgraph "Write Performance"
        CONN_POOL[Connection Pooling]
        BATCH_WRITES[Batch Writes]
        ASYNC_WRITES[Async Processing]
    end
    
    subgraph "Caching Strategy"
        REDIS_CACHE[Redis Cache]
        APP_CACHE[Application Cache]
        QUERY_CACHE_2[Query Cache]
    end
    
    subgraph "Monitoring"
        SLOW_QUERY[Slow Query Log]
        PERF_MONITOR[Performance Monitor]
        INDEX_ADVISOR[Index Advisor]
    end
    
    READ_REPLICA --> QUERY_CACHE
    QUERY_CACHE --> INDEX_OPT
    
    CONN_POOL --> BATCH_WRITES
    BATCH_WRITES --> ASYNC_WRITES
    
    REDIS_CACHE --> APP_CACHE
    APP_CACHE --> QUERY_CACHE_2
    
    SLOW_QUERY --> PERF_MONITOR
    PERF_MONITOR --> INDEX_ADVISOR
```

### Critical Database Indexes

```sql
-- User authentication optimization
CREATE INDEX idx_user_telegram_id ON users(telegram_id);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_active ON users(is_active) WHERE is_active = true;

-- Subscription queries optimization  
CREATE INDEX idx_subscription_user_status ON subscriptions(user_id, status);
CREATE INDEX idx_subscription_expires ON subscriptions(expires_at) WHERE status = 'ACTIVE';

-- Coin data optimization
CREATE INDEX idx_coin_symbol ON coins(symbol);
CREATE INDEX idx_coin_price_timestamp ON coin_prices(coin_id, timestamp DESC);
CREATE INDEX idx_coin_price_exchange ON coin_prices(exchange_id, coin_id, timestamp DESC);

-- Tracking optimization
CREATE INDEX idx_tracked_coin_user_active ON tracked_coins(user_id, is_active);
CREATE INDEX idx_tracked_coin_price_alert ON tracked_coins(coin_id, price_alert, is_active);

-- Performance monitoring
CREATE INDEX idx_subscription_usage_period ON subscription_usage(user_id, period_start, period_end);
```

---

## 🔒 Система безопасности

### Security Architecture

```mermaid
graph TB
    subgraph "Authentication Layer"
        TELEGRAM_AUTH[Telegram OAuth]
        JWT_AUTH[JWT Token Service]
        SESSION_MGR[Session Manager]
    end
    
    subgraph "Authorization Layer"
        RBAC[Role-Based Access Control]
        PERMISSION_CHECK[Permission Checker]
        RESOURCE_GUARD[Resource Guards]
    end
    
    subgraph "API Protection"
        RATE_LIMITING[Rate Limiting]
        INPUT_VALIDATION[Input Validation]
        CSRF_PROTECTION[CSRF Protection]
        CORS_POLICY[CORS Policy]
    end
    
    subgraph "Data Protection"
        ENCRYPTION[Data Encryption]
        SENSITIVE_FILTER[Sensitive Data Filter]
        AUDIT_LOG[Audit Logging]
    end
    
    TELEGRAM_AUTH --> JWT_AUTH
    JWT_AUTH --> SESSION_MGR
    
    SESSION_MGR --> RBAC
    RBAC --> PERMISSION_CHECK
    PERMISSION_CHECK --> RESOURCE_GUARD
    
    RESOURCE_GUARD --> RATE_LIMITING
    RATE_LIMITING --> INPUT_VALIDATION
    INPUT_VALIDATION --> CSRF_PROTECTION
    CSRF_PROTECTION --> CORS_POLICY
    
    CORS_POLICY --> ENCRYPTION
    ENCRYPTION --> SENSITIVE_FILTER
    SENSITIVE_FILTER --> AUDIT_LOG
```

### Authentication Flow

```mermaid
sequenceDiagram
    participant Client
    participant Frontend
    participant AuthController
    participant AuthService
    participant TelegramAPI
    participant JWTService
    participant Database
    
    Client->>Frontend: Initiate Login
    Frontend->>TelegramAPI: Telegram Widget Auth
    TelegramAPI->>Frontend: Auth Data + Hash
    Frontend->>AuthController: POST /auth/telegram
    AuthController->>AuthService: validateTelegramAuth()
    AuthService->>AuthService: Verify Hash
    AuthService->>Database: Find/Create User
    Database->>AuthService: User Entity
    AuthService->>JWTService: Generate Tokens
    JWTService->>AuthService: Access + Refresh Tokens
    AuthService->>AuthController: Authentication Result
    AuthController->>Frontend: JWT Tokens + User Data
    Frontend->>Frontend: Store Tokens
    Frontend->>Client: Login Success
```

### Authorization Roles & Permissions

```mermaid
graph TB
    subgraph "Role Hierarchy"
        SUPER_ADMIN[Super Admin]
        ADMIN[Admin]
        MODERATOR[Moderator]
        PREMIUM_USER[Premium User]
        FREE_USER[Free User]
    end
    
    subgraph "Permissions"
        SYSTEM_CONFIG[System Configuration]
        USER_MANAGEMENT[User Management]
        CONTENT_MODERATION[Content Moderation]
        PREMIUM_FEATURES[Premium Features]
        BASIC_FEATURES[Basic Features]
    end
    
    SUPER_ADMIN --> SYSTEM_CONFIG
    SUPER_ADMIN --> USER_MANAGEMENT
    SUPER_ADMIN --> CONTENT_MODERATION
    SUPER_ADMIN --> PREMIUM_FEATURES
    SUPER_ADMIN --> BASIC_FEATURES
    
    ADMIN --> USER_MANAGEMENT
    ADMIN --> CONTENT_MODERATION
    ADMIN --> PREMIUM_FEATURES
    ADMIN --> BASIC_FEATURES
    
    MODERATOR --> CONTENT_MODERATION
    MODERATOR --> BASIC_FEATURES
    
    PREMIUM_USER --> PREMIUM_FEATURES
    PREMIUM_USER --> BASIC_FEATURES
    
    FREE_USER --> BASIC_FEATURES
```

---

## 🔗 Интеграции

### External API Integration

```mermaid
graph TB
    subgraph "Exchange APIs"
        BINANCE_API[Binance API]
        OKEX_API[OKX API]
        BYBIT_API[Bybit API]
    end
    
    subgraph "Social APIs"
        TELEGRAM_API[Telegram Bot API]
        TWITTER_API[Twitter API]
    end
    
    subgraph "Payment APIs"
        STRIPE_API[Stripe API]
        PAYPAL_API[PayPal API]
    end
    
    subgraph "Integration Layer"
        API_CLIENT[HTTP Client]
        CIRCUIT_BREAKER[Circuit Breaker]
        RETRY_POLICY[Retry Policy]
        CACHE_LAYER[Response Cache]
    end
    
    subgraph "Error Handling"
        FALLBACK_DATA[Fallback Data]
        ERROR_ALERT[Error Alerting]
        HEALTH_CHECK[Health Monitoring]
    end
    
    BINANCE_API --> API_CLIENT
    OKEX_API --> API_CLIENT
    BYBIT_API --> API_CLIENT
    TELEGRAM_API --> API_CLIENT
    TWITTER_API --> API_CLIENT
    STRIPE_API --> API_CLIENT
    PAYPAL_API --> API_CLIENT
    
    API_CLIENT --> CIRCUIT_BREAKER
    CIRCUIT_BREAKER --> RETRY_POLICY
    RETRY_POLICY --> CACHE_LAYER
    
    CIRCUIT_BREAKER --> FALLBACK_DATA
    API_CLIENT --> ERROR_ALERT
    ERROR_ALERT --> HEALTH_CHECK
```

### API Integration Patterns

#### Circuit Breaker Pattern
```java
@Component
public class ExchangeApiClient {
    
    @CircuitBreaker(name = "binance-api", fallbackMethod = "getBinancePriceFallback")
    @Retry(name = "binance-api")
    @TimeLimiter(name = "binance-api")
    public CompletableFuture<PriceData> getBinancePrice(String symbol) {
        // API call implementation
    }
    
    public CompletableFuture<PriceData> getBinancePriceFallback(String symbol, Exception ex) {
        // Fallback implementation
        return getCachedPrice(symbol);
    }
}
```

#### Rate Limiting Strategy
```mermaid
graph LR
    subgraph "Rate Limiting"
        TOKEN_BUCKET[Token Bucket]
        SLIDING_WINDOW[Sliding Window]
        FIXED_WINDOW[Fixed Window]
    end
    
    subgraph "APIs"
        BINANCE_LIMIT[Binance: 1200/min]
        OKEX_LIMIT[OKX: 600/min]
        TELEGRAM_LIMIT[Telegram: 30/sec]
    end
    
    TOKEN_BUCKET --> BINANCE_LIMIT
    SLIDING_WINDOW --> OKEX_LIMIT
    FIXED_WINDOW --> TELEGRAM_LIMIT
```

---

## 🚢 Развертывание

### Deployment Architecture

```mermaid
graph TB
    subgraph "Production Environment"
        LB_PROD[Load Balancer]
        APP1[App Instance 1]
        APP2[App Instance 2]
        APP3[App Instance 3]
        DB_MASTER[(Database Master)]
        DB_REPLICA[(Database Replica)]
        REDIS_CLUSTER[Redis Cluster]
    end
    
    subgraph "Staging Environment"
        APP_STAGING[Staging App]
        DB_STAGING[(Staging DB)]
        REDIS_STAGING[Staging Redis]
    end
    
    subgraph "CI/CD Pipeline"
        GITHUB[GitHub Repository]
        ACTIONS[GitHub Actions]
        DOCKER_REGISTRY[Docker Registry]
        DEPLOY_SCRIPT[Deployment Scripts]
    end
    
    subgraph "Monitoring"
        PROMETHEUS[Prometheus]
        GRAFANA[Grafana]
        ALERTMANAGER[Alert Manager]
    end
    
    GITHUB --> ACTIONS
    ACTIONS --> DOCKER_REGISTRY
    DOCKER_REGISTRY --> DEPLOY_SCRIPT
    
    DEPLOY_SCRIPT --> APP_STAGING
    DEPLOY_SCRIPT --> LB_PROD
    
    LB_PROD --> APP1
    LB_PROD --> APP2
    LB_PROD --> APP3
    
    APP1 --> DB_MASTER
    APP2 --> DB_REPLICA
    APP3 --> REDIS_CLUSTER
    
    APP1 -.-> PROMETHEUS
    APP2 -.-> PROMETHEUS
    APP3 -.-> PROMETHEUS
    PROMETHEUS --> GRAFANA
    PROMETHEUS --> ALERTMANAGER
```

### Container Strategy

```dockerfile
# Multi-stage build for production
FROM openjdk:17-jre-slim as production

# Security: non-root user
RUN addgroup --system ct01 && adduser --system --group ct01

# Application setup
COPY target/ct01-api.jar /app/ct01-api.jar
COPY docker/entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh
RUN chown -R ct01:ct01 /app

USER ct01

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]
```

### Environment Configuration

```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  app:
    image: ct01/api:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - DATABASE_URL=${DATABASE_URL}
      - REDIS_URL=${REDIS_URL}
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - app
```

---

## 📊 Мониторинг и логирование

### Monitoring Stack

```mermaid
graph TB
    subgraph "Application Metrics"
        SPRING_ACTUATOR[Spring Actuator]
        MICROMETER[Micrometer]
        CUSTOM_METRICS[Custom Metrics]
    end
    
    subgraph "Infrastructure Metrics"
        DOCKER_STATS[Docker Stats]
        SYSTEM_METRICS[System Metrics]
        DATABASE_METRICS[Database Metrics]
    end
    
    subgraph "Collection Layer"
        PROMETHEUS_SERVER[Prometheus Server]
        GRAFANA_DASHBOARD[Grafana Dashboards]
        ALERT_MANAGER[Alert Manager]
    end
    
    subgraph "Logging Stack"
        APP_LOGS[Application Logs]
        LOGBACK[Logback]
        LOG_AGGREGATION[Log Aggregation]
    end
    
    subgraph "Alerting"
        SLACK_ALERTS[Slack Notifications]
        EMAIL_ALERTS[Email Alerts]
        SMS_ALERTS[SMS Alerts]
    end
    
    SPRING_ACTUATOR --> PROMETHEUS_SERVER
    MICROMETER --> PROMETHEUS_SERVER
    CUSTOM_METRICS --> PROMETHEUS_SERVER
    
    DOCKER_STATS --> PROMETHEUS_SERVER
    SYSTEM_METRICS --> PROMETHEUS_SERVER
    DATABASE_METRICS --> PROMETHEUS_SERVER
    
    PROMETHEUS_SERVER --> GRAFANA_DASHBOARD
    PROMETHEUS_SERVER --> ALERT_MANAGER
    
    APP_LOGS --> LOGBACK
    LOGBACK --> LOG_AGGREGATION
    
    ALERT_MANAGER --> SLACK_ALERTS
    ALERT_MANAGER --> EMAIL_ALERTS
    ALERT_MANAGER --> SMS_ALERTS
```

### Key Metrics Dashboard

```yaml
# Key Performance Indicators (KPIs)
metrics:
  application:
    - name: "Request Rate"
      query: "rate(http_requests_total[5m])"
      threshold: "100 req/sec"
    
    - name: "Response Time P99"
      query: "histogram_quantile(0.99, http_request_duration_seconds_bucket)"
      threshold: "< 2s"
    
    - name: "Error Rate"
      query: "rate(http_requests_total{status=~'5..'}[5m])"
      threshold: "< 1%"
  
  business:
    - name: "Active Users"
      query: "active_users_total"
      threshold: "> 100"
    
    - name: "Subscription Conversions"
      query: "rate(subscription_upgrades_total[1h])"
      threshold: "> 5%"
    
    - name: "API Usage by Plan"
      query: "sum by (plan_type) (rate(api_calls_total[5m]))"
      threshold: "Within limits"

  infrastructure:
    - name: "CPU Usage"
      query: "avg(cpu_usage_percent)"
      threshold: "< 80%"
    
    - name: "Memory Usage"
      query: "avg(memory_usage_percent)"
      threshold: "< 85%"
    
    - name: "Database Connections"
      query: "db_connections_active"
      threshold: "< 80% of pool"
```

---

## 📈 Scalability Strategy

### Horizontal Scaling Plan

```mermaid
graph TB
    subgraph "Current State (Phase 1)"
        LB_CURRENT[Load Balancer]
        APP_CURRENT[Single App Instance]
        DB_CURRENT[(PostgreSQL)]
        REDIS_CURRENT[Redis]
    end
    
    subgraph "Phase 2: Multiple Instances"
        LB_P2[Load Balancer]
        APP1_P2[App Instance 1]
        APP2_P2[App Instance 2]
        APP3_P2[App Instance 3]
        DB_P2[(PostgreSQL)]
        REDIS_P2[Redis]
    end
    
    subgraph "Phase 3: Database Scaling"
        LB_P3[Load Balancer]
        APP_CLUSTER_P3[App Cluster]
        DB_MASTER_P3[(Master DB)]
        DB_REPLICA1_P3[(Read Replica 1)]
        DB_REPLICA2_P3[(Read Replica 2)]
        REDIS_CLUSTER_P3[Redis Cluster]
    end
    
    subgraph "Phase 4: Microservices"
        API_GATEWAY[API Gateway]
        USER_SERVICE[User Service]
        COIN_SERVICE[Coin Service]
        SUB_SERVICE[Subscription Service]
        NOTIFICATION_SERVICE[Notification Service]
        SHARED_DB[(Shared Database)]
        SERVICE_DBs[(Service Databases)]
    end
    
    LB_CURRENT --> APP_CURRENT
    APP_CURRENT --> DB_CURRENT
    APP_CURRENT --> REDIS_CURRENT
    
    LB_P2 --> APP1_P2
    LB_P2 --> APP2_P2
    LB_P2 --> APP3_P2
    
    LB_P3 --> APP_CLUSTER_P3
    APP_CLUSTER_P3 --> DB_MASTER_P3
    APP_CLUSTER_P3 --> DB_REPLICA1_P3
    APP_CLUSTER_P3 --> DB_REPLICA2_P3
    
    API_GATEWAY --> USER_SERVICE
    API_GATEWAY --> COIN_SERVICE
    API_GATEWAY --> SUB_SERVICE
    API_GATEWAY --> NOTIFICATION_SERVICE
```

### Performance Optimization Strategy

```mermaid
graph LR
    subgraph "Database Optimization"
        INDEXING[Index Optimization]
        QUERY_OPT[Query Optimization]
        CONN_POOL[Connection Pooling]
        READ_REPLICA[Read Replicas]
    end
    
    subgraph "Caching Strategy"
        L1_CACHE[L1: Application Cache]
        L2_CACHE[L2: Redis Cache]
        L3_CACHE[L3: CDN Cache]
        CACHE_WARM[Cache Warming]
    end
    
    subgraph "Application Optimization"
        ASYNC_PROC[Async Processing]
        BULK_OPS[Bulk Operations]
        LAZY_LOAD[Lazy Loading]
        COMPRESSION[Response Compression]
    end
    
    subgraph "Infrastructure"
        LOAD_BALANCE[Load Balancing]
        AUTO_SCALE[Auto Scaling]
        RESOURCE_OPT[Resource Optimization]
        CDN[Content Delivery Network]
    end
    
    INDEXING --> L1_CACHE
    QUERY_OPT --> L2_CACHE
    CONN_POOL --> L3_CACHE
    READ_REPLICA --> CACHE_WARM
    
    L1_CACHE --> ASYNC_PROC
    L2_CACHE --> BULK_OPS
    L3_CACHE --> LAZY_LOAD
    CACHE_WARM --> COMPRESSION
    
    ASYNC_PROC --> LOAD_BALANCE
    BULK_OPS --> AUTO_SCALE
    LAZY_LOAD --> RESOURCE_OPT
    COMPRESSION --> CDN
```

---

## 🔧 Development & Deployment Best Practices

### Code Quality Standards

```mermaid
graph TB
    subgraph "Code Standards"
        CHECKSTYLE[Checkstyle]
        SPOTBUGS[SpotBugs]
        PMD[PMD Analysis]
        SONAR[SonarQube]
    end
    
    subgraph "Testing Strategy"
        UNIT_TESTS[Unit Tests (80%)]
        INTEGRATION_TESTS[Integration Tests (15%)]
        E2E_TESTS[E2E Tests (5%)]
        PERFORMANCE_TESTS[Performance Tests]
    end
    
    subgraph "CI/CD Pipeline"
        BUILD[Build & Compile]
        TEST[Run Tests]
        SECURITY_SCAN[Security Scan]
        DEPLOY[Deploy]
    end
    
    subgraph "Quality Gates"
        CODE_COVERAGE[Code Coverage > 80%]
        SECURITY_CHECK[Security Scan Pass]
        PERFORMANCE_CHECK[Performance Baseline]
        MANUAL_REVIEW[Manual Code Review]
    end
    
    CHECKSTYLE --> BUILD
    SPOTBUGS --> BUILD
    PMD --> BUILD
    SONAR --> BUILD
    
    UNIT_TESTS --> TEST
    INTEGRATION_TESTS --> TEST
    E2E_TESTS --> TEST
    PERFORMANCE_TESTS --> TEST
    
    BUILD --> TEST
    TEST --> SECURITY_SCAN
    SECURITY_SCAN --> DEPLOY
    
    CODE_COVERAGE --> DEPLOY
    SECURITY_CHECK --> DEPLOY
    PERFORMANCE_CHECK --> DEPLOY
    MANUAL_REVIEW --> DEPLOY
```

---

## 📚 Resources & Documentation

### Architecture Decision Records (ADRs)

| Decision | Status | Date | Description |
|----------|--------|------|-------------|
| [ADR-001](./adr/001-spring-boot-framework.md) | Accepted | 2024-01-01 | Choice of Spring Boot as main framework |
| [ADR-002](./adr/002-postgresql-database.md) | Accepted | 2024-01-01 | PostgreSQL as primary database |
| [ADR-003](./adr/003-redis-caching.md) | Accepted | 2024-01-02 | Redis for caching and session management |
| [ADR-004](./adr/004-jwt-authentication.md) | Accepted | 2024-01-03 | JWT-based authentication strategy |
| [ADR-005](./adr/005-react-frontend.md) | Accepted | 2024-01-05 | React with TypeScript for frontend |

### Reference Links

- **[Spring Boot Documentation](https://spring.io/projects/spring-boot)**
- **[PostgreSQL Best Practices](https://wiki.postgresql.org/wiki/Performance_Optimization)**
- **[Redis Optimization Guide](https://redis.io/docs/management/optimization/)**
- **[React Performance Patterns](https://react.dev/learn/render-and-commit)**
- **[Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)**

---

<div align="center">

**[⬆️ Вернуться к началу](#system-architecture)**

🏗️ **Architecture is not about the design decisions you make, but about the decisions you delay** 🏗️

</div> 