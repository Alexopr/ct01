package com.ct01.subscription.api.controller;

import com.ct01.subscription.api.dto.*;
import com.ct01.subscription.application.facade.SubscriptionApplicationFacade;
import com.ct01.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API контроллер для управления подписками
 * Версия API: v1
 * Bounded Context: Subscription
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription API v1", description = "API для управления подписками пользователей")
public class SubscriptionApiController {
    
    private final SubscriptionApplicationFacade subscriptionFacade;
    
    /**
     * Получить доступные планы подписки
     */
    @Operation(
        summary = "Получить доступные планы подписки",
        description = "Возвращает список всех доступных планов подписки с характеристиками",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Планы получены")
        }
    )
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<ApiSubscriptionPlanDto>>> getAvailablePlans(
            @AuthenticationPrincipal Object user,
            HttpServletRequest request) {
        
        log.debug("Запрос доступных планов подписки");
        
        List<ApiSubscriptionPlanDto> plans = subscriptionFacade.getAvailablePlans(user);
        
        return ResponseEntity.ok(
            ApiResponse.success(plans, 
                String.format("Получено %d планов подписки", plans.size()))
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить текущую подписку пользователя
     */
    @Operation(
        summary = "Получить текущую подписку",
        description = "Возвращает информацию о текущей подписке пользователя",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Подписка получена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Подписка не найдена")
        }
    )
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ApiSubscriptionStatusDto>> getCurrentSubscription(
            @AuthenticationPrincipal Object user,
            HttpServletRequest request) {
        
        log.debug("Запрос текущей подписки пользователя");
        
        return subscriptionFacade.getCurrentSubscription(user)
            .map(subscription -> ResponseEntity.ok(
                ApiResponse.success(subscription, "Текущая подписка получена")
                    .withTraceId(getTraceId(request))
            ))
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Получить лимиты использования
     */
    @Operation(
        summary = "Получить лимиты использования",
        description = "Возвращает текущие лимиты использования для подписки пользователя",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Лимиты получены")
        }
    )
    @GetMapping("/limits")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ApiUsageLimitDto>>> getUsageLimits(
            @Parameter(description = "Название модуля", example = "crypto")
            @RequestParam(required = false) String moduleName,
            @AuthenticationPrincipal Object user,
            HttpServletRequest request) {
        
        log.debug("Запрос лимитов использования для модуля: {}", moduleName);
        
        List<ApiUsageLimitDto> limits = subscriptionFacade.getUsageLimits(user, moduleName);
        
        return ResponseEntity.ok(
            ApiResponse.success(limits, "Лимиты использования получены")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Обновить подписку
     */
    @Operation(
        summary = "Обновить подписку",
        description = "Изменить план подписки пользователя",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Подписка обновлена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Недостаточно прав")
        }
    )
    @PostMapping("/upgrade")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ApiSubscriptionStatusDto>> upgradeSubscription(
            @Valid @RequestBody ApiSubscriptionUpgradeRequest request,
            @AuthenticationPrincipal Object user,
            HttpServletRequest httpRequest) {
        
        log.info("Запрос обновления подписки на план: {}", request.planId());
        
        try {
            ApiSubscriptionStatusDto result = subscriptionFacade.upgradeSubscription(user, request);
            
            return ResponseEntity.ok(
                ApiResponse.success(result, "Подписка успешно обновлена")
                    .withTraceId(getTraceId(httpRequest))
            );
        } catch (IllegalArgumentException e) {
            log.error("Ошибка обновления подписки: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_PLAN", e.getMessage()));
        }
    }
    
    /**
     * Отменить подписку
     */
    @Operation(
        summary = "Отменить подписку",
        description = "Отменить текущую подписку пользователя",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Подписка отменена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Активная подписка не найдена")
        }
    )
    @PostMapping("/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> cancelSubscription(
            @AuthenticationPrincipal Object user,
            HttpServletRequest request) {
        
        log.info("Запрос отмены подписки");
        
        boolean cancelled = subscriptionFacade.cancelSubscription(user);
        
        if (cancelled) {
            return ResponseEntity.ok(
                ApiResponse.success("Подписка отменена", "Подписка успешно отменена")
                    .withTraceId(getTraceId(request))
            );
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Проверить лимит ресурса
     */
    @Operation(
        summary = "Проверить лимит ресурса",
        description = "Проверить, не превышен ли лимит использования ресурса",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Проверка выполнена")
        }
    )
    @GetMapping("/check-limit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ApiResourceLimitCheckDto>> checkResourceLimit(
            @Parameter(description = "Название модуля", example = "crypto", required = true)
            @RequestParam String moduleName,
            @Parameter(description = "Тип ресурса", example = "api_requests", required = true)
            @RequestParam String resourceType,
            @Parameter(description = "Количество для проверки", example = "1")
            @RequestParam(defaultValue = "1") int amount,
            @AuthenticationPrincipal Object user,
            HttpServletRequest request) {
        
        log.debug("Проверка лимита ресурса: {} в модуле: {}, количество: {}", 
                 resourceType, moduleName, amount);
        
        ApiResourceLimitCheckDto result = subscriptionFacade.checkResourceLimit(
            user, moduleName, resourceType, amount);
        
        return ResponseEntity.ok(
            ApiResponse.success(result, "Проверка лимита выполнена")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить историю подписок
     */
    @Operation(
        summary = "Получить историю подписок",
        description = "Возвращает историю всех подписок пользователя",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "История получена")
        }
    )
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ApiSubscriptionHistoryDto>>> getSubscriptionHistory(
            @AuthenticationPrincipal Object user,
            HttpServletRequest request) {
        
        log.debug("Запрос истории подписок");
        
        List<ApiSubscriptionHistoryDto> history = subscriptionFacade.getSubscriptionHistory(user);
        
        return ResponseEntity.ok(
            ApiResponse.success(history, 
                String.format("Получено %d записей истории", history.size()))
                .withTraceId(getTraceId(request))
        );
    }
    
    private String getTraceId(HttpServletRequest request) {
        return request.getHeader("X-Trace-ID");
    }
} 