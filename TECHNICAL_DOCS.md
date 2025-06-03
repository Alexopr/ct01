# Техническая документация

## 🏗 Архитектурные решения

### Backend Architecture Patterns

#### 1. Layered Architecture (Слоистая архитектура)
```
Presentation Layer (Controllers) 
    ↓
Business Layer (Services)
    ↓  
Data Access Layer (Repositories)
    ↓
Database Layer (PostgreSQL/Redis)
```

#### 2. Security Architecture
- **Spring Security** с методом аннотаций `@PreAuthorize`
- **RBAC** (Role-Based Access Control) с детализированными разрешениями
- **Session-based** аутентификация с CSRF защитой
- **Telegram OAuth** интеграция

#### 3. Caching Strategy
```java
@Cacheable(value = "coins", key = "#symbol")
public Coin findBySymbol(String symbol) {
    // Кэширование в Redis
}

@CacheEvict(value = "coins", allEntries = true)
public void clearCoinsCache() {
    // Очистка кэша при обновлениях
}
```

### Frontend Architecture Patterns

#### 1. Component Architecture
```
Pages (Route Components)
    ↓
Feature Components
    ↓
UI Components (Reusable)
    ↓
Base Components (Material-UI)
```

#### 2. State Management
- **Context API** для глобального состояния
- **Local State** для компонентного состояния
- **API State** управляется через сервисы

#### 3. Type Safety
- **Полная типизация** всех API интерфейсов
- **Строгие типы** для форм и валидации
- **Type Guards** для runtime проверок

## 🔧 Паттерны кода

### Backend Patterns

#### 1. Service Layer Pattern
```java
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    public User create(UserFormData userData) {
        // Валидация, преобразование, сохранение
    }
}
```

#### 2. Repository Pattern
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
}
```

#### 3. DTO Pattern
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private List<RoleDto> roles;
    
    // Конвертация из Entity
    public static UserDto from(User user) {
        return UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .roles(user.getRoles().stream()
                .map(RoleDto::from)
                .collect(Collectors.toList()))
            .build();
    }
}
```

#### 4. Exception Handling Pattern
```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(ex.getStatus().value())
            .error(ex.getError())
            .message(ex.getMessage())
            .details(ex.getDetails())
            .build();
            
        return ResponseEntity.status(ex.getStatus()).body(error);
    }
}
```

### Frontend Patterns

#### 1. Service Layer Pattern
```typescript
class ApiService {
  private api: AxiosInstance;
  
  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      withCredentials: true,
    });
    
    this.setupInterceptors();
  }
  
  private setupInterceptors() {
    // Request/Response interceptors
  }
  
  async get<T>(url: string, params?: any): Promise<T> {
    const response = await this.api.get<T>(url, { params });
    return response.data;
  }
}
```

#### 2. Custom Hooks Pattern
```typescript
export const useUsers = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const fetchUsers = useCallback(async () => {
    try {
      setLoading(true);
      const response = await userService.getUsers();
      setUsers(response.content);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);
  
  return { users, loading, error, fetchUsers };
};
```

#### 3. Context Pattern
```typescript
interface AuthContextType {
  user: User | null;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  isAuthenticated: boolean;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
```

## 🔐 Безопасность

### Backend Security Implementation

#### 1. Method-Level Security
```java
@PreAuthorize("hasAuthority('USER_READ') or hasRole('ADMIN')")
public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
    // Только пользователи с правами USER_READ или роль ADMIN
}

@PreAuthorize("@userService.isCurrentUser(#userId, authentication.name) or hasAuthority('USER_WRITE')")
public ResponseEntity<UserDto> updateUser(@PathVariable Long userId, @RequestBody UserDto userDto) {
    // Пользователь может редактировать свой профиль или иметь права USER_WRITE
}
```

#### 2. CORS Configuration
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

