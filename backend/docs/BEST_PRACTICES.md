# Best Practices и Lessons Learned

## Ключевые принципы

### 1. Domain-Driven Design

#### ✅ Лучшие практики
- **Rich Domain Models**: Включайте бизнес-логику в доменные объекты
- **Value Objects с валидацией**: Immutable объекты с проверкой в конструкторе
- **Ubiquitous Language**: Единый язык во всех слоях

```java
// ✅ Хорошо: Rich Value Object
public record Coin(Long id, String symbol, String name, boolean isActive) {
    public Coin {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be null or blank");
        }
    }
    
    public boolean isValid() {
        return isActive && symbol != null;
    }
}
```

### 2. Clean Architecture

#### ✅ Принципы
- **Domain не зависит от внешних библиотек**
- **Application Facade упрощает интеграцию**
- **Repository интерфейсы в domain**
- **Mappers изолируют слои**

```java
// ✅ Чистый domain service
@Component
public class CoinDomainService {
    public List<TrackedCoin> getHighPriorityCoins(List<TrackedCoin> coins) {
        return coins.stream()
            .filter(TrackedCoin::isActive)
            .filter(coin -> coin.getPriority() >= 8)
            .toList();
    }
}
```

### 3. Command/Query Separation

#### ✅ Sealed interfaces для типобезопасности
```java
public sealed interface CoinCommand {
    record CreateCoinCommand(String symbol, String name) implements CoinCommand {}
    record UpdateCoinCommand(Long id, String name) implements CoinCommand {}
}

public sealed interface CoinResult {
    record CoinOperationResult(boolean success, Coin coin, String errorMessage) {
        public static CoinOperationResult success(Coin coin) {
            return new CoinOperationResult(true, coin, null);
        }
        public static CoinOperationResult error(String message) {
            return new CoinOperationResult(false, null, message);
        }
    }
}
```

### 4. Error Handling

#### ✅ Result pattern вместо exceptions
```java
public TrackedCoinOperationResult createTrackedCoin(CreateTrackedCoinCommand command) {
    try {
        if (command.symbol() == null || command.symbol().isBlank()) {
            return TrackedCoinOperationResult.error("Symbol is required");
        }
        
        TrackedCoin saved = trackedCoinRepository.save(trackedCoin);
        return TrackedCoinOperationResult.success(saved);
        
    } catch (Exception e) {
        log.error("Failed to create tracked coin: {}", command.symbol(), e);
        return TrackedCoinOperationResult.error("Creation failed: " + e.getMessage());
    }
}
```

### 5. Repository Pattern

#### ✅ Доменные интерфейсы
```java
// Domain layer
public interface CoinRepository {
    Optional<Coin> findBySymbol(String symbol);
    List<Coin> findActiveCoins();
    Coin save(Coin coin);
}

// Infrastructure layer
@Repository
public class CoinRepositoryImpl implements CoinRepository {
    private final JpaCoinRepository jpaCoinRepository;
    private final CoinMapper coinMapper;
    
    @Override
    public List<Coin> findActiveCoins() {
        return jpaCoinRepository.findByIsActiveTrue()
            .stream()
            .map(coinMapper::toDomain)
            .toList();
    }
}
```

### 6. API Design

#### ✅ Консистентный формат ответов
```java
@PostMapping
public ResponseEntity<ApiResponseDto<ApiCoinDto>> createCoin(@Valid @RequestBody ApiCoinDto dto) {
    var result = facade.createCoin(dto.getSymbol(), dto.getName());
    
    if (result.success()) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponseDto.success(convertToDto(result.coin())));
    } else {
        return ResponseEntity.badRequest()
            .body(ApiResponseDto.error(result.errorMessage(), "CREATION_FAILED"));
    }
}
```

### 7. Migration Strategy

#### ✅ Adapter pattern для backward compatibility
```java
@Service
@ConditionalOnProperty(name = "legacy-api.enabled", havingValue = "true")
public class LegacyApiAdapter {
    private final CryptoApplicationFacade facade;
    private final MigrationService migrationService;
    
    public LegacyDto getCoin(String symbol) {
        migrationService.recordLegacyApiUsage("/api/coins/" + symbol, "GET");
        return facade.getCoinBySymbol(symbol)
            .map(this::convertToLegacyDto)
            .orElse(null);
    }
}
```

