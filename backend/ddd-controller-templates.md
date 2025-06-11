# 🏗️ DDD Controller Templates

Стандартизированные шаблоны для создания контроллеров в DDD архитектуре (com.ct01) для миграции legacy кода из alg.coyote001.

## 🎯 Цель документа
Стандартизированные шаблоны для создания контроллеров в DDD архитектуре (com.ct01) для миграции legacy кода из alg.coyote001.

## 📐 Архитектурные принципы DDD контроллеров

### **1. Структура пакетов:**
```
com/ct01/{bounded-context}/api/controller/
├── {Entity}ApiController.java     # Основной API контроллер
├── dto/
│   ├── Api{Entity}Dto.java       # Response DTO  
│   ├── Api{Entity}CreateDto.java # Create request DTO
│   └── Api{Entity}UpdateDto.java # Update request DTO
```

### **2. Зависимости:**
- **Application Facade** (не прямые сервисы!)
- **Shared DTO** для общих ответов
- **Infrastructure сервисы** только при необходимости

### **3. Naming Convention:**
- **Контроллер:** `{Entity}ApiController`
- **Путь:** `/api/v1/{bounded-context}/{entity-plural}`
- **DTO:** `Api{Entity}Dto`
- **Методы:** `get{Entity}`, `create{Entity}`, `update{Entity}`, `delete{Entity}`

---

## 🎯 Template 1: CRUD Controller (базовый)

```java
package com.ct01.{boundedContext}.api.controller;

import com.ct01.{boundedContext}.api.dto.*;
import com.ct01.{boundedContext}.application.facade.{BoundedContext}ApplicationFacade;
import com.ct01.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API контроллер для операций с {Entity}
 * Версия API: v1
 * Bounded Context: {BoundedContext}
 */
@RestController
@RequestMapping("/api/v1/{bounded-context}/{entity-plural}")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "{Entity} API v1", description = "API для работы с {entity description}")
public class {Entity}ApiController {
    
    private final {BoundedContext}ApplicationFacade {boundedContext}Facade;
    
    /**
     * Получить все {entity-plural}
     */
    @Operation(
        summary = "Получить все {entity-plural}",
        description = "Возвращает список всех {entity-plural}",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список получен")
        }
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<Api{Entity}Dto>>> getAll{Entities}(
            HttpServletRequest request) {
        
        log.debug("Запрос всех {entity-plural}");
        
        List<{Entity}> entities = {boundedContext}Facade.getAll{Entities}();
        
        List<Api{Entity}Dto> dtos = entities.stream()
            .map(Api{Entity}Dto::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(
            ApiResponse.success(dtos, 
                String.format("Получено %d {entity-plural}", dtos.size()))
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить {entity} по ID
     */
    @Operation(
        summary = "Получить {entity} по ID",
        description = "Возвращает {entity} по указанному ID",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "{Entity} найден"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "{Entity} не найден"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректный ID")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Api{Entity}Dto>> get{Entity}ById(
            @Parameter(description = "ID {entity}", example = "1")
            @PathVariable Long id,
            HttpServletRequest request) {
        
        log.debug("Запрос {entity} с ID: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID {entity} должен быть положительным числом");
        }
        
        return {boundedContext}Facade.get{Entity}ById(id)
            .map(entity -> ResponseEntity.ok(
                ApiResponse.success(
                    Api{Entity}Dto.from(entity), 
                    "{Entity} найден")
                    .withTraceId(getTraceId(request))
            ))
            .orElseThrow(() -> new EntityNotFoundException(
                "{Entity} с ID " + id + " не найден"));
    }
    
    /**
     * Создать новый {entity}
     */
    @Operation(
        summary = "Создать {entity}",
        description = "Создает новый {entity}",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "{Entity} создан"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "{Entity} уже существует")
        }
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Api{Entity}Dto>> create{Entity}(
            @RequestBody Api{Entity}CreateDto createDto,
            HttpServletRequest request) {
        
        log.debug("Создание {entity}: {}", createDto);
        
        {Entity}Result.{Entity}OperationResult result = {boundedContext}Facade.create{Entity}(createDto);
        
        if (!result.success()) {
            if (result.errorCode() != null && result.errorCode().contains("ALREADY_EXISTS")) {
                throw new IllegalStateException(result.message());
            }
            throw new IllegalArgumentException(result.message());
        }
        
        return ResponseEntity.ok(
            ApiResponse.success(
                Api{Entity}Dto.from(result.{entity}()),
                "{Entity} успешно создан")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Обновить {entity}
     */
    @Operation(
        summary = "Обновить {entity}",
        description = "Обновляет существующий {entity}",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "{Entity} обновлен"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "{Entity} не найден"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректные данные")
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Api{Entity}Dto>> update{Entity}(
            @Parameter(description = "ID {entity}", example = "1")
            @PathVariable Long id,
            @RequestBody Api{Entity}UpdateDto updateDto,
            HttpServletRequest request) {
        
        log.debug("Обновление {entity} с ID: {}, данные: {}", id, updateDto);
        
        {Entity}Result.{Entity}OperationResult result = {boundedContext}Facade.update{Entity}(id, updateDto);
        
        if (!result.success()) {
            if (result.errorCode() != null && result.errorCode().contains("NOT_FOUND")) {
                throw new EntityNotFoundException(result.message());
            }
            throw new IllegalArgumentException(result.message());
        }
        
        return ResponseEntity.ok(
            ApiResponse.success(
                Api{Entity}Dto.from(result.{entity}()),
                "{Entity} успешно обновлен")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Удалить {entity}
     */
    @Operation(
        summary = "Удалить {entity}",
        description = "Удаляет {entity} по ID",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "{Entity} удален"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "{Entity} не найден")
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete{Entity}(
            @Parameter(description = "ID {entity}", example = "1")
            @PathVariable Long id,
            HttpServletRequest request) {
        
        log.debug("Удаление {entity} с ID: {}", id);
        
        {Entity}Result.{Entity}OperationResult result = {boundedContext}Facade.delete{Entity}(id);
        
        if (!result.success()) {
            if (result.errorCode() != null && result.errorCode().contains("NOT_FOUND")) {
                throw new EntityNotFoundException(result.message());
            }
            throw new IllegalArgumentException(result.message());
        }
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "{Entity} успешно удален")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить trace ID из запроса
     */
    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-ID");
        return traceId != null ? traceId : "system-generated";
    }
}
```