#### 3. Password Security
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // Увеличенная сложность
}
```

### Frontend Security Implementation

#### 1. Protected Routes
```typescript
const ProtectedRoute: React.FC<{ children: React.ReactNode; requiredRole?: string }> = ({ 
  children, 
  requiredRole 
}) => {
  const { user, isAuthenticated } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  if (requiredRole && !user?.roles.some(role => role.name === requiredRole)) {
    return <Navigate to="/unauthorized" replace />;
  }
  
  return <>{children}</>;
};
```

#### 2. API Security
```typescript
// Автоматическое добавление CSRF токена
private getCsrfToken(): string | null {
  const cookies = document.cookie.split(';');
  for (const cookie of cookies) {
    const [name, value] = cookie.trim().split('=');
    if (name === 'XSRF-TOKEN') {
      return decodeURIComponent(value);
    }
  }
  return null;
}
```

## 📊 Производительность

### Backend Performance

#### 1. Database Optimization
```java
// Использование индексов
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_telegram_id", columnList = "telegram_id")
})
public class User {
    // ...
}

// Оптимизированные запросы
@Query("SELECT u FROM User u JOIN FETCH u.roles r JOIN FETCH r.permissions WHERE u.username = :username")
Optional<User> findByUsernameWithRolesAndPermissions(@Param("username") String username);
```

#### 2. Caching Strategy
```java
// Кэширование на уровне сервиса
@Cacheable(value = "exchangeRates", key = "#coinSymbol + '_' + #exchangeName")
public BigDecimal getCurrentPrice(String coinSymbol, String exchangeName) {
    return exchangeAdapter.getCurrentPrice(coinSymbol);
}

// Кэширование с TTL
@Cacheable(value = "userSessions", key = "#sessionId")
@CacheEvict(value = "userSessions", key = "#sessionId", condition = "#result == null")
public UserSession getSession(String sessionId) {
    // ...
}
```

#### 3. Connection Pooling
```properties
# HikariCP настройки
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=20000
```

### Frontend Performance

#### 1. Code Splitting
```typescript
// Lazy loading компонентов
const AdminPanel = lazy(() => import('./pages/AdminPanel'));
const UserManagement = lazy(() => import('./pages/UserManagement'));

// В роутере
<Route 
  path="/admin" 
  element={
    <Suspense fallback={<CircularProgress />}>
      <AdminPanel />
    </Suspense>
  } 
/>
```

#### 2. Memoization
```typescript
// Мемоизация компонентов
const UserCard = React.memo<UserCardProps>(({ user, onEdit, onDelete }) => {
  return (
    <Card>
      <CardContent>
        <Typography>{user.username}</Typography>
        <Button onClick={() => onEdit(user.id)}>Edit</Button>
        <Button onClick={() => onDelete(user.id)}>Delete</Button>
      </CardContent>
    </Card>
  );
});

// Мемоизация вычислений
const filteredUsers = useMemo(() => {
  return users.filter(user => 
    user.username.toLowerCase().includes(searchTerm.toLowerCase())
  );
}, [users, searchTerm]);
```

## 🧪 Тестирование

### Backend Testing Patterns

#### 1. Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldCreateUserSuccessfully() {
        // Given
        UserFormData userData = UserFormData.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password123")
            .build();
            
        User savedUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();
            
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        
        // When
        User result = userService.createUser(userData);
        
        // Then
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }
}
```

