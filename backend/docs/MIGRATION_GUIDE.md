# Migration Guide: Legacy to Modular DDD Architecture

## Обзор

Данное руководство поможет команде разработчиков мигрировать существующий код к новой модульной архитектуре на основе Domain-Driven Design (DDD).

## Цели миграции

1. **Улучшение архитектуры**: Переход к чистой архитектуре с четким разделением слоев
2. **Повышение тестируемости**: Изоляция бизнес-логики от инфраструктуры
3. **Масштабируемость**: Модульная структура для независимой разработки
4. **Поддерживаемость**: Стандартизация кода и паттернов

## Этапы миграции

### Этап 1: Анализ и планирование

#### 1.1 Определение границ домена (Bounded Context)
```bash
# Вопросы для определения домена:
- Какую бизнес-функциональность представляет модуль?
- Какие сущности принадлежат этому домену?
- Где естественные границы между доменами?
- Какой ubiquitous language используется?
```

#### 1.2 Инвентаризация существующего кода
```bash
# Проанализируйте следующие компоненты:
src/main/java/alg/coyote001/
├── controller/        # -> API Layer
├── service/          # -> Application Layer + Domain Service
├── entity/           # -> Domain Layer (entities)
├── repository/       # -> Infrastructure Layer
└── dto/             # -> API DTOs
```

#### 1.3 Создание миграционного плана
```markdown
1. Определите приоритет модулей для миграции
2. Создайте mapping таблицу legacy -> new structure
3. Спланируйте зависимости между модулями
4. Определите график миграции
```

### Этап 2: Создание структуры нового модуля

#### 2.1 Создание базовой структуры
```bash
mkdir -p src/main/java/com/ct01/{domain}/domain/{entity,repository,service}
mkdir -p src/main/java/com/ct01/{domain}/application/{dto,usecase,facade,config}
mkdir -p src/main/java/com/ct01/{domain}/infrastructure/{mapper,persistence,config}
mkdir -p src/main/java/com/ct01/{domain}/api/{dto,controller,config}
```

#### 2.2 Пример структуры для домена "trading"
```
com.ct01.trading/
├── domain/
│   ├── Trade.java                    # Value Object
│   ├── TradingStrategy.java          # Aggregate
│   ├── repository/
│   │   ├── TradeRepository.java
│   │   └── TradingStrategyRepository.java
│   └── service/
│       └── TradingDomainService.java
├── application/
│   ├── dto/
│   │   ├── TradingCommand.java
│   │   ├── TradingQuery.java
│   │   └── TradingResult.java
│   ├── usecase/
│   │   ├── ManageTradingUseCase.java
│   │   └── GetTradingDataUseCase.java
│   ├── facade/
│   │   └── TradingApplicationFacade.java
│   └── config/
│       └── TradingApplicationConfig.java
├── infrastructure/
│   ├── mapper/
│   │   ├── TradeMapper.java
│   │   └── TradingStrategyMapper.java
│   ├── persistence/
│   │   ├── TradeRepositoryImpl.java
│   │   └── TradingStrategyRepositoryImpl.java
│   └── config/
│       └── TradingDomainConfig.java
└── api/
    ├── dto/
    │   ├── ApiTradeDto.java
    │   ├── ApiTradingStrategyDto.java
    │   └── ApiResponseDto.java
    ├── controller/
    │   ├── TradeApiController.java
    │   └── TradingStrategyApiController.java
    └── config/
        └── TradingApiConfig.java
```

### Этап 3: Миграция Domain Layer

#### 3.1 Создание Value Objects (Entities)
```java
// Legacy Entity (НЕ изменяем)
@Entity
@Table(name = "trades")
public class Trade {
    @Id
    private Long id;
    private String symbol;
    private BigDecimal amount;
    // ... другие поля
}

// Новый Domain Value Object
public record Trade(
    Long id,
    String symbol,
    BigDecimal amount,
    TradeType type,
    TradeStatus status,
    LocalDateTime executedAt
) {
    // Конструктор с валидацией
    public Trade {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be null or blank");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
    
    // Бизнес-методы
    public boolean isCompleted() {
        return status == TradeStatus.COMPLETED;
    }
    
    public BigDecimal calculateFee(BigDecimal feeRate) {
        return amount.multiply(feeRate);
    }
}
```

