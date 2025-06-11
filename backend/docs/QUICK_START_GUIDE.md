# Quick Start Guide - Новый Домен за 60 минут

## Шаг 1: Подготовка (5 мин)

```bash
# Определите параметры
DOMAIN="trading"
ENTITY="Trade"
PRIMARY_FIELD="symbol"

# Создайте структуру папок
mkdir -p src/main/java/com/ct01/trading/{domain/{repository,service},application/{dto,usecase,facade},infrastructure/{mapper,persistence,config},api/{dto,controller}}
```

## Шаг 2: Value Object (5 мин)

```java
// domain/Trade.java
public record Trade(
    Long id,
    String symbol,
    BigDecimal amount,
    TradeType type,
    boolean isActive,
    LocalDateTime createdAt
) {
    public Trade {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol required");
        }
    }
    
    public boolean isValid() {
        return isActive && symbol != null;
    }
}

enum TradeType { BUY, SELL }
```

## Шаг 3: Repository Interface (3 мин)

```java
// domain/repository/TradeRepository.java
public interface TradeRepository {
    Optional<Trade> findById(Long id);
    List<Trade> findAll();
    Trade save(Trade trade);
    void deleteById(Long id);
    List<Trade> findActiveBySymbol(String symbol);
}
```

## Шаг 4: DTOs (5 мин)

```java
// application/dto/TradingCommand.java
public sealed interface TradingCommand {
    record CreateTradeCommand(String symbol, BigDecimal amount, String type) implements TradingCommand {}
    record UpdateTradeCommand(Long id, BigDecimal amount) implements TradingCommand {}
}

// TradingResult.java
public sealed interface TradingResult {
    record TradeOperationResult(boolean success, Trade trade, String errorMessage) {
        public static TradeOperationResult success(Trade trade) {
            return new TradeOperationResult(true, trade, null);
        }
        public static TradeOperationResult error(String message) {
            return new TradeOperationResult(false, null, message);
        }
    }
}
```

## Шаг 5: Use Case (8 мин)

```java
// application/usecase/ManageTradeUseCase.java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ManageTradeUseCase {
    private final TradeRepository tradeRepository;
    
    public TradingResult.TradeOperationResult createTrade(TradingCommand.CreateTradeCommand cmd) {
        try {
            if (cmd.symbol() == null || cmd.symbol().isBlank()) {
                return TradingResult.TradeOperationResult.error("Symbol required");
            }
            
            Trade trade = new Trade(
                null, cmd.symbol(), cmd.amount(), 
                TradeType.valueOf(cmd.type()), true, LocalDateTime.now()
            );
            
            Trade saved = tradeRepository.save(trade);
            log.info("Created trade: {}", saved.id());
            return TradingResult.TradeOperationResult.success(saved);
            
        } catch (Exception e) {
            log.error("Failed to create trade", e);
            return TradingResult.TradeOperationResult.error("Creation failed: " + e.getMessage());
        }
    }
}
```

## Шаг 6: Application Facade (5 мин)

```java
// application/facade/TradingApplicationFacade.java
@Service
@RequiredArgsConstructor
public class TradingApplicationFacade {
    private final ManageTradeUseCase manageTradeUseCase;
    private final GetTradeUseCase getTradeUseCase;
    
    public TradingResult.TradeOperationResult createTrade(String symbol, BigDecimal amount, String type) {
        var command = new TradingCommand.CreateTradeCommand(symbol, amount, type);
        return manageTradeUseCase.createTrade(command);
    }
    
    public Optional<Trade> getTradeById(Long id) {
        return getTradeUseCase.getTradeById(id);
    }
}
```

## Шаг 7: Mapper (5 мин)

```java
// infrastructure/mapper/TradeMapper.java
@Component
public class TradeMapper {
    public Trade toDomain(alg.coyote001.entity.Trade jpa) {
        return new Trade(jpa.getId(), jpa.getSymbol(), jpa.getAmount(), 
                        TradeType.valueOf(jpa.getType()), jpa.getIsActive(), jpa.getCreatedAt());
    }
    
    public alg.coyote001.entity.Trade toJpa(Trade domain) {
        var jpa = new alg.coyote001.entity.Trade();
        jpa.setId(domain.id());
        jpa.setSymbol(domain.symbol());
        jpa.setAmount(domain.amount());
        jpa.setType(domain.type().name());
        jpa.setIsActive(domain.isActive());
        jpa.setCreatedAt(domain.createdAt());
        return jpa;
    }
}
```

## Шаг 8: Repository Impl (5 мин)

```java
// infrastructure/persistence/TradeRepositoryImpl.java
@Repository
@RequiredArgsConstructor
public class TradeRepositoryImpl implements TradeRepository {
    private final alg.coyote001.repository.TradeRepository jpaRepo;
    private final TradeMapper mapper;
    
    @Override
    public Optional<Trade> findById(Long id) {
        return jpaRepo.findById(id).map(mapper::toDomain);
    }
    
    @Override
    public Trade save(Trade trade) {
        var saved = jpaRepo.save(mapper.toJpa(trade));
        return mapper.toDomain(saved);
    }
    
    @Override
    public List<Trade> findActiveBySymbol(String symbol) {
        return jpaRepo.findBySymbolAndIsActiveTrue(symbol)
            .stream().map(mapper::toDomain).toList();
    }
}
```