#### 2. Integration Tests
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class UserControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldCreateUserViaApi() {
        // Given
        UserFormData userData = UserFormData.builder()
            .username("apiuser")
            .email("api@example.com")
            .password("password123")
            .build();
            
        // When
        ResponseEntity<UserDto> response = restTemplate.postForEntity(
            "/api/v1/users", 
            userData, 
            UserDto.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getUsername()).isEqualTo("apiuser");
        
        // Verify in database
        Optional<User> savedUser = userRepository.findByUsername("apiuser");
        assertThat(savedUser).isPresent();
    }
}
```

### Frontend Testing Patterns

#### 1. Component Tests
```typescript
describe('UserCard', () => {
  const mockUser: User = {
    id: 1,
    username: 'testuser',
    email: 'test@example.com',
    roles: [],
    enabled: true,
    createdAt: '2023-01-01T00:00:00Z',
    updatedAt: '2023-01-01T00:00:00Z'
  };
  
  it('should render user information', () => {
    const onEdit = jest.fn();
    const onDelete = jest.fn();
    
    render(<UserCard user={mockUser} onEdit={onEdit} onDelete={onDelete} />);
    
    expect(screen.getByText('testuser')).toBeInTheDocument();
    expect(screen.getByText('test@example.com')).toBeInTheDocument();
  });
  
  it('should call onEdit when edit button is clicked', () => {
    const onEdit = jest.fn();
    const onDelete = jest.fn();
    
    render(<UserCard user={mockUser} onEdit={onEdit} onDelete={onDelete} />);
    
    fireEvent.click(screen.getByText('Edit'));
    
    expect(onEdit).toHaveBeenCalledWith(1);
  });
});
```

#### 2. API Service Tests
```typescript
describe('ApiService', () => {
  let apiService: ApiService;
  
  beforeEach(() => {
    apiService = new ApiService();
  });
  
  it('should login successfully', async () => {
    const mockResponse: AuthResponse = {
      user: mockUser,
      sessionId: 'session123',
      expiresAt: '2023-12-31T23:59:59Z'
    };
    
    jest.spyOn(axios, 'post').mockResolvedValue({ data: mockResponse });
    
    const result = await apiService.login({
      username: 'testuser',
      password: 'password123'
    });
    
    expect(result).toEqual(mockResponse);
    expect(axios.post).toHaveBeenCalledWith('/auth/login', {
      username: 'testuser',
      password: 'password123'
    });
  });
});
```

## 🚀 Развертывание

### Production Checklist

#### Backend
- [ ] Environment variables настроены
- [ ] Database migrations выполнены
- [ ] Redis подключен и настроен
- [ ] CORS настроен для production доменов
- [ ] Логирование настроено (уровень WARN/ERROR)
- [ ] Health checks работают
- [ ] SSL сертификаты установлены
- [ ] Backup стратегия настроена

#### Frontend
- [ ] Build оптимизирован для production
- [ ] Environment variables настроены
- [ ] CDN настроен для статических ресурсов
- [ ] Gzip compression включен
- [ ] Security headers настроены
- [ ] Analytics подключена (если нужна)

### Monitoring

#### Application Metrics
```java
// Custom metrics
@Component
public class UserMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter userLoginCounter;
    private final Timer userCreationTimer;
    
    public UserMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.userLoginCounter = Counter.builder("user.login.count")
            .description("Number of user logins")
            .register(meterRegistry);
        this.userCreationTimer = Timer.builder("user.creation.time")
            .description("Time taken to create a user")
            .register(meterRegistry);
    }
    
    public void incrementLoginCount() {
        userLoginCounter.increment();
    }
    
    public void recordUserCreationTime(Duration duration) {
        userCreationTimer.record(duration);
    }
}
```

#### Health Checks
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final UserRepository userRepository;
    
    @Override
    public Health health() {
        try {
            long userCount = userRepository.count();
            return Health.up()
                .withDetail("userCount", userCount)
                .withDetail("status", "Database is accessible")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 📝 Соглашения о коде

### Backend Code Conventions

#### 1. Naming Conventions
```java
// Classes: PascalCase
public class UserManagementController { }

// Methods: camelCase
public ResponseEntity<UserDto> createUser() { }

// Constants: UPPER_SNAKE_CASE
private static final String DEFAULT_ROLE = "USER";

// Variables: camelCase
private final UserService userService;
```

#### 2. Package Structure
```
alg.coyote001
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data access
├── entity/         # JPA entities
├── dto/           # Data transfer objects
├── config/        # Configuration classes
├── security/      # Security configuration
├── exception/     # Exception handling
└── util/          # Utility classes
```

### Frontend Code Conventions

#### 1. File Naming
```
components/
├── UserCard.tsx           # PascalCase for components
├── userService.ts         # camelCase for services
├── api.ts                # lowercase for utilities
└── types/
    └── api.ts            # lowercase for type definitions