#### 3.2 Создание Aggregates (для сложных сущностей)
```java
public class TradingStrategy {
    private final Long id;
    private final String name;
    private final List<TradeRule> rules;
    private boolean isActive;
    
    public TradingStrategy(Long id, String name, List<TradeRule> rules) {
        this.id = id;
        this.name = validateName(name);
        this.rules = List.copyOf(rules); // Immutable copy
        this.isActive = true;
    }
    
    // Бизнес-методы
    public boolean canExecuteTrade(Trade trade) {
        return isActive && rules.stream()
            .allMatch(rule -> rule.matches(trade));
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Strategy name cannot be empty");
        }
        return name;
    }
}
```

#### 3.3 Создание Domain Repository Interfaces
```java
public interface TradeRepository {
    Optional<Trade> findById(Long id);
    List<Trade> findBySymbol(String symbol);
    List<Trade> findCompletedTrades();
    List<Trade> findByDateRange(LocalDateTime from, LocalDateTime to);
    Trade save(Trade trade);
    void delete(Long id);
}

public interface TradingStrategyRepository {
    Optional<TradingStrategy> findById(Long id);
    List<TradingStrategy> findActiveStrategies();
    List<TradingStrategy> findByName(String name);
    TradingStrategy save(TradingStrategy strategy);
}
```

#### 3.4 Создание Domain Services
```java
@Component
public class TradingDomainService {
    
    public List<Trade> getEligibleTrades(
            List<Trade> trades, 
            TradingStrategy strategy) {
        return trades.stream()
            .filter(strategy::canExecuteTrade)
            .toList();
    }
    
    public BigDecimal calculateTotalProfit(List<Trade> trades) {
        return trades.stream()
            .filter(Trade::isCompleted)
            .map(Trade::getProfit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public TradingStatistics generateStatistics(
            List<Trade> trades, 
            LocalDateTime period) {
        // Бизнес-логика расчета статистики
        return new TradingStatistics(/* ... */);
    }
}
```

### Этап 4: Миграция Application Layer

#### 4.1 Создание Command/Query DTOs
```java
// Commands (для операций записи)
public sealed interface TradingCommand {
    record CreateTradeCommand(
        String symbol,
        BigDecimal amount,
        TradeType type,
        Long strategyId
    ) implements TradingCommand {}
    
    record UpdateTradeStatusCommand(
        Long tradeId,
        TradeStatus status
    ) implements TradingCommand {}
    
    record ActivateStrategyCommand(
        Long strategyId
    ) implements TradingCommand {}
}

// Queries (для операций чтения)
public sealed interface TradingQuery {
    record GetTradeQuery(Long tradeId) implements TradingQuery {}
    
    record GetTradesBySymbolQuery(String symbol) implements TradingQuery {}
    
    record GetTradingStatisticsQuery(
        LocalDateTime from,
        LocalDateTime to,
        Long strategyId
    ) implements TradingQuery {}
}

// Results
public sealed interface TradingResult {
    record TradeOperationResult(
        boolean success,
        Trade trade,
        String errorMessage
    ) implements TradingResult {
        public static TradeOperationResult success(Trade trade) {
            return new TradeOperationResult(true, trade, null);
        }
        
        public static TradeOperationResult error(String message) {
            return new TradeOperationResult(false, null, message);
        }
    }
    
    record TradeListResult(List<Trade> trades) implements TradingResult {}
    
    record StatisticsResult(TradingStatistics statistics) implements TradingResult {}
}
```

