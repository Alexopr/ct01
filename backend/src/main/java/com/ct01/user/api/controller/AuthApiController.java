package com.ct01.user.api.controller;

import com.ct01.shared.dto.ApiResponse;
import com.ct01.shared.security.SecurityUtils;
import com.ct01.user.api.dto.*;
import com.ct01.user.application.UserApplicationFacade;
import com.ct01.user.application.dto.AuthResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
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
        description = "Возвращает CSRF токен для защиты от CSRF атак",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CSRF токен сгенерирован")
        }
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
        description = "Аутентификация пользователя по email и паролю",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный вход"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Неверные учетные данные"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "423", description = "Аккаунт заблокирован")
        }
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokenDto>> login(
            @Valid @RequestBody LoginRequestDto loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        log.debug("Попытка входа для пользователя: {}", loginRequest.getEmail());
        
        AuthResult.LoginResult result = userFacade.authenticate(
            loginRequest.getEmail(), 
            loginRequest.getPassword(),
            SecurityUtils.getClientIpAddress(request)
        );
        
        if (!result.success()) {
            throw new BadCredentialsException(result.message());
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
            @Valid @RequestBody RegisterRequestDto registerRequest,
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
        description = "Получить информацию о текущем аутентифицированном пользователе",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Информация о пользователе получена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Пользователь не найден")
        }
    )
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ApiUserDto>> getCurrentUser(HttpServletRequest request) {
        log.debug("Запрос информации о текущем пользователе");
        
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BadCredentialsException("Пользователь не аутентифицирован");
        }
        
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
        description = "Выход из системы и аннулирование токена",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Выход выполнен успешно")
        }
    )
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        
        log.debug("Выход пользователя из системы");
        
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId != null) {
            userFacade.invalidateUserSessions(userId);
        }
        
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
        description = "Аутентификация пользователя через Telegram",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Telegram аутентификация успешна"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Неверные данные Telegram"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректные данные")
        }
    )
    @PostMapping("/telegram")
    public ResponseEntity<ApiResponse<AuthTokenDto>> telegramAuth(
            @Valid @RequestBody TelegramAuthDto telegramData,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        log.debug("Telegram аутентификация для пользователя: {}", telegramData.getTelegramId());
        
        AuthResult.TelegramAuthResult result = userFacade.authenticateViaTelegram(telegramData);
        
        if (!result.success()) {
            throw new BadCredentialsException(result.message());
        }
        
        SecurityUtils.setAuthCookie(response, result.token());
        
        return ResponseEntity.ok(
            ApiResponse.success(
                AuthTokenDto.from(result),
                "Telegram аутентификация успешна")
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