package com.ct01.admin.api.controller;

import com.ct01.admin.api.dto.*;
import com.ct01.admin.application.facade.AdminApplicationFacade;
import com.ct01.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API контроллер для анализа и обнаружения API
 * Версия API: v1
 * Bounded Context: Admin
 */
@RestController
@RequestMapping("/api/v1/admin/analysis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API Analysis v1", description = "API для анализа и обнаружения endpoints системы")
public class ApiAnalysisApiController {
    
    private final AdminApplicationFacade adminFacade;
    
    /**
     * Обнаружить все API endpoints
     */
    @Operation(
        summary = "Обнаружить все API endpoints",
        description = "Сканирует и анализирует все API endpoints в приложении",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Обнаружение завершено"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка во время обнаружения"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещен")
        }
    )
    @GetMapping("/discover")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ApiDiscoveryReportDto>> discoverEndpoints(
            HttpServletRequest request) {
        
        log.info("Запуск процесса обнаружения API");
        
        try {
            ApiDiscoveryReportDto report = adminFacade.discoverAllEndpoints();
            
            log.info("Обнаружение API завершено успешно. Найдено {} endpoints", 
                    report.totalEndpoints());
            
            return ResponseEntity.ok(
                ApiResponse.success(report, "Обнаружение API завершено успешно")
                    .withTraceId(getTraceId(request))
            );
            
        } catch (Exception e) {
            log.error("Ошибка во время обнаружения API", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("DISCOVERY_ERROR", "Ошибка во время обнаружения API"));
        }
    }
    
    /**
     * Обнаружить и сохранить API endpoints в файл
     */
    @Operation(
        summary = "Обнаружить и сохранить API endpoints",
        description = "Сканирует все API endpoints и сохраняет отчет в файл",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Обнаружение завершено и сохранено"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректный путь к файлу"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка во время обнаружения или сохранения")
        }
    )
    @PostMapping("/discover/save")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> discoverAndSaveEndpoints(
            @Parameter(description = "Имя файла", example = "api-discovery-report.json")
            @RequestParam(defaultValue = "api-discovery-report.json") String filename,
            HttpServletRequest request) {
        
        log.info("Запуск процесса обнаружения и сохранения API, файл: {}", filename);
        
        try {
            Map<String, Object> result = adminFacade.discoverAndSaveEndpoints(filename);
            
            log.info("Обнаружение и сохранение API завершено успешно");
            return ResponseEntity.ok(
                ApiResponse.success(result, "Обнаружение API завершено и сохранено успешно")
                    .withTraceId(getTraceId(request))
            );
            
        } catch (IllegalArgumentException e) {
            log.error("Ошибка сохранения отчета в файл", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("SAVE_ERROR", "Не удалось сохранить отчет в файл: " + e.getMessage()));
                    
        } catch (Exception e) {
            log.error("Ошибка во время обнаружения API", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("DISCOVERY_ERROR", "Не удалось выполнить обнаружение API: " + e.getMessage()));
        }
    }
    
    /**
     * Получить отчет об обнаружении в JSON формате
     */
    @Operation(
        summary = "Получить отчет в JSON формате",
        description = "Возвращает отчет об обнаружении API в виде JSON строки",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "JSON отчет сгенерирован"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка во время обнаружения")
        }
    )
    @GetMapping("/discover/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> getDiscoveryAsJson(
            HttpServletRequest request) {
        
        log.info("Получение отчета обнаружения API в JSON формате");
        
        try {
            String jsonReport = adminFacade.getDiscoveryAsJson();
            
            return ResponseEntity.ok(
                ApiResponse.success(jsonReport, "JSON отчет сгенерирован успешно")
                    .withTraceId(getTraceId(request))
            );
                    
        } catch (Exception e) {
            log.error("Ошибка конвертации отчета в JSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("JSON_ERROR", "Не удалось конвертировать отчет в JSON"));
        }
    }
    
    /**
     * Получить сводную статистику API
     */
    @Operation(
        summary = "Получить сводную статистику API",
        description = "Возвращает сводную статистику всех API endpoints",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Статистика сгенерирована"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка во время анализа")
        }
    )
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ApiDiscoverySummaryDto>> getApiSummary(
            HttpServletRequest request) {
        
        log.info("Получение сводной статистики API");
        
        try {
            ApiDiscoverySummaryDto summary = adminFacade.getApiSummary();
            
            return ResponseEntity.ok(
                ApiResponse.success(summary, "Статистика API получена успешно")
                    .withTraceId(getTraceId(request))
            );
            
        } catch (Exception e) {
            log.error("Ошибка получения статистики API", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SUMMARY_ERROR", "Ошибка получения статистики API"));
        }
    }
    
    /**
     * Проверка работоспособности сервиса анализа
     */
    @Operation(
        summary = "Проверка работоспособности анализа",
        description = "Проверяет работоспособность сервиса анализа API",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Сервис работает"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка сервиса")
        }
    )
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkAnalysisHealth(
            HttpServletRequest request) {
        
        log.info("Проверка работоспособности сервиса анализа API");
        
        try {
            Map<String, Object> health = adminFacade.checkAnalysisHealth();
            
            return ResponseEntity.ok(
                ApiResponse.success(health, "Сервис анализа API работает")
                    .withTraceId(getTraceId(request))
            );
            
        } catch (Exception e) {
            log.error("Ошибка проверки работоспособности", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("HEALTH_ERROR", "Ошибка проверки работоспособности сервиса"));
        }
    }
    
    /**
     * Тестовое обнаружение API (без аутентификации для тестов)
     */
    @Operation(
        summary = "Тестовое обнаружение API",
        description = "Тестовый endpoint для обнаружения API без аутентификации",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Тест завершен"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка во время теста")
        }
    )
    @GetMapping("/discover/test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testDiscovery(
            HttpServletRequest request) {
        
        log.info("Запуск тестового обнаружения API");
        
        try {
            Map<String, Object> testResult = adminFacade.testDiscovery();
            
            log.info("Тестовое обнаружение API завершено успешно");
            
            return ResponseEntity.ok(
                ApiResponse.success(testResult, "Тест обнаружения API завершен успешно")
                    .withTraceId(getTraceId(request))
            );
            
        } catch (Exception e) {
            log.error("Ошибка во время тестового обнаружения API", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("TEST_ERROR", "Тест обнаружения API неудачен: " + e.getMessage()));
        }
    }
    
    private String getTraceId(HttpServletRequest request) {
        return request.getHeader("X-Trace-ID");
    }
} 