---

## 🔐 Template 2: Auth Controller

```java
package com.ct01.user.api.controller;

import com.ct01.user.api.dto.*;
import com.ct01.user.application.facade.UserApplicationFacade;
import com.ct01.shared.dto.ApiResponse;
import com.ct01.shared.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API контроллер для аутентификации
 * Версия API: v1
 * Bounded Context: User
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication API v1", description = "API для аутентификации пользователей")
public class AuthApiController {
    
    private final UserApplicationFacade userFacade;
    
    /**
     * Получить CSRF токен
     */
    @Operation(
        summary = "Получить CSRF токен",
        description = "Возвращает CSRF токен для защиты от CSRF атак"
    )
    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse<CsrfTokenDto>> getCsrfToken(HttpServletRequest request) {
        log.debug("Запрос CSRF токена");
        
        CsrfTokenDto tokenDto = SecurityUtils.generateCsrfToken(request);
        
        return ResponseEntity.ok(
            ApiResponse.success(tokenDto, "CSRF токен сгенерирован")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Вход в систему
     */
    @Operation(
        summary = "Вход в систему",
        description = "Аутентификация пользователя по логину и паролю",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный вход"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Неверные учетные данные"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "423", description = "Аккаунт заблокирован")
        }
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokenDto>> login(
            @RequestBody LoginRequestDto loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        log.debug("Попытка входа для пользователя: {}", loginRequest.getEmail());
        
        AuthResult.LoginResult result = userFacade.authenticate(
            loginRequest.getEmail(), 
            loginRequest.getPassword(),
            request.getRemoteAddr()
        );
        
        if (!result.success()) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                result.message());
        }
        
        // Установка cookie для сессии
        SecurityUtils.setAuthCookie(response, result.token());
        
        return ResponseEntity.ok(
            ApiResponse.success(
                AuthTokenDto.from(result),
                "Успешный вход в систему")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Регистрация пользователя
     */
    @Operation(
        summary = "Регистрация",
        description = "Регистрация нового пользователя",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Пользователь зарегистрирован"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Пользователь уже существует"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректные данные")
        }
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthTokenDto>> register(
            @RequestBody RegisterRequestDto registerRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        log.debug("Регистрация пользователя: {}", registerRequest.getEmail());
        
        AuthResult.RegistrationResult result = userFacade.registerUser(
            registerRequest.getEmail(),
            registerRequest.getPassword(),
            registerRequest.getName()
        );
        
        if (!result.success()) {
            if (result.errorCode() != null && result.errorCode().contains("ALREADY_EXISTS")) {
                throw new IllegalStateException(result.message());
            }
            throw new IllegalArgumentException(result.message());
        }
        
        // Автоматический вход после регистрации
        SecurityUtils.setAuthCookie(response, result.token());
        
        return ResponseEntity.ok(
            ApiResponse.success(
                AuthTokenDto.from(result),
                "Пользователь успешно зарегистрирован")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить информацию о текущем пользователе
     */
    @Operation(
        summary = "Текущий пользователь",
        description = "Получить информацию о текущем аутентифицированном пользователе"
    )
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ApiUserDto>> getCurrentUser(HttpServletRequest request) {
        log.debug("Запрос информации о текущем пользователе");
        
        Long userId = SecurityUtils.getCurrentUserId();
        
        return userFacade.getUserById(userId)
            .map(user -> ResponseEntity.ok(
                ApiResponse.success(
                    ApiUserDto.from(user),
                    "Информация о пользователе получена")
                    .withTraceId(getTraceId(request))
            ))
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                "Пользователь не найден"));
    }
    
    /**
     * Выход из системы
     */
    @Operation(
        summary = "Выход",
        description = "Выход из системы и аннулирование токена"
    )
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        
        log.debug("Выход пользователя из системы");
        
        Long userId = SecurityUtils.getCurrentUserId();
        userFacade.invalidateUserSessions(userId);
        
        SecurityUtils.clearAuthCookie(response);
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "Выход выполнен успешно")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Аутентификация через Telegram
     */
    @Operation(
        summary = "Вход через Telegram",
        description = "Аутентификация пользователя через Telegram"
    )
    @PostMapping("/telegram")
    public ResponseEntity<ApiResponse<AuthTokenDto>> telegramAuth(
            @RequestBody TelegramAuthDto telegramData,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        log.debug("Telegram аутентификация для пользователя: {}", telegramData.getTelegramId());
        
        AuthResult.TelegramAuthResult result = userFacade.authenticateViaTelegram(telegramData);
        
        if (!result.success()) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                result.message());
        }
        
        SecurityUtils.setAuthCookie(response, result.token());
        
        return ResponseEntity.ok(
            ApiResponse.success(
                AuthTokenDto.from(result),
                "Telegram аутентификация успешна")
                .withTraceId(getTraceId(request))
        );
    }
    
    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-ID");
        return traceId != null ? traceId : "system-generated";
    }
}
```

