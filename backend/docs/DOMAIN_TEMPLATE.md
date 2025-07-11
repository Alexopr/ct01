# Domain Implementation Template

## Обзор

Данный шаблон поможет быстро создать новый домен в системе CT.01, следуя принципам DDD и чистой архитектуры.

## Быстрый старт

### 1. Создание структуры папок

```bash
# Замените {DOMAIN} на имя вашего домена (например, trading, portfolio, analytics)
mkdir -p src/main/java/com/ct01/{DOMAIN}/domain/{repository,service}
mkdir -p src/main/java/com/ct01/{DOMAIN}/application/{dto,usecase,facade,config}
mkdir -p src/main/java/com/ct01/{DOMAIN}/infrastructure/{mapper,persistence,config}
mkdir -p src/main/java/com/ct01/{DOMAIN}/api/{dto,controller,config}
```

### 2. Итоговая структура

```
com.ct01.{DOMAIN}/
├── domain/                           # Бизнес-логика (чистый слой)
│   ├── {Entity}.java                # Value Objects/Entities
│   ├── {Aggregate}.java             # Aggregates с бизнес-методами
│   ├── repository/                  # Интерфейсы репозиториев
│   │   ├── {Entity}Repository.java
│   │   └── {Aggregate}Repository.java
│   └── service/                     # Доменные сервисы
│       └── {Domain}DomainService.java
├── application/                     # Сценарии использования
│   ├── dto/                         # Command/Query/Result DTOs
│   │   ├── {Entity}Command.java
│   │   ├── {Entity}Query.java
│   │   └── {Entity}Result.java
│   ├── usecase/                     # Use Cases
│   │   ├── Manage{Entity}UseCase.java
│   │   └── Get{Entity}UseCase.java
│   ├── facade/                      # Application Facade
│   │   └── {Domain}ApplicationFacade.java
│   └── config/                      # Application Configuration
│       └── {Domain}ApplicationConfig.java
├── infrastructure/                  # Внешние интеграции
│   ├── mapper/                      # Мапперы между слоями
│   │   ├── {Entity}Mapper.java
│   │   └── {Aggregate}Mapper.java
│   ├── persistence/                 # Реализации репозиториев
│   │   ├── {Entity}RepositoryImpl.java
│   │   └── {Aggregate}RepositoryImpl.java
│   └── config/                      # Infrastructure Configuration
│       └── {Domain}DomainConfig.java
└── api/                            # HTTP API
    ├── dto/                        # API DTOs
    │   ├── Api{Entity}Dto.java
    │   ├── Api{Aggregate}Dto.java
    │   ├── ApiResponseDto.java
    │   └── ApiErrorDto.java
    ├── controller/                 # REST Controllers
    │   ├── {Entity}ApiController.java
    │   └── {Aggregate}ApiController.java
    └── config/                     # API Configuration
        └── {Domain}ApiConfig.java
```

## Плейсхолдеры для замены

При использовании шаблона замените:

- `{DOMAIN}` → `trading` (имя домена)
- `{Domain}` → `Trading` (с заглавной буквы)
- `{domain}` → `trading` (camelCase)
- `{Entity}` → `Trade` (основная сущность)
- `{entity}` → `trade` (camelCase)
- `{entities}` → `trades` (множественное число)
- `{Entities}` → `Trades` (множественное с заглавной)
- `{Aggregate}` → `TradingStrategy` (агрегат)
- `{PrimaryField}` → `Symbol` (основное поле)
- `{primaryField}` → `symbol` (camelCase)

## Шаблоны файлов

### Domain Layer

#### Value Object Template
```java
public record {Entity}(
    Long id,
    String {primaryField},
    {FieldType} {secondaryField},
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public {Entity} {
        if ({primaryField} == null || {primaryField}.isBlank()) {
            throw new IllegalArgumentException("{PrimaryField} cannot be null or blank");
        }
    }
    
    public boolean isValid() {
        return isActive && {primaryField} != null;
    }
}
```

#### Repository Interface Template
```java
public interface {Entity}Repository {
    Optional<{Entity}> findById(Long id);
    List<{Entity}> findAll();
    {Entity} save({Entity} {entity});
    void deleteById(Long id);
    
    // Бизнес-специфичные запросы
    List<{Entity}> findActive();
    Optional<{Entity}> findBy{PrimaryField}IgnoreCase(String {primaryField});
}
```

### Application Layer

#### Use Case Template
```java
@Service
@Transactional
public class Manage{Entity}UseCase {
    
    private final {Entity}Repository {entity}Repository;
    
    public {Domain}Result.{Entity}OperationResult create{Entity}(
            {Domain}Command.Create{Entity}Command command) {
        
        try {
            {Entity} {entity} = new {Entity}(
                null,
                command.{primaryField}(),
                command.{secondaryField}(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            
            {Entity} saved = {entity}Repository.save({entity});
            return {Domain}Result.{Entity}OperationResult.success(saved);
            
        } catch (Exception e) {
            return {Domain}Result.{Entity}OperationResult.error(e.getMessage());
        }
    }
}
```

### Infrastructure Layer

#### Mapper Template
```java
@Component
public class {Entity}Mapper {
    
    public {Entity} toDomain(alg.coyote001.entity.{Entity} jpaEntity) {
        return new {Entity}(
            jpaEntity.getId(),
            jpaEntity.get{PrimaryField}(),
            jpaEntity.get{SecondaryField}(),
            jpaEntity.getIsActive(),
            jpaEntity.getCreatedAt(),
            jpaEntity.getUpdatedAt()
        );
    }
    
    public alg.coyote001.entity.{Entity} toJpa({Entity} domain) {
        alg.coyote001.entity.{Entity} jpa = new alg.coyote001.entity.{Entity}();
        jpa.setId(domain.id());
        jpa.set{PrimaryField}(domain.{primaryField}());
        // ... остальные поля
        return jpa;
    }
}
```

### API Layer

#### Controller Template
```java
@RestController
@RequestMapping("/api/v1/{domain}/{entities}")
@Tag(name = "{Domain} API")
public class {Entity}ApiController {
    
    private final {Domain}ApplicationFacade facade;
    
    @PostMapping
    public ResponseEntity<ApiResponseDto<Api{Entity}Dto>> create{Entity}(
            @Valid @RequestBody Api{Entity}Dto dto) {
        
        var result = facade.create{Entity}(
            dto.get{PrimaryField}(),
            dto.get{SecondaryField}()
        );
        
        if (result.success()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(convertToDto(result.{entity}())));
        } else {
            return ResponseEntity.badRequest()
                .body(ApiResponseDto.error(result.errorMessage(), "CREATION_FAILED"));
        }
    }
}
```

## Контрольный список

### ✅ Обязательные шаги
- [ ] Создана структура папок
- [ ] Заменены все плейсхолдеры
- [ ] Созданы JPA репозитории
- [ ] Добавлена конфигурация Spring
- [ ] Написаны unit тесты
- [ ] Написаны integration тесты
- [ ] Обновлена документация Swagger

### ✅ Проверка архитектуры
- [ ] Domain слой не зависит от внешних библиотек
- [ ] Application слой использует только domain интерфейсы
- [ ] Infrastructure реализует domain интерфейсы
- [ ] API слой работает только через Application Facade

## Заключение

Следуйте этому шаблону для создания новых доменов. Используйте crypto домен как пример реализации.