## Шаг 9: API Controller (10 мин)

```java
// api/controller/TradeApiController.java
@RestController
@RequestMapping("/api/v1/trading/trades")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trading API")
public class TradeApiController {
    private final TradingApplicationFacade facade;
    
    @PostMapping
    public ResponseEntity<ApiResponseDto<ApiTradeDto>> createTrade(@Valid @RequestBody ApiTradeDto dto) {
        var result = facade.createTrade(dto.getSymbol(), dto.getAmount(), dto.getType());
        
        if (result.success()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(convertToDto(result.trade())));
        } else {
            return ResponseEntity.badRequest()
                .body(ApiResponseDto.error(result.errorMessage(), "CREATION_FAILED"));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ApiTradeDto>> getTrade(@PathVariable Long id) {
        return facade.getTradeById(id)
            .map(trade -> ResponseEntity.ok(ApiResponseDto.success(convertToDto(trade))))
            .orElse(ResponseEntity.notFound().build());
    }
    
    private ApiTradeDto convertToDto(Trade trade) {
        var dto = new ApiTradeDto();
        dto.setId(trade.id());
        dto.setSymbol(trade.symbol());
        dto.setAmount(trade.amount());
        dto.setType(trade.type().name());
        dto.setIsActive(trade.isActive());
        return dto;
    }
}
```

## Шаг 10: API DTO (5 мин)

```java
// api/dto/ApiTradeDto.java
@Data
public class ApiTradeDto {
    private Long id;
    
    @NotBlank(message = "Symbol required")
    private String symbol;
    
    @NotNull @Positive
    private BigDecimal amount;
    
    @Pattern(regexp = "BUY|SELL")
    private String type;
    
    private Boolean isActive;
    private LocalDateTime createdAt;
}

// api/dto/ApiResponseDto.java (если не существует)
@Data
public class ApiResponseDto<T> {
    private boolean success;
    private T data;
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;
    
    public static <T> ApiResponseDto<T> success(T data) {
        var response = new ApiResponseDto<T>();
        response.success = true;
        response.data = data;
        response.timestamp = LocalDateTime.now();
        return response;
    }
    
    public static <T> ApiResponseDto<T> error(String message, String code) {
        var response = new ApiResponseDto<T>();
        response.success = false;
        response.message = message;
        response.errorCode = code;
        response.timestamp = LocalDateTime.now();
        return response;
    }
}
```

## Шаг 11: Configuration (5 мин)

```java
// infrastructure/config/TradingDomainConfig.java
@Configuration
public class TradingDomainConfig {
    
    @Bean
    public TradeRepository tradeRepository(
            alg.coyote001.repository.TradeRepository jpaRepo,
            TradeMapper mapper) {
        return new TradeRepositoryImpl(jpaRepo, mapper);
    }
}

// Обновить главную конфигурацию:
@ComponentScan(basePackages = {
    "alg.coyote001",
    "com.ct01.crypto",
    "com.ct01.trading"  // Добавить
})
```

## Шаг 12: JPA Repository (2 мин)

```java
// В alg.coyote001.repository
@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findBySymbolAndIsActiveTrue(String symbol);
    List<Trade> findByIsActiveTrue();
}
```

## Шаг 13: Базовый тест (5 мин)

```java
// test/.../domain/TradeTest.java
class TradeTest {
    @Test
    void should_Create_Valid_Trade() {
        var trade = new Trade(1L, "BTCUSD", new BigDecimal("100"), 
                             TradeType.BUY, true, LocalDateTime.now());
        
        assertThat(trade.symbol()).isEqualTo("BTCUSD");
        assertThat(trade.isValid()).isTrue();
    }
    
    @Test
    void should_Fail_For_Empty_Symbol() {
        assertThatThrownBy(() -> new Trade(1L, "", new BigDecimal("100"), 
                                          TradeType.BUY, true, LocalDateTime.now()))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

## Контрольный список ✅

- [ ] Domain объекты без внешних зависимостей
- [ ] Repository interface в domain
- [ ] Use cases возвращают Result objects  
- [ ] Application Facade для упрощения API
- [ ] Mappers изолируют слои
- [ ] API использует валидацию
- [ ] Spring configuration настроена
- [ ] JPA repository создан
- [ ] Базовые тесты написаны

## Общее время: ~60 минут

Следуя этому руководству, за час вы создадите полностью функциональный домен с правильной DDD архитектурой.

## Проверка работы

```bash
# Запустите приложение
./mvnw spring-boot:run

# Проверьте Swagger
curl http://localhost:8080/swagger-ui.html

# Создайте trade
curl -X POST http://localhost:8080/api/v1/trading/trades \
  -H "Content-Type: application/json" \
  -d '{"symbol":"BTCUSD","amount":100.50,"type":"BUY"}'
```