```

#### 2. Component Structure
```typescript
// Component props interface
interface UserCardProps {
  user: User;
  onEdit: (id: number) => void;
  onDelete: (id: number) => void;
}

// Component implementation
export const UserCard: React.FC<UserCardProps> = ({ user, onEdit, onDelete }) => {
  // Hooks at the top
  const [loading, setLoading] = useState(false);
  
  // Event handlers
  const handleEdit = useCallback(() => {
    onEdit(user.id);
  }, [user.id, onEdit]);
  
  // Render
  return (
    <Card>
      {/* JSX */}
    </Card>
  );
};
```

## 🔄 Процесс разработки

### Git Workflow
1. **Feature branches**: `feature/user-management`
2. **Bug fixes**: `bugfix/login-error`
3. **Hotfixes**: `hotfix/security-patch`
4. **Releases**: `release/v1.0.0`

### Commit Messages
```
feat(user): add user creation functionality
fix(auth): resolve login session timeout issue
docs(api): update authentication endpoints documentation
refactor(service): simplify user service methods
test(user): add unit tests for user service
```

### Code Review Checklist
- [ ] Код соответствует стандартам проекта
- [ ] Тесты написаны и проходят
- [ ] Документация обновлена
- [ ] Безопасность проверена
- [ ] Производительность оценена
- [ ] Обратная совместимость сохранена
- [ ] Логирование добавлено где необходимо
- [ ] Обработка ошибок реализована
- [ ] API изменения задокументированы
- [ ] Миграции базы данных созданы (если нужны)

### Pull Request Template
```markdown
## Описание изменений
Краткое описание того, что было изменено и почему.

## Тип изменений
- [ ] Bug fix (исправление бага)
- [ ] New feature (новая функциональность)
- [ ] Breaking change (изменения, нарушающие обратную совместимость)
- [ ] Documentation update (обновление документации)

## Как тестировать
1. Шаги для тестирования изменений
2. Ожидаемый результат

## Чеклист
- [ ] Код прошел самопроверку
- [ ] Тесты добавлены/обновлены
- [ ] Документация обновлена
- [ ] Нет новых предупреждений компилятора
```

## 🔍 Диагностика и отладка

### Backend Debugging

#### 1. Логирование
```java
// Структурированное логирование
@Slf4j
@Service
public class UserService {
    
    public User createUser(UserFormData userData) {
        log.info("Creating user with username: {}", userData.getUsername());
        
        try {
            User user = convertToEntity(userData);
            User savedUser = userRepository.save(user);
            
            log.info("User created successfully with ID: {}", savedUser.getId());
            return savedUser;
            
        } catch (Exception e) {
            log.error("Failed to create user: {}", userData.getUsername(), e);
            throw new BusinessException("Failed to create user", e);
        }
    }
}
```

#### 2. Actuator Endpoints
```properties
# Включение диагностических endpoints
management.endpoints.web.exposure.include=health,metrics,info,env,loggers
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
```

#### 3. Database Query Logging
```properties
# Логирование SQL запросов
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
spring.jpa.properties.hibernate.format_sql=true
```

### Frontend Debugging

#### 1. Error Boundaries
```typescript
interface ErrorBoundaryState {
  hasError: boolean;
  error?: Error;
}

class ErrorBoundary extends Component<PropsWithChildren, ErrorBoundaryState> {
  constructor(props: PropsWithChildren) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
    // Отправка ошибки в систему мониторинга
  }

  render() {
    if (this.state.hasError) {
      return (
        <Alert severity="error">
          <AlertTitle>Что-то пошло не так</AlertTitle>
          <Typography>Произошла неожиданная ошибка. Пожалуйста, обновите страницу.</Typography>
        </Alert>
      );
    }

    return this.props.children;
  }
}
```

#### 2. Development Tools
```typescript
// Redux DevTools для отладки состояния
const store = configureStore({
  reducer: rootReducer,
  devTools: process.env.NODE_ENV !== 'production',
});

