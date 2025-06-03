package alg.coyote001.controller;

import alg.coyote001.config.SubscriptionPlan;
import alg.coyote001.dto.SubscriptionPlanDto;
import alg.coyote001.dto.SubscriptionStatusDto;
import alg.coyote001.dto.SubscriptionUpgradeRequest;
import alg.coyote001.dto.UsageLimitDto;
import alg.coyote001.entity.User;
import alg.coyote001.entity.UserSubscription;
import alg.coyote001.entity.UserSubscription.PlanType;
import alg.coyote001.service.SubscriptionLimitService;
import alg.coyote001.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST контроллер для управления подписками
 */
@RestController
@RequestMapping("/api/subscription")
@Tag(name = "Subscription Management", description = "API для управления подписками пользователей")
@Validated
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    private final SubscriptionService subscriptionService;
    private final SubscriptionLimitService limitService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService,
                                  SubscriptionLimitService limitService) {
        this.subscriptionService = subscriptionService;
        this.limitService = limitService;
    }

    /**
     * Получить доступные планы подписки
     */
    @GetMapping("/plans")
    @Operation(summary = "Получить доступные планы подписки", 
               description = "Возвращает список всех доступных планов подписки с их характеристиками")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Планы успешно получены"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<List<SubscriptionPlanDto>> getAvailablePlans(
            @AuthenticationPrincipal User user) {
        
        try {
            var config = subscriptionService.getSubscriptionConfiguration();
            PlanType currentPlanType = user != null ? 
                subscriptionService.getUserPlanType(user.getId()) : PlanType.FREE;

            List<SubscriptionPlanDto> plans = config.getSubscriptionPlans().entrySet().stream()
                    .map(entry -> convertToPlanDto(entry.getKey(), entry.getValue(), currentPlanType))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(plans);
            
        } catch (Exception e) {
            logger.error("Ошибка при получении планов подписки", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить текущий статус подписки пользователя
     */
    @GetMapping("/status")
    @Operation(summary = "Получить статус подписки", 
               description = "Возвращает текущий статус подписки пользователя и использование лимитов")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Статус успешно получен"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
        @ApiResponse(responseCode = "404", description = "Подписка не найдена")
    })
    public ResponseEntity<SubscriptionStatusDto> getSubscriptionStatus(
            @AuthenticationPrincipal User user) {
        
        try {
            Optional<UserSubscription> subscription = subscriptionService.getActiveSubscription(user.getId());
            
            if (subscription.isEmpty()) {
                // Создаем FREE подписку если ее нет
                UserSubscription freeSubscription = subscriptionService.createFreeSubscription(user.getId());
                subscription = Optional.of(freeSubscription);
            }

            SubscriptionStatusDto statusDto = convertToStatusDto(subscription.get(), user.getId());
            return ResponseEntity.ok(statusDto);
            
        } catch (Exception e) {
            logger.error("Ошибка при получении статуса подписки для пользователя {}", user.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Обновить подписку пользователя
     */
    @PostMapping("/upgrade")
    @Operation(summary = "Обновить подписку", 
               description = "Обновляет подписку пользователя до выбранного плана")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Подписка успешно обновлена"),
        @ApiResponse(responseCode = "400", description = "Неверные данные запроса"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
        @ApiResponse(responseCode = "409", description = "Конфликт - уже имеется активная подписка данного типа")
    })
    public ResponseEntity<SubscriptionStatusDto> upgradeSubscription(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SubscriptionUpgradeRequest request) {
        
        try {
            PlanType targetPlanType = PlanType.valueOf(request.getPlanType());
            PlanType currentPlanType = subscriptionService.getUserPlanType(user.getId());

            // Проверяем, не пытается ли пользователь "обновиться" до того же плана
            if (currentPlanType == targetPlanType) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(convertToStatusDto(subscriptionService.getActiveSubscription(user.getId()).get(), user.getId()));
            }

            UserSubscription newSubscription;
            
            if (targetPlanType == PlanType.PREMIUM) {
                newSubscription = subscriptionService.upgradeToPremium(
                    user.getId(),
                    request.getPrice(),
                    request.getCurrency(),
                    request.getPaymentTransactionId(),
                    request.getPaymentProvider()
                );
            } else {
                // Downgrade to FREE
                newSubscription = subscriptionService.downgradeToFree(
                    user.getId(),
                    "Пользователь выбрал бесплатный план"
                );
            }

            SubscriptionStatusDto statusDto = convertToStatusDto(newSubscription, user.getId());
            return ResponseEntity.ok(statusDto);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Неверный тип плана: {}", request.getPlanType());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Ошибка при обновлении подписки пользователя {}", user.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Отменить подписку
     */
    @PostMapping("/cancel")
    @Operation(summary = "Отменить подписку", 
               description = "Отменяет текущую подписку пользователя")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Подписка успешно отменена"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
        @ApiResponse(responseCode = "404", description = "Активная подписка не найдена")
    })
    public ResponseEntity<SubscriptionStatusDto> cancelSubscription(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String reason) {
        
        try {
            Optional<UserSubscription> subscription = subscriptionService.getActiveSubscription(user.getId());
            
            if (subscription.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String cancellationReason = reason != null ? reason : "Отмена по требованию пользователя";
            UserSubscription cancelledSubscription = subscriptionService.cancelSubscription(
                subscription.get().getId(), cancellationReason);

            SubscriptionStatusDto statusDto = convertToStatusDto(cancelledSubscription, user.getId());
            return ResponseEntity.ok(statusDto);
            
        } catch (Exception e) {
            logger.error("Ошибка при отмене подписки пользователя {}", user.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить использование лимитов по модулю
     */
    @GetMapping("/limits/{moduleName}")
    @Operation(summary = "Получить лимиты модуля", 
               description = "Возвращает текущее использование лимитов для указанного модуля")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Лимиты успешно получены"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
        @ApiResponse(responseCode = "404", description = "Модуль не найден")
    })
    public ResponseEntity<Map<String, UsageLimitDto>> getModuleLimits(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Название модуля") @PathVariable String moduleName) {
        
        try {
            PlanType planType = subscriptionService.getUserPlanType(user.getId());
            Map<String, UsageLimitDto> limits = new HashMap<>();

            // Получаем лимиты для различных ресурсов модуля
            switch (moduleName.toLowerCase()) {
                case "twitter_tracker":
                    limits.put("max_accounts", createUsageLimitDto(user.getId(), moduleName, "max_accounts", planType));
                    limits.put("max_alerts_per_day", createUsageLimitDto(user.getId(), moduleName, "max_alerts_per_day", planType));
                    break;
                case "telegram_tracker":
                    limits.put("max_channels", createUsageLimitDto(user.getId(), moduleName, "max_channels", planType));
                    limits.put("max_alerts_per_day", createUsageLimitDto(user.getId(), moduleName, "max_alerts_per_day", planType));
                    break;
                case "market_analytics":
                    limits.put("max_tracked_contracts", createUsageLimitDto(user.getId(), moduleName, "max_tracked_contracts", planType));
                    break;
                case "smart_money_tracking":
                    limits.put("max_wallets", createUsageLimitDto(user.getId(), moduleName, "max_wallets", planType));
                    break;
                default:
                    return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(limits);
            
        } catch (Exception e) {
            logger.error("Ошибка при получении лимитов модуля {} для пользователя {}", moduleName, user.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Проверить, можно ли использовать ресурс
     */
    @GetMapping("/check/{moduleName}/{resourceType}")
    @Operation(summary = "Проверить доступность ресурса", 
               description = "Проверяет, может ли пользователь использовать указанный ресурс")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Проверка выполнена"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<Map<String, Object>> checkResourceAvailability(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Название модуля") @PathVariable String moduleName,
            @Parameter(description = "Тип ресурса") @PathVariable String resourceType,
            @Parameter(description = "Количество для проверки") @RequestParam(defaultValue = "1") int amount) {
        
        try {
            boolean canUse = limitService.canUseResource(user.getId(), moduleName, resourceType, amount);
            int currentUsage = limitService.getCurrentUsage(user.getId(), moduleName, resourceType);
            int remaining = limitService.getRemainingUsage(user.getId(), moduleName, resourceType);
            PlanType planType = subscriptionService.getUserPlanType(user.getId());
            Integer limit = limitService.getResourceLimit(planType, moduleName, resourceType);

            Map<String, Object> response = new HashMap<>();
            response.put("canUse", canUse);
            response.put("currentUsage", currentUsage);
            response.put("limit", limit);
            response.put("remaining", remaining);
            response.put("requestedAmount", amount);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка при проверке ресурса {}.{} для пользователя {}", 
                        moduleName, resourceType, user.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Вспомогательные методы

    private SubscriptionPlanDto convertToPlanDto(String planType, SubscriptionPlan plan, PlanType currentPlanType) {
        return SubscriptionPlanDto.builder()
                .planType(planType)
                .name(plan.getName())
                .price(BigDecimal.valueOf(plan.getPrice()))
                .currency(plan.getCurrency())
                .billingCycle(plan.getBillingCycle())
                .trialDays(plan.getTrialDays())
                .refundDays(plan.getRefundDays())
                .features(plan.getFeatures())
                .limits(convertLimitsToMap(plan.getLimits()))
                .isCurrent(planType.equals(currentPlanType.name()))
                .isRecommended("PREMIUM".equals(planType))
                .build();
    }

    private SubscriptionStatusDto convertToStatusDto(UserSubscription subscription, Long userId) {
        Map<String, UsageLimitDto> currentUsage = new HashMap<>();
        
        // Получаем текущее использование для основных модулей
        PlanType planType = subscription.getPlanType();
        currentUsage.put("twitter_tracker", createUsageLimitDto(userId, "twitter_tracker", "max_accounts", planType));
        currentUsage.put("telegram_tracker", createUsageLimitDto(userId, "telegram_tracker", "max_channels", planType));

        return SubscriptionStatusDto.builder()
                .subscriptionId(subscription.getId())
                .planType(subscription.getPlanType().name())
                .planName(subscriptionService.getPlanConfiguration(subscription.getPlanType()).getName())
                .status(subscription.getStatus().name())
                .startsAt(subscription.getStartsAt())
                .expiresAt(subscription.getExpiresAt())
                .trialEndsAt(subscription.getTrialEndsAt())
                .nextBillingDate(subscription.getNextBillingDate())
                .price(subscription.getPrice())
                .currency(subscription.getCurrency())
                .autoRenewal(subscription.isAutoRenewal())
                .isActive(subscription.isActive())
                .isInTrial(subscription.isInTrial())
                .daysUntilExpiry(subscription.getDaysUntilExpiry())
                .currentUsage(currentUsage)
                .build();
    }

    private UsageLimitDto createUsageLimitDto(Long userId, String moduleName, String resourceType, PlanType planType) {
        Integer limit = limitService.getResourceLimit(planType, moduleName, resourceType);
        
        if (limit == null || limit == 0) {
            return UsageLimitDto.unavailable(moduleName, resourceType);
        }
        
        if (limit == -1) {
            return UsageLimitDto.unlimited(moduleName, resourceType);
        }

        int currentUsage = limitService.getCurrentUsage(userId, moduleName, resourceType);
        int remaining = limitService.getRemainingUsage(userId, moduleName, resourceType);
        double usagePercentage = limit > 0 ? (double) currentUsage / limit * 100.0 : 0.0;
        boolean isExceeded = limitService.isLimitExceeded(userId, moduleName, resourceType);

        return UsageLimitDto.builder()
                .moduleName(moduleName)
                .resourceType(resourceType)
                .currentUsage(currentUsage)
                .limit(limit)
                .remaining(remaining)
                .usagePercentage(usagePercentage)
                .isLimitExceeded(isExceeded)
                .resetPeriod("DAILY")
                .isUnlimited(false)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertLimitsToMap(Object limits) {
        // Простейшая реализация - можно улучшить с помощью ObjectMapper
        return new HashMap<>();
    }
} 