#### 4.2 Создание Use Cases
```java
@Service
@RequiredArgsConstructor
@Transactional
public class ManageTradingUseCase {
    
    private final TradeRepository tradeRepository;
    private final TradingStrategyRepository strategyRepository;
    private final TradingDomainService tradingDomainService;
    
    public TradingResult.TradeOperationResult createTrade(
            TradingCommand.CreateTradeCommand command) {
        
        try {
            // Валидация
            if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
                return TradingResult.TradeOperationResult.error("Amount must be positive");
            }
            
            // Бизнес-логика
            Optional<TradingStrategy> strategy = strategyRepository.findById(command.strategyId());
            if (strategy.isEmpty()) {
                return TradingResult.TradeOperationResult.error("Strategy not found");
            }
            
            // Создание и сохранение
            Trade trade = new Trade(
                null, // будет сгенерирован при сохранении
                command.symbol(),
                command.amount(),
                command.type(),
                TradeStatus.PENDING,
                LocalDateTime.now()
            );
            
            Trade savedTrade = tradeRepository.save(trade);
            return TradingResult.TradeOperationResult.success(savedTrade);
            
        } catch (Exception e) {
            log.error("Failed to create trade", e);
            return TradingResult.TradeOperationResult.error("Failed to create trade: " + e.getMessage());
        }
    }
}

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTradingDataUseCase {
    
    private final TradeRepository tradeRepository;
    private final TradingDomainService tradingDomainService;
    
    public Optional<Trade> getTrade(TradingQuery.GetTradeQuery query) {
        return tradeRepository.findById(query.tradeId());
    }
    
    public TradingResult.TradeListResult getTradesBySymbol(
            TradingQuery.GetTradesBySymbolQuery query) {
        List<Trade> trades = tradeRepository.findBySymbol(query.symbol());
        return new TradingResult.TradeListResult(trades);
    }
    
    public TradingResult.StatisticsResult getTradingStatistics(
            TradingQuery.GetTradingStatisticsQuery query) {
        List<Trade> trades = tradeRepository.findByDateRange(query.from(), query.to());
        TradingStatistics stats = tradingDomainService.generateStatistics(trades, query.from());
        return new TradingResult.StatisticsResult(stats);
    }
}
```

#### 4.3 Создание Application Facade
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class TradingApplicationFacade {
    
    private final ManageTradingUseCase manageTradingUseCase;
    private final GetTradingDataUseCase getTradingDataUseCase;
    
    // Упрощенные методы для API слоя
    public TradingResult.TradeOperationResult createTrade(
            String symbol,
            BigDecimal amount,
            String tradeType,
            Long strategyId) {
        
        TradeType type = TradeType.valueOf(tradeType.toUpperCase());
        TradingCommand.CreateTradeCommand command = new TradingCommand.CreateTradeCommand(
            symbol, amount, type, strategyId
        );
        
        return manageTradingUseCase.createTrade(command);
    }
    
    public Optional<Trade> getTradeById(Long tradeId) {
        TradingQuery.GetTradeQuery query = new TradingQuery.GetTradeQuery(tradeId);
        return getTradingDataUseCase.getTrade(query);
    }
    
    public List<Trade> getTradesBySymbol(String symbol) {
        TradingQuery.GetTradesBySymbolQuery query = new TradingQuery.GetTradesBySymbolQuery(symbol);
        TradingResult.TradeListResult result = getTradingDataUseCase.getTradesBySymbol(query);
        return result.trades();
    }
}
```

### Этап 5: Миграция Infrastructure Layer

#### 5.1 Создание Mappers
```java
@Component
public class TradeMapper {
    
    public Trade toDomain(alg.coyote001.entity.Trade jpaEntity) {
        if (jpaEntity == null) return null;
        
        return new Trade(
            jpaEntity.getId(),
            jpaEntity.getSymbol(),
            jpaEntity.getAmount(),
            TradeType.valueOf(jpaEntity.getType()),
            TradeStatus.valueOf(jpaEntity.getStatus()),
            jpaEntity.getExecutedAt()
        );
    }
    