// React Developer Tools профилирование
const App: React.FC = () => {
  return (
    <React.StrictMode>
      <ErrorBoundary>
        <Router>
          <Routes>
            {/* routes */}
          </Routes>
        </Router>
      </ErrorBoundary>
    </React.StrictMode>
  );
};
```

## 📊 Мониторинг и метрики

### Backend Monitoring

#### 1. Custom Metrics
```java
@Component
public class ApplicationMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter apiCallsCounter;
    private final Timer responseTimer;
    private final Gauge activeUsersGauge;
    
    public ApplicationMetrics(MeterRegistry meterRegistry, UserService userService) {
        this.meterRegistry = meterRegistry;
        
        this.apiCallsCounter = Counter.builder("api.calls.total")
            .description("Total API calls")
            .tag("application", "crypto-admin")
            .register(meterRegistry);
            
        this.responseTimer = Timer.builder("api.response.time")
            .description("API response time")
            .register(meterRegistry);
            
        this.activeUsersGauge = Gauge.builder("users.active.count")
            .description("Number of active users")
            .register(meterRegistry, userService, UserService::getActiveUserCount);
    }
    
    public void incrementApiCalls(String endpoint, String method) {
        apiCallsCounter.increment(
            Tags.of(
                Tag.of("endpoint", endpoint),
                Tag.of("method", method)
            )
        );
    }
    
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
}
```

#### 2. Health Indicators
```java
@Component
public class ExchangeHealthIndicator implements HealthIndicator {
    
    private final List<ExchangeAdapter> exchangeAdapters;
    
    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        
        Map<String, Object> details = new HashMap<>();
        boolean allHealthy = true;
        
        for (ExchangeAdapter adapter : exchangeAdapters) {
            try {
                boolean isHealthy = adapter.isHealthy();
                details.put(adapter.getName(), isHealthy ? "UP" : "DOWN");
                
                if (!isHealthy) {
                    allHealthy = false;
                }
            } catch (Exception e) {
                details.put(adapter.getName(), "ERROR: " + e.getMessage());
                allHealthy = false;
            }
        }
        
        return allHealthy ? 
            builder.up().withDetails(details).build() :
            builder.down().withDetails(details).build();
    }
}
```

### Frontend Monitoring

#### 1. Performance Monitoring
```typescript
// Web Vitals мониторинг
import { getCLS, getFID, getFCP, getLCP, getTTFB } from 'web-vitals';

const sendToAnalytics = (metric: any) => {
  // Отправка метрик в систему аналитики
  console.log('Performance metric:', metric);
};

getCLS(sendToAnalytics);
getFID(sendToAnalytics);
getFCP(sendToAnalytics);
getLCP(sendToAnalytics);
getTTFB(sendToAnalytics);
```

#### 2. Error Tracking
```typescript
// Глобальная обработка ошибок
window.addEventListener('error', (event) => {
  console.error('Global error:', event.error);
  // Отправка в систему мониторинга
});

window.addEventListener('unhandledrejection', (event) => {
  console.error('Unhandled promise rejection:', event.reason);
  // Отправка в систему мониторинга
});
```

## 🔧 Инструменты разработки

### Backend Development Tools

#### 1. Форматирование кода
```xml
<!-- spotless-maven-plugin -->
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <version>2.22.8</version>
    <configuration>
        <java>
            <googleJavaFormat>
                <version>1.15.0</version>
                <style>GOOGLE</style>
            </googleJavaFormat>
            <removeUnusedImports />
            <trimTrailingWhitespace />
            <endWithNewline />
        </java>
    </configuration>
</plugin>
```

#### 2. Static Analysis
```xml
<!-- SpotBugs -->
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.7.1.1</version>
    <configuration>
        <effort>Max</effort>
        <threshold>Low</threshold>
        <xmlOutput>true</xmlOutput>
    </configuration>
</plugin>

<!-- Checkstyle -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.1.2</version>
    <configuration>
        <configLocation>checkstyle.xml</configLocation>
        <encoding>UTF-8</encoding>
        <consoleOutput>true</consoleOutput>
        <failsOnError>true</failsOnError>
    </configuration>