## Извлеченные уроки

### 1. Архитектурные
- **Value Objects критичны**: Типобезопасность предотвращает ошибки
- **Фасады упрощают интеграцию**: Скрывают сложность Use Cases
- **Mappers обязательны**: Изоляция между JPA и domain объектами

### 2. Практические
- **Result pattern лучше exceptions**: Явная обработка ошибок
- **Constructor injection предпочтительнее**: Лучше field injection
- **Sealed interfaces для exhaustiveness**: Компилятор проверяет полноту

### 3. Миграционные
- **Постепенная миграция**: Adapter pattern с мониторингом
- **Feature flags важны**: Управление legacy функциональностью
- **Deprecation warnings помогают**: Четкие сигналы для разработчиков

## Распространенные ошибки

### ❌ Что НЕ делать

```java
// Анемичные модели
public class Coin {
    private String symbol;
    // Только геттеры/сеттеры, нет бизнес-логики
}

// Domain зависит от инфраструктуры
@Component
public class CoinDomainService {
    @Autowired
    private CoinRepository repository; // НЕ ДЕЛАЙТЕ ТАК!
}

// Толстые controllers
@PostMapping
public ResponseEntity<CoinDto> createCoin(@RequestBody CoinDto dto) {
    // Много бизнес-логики в контроллере - НЕ ДЕЛАЙТЕ ТАК!
}
```

## Testing Strategy

### ✅ Многоуровневое тестирование

```java
// Unit test для domain
@Test
void should_Create_Valid_Coin() {
    Coin coin = new Coin(1L, "BTC", "Bitcoin", true);
    assertThat(coin.isValid()).isTrue();
}

// Integration test для use case
@SpringBootTest
@Transactional
class ManageCoinUseCaseTest {
    @Test
    void should_Create_Coin_Successfully() {
        var result = useCase.createCoin(new CreateCoinCommand("ETH", "Ethereum"));
        assertThat(result.success()).isTrue();
    }
}
```

## Configuration

### ✅ Explicit Spring configuration

```java
@Configuration
public class CryptoDomainConfig {
    
    @Bean
    public CoinRepository coinRepository(JpaCoinRepository jpaRepo, CoinMapper mapper) {
        return new CoinRepositoryImpl(jpaRepo, mapper);
    }
    
    @Bean
    public CoinDomainService coinDomainService(CoinRepository coinRepository) {
        return new CoinDomainService(coinRepository);
    }
}
```

## Мониторинг

### ✅ Structured logging

```java
@Service
@Slf4j
public class ManageCoinUseCase {
    
    public CoinOperationResult createCoin(CreateCoinCommand command) {
        log.info("Creating coin with symbol: {}", command.symbol());
        
        try {
            Coin saved = coinRepository.save(coin);
            log.info("Successfully created coin with ID: {}", saved.id());
            return CoinOperationResult.success(saved);
        } catch (Exception e) {
            log.error("Failed to create coin: {}", command.symbol(), e);
            return CoinOperationResult.error("Creation failed");
        }
    }
}
```

## Рекомендации

### Для новых доменов
1. **Используйте шаблоны** из DOMAIN_TEMPLATE.md
2. **Следуйте принципам** чистой архитектуры
3. **Начинайте с простого** Value Objects
4. **Тестируйте на всех уровнях**

### Для миграции
1. **Adapter pattern** для совместимости
2. **Мониторинг использования** legacy API
3. **Постепенное отключение** старой функциональности
4. **Четкие deprecation warnings**

## Заключение

- **DDD принципы работают** - четкое разделение упрощает код
- **Value Objects критичны** - типобезопасность предотвращает ошибки  
- **Result pattern эффективен** - явная обработка ошибок
- **Тестирование обязательно** - уверенность в корректности
- **Документация важна** - помогает команде следовать принципам
