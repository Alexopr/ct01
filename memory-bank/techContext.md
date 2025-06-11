# Technical Context - Cryptocurrency Dashboard

## 🛠 Технологический стек

### Backend Stack
```yaml
Framework: Spring Boot 3.4.5
Language: Java 17
Security: Spring Security 6.2.6
Database: PostgreSQL 13+
Cache: Redis 6+
Build Tool: Maven 3.9+
ORM: Spring Data JPA + Hibernate
Migrations: Flyway
Testing: JUnit 5, Mockito, TestContainers
Documentation: OpenAPI 3 (Swagger)
```

### Frontend Stack
```yaml
Framework: React 18
Language: TypeScript 5+
UI Library: NextUI (Material Design)
Build Tool: Vite 5+
State Management: Context API + React Hooks
HTTP Client: Axios
Routing: React Router v6
Styling: TailwindCSS + NextUI components
Testing: Jest, React Testing Library
```

### Infrastructure & DevOps
```yaml
Containerization: Docker + Docker Compose
Database: PostgreSQL (production), H2 (testing)
Caching: Redis (session storage, API cache)
Reverse Proxy: Nginx (production)
Process Manager: PM2 (production)
Monitoring: Spring Actuator + custom metrics
```

## 🏗 Архитектурные паттерны

### Backend Architecture (Layered + DDD)
```
┌─────────────────────────────────────┐
│            Controllers              │ ← REST API Layer
├─────────────────────────────────────┤
│             Services                │ ← Business Logic
├─────────────────────────────────────┤
│            Repositories             │ ← Data Access Layer
├─────────────────────────────────────┤
│             Entities                │ ← Domain Models
└─────────────────────────────────────┘
```

### Key Design Patterns
- **Repository Pattern** - для абстракции доступа к данным
- **Service Layer Pattern** - для бизнес-логики
- **DTO Pattern** - для transfer objects между слоями
- **Factory Pattern** - для создания сложных объектов
- **Observer Pattern** - для WebSocket уведомлений
- **Strategy Pattern** - для различных типов аутентификации

## 🔐 Security Implementation

### Authentication & Authorization
```java
// Spring Security Configuration
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    // Session-based auth + CSRF protection
    // Rate limiting per IP
    // Role-based access control (@PreAuthorize)
}
```

### Security Features
- **CSRF Protection** - токен-based защита
- **Rate Limiting** - защита от DoS атак
- **Session Management** - secure session handling
- **Password Encryption** - BCrypt with strength 12
- **CORS Configuration** - ограниченные домены
- **SQL Injection Protection** - JPA prepared statements

## 📊 Database Design

### Core Entities
```sql
-- Основные таблицы
users              -- Пользователи системы
roles              -- Роли (USER, ADMIN, PREMIUM)
user_roles         -- Связь пользователей и ролей
permissions        -- Детальные разрешения
role_permissions   -- Связь ролей и разрешений

-- Крипто данные
coins              -- Информация о криптовалютах
exchanges          -- Биржи
coin_prices        -- Исторические цены
tracked_coins      -- Отслеживаемые пользователем монеты
price_alerts       -- Настройки алертов

-- Система
settings           -- Системные настройки
audit_logs         -- Логи действий пользователей
sessions           -- Активные сессии пользователей
```

### Database Constraints
- **Foreign Keys** - обеспечение ссылочной целостности
- **Unique Constraints** - предотвращение дублирования
- **Check Constraints** - валидация данных на уровне БД
- **Indexes** - оптимизация запросов

## 🚀 Performance Optimizations

### Caching Strategy
```java
// Redis Cache Configuration
@Cacheable(value = "coinPrices", key = "#coinId")
public CoinPrice getCoinPrice(String coinId) {
    // Cache expensive API calls
}

@CacheEvict(value = "coinPrices", allEntries = true)
@Scheduled(fixedRate = 60000) // Refresh every minute
public void refreshPriceCache() {
    // Periodic cache invalidation
}
```

### Database Optimizations
- **Connection Pooling** - HikariCP для эффективного использования соединений
- **Query Optimization** - N+1 problem решается через @EntityGraph
- **Pagination** - Spring Data Pageable для больших datasets
- **Indexes** - на часто запрашиваемые поля

## 🔄 Real-time Features

### WebSocket Implementation
```java
@Controller
public class PriceWebSocketController {
    @MessageMapping("/subscribe/{coinId}")
    @SendTo("/topic/prices/{coinId}")
    public PriceUpdate subscribeToPriceUpdates(@DestinationVariable String coinId) {
        // Real-time price broadcasting
    }
}
```

### Event-Driven Architecture
- **ApplicationEvents** - для внутренней коммуникации
- **@EventListener** - асинхронная обработка событий
- **WebSocket** - для real-time updates в UI

## 🧪 Testing Strategy

### Backend Testing
```java
// Integration Tests
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class UserServiceIntegrationTest {
    // Full context testing
}

// Unit Tests
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    // Isolated component testing
}
```

### Testing Levels
- **Unit Tests** - изолированное тестирование компонентов
- **Integration Tests** - тестирование взаимодействий
- **Repository Tests** - @DataJpaTest для database layer
- **Web Layer Tests** - @WebMvcTest для controllers
- **Security Tests** - тестирование авторизации и аутентификации

## 🔧 Development Tools & Workflow

### Development Environment
```bash
# Required versions
Java: 17+
Maven: 3.9+
Node.js: 18+
PostgreSQL: 13+
Redis: 6+
Docker: 20+
```

### Code Quality Tools
- **Checkstyle** - code formatting standards
- **SpotBugs** - static analysis
- **SonarQube** - code quality metrics
- **ESLint + Prettier** - frontend code quality

## 🌐 API Design

### REST API Standards
- **RESTful URLs** - resource-based naming
- **HTTP Status Codes** - правильное использование
- **Pagination** - для больших datasets
- **Filtering** - query parameters для фильтрации
- **Versioning** - через URL path (/api/v1/)

### Response Format
```json
{
  "data": {}, 
  "success": true,
  "message": "Operation completed successfully",
  "errors": [],
  "metadata": {
    "timestamp": "2024-01-01T12:00:00Z",
    "version": "1.0",
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 100
    }
  }
}
```

## 🔍 Monitoring & Observability

### Application Monitoring
- **Spring Actuator** - health checks, metrics
- **Custom Metrics** - бизнес-метрики через Micrometer
- **Logging** - структурированное логирование (JSON)
- **Error Tracking** - централизованная обработка ошибок

### Performance Monitoring
- **Response Time** - отслеживание времени ответа API
- **Database Performance** - мониторинг медленных запросов
- **Cache Hit Ratio** - эффективность кэширования
- **WebSocket Connections** - количество активных соединений 