</plugin>
```

### Frontend Development Tools

#### 1. ESLint Configuration
```json
{
  "extends": [
    "@typescript-eslint/recommended",
    "plugin:react/recommended",
    "plugin:react-hooks/recommended"
  ],
  "rules": {
    "@typescript-eslint/explicit-function-return-type": "error",
    "@typescript-eslint/no-unused-vars": "error",
    "react/prop-types": "off",
    "react/react-in-jsx-scope": "off"
  },
  "settings": {
    "react": {
      "version": "detect"
    }
  }
}
```

#### 2. Prettier Configuration
```json
{
  "semi": true,
  "trailingComma": "es5",
  "singleQuote": true,
  "printWidth": 100,
  "tabWidth": 2,
  "useTabs": false
}
```

## 🚀 Автоматизация CI/CD

### GitHub Actions

#### 1. Backend Pipeline
```yaml
name: Backend CI/CD

on:
  push:
    branches: [ main, develop ]
    paths: [ 'backend/**' ]
  pull_request:
    branches: [ main ]
    paths: [ 'backend/**' ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:13
        env:
          POSTGRES_PASSWORD: postgres
          POSTGRES_DB: crypto_test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
          
      redis:
        image: redis:6-alpine
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        
    - name: Run tests
      run: |
        cd backend
        ./mvnw clean test
        
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: backend/target/surefire-reports/*.xml
        reporter: java-junit
        
    - name: Code coverage
      run: |
        cd backend
        ./mvnw jacoco:report
        
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: backend/target/site/jacoco/jacoco.xml

  build:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Build with Maven
      run: |
        cd backend
        ./mvnw clean package -DskipTests
        
    - name: Build Docker image
      run: |
        cd backend
        docker build -t crypto-admin-backend:${{ github.sha }} .
        
    - name: Deploy to staging
      if: github.ref == 'refs/heads/develop'
      run: |
        # Deployment script
        echo "Deploying to staging..."
```

#### 2. Frontend Pipeline
```yaml
name: Frontend CI/CD

on:
  push:
    branches: [ main, develop ]
    paths: [ 'frontend/**' ]
  pull_request:
    branches: [ main ]
    paths: [ 'frontend/**' ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
        cache: 'npm'
        cache-dependency-path: frontend/package-lock.json
        
    - name: Install dependencies
      run: |
        cd frontend
        npm ci
        
    - name: Type check
      run: |
        cd frontend
        npm run type-check
        
    - name: Lint
      run: |
        cd frontend
        npm run lint
        
    - name: Test
      run: |
        cd frontend
        npm test -- --coverage
        
    - name: Upload coverage
      uses: codecov/codecov-action@v3
      with:
        file: frontend/coverage/lcov.info

  build:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
        cache: 'npm'
        cache-dependency-path: frontend/package-lock.json
        
    - name: Install dependencies
      run: |
        cd frontend
        npm ci
        
    - name: Build
      run: |
        cd frontend
        npm run build
        
    - name: Deploy to CDN
      run: |
        # Deployment to CDN
        echo "Deploying to CDN..."
```

## 📚 Документация API

### OpenAPI Specification

#### 1. Swagger Configuration
```java
@Configuration
@EnableOpenApi
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Crypto Admin API")
                .version("1.0.0")
                .description("API для администрирования криптовалютной платформы")
                .contact(new Contact()
                    .name("Development Team")
                    .email("dev@example.com"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Development server"),
                new Server().url("https://api.crypto-admin.com").description("Production server")
            ))
            .components(new Components()
                .addSecuritySchemes("sessionAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.COOKIE)
                    .name("JSESSIONID")));
    }
}
```

#### 2. API Documentation Annotations
```java
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "API для управления пользователями")
public class UserManagementController {
    
    @Operation(
        summary = "Получить всех пользователей",
        description = "Возвращает постраничный список всех пользователей",
        security = @SecurityRequirement(name = "sessionAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список пользователей получен успешно",
            content = @Content(schema = @Schema(implementation = PageUserDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Не авторизован",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Недостаточно прав",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(
        @Parameter(description = "Параметры пагинации") Pageable pageable
    ) {
        // Implementation
    }
}
```

## 🔒 Безопасность в Production

### Security Headers
```java
@Configuration
public class SecurityHeadersConfig {
    
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityHeadersFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}

public class SecurityHeadersFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
            
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Security headers
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        httpResponse.setHeader("Content-Security-Policy", 
            "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        chain.doFilter(request, response);
    }
}
```

### Rate Limiting
```java
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    
    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
            
        String clientIp = getClientIpAddress(request);
        String key = "rate_limit:" + clientIp;
        
        String requests = redisTemplate.opsForValue().get(key);
        
        if (requests == null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
            return true;
        }
        
        int requestCount = Integer.parseInt(requests);
        
        if (requestCount >= MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded");
            return false;
        }
        
        redisTemplate.opsForValue().increment(key);
        return true;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

## 📱 Масштабирование и оптимизация

### Database Optimization

#### 1. Connection Pooling
```properties
# HikariCP оптимальные настройки
spring.datasource.hikari.maximum-pool-size=25
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.leak-detection-threshold=60000
```

#### 2. Query Optimization
```java
// Использование проекций для оптимизации запросов
public interface UserProjection {
    Long getId();
    String getUsername();
    String getEmail();
}

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u.id as id, u.username as username, u.email as email FROM User u")
    Page<UserProjection> findAllProjections(Pageable pageable);
    
    // Batch операции
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = CURRENT_TIMESTAMP WHERE u.id IN :ids")
    void updateLastLoginBatch(@Param("ids") List<Long> userIds);
}
```

### Caching Strategy

#### 1. Multi-level Caching
```java
@Service
public class CoinPriceService {
    
    private final LoadingCache<String, BigDecimal> localCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofMinutes(1))
        .build(this::fetchPriceFromRedis);
    
    @Cacheable(value = "coin_prices", key = "#coinSymbol + '_' + #exchange")
    public BigDecimal getCurrentPrice(String coinSymbol, String exchange) {
        try {
            return localCache.get(coinSymbol + "_" + exchange);
        } catch (Exception e) {
            return fetchPriceFromApi(coinSymbol, exchange);
        }
    }
    
    private BigDecimal fetchPriceFromRedis(String key) {
        // Получение из Redis
        String price = redisTemplate.opsForValue().get("price:" + key);
        return price != null ? new BigDecimal(price) : fetchPriceFromApi(key);
    }
}
```

### Async Processing

#### 1. Асинхронная обработка
```java
@Service
public class NotificationService {
    
    @Async("taskExecutor")
    @EventListener
    public void handlePriceAlert(PriceAlertEvent event) {
        log.info("Processing price alert for coin: {}", event.getCoinSymbol());
        
        // Отправка уведомлений
        sendEmailNotification(event);
        sendPushNotification(event);
        
        log.info("Price alert processed successfully");
    }
    
    @Async("taskExecutor")
    public CompletableFuture<Void> updatePricesAsync(List<String> coinSymbols) {
        return CompletableFuture.runAsync(() -> {
            coinSymbols.parallelStream().forEach(this::updateCoinPrice);
        });
    }
}

@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("async-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

---

## 🎯 Заключение

Эта техническая документация содержит все необходимые детали для эффективной разработки, развертывания и поддержки криптовалютной административной панели. Документ будет обновляться по мере развития проекта и внедрения новых технологий.

### Ключевые достижения проекта:
- ✅ Современная архитектура с разделением ответственности
- ✅ Высокий уровень безопасности с детализированными правами доступа
- ✅ Полная типизация TypeScript на frontend
- ✅ Централизованная обработка ошибок
- ✅ Оптимизированная производительность с кэшированием
- ✅ Комплексное тестирование
- ✅ Готовность к production развертыванию

### Следующие шаги:
1. Внедрение мониторинга и логирования в production
2. Настройка автоматического масштабирования
3. Добавление интеграционных тестов
4. Оптимизация производительности базы данных
5. Внедрение CDN для frontend ресурсов 