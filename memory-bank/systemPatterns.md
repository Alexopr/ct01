# System Patterns - Cryptocurrency Dashboard

## 🏗 Архитектурные паттерны

### 1. Layered Architecture (Слоистая архитектура)

```
┌─────────────────────────────────────┐
│         Presentation Layer          │ ← Controllers, WebSocket endpoints
├─────────────────────────────────────┤
│          Business Layer             │ ← Services, Business Logic
├─────────────────────────────────────┤
│        Persistence Layer            │ ← Repositories, Data Access
├─────────────────────────────────────┤
│          Database Layer             │ ← PostgreSQL, Redis
└─────────────────────────────────────┘
```

**Принципы:**
- Каждый слой зависит только от нижележащих слоев
- Четкое разделение ответственности
- Возможность изменения реализации слоя без влияния на другие

### 2. Repository Pattern

```java
// Абстракция доступа к данным
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.lastLoginDate > :date")
    List<User> findActiveUsers(@Param("date") LocalDateTime date);
}

// Использование в сервисе
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
```

**Преимущества:**
- Абстракция от конкретной реализации БД
- Легкость тестирования через моки
- Централизация логики доступа к данным

### 3. Service Layer Pattern

```java
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final EmailService emailService;
    
    public UserDto createUser(CreateUserRequest request) {
        // Валидация бизнес-логики
        validateUserCreation(request);
        
        // Создание пользователя
        User user = new User(request.getUsername(), request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Назначение ролей по умолчанию
        Role defaultRole = roleService.findByName("USER");
        user.addRole(defaultRole);
        
        // Сохранение
        User savedUser = userRepository.save(user);
        
        // Отправка welcome email
        emailService.sendWelcomeEmail(savedUser.getEmail());
        
        return convertToDto(savedUser);
    }
}
```

**Ответственности:**
- Бизнес-логика и валидация
- Координация между репозиториями
- Управление транзакциями

### 4. DTO Pattern (Data Transfer Object)

```java
// API DTO для клиента
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private LocalDateTime lastLoginDate;
    
    // Конструкторы, геттеры, сеттеры
}

// Request DTO для создания
public class CreateUserRequest {
    @NotBlank(message = "Username is required")
    private String username;
    
    @Email(message = "Valid email is required")
    private String email;
    
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}

// Конвертация в сервисе
public UserDto convertToDto(User user) {
    return UserDto.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .roles(user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList()))
        .build();
}
```

## 🔐 Security Patterns

### 1. Authentication Strategy Pattern

```java
public interface AuthenticationStrategy {
    AuthenticationResult authenticate(AuthenticationRequest request);
}

@Component
public class StandardAuthenticationStrategy implements AuthenticationStrategy {
    public AuthenticationResult authenticate(AuthenticationRequest request) {
        // Стандартная username/password аутентификация
    }
}

@Component
public class TelegramAuthenticationStrategy implements AuthenticationStrategy {
    public AuthenticationResult authenticate(AuthenticationRequest request) {
        // Telegram OAuth аутентификация
    }
}

@Service
public class AuthenticationService {
    private final Map<String, AuthenticationStrategy> strategies;
    
    public AuthenticationResult authenticate(String type, AuthenticationRequest request) {
        AuthenticationStrategy strategy = strategies.get(type);
        return strategy.authenticate(request);
    }
}
```

### 2. Role-Based Access Control (RBAC)

```java
// Иерархия ролей
public enum Role {
    ADMIN("ADMIN", Set.of(Permission.values())),
    PREMIUM("PREMIUM", Set.of(USER_READ, USER_WRITE, COIN_READ, COIN_WRITE)),
    USER("USER", Set.of(USER_READ, COIN_READ));
    
    private final String name;
    private final Set<Permission> permissions;
}

// Использование в контроллерах
@RestController
public class UserController {
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('ADMIN')")
    @GetMapping("/api/v1/users")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        // Доступ только для пользователей с правом USER_READ или роль ADMIN
    }
    
    @PreAuthorize("@userService.isCurrentUser(#userId, authentication.name) or hasAuthority('USER_WRITE')")
    @PutMapping("/api/v1/users/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId, @RequestBody UserDto userDto) {
        // Пользователь может редактировать свой профиль или иметь право USER_WRITE
    }
}
```