    public alg.coyote001.entity.Trade toJpa(Trade domain) {
        if (domain == null) return null;
        
        alg.coyote001.entity.Trade jpaEntity = new alg.coyote001.entity.Trade();
        jpaEntity.setId(domain.id());
        jpaEntity.setSymbol(domain.symbol());
        jpaEntity.setAmount(domain.amount());
        jpaEntity.setType(domain.type().name());
        jpaEntity.setStatus(domain.status().name());
        jpaEntity.setExecutedAt(domain.executedAt());
        
        return jpaEntity;
    }
    
    public List<Trade> toDomainList(List<alg.coyote001.entity.Trade> jpaEntities) {
        return jpaEntities.stream()
            .map(this::toDomain)
            .toList();
    }
}
```

#### 5.2 Создание Repository Implementations
```java
@Repository
@RequiredArgsConstructor
public class TradeRepositoryImpl implements TradeRepository {
    
    private final alg.coyote001.repository.TradeRepository jpaTradeRepository;
    private final TradeMapper tradeMapper;
    
    @Override
    public Optional<Trade> findById(Long id) {
        return jpaTradeRepository.findById(id)
            .map(tradeMapper::toDomain);
    }
    
    @Override
    public List<Trade> findBySymbol(String symbol) {
        List<alg.coyote001.entity.Trade> jpaEntities = 
            jpaTradeRepository.findBySymbolIgnoreCase(symbol);
        return tradeMapper.toDomainList(jpaEntities);
    }
    
    @Override
    public List<Trade> findCompletedTrades() {
        List<alg.coyote001.entity.Trade> jpaEntities = 
            jpaTradeRepository.findByStatus("COMPLETED");
        return tradeMapper.toDomainList(jpaEntities);
    }
    
    @Override
    public Trade save(Trade trade) {
        alg.coyote001.entity.Trade jpaEntity = tradeMapper.toJpa(trade);
        alg.coyote001.entity.Trade saved = jpaTradeRepository.save(jpaEntity);
        return tradeMapper.toDomain(saved);
    }
    
    @Override
    public void delete(Long id) {
        jpaTradeRepository.deleteById(id);
    }
}
```

### Этап 6: Миграция API Layer

#### 6.1 Создание API DTOs
```java
public class ApiTradeDto {
    @NotBlank(message = "Symbol is required")
    private String symbol;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Trade type is required")
    @Pattern(regexp = "BUY|SELL", message = "Trade type must be BUY or SELL")
    private String type;
    
    @NotNull(message = "Strategy ID is required")
    private Long strategyId;
    
    // Поля только для чтения (response)
    private Long id;
    private String status;
    private LocalDateTime executedAt;
    
    // Конструкторы, геттеры, сеттеры
}

public class ApiResponseDto<T> {
    private boolean success;
    private T data;
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;
    
    public static <T> ApiResponseDto<T> success(T data) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.success = true;
        response.data = data;
        response.timestamp = LocalDateTime.now();
        return response;
    }
    
    public static <T> ApiResponseDto<T> error(String message, String errorCode) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.success = false;
        response.message = message;
        response.errorCode = errorCode;
        response.timestamp = LocalDateTime.now();
        return response;
    }
}
```

#### 6.2 Создание API Controllers
```java
@RestController
@RequestMapping("/api/v1/trading/trades")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trading API", description = "API для управления трейдами")
public class TradeApiController {
    
    private final TradingApplicationFacade tradingApplicationFacade;
    