---

## 🔧 Template 3: Settings Controller

```java
package com.ct01.user.api.controller;

import com.ct01.user.api.dto.*;
import com.ct01.user.application.facade.UserApplicationFacade;
import com.ct01.shared.dto.ApiResponse;
import com.ct01.shared.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API контроллер для настроек пользователя
 * Версия API: v1
 * Bounded Context: User
 */
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Settings API v1", description = "API для управления настройками пользователя")
public class SettingsApiController {
    
    private final UserApplicationFacade userFacade;
    
    /**
     * Получить все настройки пользователя
     */
    @Operation(
        summary = "Получить все настройки",
        description = "Возвращает все настройки текущего пользователя"
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllSettings(HttpServletRequest request) {
        log.debug("Запрос всех настроек пользователя");
        
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> settings = userFacade.getAllUserSettings(userId);
        
        return ResponseEntity.ok(
            ApiResponse.success(settings, "Настройки получены")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить настройки по категории
     */
    @Operation(
        summary = "Получить настройки категории",
        description = "Возвращает настройки указанной категории"
    )
    @GetMapping("/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSettingsByCategory(
            @Parameter(description = "Категория настроек", example = "trading")
            @PathVariable String category,
            HttpServletRequest request) {
        
        log.debug("Запрос настроек категории: {}", category);
        
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> settings = userFacade.getUserSettingsByCategory(userId, category);
        
        return ResponseEntity.ok(
            ApiResponse.success(settings, 
                String.format("Настройки категории '%s' получены", category))
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Обновить настройки категории
     */
    @Operation(
        summary = "Обновить настройки категории",
        description = "Обновляет настройки указанной категории"
    )
    @PutMapping("/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateCategorySettings(
            @Parameter(description = "Категория настроек", example = "trading")
            @PathVariable String category,
            @RequestBody Map<String, Object> settings,
            HttpServletRequest request) {
        
        log.debug("Обновление настроек категории '{}': {}", category, settings);
        
        Long userId = SecurityUtils.getCurrentUserId();
        
        SettingsResult.SettingsUpdateResult result = userFacade.updateUserSettings(
            userId, category, settings);
        
        if (!result.success()) {
            throw new IllegalArgumentException(result.message());
        }
        
        return ResponseEntity.ok(
            ApiResponse.success(result.settings(), 
                String.format("Настройки категории '%s' обновлены", category))
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Сбросить настройки категории
     */
    @Operation(
        summary = "Сбросить настройки",
        description = "Сбрасывает настройки категории к значениям по умолчанию"
    )
    @PostMapping("/{category}/reset")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetCategorySettings(
            @Parameter(description = "Категория настроек", example = "trading")
            @PathVariable String category,
            HttpServletRequest request) {
        
        log.debug("Сброс настроек категории: {}", category);
        
        Long userId = SecurityUtils.getCurrentUserId();
        
        SettingsResult.SettingsResetResult result = userFacade.resetUserSettings(userId, category);
        
        if (!result.success()) {
            throw new IllegalArgumentException(result.message());
        }
        
        return ResponseEntity.ok(
            ApiResponse.success(result.settings(), 
                String.format("Настройки категории '%s' сброшены", category))
                .withTraceId(getTraceId(request))
        );
    }
    
    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-ID");
        return traceId != null ? traceId : "system-generated";
    }
}
```

