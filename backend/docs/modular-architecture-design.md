# 🏗️ Дизайн модульной архитектуры CT.01

## 📦 Структура модулей

### ✅ Созданная структура пакетов:

```
src/main/java/alg/coyote001/
├── core/                    # Общие компоненты
│   ├── config/             # Конфигурации (общие)
│   ├── security/           # Безопасность (общая)
│   ├── exception/          # Обработка ошибок
│   └── util/               # Утилиты
│
├── user/                   # Модуль пользователей
│   ├── controller/         # REST контроллеры
│   ├── service/            # Бизнес-логика
│   ├── repository/         # Доступ к данным
│   ├── entity/             # JPA сущности
│   └── dto/                # Data Transfer Objects
│
├── subscription/           # Модуль подписок
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── crypto/                 # Модуль криптовалют
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── exchange/          # Адаптеры бирж
│
├── market/                 # Модуль рыночных данных
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── notification/           # Модуль уведомлений
│   ├── controller/
│   ├── service/
│   ├── websocket/         # WebSocket handlers
│   └── dto/
│
└── admin/                  # Модуль администрирования
    ├── controller/
    ├── service/
    └── dto/
```

## 🎯 Принципы модульной архитектуры

### 1. **Доменное разделение (Domain-Driven Design)**
- Каждый модуль представляет отдельный бизнес-домен
- Четкие границы между модулями
- Минимизация межмодульных зависимостей

### 2. **Слоевая архитектура внутри модулей**
```
Controller Layer → Service Layer → Repository Layer → Entity Layer
     ↓                ↓               ↓                ↓
   HTTP             Business        Data Access     Database
  Handling           Logic          Abstraction      Entities
```

### 3. **Dependency Rules**
- **Зависимости направлены внутрь:** Controller → Service → Repository → Entity
- **Запрет круговых зависимостей** между модулями
- **Интерфейсы для абстракции** взаимодействия между модулями

## 📋 Стандарты именования

### 📁 Пакеты
```java
// Правильно:
alg.coyote001.crypto.controller
alg.coyote001.crypto.service
alg.coyote001.crypto.repository

// Неправильно:
alg.coyote001.controllers.crypto
alg.coyote001.service.crypto
```

### 🏷️ Классы и интерфейсы

#### Контроллеры
```java
// Новый стиль (модульный):
@RestController
@RequestMapping("/api/v1/crypto/coins")
public class CoinController { ... }

@RestController  
@RequestMapping("/api/v1/crypto/tracking")
public class TrackingController { ... }

// Старый стиль (deprecated):
@RestController
@RequestMapping("/api/v1/coins")
@Deprecated
public class CoinDataController { ... }
```

#### Сервисы
```java
// Интерфейс + реализация:
public interface CoinService { ... }

@Service
public class CoinServiceImpl implements CoinService { ... }

// Доменные сервисы:
@Service
public class CoinPriceService { ... }

@Service
public class CoinTrackingService { ... }
```

#### Репозитории
```java
// JPA репозитории:
@Repository
public interface CoinRepository extends JpaRepository<Coin, Long> { ... }

// Кастомные репозитории:
public interface CoinCustomRepository { ... }

@Repository
public class CoinCustomRepositoryImpl implements CoinCustomRepository { ... }
```

### 🔗 API Маршруты (новая структура)

#### ✅ Новые стандартизированные маршруты:

```java
// Пользователи и аутентификация
/api/v1/auth/*                    - AuthController
/api/v1/users/*                   - UserController  
/api/v1/users/{id}/settings       - UserSettingsController

// Подписки
/api/v1/subscriptions/*           - SubscriptionController

// Криптовалюты (основной домен)
/api/v1/crypto/coins/*            - CoinController
/api/v1/crypto/tracking/*         - TrackingController
/api/v1/crypto/watchlists/*       - WatchlistController

// Рыночные данные  
/api/v1/exchanges/*               - ExchangeController
/api/v1/market/prices/*           - PriceController
/api/v1/market/tickers/*          - TickerController

// Уведомления
/api/v1/realtime/websocket/*      - WebSocketController
/api/v1/notifications/*           - NotificationController

// Администрирование
/api/v1/admin/system/*            - SystemController
/api/v1/admin/analysis/*          - AnalysisController
```

#### ❌ Устаревшие маршруты (поддержка + deprecation):
```java
/api/v1/coins → /api/v1/crypto/coins
/api/v1/tracked-coins → /api/v1/crypto/tracking
/api/v1/prices → /api/v1/market/prices
/api/v1/websocket → /api/v1/realtime/websocket
/api/v1/system → /api/v1/admin/system
/api/v1/analysis → /api/v1/admin/analysis
```

## 🔧 Технические стандарты

### 🏗️ Структура контроллера
```java
@RestController
@RequestMapping("/api/v1/crypto/coins")
@Tag(name = "Crypto Coins", description = "API для управления криптовалютами")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CoinController {
    
    private final CoinService coinService;
    
    @GetMapping
    @Operation(summary = "Получить список монет")
    public ResponseEntity<Page<CoinDto>> getCoins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Implementation
    }
}
```