    @Operation(summary = "Create new trade", description = "Создать новый трейд")
    @PostMapping
    public ResponseEntity<ApiResponseDto<ApiTradeDto>> createTrade(
            @Valid @RequestBody ApiTradeDto tradeDto) {
        
        log.info("Creating new trade: {}", tradeDto.getSymbol());
        
        try {
            TradingResult.TradeOperationResult result = tradingApplicationFacade.createTrade(
                tradeDto.getSymbol(),
                tradeDto.getAmount(),
                tradeDto.getType(),
                tradeDto.getStrategyId()
            );
            
            if (result.success()) {
                ApiTradeDto responseDto = convertToApiDto(result.trade());
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success(responseDto));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error(result.errorMessage(), "TRADE_CREATION_FAILED"));
            }
            
        } catch (Exception e) {
            log.error("Failed to create trade", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Internal server error", "INTERNAL_ERROR"));
        }
    }
    
    @Operation(summary = "Get trade by ID", description = "Получить трейд по ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ApiTradeDto>> getTrade(@PathVariable Long id) {
        log.debug("Fetching trade by ID: {}", id);
        
        Optional<Trade> trade = tradingApplicationFacade.getTradeById(id);
        
        return trade.map(t -> ResponseEntity.ok(
                ApiResponseDto.success(convertToApiDto(t))
            )).orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Get trades by symbol", description = "Получить трейды по символу")
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<ApiTradeDto>>> getTradesBySymbol(
            @RequestParam String symbol) {
        
        log.debug("Fetching trades by symbol: {}", symbol);
        
        List<Trade> trades = tradingApplicationFacade.getTradesBySymbol(symbol);
        List<ApiTradeDto> tradeDtos = trades.stream()
            .map(this::convertToApiDto)
            .toList();
        
        return ResponseEntity.ok(ApiResponseDto.success(tradeDtos));
    }
    
    private ApiTradeDto convertToApiDto(Trade trade) {
        ApiTradeDto dto = new ApiTradeDto();
        dto.setId(trade.id());
        dto.setSymbol(trade.symbol());
        dto.setAmount(trade.amount());
        dto.setType(trade.type().name());
        dto.setStatus(trade.status().name());
        dto.setExecutedAt(trade.executedAt());
        return dto;
    }
}
```

### Этап 7: Spring Configuration

#### 7.1 Domain Configuration
```java
@Configuration
public class TradingDomainConfig {
    
    @Bean
    public TradingDomainService tradingDomainService() {
        return new TradingDomainService();
    }
    
    @Bean
    public TradeMapper tradeMapper() {
        return new TradeMapper();
    }
    
    @Bean
    public TradingStrategyMapper tradingStrategyMapper() {
        return new TradingStrategyMapper();
    }
    
    @Bean
    public TradeRepository tradeRepository(
            alg.coyote001.repository.TradeRepository jpaTradeRepository,
            TradeMapper tradeMapper) {
        return new TradeRepositoryImpl(jpaTradeRepository, tradeMapper);
    }
    
    @Bean
    public TradingStrategyRepository tradingStrategyRepository(
            alg.coyote001.repository.TradingStrategyRepository jpaTradingStrategyRepository,
            TradingStrategyMapper tradingStrategyMapper) {
        return new TradingStrategyRepositoryImpl(jpaTradingStrategyRepository, tradingStrategyMapper);
    }
}
```

#### 7.2 Application Configuration
```java
@Configuration
public class TradingApplicationConfig {
    
    @Bean
    public ManageTradingUseCase manageTradingUseCase(
            TradeRepository tradeRepository,
            TradingStrategyRepository strategyRepository,
            TradingDomainService tradingDomainService) {
        return new ManageTradingUseCase(tradeRepository, strategyRepository, tradingDomainService);
    }
    
    @Bean
    public GetTradingDataUseCase getTradingDataUseCase(
            TradeRepository tradeRepository,
            TradingDomainService tradingDomainService) {
        return new GetTradingDataUseCase(tradeRepository, tradingDomainService);
    }
    