---

## 📊 Template Mapping для Legacy Controllers

### **Критически важные контроллеры:**

#### 1. **AuthController** → `Template 2: Auth Controller`
- **Bounded Context:** User
- **Package:** `com.ct01.user.api.controller`
- **Класс:** `AuthApiController`

#### 2. **UserManagementController** → `Template 1: CRUD Controller` + Admin методы
- **Bounded Context:** User + Admin
- **Package:** `com.ct01.user.api.controller` + `com.ct01.admin.api.controller`
- **Классы:** `UserApiController` + `AdminUserApiController`

#### 3. **SubscriptionController** → `Template 1: CRUD Controller` + специальные методы
- **Bounded Context:** Subscription  
- **Package:** `com.ct01.subscription.api.controller`
- **Класс:** `SubscriptionApiController`

#### 4. **SettingsController** → `Template 3: Settings Controller`
- **Bounded Context:** User
- **Package:** `com.ct01.user.api.controller`
- **Класс:** `SettingsApiController`

### **Важные контроллеры:**

#### 5. **ExchangeController** → `Template 1: CRUD Controller`
- **Bounded Context:** Market
- **Package:** `com.ct01.market.api.controller`
- **Класс:** `ExchangeApiController`

#### 6. **SystemController** → Специальный template для админа
- **Bounded Context:** Core/Admin
- **Package:** `com.ct01.core.api.controller`
- **Класс:** `SystemApiController`

---

## 🚀 Следующие шаги

1. ✅ **Создать шаблоны контроллеров** 
2. ⏭️ **Начать с AuthController** (подзадача 41.3)
3. ⏭️ **Реализовать User API endpoints** (подзадача 41.4) 
4. ⏭️ **Создать Subscription API endpoints** (подзадача 41.5)
5. ⏭️ **Постепенно мигрировать остальные контроллеры**

---

## 📝 Checklist для каждого нового контроллера

- [ ] **Структура пакетов** согласно DDD
- [ ] **Application Facade** как основная зависимость
- [ ] **Shared ApiResponse** для унифицированных ответов
- [ ] **Swagger аннотации** для документации
- [ ] **Валидация входных данных**
- [ ] **Логирование операций**
- [ ] **Обработка ошибок** через exceptions
- [ ] **TraceId** для трассировки запросов
- [ ] **Security аннотации** где необходимо
- [ ] **DTO маппинг** instead прямого использования domain моделей 