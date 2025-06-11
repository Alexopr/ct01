package com.ct01.system.api.controller;

import com.ct01.system.api.dto.*;
import com.ct01.system.application.facade.SystemApplicationFacade;
import com.ct01.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API контроллер для системной информации
 * Версия API: v1
 * Bounded Context: System
 */
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "System API v1", description = "API для получения системной информации и статистики")
public class SystemApiController {
    
    private final SystemApplicationFacade systemFacade;
    
    /**
     * Получить информацию о системе
     */
    @Operation(
        summary = "Получить информацию о системе",
        description = "Возвращает общую информацию о состоянии системы",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Информация получена")
        }
    )
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<ApiSystemInfoDto>> getSystemInfo(
            HttpServletRequest request) {
        
        log.debug("Запрос информации о системе");
        
        ApiSystemInfoDto systemInfo = systemFacade.getSystemInfo();
        
        return ResponseEntity.ok(
            ApiResponse.success(systemInfo, "Информация о системе получена")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить статистику системы
     */
    @Operation(
        summary = "Получить статистику системы",
        description = "Возвращает статистическую информацию о работе системы",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Статистика получена")
        }
    )
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ApiSystemStatsDto>> getSystemStatistics(
            HttpServletRequest request) {
        
        log.debug("Запрос статистики системы");
        
        ApiSystemStatsDto stats = systemFacade.getSystemStatistics();
        
        return ResponseEntity.ok(
            ApiResponse.success(stats, "Статистика системы получена")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Проверить здоровье системы
     */
    @Operation(
        summary = "Проверить здоровье системы",
        description = "Возвращает результаты проверки здоровья всех компонентов системы",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Проверка выполнена")
        }
    )
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<ApiHealthCheckDto>> getHealthCheck(
            HttpServletRequest request) {
        
        log.debug("Запрос проверки здоровья системы");
        
        ApiHealthCheckDto healthCheck = systemFacade.performHealthCheck();
        
        return ResponseEntity.ok(
            ApiResponse.success(healthCheck, "Проверка здоровья системы выполнена")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить конфигурацию системы
     */
    @Operation(
        summary = "Получить конфигурацию системы",
        description = "Возвращает текущую конфигурацию системы (только для администраторов)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Конфигурация получена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещен")
        }
    )
    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemConfiguration(
            HttpServletRequest request) {
        
        log.debug("Запрос конфигурации системы");
        
        Map<String, Object> config = systemFacade.getSystemConfiguration();
        
        return ResponseEntity.ok(
            ApiResponse.success(config, "Конфигурация системы получена")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить метрики производительности
     */
    @Operation(
        summary = "Получить метрики производительности",
        description = "Возвращает детальные метрики производительности системы",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Метрики получены")
        }
    )
    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ApiPerformanceMetricsDto>> getPerformanceMetrics(
            HttpServletRequest request) {
        
        log.debug("Запрос метрик производительности");
        
        ApiPerformanceMetricsDto metrics = systemFacade.getPerformanceMetrics();
        
        return ResponseEntity.ok(
            ApiResponse.success(metrics, "Метрики производительности получены")
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить версию системы
     */
    @Operation(
        summary = "Получить версию системы", 
        description = "Возвращает информацию о версии системы и её компонентов",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Версия получена")
        }
    )
    @GetMapping("/version")
    public ResponseEntity<ApiResponse<ApiVersionInfoDto>> getVersionInfo(
            HttpServletRequest request) {
        
        log.debug("Запрос версии системы");
        
        ApiVersionInfoDto versionInfo = systemFacade.getVersionInfo();
        
        return ResponseEntity.ok(
            ApiResponse.success(versionInfo, "Информация о версии получена")
                .withTraceId(getTraceId(request))
        );
    }
    
    private String getTraceId(HttpServletRequest request) {
        return request.getHeader("X-Trace-ID");
    }
} 