    @Bean
    public TradingApplicationFacade tradingApplicationFacade(
            ManageTradingUseCase manageTradingUseCase,
            GetTradingDataUseCase getTradingDataUseCase) {
        return new TradingApplicationFacade(manageTradingUseCase, getTradingDataUseCase);
    }
}
```

### Этап 8: Миграционная стратегия

#### 8.1 Создание Migration Adapter (если нужно)
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class TradingLegacyAdapter {
    
    private final TradingApplicationFacade tradingApplicationFacade;
    private final MigrationService migrationService;
    
    public alg.coyote001.dto.TradeDto createTrade(alg.coyote001.dto.TradeDto legacyDto) {
        migrationService.recordLegacyApiUsage("/api/trades", "POST");
        
        log.debug("Legacy API: Creating trade {} via new facade", legacyDto.getSymbol());
        
        TradingResult.TradeOperationResult result = tradingApplicationFacade.createTrade(
            legacyDto.getSymbol(),
            legacyDto.getAmount(),
            legacyDto.getType(),
            legacyDto.getStrategyId()
        );
        
        if (!result.success()) {
            throw new IllegalArgumentException("Failed to create trade: " + result.errorMessage());
        }
        
        return convertToLegacyDto(result.trade());
    }
    
    private alg.coyote001.dto.TradeDto convertToLegacyDto(Trade trade) {
        alg.coyote001.dto.TradeDto dto = new alg.coyote001.dto.TradeDto();
        dto.setId(trade.id());
        dto.setSymbol(trade.symbol());
        dto.setAmount(trade.amount());
        dto.setType(trade.type().name());
        dto.setStatus(trade.status().name());
        dto.setExecutedAt(trade.executedAt());
        return dto;
    }
}
```

#### 8.2 Обновление Legacy Controller
```java
@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
@Slf4j
@Deprecated(since = "2.0.0", forRemoval = true)
@ConditionalOnProperty(name = "trading.migration.legacy-api.enabled", havingValue = "true", matchIfMissing = true)
public class TradeController {
    
    private final TradingLegacyAdapter tradingLegacyAdapter;
    private final MigrationService migrationService;
    
    @PostMapping
    @Deprecated(since = "2.0.0", forRemoval = true)
    public ResponseEntity<alg.coyote001.dto.TradeDto> createTrade(
            @Valid @RequestBody alg.coyote001.dto.TradeDto tradeDto) {
        
        log.warn("DEPRECATED API USAGE: POST /api/v1/trades - Use POST /api/v1/trading/trades instead");
        
        if (!migrationService.isLegacyApiEnabled()) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
        
        try {
            alg.coyote001.dto.TradeDto createdTrade = tradingLegacyAdapter.createTrade(tradeDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                .header("X-Deprecated-API", "true")
                .header("X-New-API-Endpoint", "/api/v1/trading/trades")
                .body(createdTrade);
        } catch (Exception e) {
            log.error("Failed to create trade via legacy API", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
```

## Контрольные списки (Checklists)

### ✅ Domain Layer Checklist
- [ ] Value objects созданы с валидацией
- [ ] Aggregates содержат бизнес-логику
- [ ] Domain repositories определены как интерфейсы
- [ ] Domain services содержат бизнес-правила
- [ ] Нет зависимостей от внешних библиотек
- [ ] Unit тесты покрывают бизнес-логику

### ✅ Application Layer Checklist
- [ ] Commands/Queries определены для всех операций
- [ ] Use cases реализуют бизнес-сценарии
- [ ] Application facade упрощает интеграцию
- [ ] Result objects используются для возврата данных
- [ ] Обработка ошибок через Result pattern
- [ ] Integration тесты покрывают use cases

### ✅ Infrastructure Layer Checklist
- [ ] Mappers конвертируют между слоями
- [ ] Repository implementations работают с JPA
- [ ] Конфигурация Spring правильно настроена
- [ ] Зависимости инъектируются корректно
- [ ] База данных интеграция работает
- [ ] Integration тесты покрывают repositories

### ✅ API Layer Checklist
- [ ] API DTOs с валидацией определены
- [ ] Controllers используют application facade
- [ ] Swagger документация актуальна
- [ ] HTTP статусы корректны
- [ ] Обработка ошибок унифицирована
- [ ] API тесты покрывают endpoints