## 📊 Data Access Patterns

### 1. Active Record vs Data Mapper

**Мы используем Data Mapper через JPA:**

```java
// Entity (Domain Model)
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles")
    private Set<Role> roles = new HashSet<>();
    
    // Бизнес-методы
    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }
    
    public boolean hasPermission(Permission permission) {
        return roles.stream()
            .anyMatch(role -> role.hasPermission(permission));
    }
}

// Repository (Data Mapper)
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByUsername(String username);
}
```

### 2. Unit of Work Pattern (через @Transactional)

```java
@Service
@Transactional
public class UserService {
    public void transferUserData(Long fromUserId, Long toUserId) {
        // Все операции в одной транзакции
        User fromUser = userRepository.findById(fromUserId).orElseThrow();
        User toUser = userRepository.findById(toUserId).orElseThrow();
        
        // Перенос данных
        List<TrackedCoin> trackedCoins = fromUser.getTrackedCoins();
        toUser.getTrackedCoins().addAll(trackedCoins);
        fromUser.getTrackedCoins().clear();
        
        // Все изменения будут сохранены в конце транзакции
        // или откачены в случае ошибки
    }
}
```

## 🔄 Integration Patterns

### 1. Adapter Pattern для внешних API

```java
// Единый интерфейс для всех бирж
public interface ExchangeAdapter {
    List<CoinPrice> getCurrentPrices(List<String> coinIds);
    CoinPrice getCoinPrice(String coinId);
    List<Exchange> getSupportedExchanges();
}

// Реализация для CoinGecko
@Component
public class CoinGeckoAdapter implements ExchangeAdapter {
    private final CoinGeckoApiClient apiClient;
    
    @Override
    public List<CoinPrice> getCurrentPrices(List<String> coinIds) {
        CoinGeckoResponse response = apiClient.getPrice(coinIds);
        return convertToCoinPrices(response);
    }
}

// Реализация для Binance
@Component  
public class BinanceAdapter implements ExchangeAdapter {
    private final BinanceApiClient apiClient;
    
    @Override
    public List<CoinPrice> getCurrentPrices(List<String> coinIds) {
        List<BinancePrice> prices = apiClient.getTicker24hr();
        return convertToCoinPrices(prices);
    }
}

// Сервис для работы с биржами
@Service
public class ExchangeService {
    private final List<ExchangeAdapter> adapters;
    
    public List<CoinPrice> getAggregatedPrices(List<String> coinIds) {
        return adapters.stream()
            .flatMap(adapter -> adapter.getCurrentPrices(coinIds).stream())
            .collect(Collectors.toList());
    }
}
```

### 2. Event-Driven Pattern

```java
// Событие изменения цены
public class PriceChangedEvent extends ApplicationEvent {
    private final String coinId;
    private final BigDecimal oldPrice;
    private final BigDecimal newPrice;
    
    public PriceChangedEvent(Object source, String coinId, BigDecimal oldPrice, BigDecimal newPrice) {
        super(source);
        this.coinId = coinId;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
    }
}

// Публикация события
@Service
public class PriceService {
    private final ApplicationEventPublisher eventPublisher;
    
    public void updatePrice(String coinId, BigDecimal newPrice) {
        BigDecimal oldPrice = getCurrentPrice(coinId);
        
        // Обновление цены
        savePriceToDatabase(coinId, newPrice);
        
        // Публикация события
        PriceChangedEvent event = new PriceChangedEvent(this, coinId, oldPrice, newPrice);
        eventPublisher.publishEvent(event);
    }
}

// Обработчики событий
@Component
public class PriceEventHandlers {
    
    @EventListener
    @Async
    public void handlePriceChange(PriceChangedEvent event) {
        // Обновление кэша
        updatePriceCache(event.getCoinId(), event.getNewPrice());
    }
    
    @EventListener
    @Async
    public void checkPriceAlerts(PriceChangedEvent event) {
        // Проверка алертов пользователей
        alertService.checkAndSendAlerts(event);
    }
    
    @EventListener
    @Async
    public void broadcastPriceUpdate(PriceChangedEvent event) {
        // WebSocket уведомления
        webSocketService.broadcastPriceUpdate(event);
    }
}
```