### 🎯 Структура сервиса
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CoinServiceImpl implements CoinService {
    
    private final CoinRepository coinRepository;
    private final CoinMapper coinMapper;
    
    @Override
    @Cacheable("coins")
    public Page<CoinDto> getCoins(Pageable pageable) {
        // Implementation
    }
    
    @Override
    @Transactional
    public CoinDto createCoin(CreateCoinRequest request) {
        // Implementation  
    }
}
```

### 💾 Структура репозитория
```java
@Repository
public interface CoinRepository extends JpaRepository<Coin, Long> {
    
    @Query("SELECT c FROM Coin c WHERE c.symbol = :symbol")
    Optional<Coin> findBySymbol(@Param("symbol") String symbol);
    
    @Query("SELECT c FROM Coin c WHERE c.isActive = true")
    List<Coin> findActiveCoins();
}
```

## 📊 Межмодульное взаимодействие

### 🔗 Разрешенные зависимости:

```
core ← все модули (общие компоненты)
user ↔ subscription (лимиты пользователей)
crypto ↔ market (цены монет)
crypto ↔ notification (уведомления о ценах)
admin → все модули (мониторинг)
```

### ❌ Запрещенные зависимости:
- Прямые зависимости между subscription ↔ market
- Циклические зависимости
- Обращение к repository другого модуля напрямую

### 🛠️ Механизмы взаимодействия:

#### 1. **Интерфейсы модулей**
```java
// В core модуле:
public interface CryptoFacade {
    List<CoinDto> getActiveCoins();
    Optional<CoinDto> getCoinBySymbol(String symbol);
}

// В crypto модуле:
@Component
public class CryptoFacadeImpl implements CryptoFacade {
    // Implementation
}
```

#### 2. **События (Spring Events)**
```java
// Публикация события:
@EventListener
public void handlePriceUpdate(PriceUpdateEvent event) {
    applicationEventPublisher.publishEvent(
        new CoinPriceChangedEvent(event.getCoinSymbol(), event.getNewPrice())
    );
}

// Обработка события:
@EventListener
public void onCoinPriceChanged(CoinPriceChangedEvent event) {
    notificationService.sendPriceAlert(event);
}
```

#### 3. **Configuration Properties**
```java
@ConfigurationProperties(prefix = "app.modules")
@Data
public class ModuleProperties {
    private CryptoModuleProperties crypto;
    private MarketModuleProperties market;
    private NotificationModuleProperties notification;
}
```

## 🧪 Правила тестирования

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class CoinServiceTest {
    
    @Mock
    private CoinRepository coinRepository;
    
    @InjectMocks
    private CoinServiceImpl coinService;
    
    // Tests
}
```

### Integration Tests
```java
@SpringBootTest
@DataJpaTest
@TestPropertySource(locations = "classpath:test.properties")
class CoinRepositoryTest {
    // Repository integration tests
}
```

### API Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CoinControllerIntegrationTest {
    // API integration tests
}
```

## 📚 Документация и миграция

### 📖 Swagger/OpenAPI
```java
@Tag(name = "Crypto Coins", description = "API для управления криптовалютами")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Успешно"),
    @ApiResponse(responseCode = "404", description = "Не найдено")
})
```

### ⚠️ Deprecation Strategy
```java
@RestController
@RequestMapping("/api/v1/coins")
@Deprecated(since = "1.5.0", forRemoval = true)
@Tag(name = "Coins (Deprecated)", 
     description = "⚠️ УСТАРЕЛО: Используйте /api/v1/crypto/coins")
public class CoinDataController {
    
    @GetMapping
    @Deprecated(since = "1.5.0", forRemoval = true)
    @Operation(
        summary = "⚠️ УСТАРЕЛО: Используйте GET /api/v1/crypto/coins",
        description = "Этот эндпоинт будет удален в версии 2.0.0"
    )
    public ResponseEntity<Page<CoinDto>> getAllCoins() {
        // Redirect to new implementation
        return cryptoFacade.getCoins();
    }
}
```

## 🚀 План миграции

### Phase 1: Core + Crypto (в процессе)
1. ✅ Создана модульная структура пакетов
2. ⏳ Создание core компонентов
3. ⏳ Миграция crypto модуля
4. ⏳ Новые API маршруты для crypto

### Phase 2: Market + User
1. Миграция market данных  
2. Реорганизация user/auth модулей

### Phase 3: Subscription + Notification
1. Модуляризация подписок
2. Вынесение WebSocket в notification

### Phase 4: Admin + Cleanup
1. Консолидация admin функций
2. Удаление устаревшего кода
3. Обновление документации

---
*Этот дизайн обеспечивает масштабируемую, поддерживаемую архитектуру с четким разделением ответственности* 