### ✅ Migration Checklist
- [ ] Legacy adapter создан (если нужно)
- [ ] Legacy controllers обновлены
- [ ] Deprecation warnings добавлены
- [ ] Migration service настроен
- [ ] Мониторинг миграции включен
- [ ] Backward compatibility сохранена

## Общие ошибки и как их избежать

### 1. Нарушение зависимостей между слоями
```java
// ❌ НЕПРАВИЛЬНО: Domain зависит от Infrastructure
public class Trade {
    @Autowired
    private TradeRepository repository; // НЕ ДЕЛАЙТЕ ТАК!
}

// ✅ ПРАВИЛЬНО: Domain не зависит от Infrastructure
public record Trade(Long id, String symbol, BigDecimal amount) {
    // Только бизнес-логика, никаких зависимостей
}
```

### 2. Смешивание JPA entities с Domain objects
```java
// ❌ НЕПРАВИЛЬНО: Использование JPA entity в domain
public class TradingDomainService {
    public void processTrade(alg.coyote001.entity.Trade jpaEntity) {} // НЕ ДЕЛАЙТЕ ТАК!
}

// ✅ ПРАВИЛЬНО: Использование domain objects
public class TradingDomainService {
    public void processTrade(Trade domainTrade) {} // Правильно!
}
```

### 3. Толстые controllers
```java
// ❌ НЕПРАВИЛЬНО: Бизнес-логика в controller
@PostMapping
public ResponseEntity<TradeDto> createTrade(@RequestBody TradeDto dto) {
    // Валидация
    if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        return ResponseEntity.badRequest().build();
    }
    
    // Бизнес-логика
    Trade trade = new Trade(dto.getSymbol(), dto.getAmount());
    // ... много бизнес-логики
}

// ✅ ПРАВИЛЬНО: Делегирование в application layer
@PostMapping
public ResponseEntity<ApiTradeDto> createTrade(@Valid @RequestBody ApiTradeDto dto) {
    TradingResult.TradeOperationResult result = tradingApplicationFacade.createTrade(
        dto.getSymbol(), dto.getAmount(), dto.getType(), dto.getStrategyId()
    );
    
    return result.success() 
        ? ResponseEntity.ok(ApiResponseDto.success(convertToApiDto(result.trade())))
        : ResponseEntity.badRequest().body(ApiResponseDto.error(result.errorMessage(), "CREATION_FAILED"));
}
```

## Инструменты и полезные команды

### Анализ кода
```bash
# Найти все legacy controllers
find src -name "*Controller.java" -path "*/alg/coyote001/*"

# Найти все legacy services
find src -name "*Service.java" -path "*/alg/coyote001/*"

# Найти использование legacy entities в новом коде
grep -r "alg.coyote001.entity" src/main/java/com/ct01/
```

### Генерация структуры
```bash
# Создать структуру нового модуля
mkdir -p src/main/java/com/ct01/{domain}/{domain,application,infrastructure,api}
mkdir -p src/main/java/com/ct01/{domain}/domain/{repository,service}
mkdir -p src/main/java/com/ct01/{domain}/application/{dto,usecase,facade,config}
mkdir -p src/main/java/com/ct01/{domain}/infrastructure/{mapper,persistence,config}
mkdir -p src/main/java/com/ct01/{domain}/api/{dto,controller,config}
```

### Тестирование
```bash
# Запуск тестов для нового модуля
./gradlew test --tests "*com.ct01.{domain}*"

# Запуск integration тестов
./gradlew integrationTest --tests "*{Domain}ApiTest"
```

## Заключение

Данное руководство обеспечивает пошаговый процесс миграции к новой архитектуре. Следуйте этапам последовательно, используйте контрольные списки для проверки правильности реализации и не забывайте о тестировании на каждом этапе.

**Помните**: Миграция - это итеративный процесс. Начните с простого модуля, отработайте процесс, а затем применяйте полученный опыт к более сложным доменам.

Для получения помощи обращайтесь к команде архитектуры или создавайте issue в проекте с меткой "migration". 