## 🎯 Frontend Patterns

### 1. Context + Reducer Pattern

```typescript
// State management для аутентификации
interface AuthState {
  user: User | null;
  loading: boolean;
  error: string | null;
}

type AuthAction =
  | { type: 'LOGIN_START' }
  | { type: 'LOGIN_SUCCESS'; payload: User }
  | { type: 'LOGIN_FAILURE'; payload: string }
  | { type: 'LOGOUT' };

const authReducer = (state: AuthState, action: AuthAction): AuthState => {
  switch (action.type) {
    case 'LOGIN_START':
      return { ...state, loading: true, error: null };
    case 'LOGIN_SUCCESS':
      return { ...state, loading: false, user: action.payload, error: null };
    case 'LOGIN_FAILURE':
      return { ...state, loading: false, error: action.payload };
    case 'LOGOUT':
      return { ...state, user: null, error: null };
    default:
      return state;
  }
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, initialState);
  
  const login = async (credentials: LoginCredentials) => {
    dispatch({ type: 'LOGIN_START' });
    try {
      const user = await authService.login(credentials);
      dispatch({ type: 'LOGIN_SUCCESS', payload: user });
    } catch (error) {
      dispatch({ type: 'LOGIN_FAILURE', payload: error.message });
    }
  };
  
  return (
    <AuthContext.Provider value={{ ...state, login }}>
      {children}
    </AuthContext.Provider>
  );
};
```

### 2. Custom Hooks Pattern

```typescript
// Hook для работы с API
export const useApi = <T>(apiCall: () => Promise<T>) => {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const execute = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await apiCall();
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  }, [apiCall]);
  
  useEffect(() => {
    execute();
  }, [execute]);
  
  return { data, loading, error, refetch: execute };
};

// Использование
const UserList: React.FC = () => {
  const { data: users, loading, error, refetch } = useApi(() => 
    userService.getAllUsers()
  );
  
  if (loading) return <Spinner />;
  if (error) return <ErrorMessage message={error} />;
  
  return (
    <div>
      {users?.map(user => (
        <UserCard key={user.id} user={user} />
      ))}
      <Button onClick={refetch}>Refresh</Button>
    </div>
  );
};
```

## 🔄 Caching Patterns

### 1. Multi-Level Caching

```java
@Service
public class CoinService {
    
    // L1 Cache: Application level (Redis)
    @Cacheable(value = "coinPrices", key = "#coinId")
    public CoinPrice getCoinPrice(String coinId) {
        return fetchPriceFromApi(coinId);
    }
    
    // L2 Cache: HTTP level (Browser cache)
    @GetMapping("/api/v1/coins/{coinId}/price")
    @ResponseBody
    public ResponseEntity<CoinPrice> getCoinPriceEndpoint(@PathVariable String coinId) {
        CoinPrice price = coinService.getCoinPrice(coinId);
        
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)))
            .body(price);
    }
    
    // Cache warming
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void warmUpCache() {
        List<String> popularCoins = List.of("bitcoin", "ethereum", "binancecoin");
        popularCoins.forEach(this::getCoinPrice);
    }
}
```

### 2. Cache-Aside Pattern

```java
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public User getUserById(Long userId) {
        String cacheKey = "user:" + userId;
        
        // Попытка получить из кэша
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            return cachedUser;
        }
        
        // Загрузка из БД
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // Сохранение в кэш
            redisTemplate.opsForValue().set(cacheKey, user, Duration.ofMinutes(30));
        }
        
        return user;
    }
    
    public User updateUser(User user) {
        User updatedUser = userRepository.save(user);
        
        // Invalidate cache
        String cacheKey = "user:" + user.getId();
        redisTemplate.delete(cacheKey);
        
        return updatedUser;
    }
}
```

Эти паттерны обеспечивают масштабируемость, поддерживаемость и производительность приложения. 