package com.ct01.crypto.api.controller;

import com.ct01.crypto.api.dto.*;
import com.ct01.crypto.application.facade.CryptoApplicationFacade;
import com.ct01.crypto.domain.Exchange;
import com.ct01.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API контроллер для операций с биржами
 * Версия API: v1
 * Bounded Context: Crypto
 */
@RestController
@RequestMapping("/api/v1/crypto/exchanges")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Exchanges API v1", description = "API для работы с биржами криптовалют")
public class ExchangeApiController {
    
    private final CryptoApplicationFacade cryptoFacade;
    
    /**
     * Получить все биржи
     */
    @Operation(
        summary = "Получить все биржи",
        description = "Возвращает пагинированный список всех биржей",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список получен")
        }
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApiExchangeDto>>> getAllExchanges(
            Pageable pageable,
            HttpServletRequest request) {
        
        log.debug("Запрос всех бирж с пагинацией: {}", pageable);
        
        // Заглушка для реализации фасада
        Page<Exchange> exchanges = cryptoFacade.getAllExchanges(pageable);
        
        Page<ApiExchangeDto> dtos = exchanges.map(ApiExchangeDto::from);
        
        return ResponseEntity.ok(
            ApiResponse.success(dtos, 
                String.format("Получено %d бирж", dtos.getNumberOfElements()))
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить только активные биржи
     */
    @Operation(
        summary = "Получить активные биржи",
        description = "Возвращает список всех активных биржей",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список получен")
        }
    )
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ApiExchangeDto>>> getActiveExchanges(
            HttpServletRequest request) {
        
        log.debug("Запрос активных бирж");
        
        List<Exchange> exchanges = cryptoFacade.getActiveExchanges();
        
        List<ApiExchangeDto> dtos = exchanges.stream()
            .map(ApiExchangeDto::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(
            ApiResponse.success(dtos, 
                String.format("Получено %d активных бирж", dtos.size()))
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить биржу по названию
     */
    @Operation(
        summary = "Получить биржу по названию",
        description = "Возвращает информацию о бирже по её названию",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Биржа найдена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Биржа не найдена")
        }
    )
    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<ApiExchangeDto>> getExchangeByName(
            @Parameter(description = "Название биржи", example = "BINANCE")
            @PathVariable String name,
            HttpServletRequest request) {
        
        log.debug("Запрос биржи по названию: {}", name);
        
        return cryptoFacade.getExchangeByName(name)
            .map(exchange -> ResponseEntity.ok(
                ApiResponse.success(ApiExchangeDto.from(exchange), 
                    "Биржа найдена")
                    .withTraceId(getTraceId(request))
            ))
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Получить статус биржи
     */
    @Operation(
        summary = "Получить статус биржи",
        description = "Возвращает текущий статус доступности биржи",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Статус получен"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Биржа не найдена")
        }
    )
    @GetMapping("/{name}/status")
    public ResponseEntity<ApiResponse<ApiExchangeStatusDto>> getExchangeStatus(
            @Parameter(description = "Название биржи", example = "BINANCE")
            @PathVariable String name,
            HttpServletRequest request) {
        
        log.info("Запрос статуса биржи: {}", name);
        
        try {
            Map<String, Object> status = cryptoFacade.getExchangeStatus(name);
            ApiExchangeStatusDto statusDto = ApiExchangeStatusDto.from(name, status);
            
            return ResponseEntity.ok(
                ApiResponse.success(statusDto, "Статус биржи получен")
                    .withTraceId(getTraceId(request))
            );
        } catch (IllegalArgumentException e) {
            log.error("Ошибка получения статуса биржи {}: {}", name, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Получить поддерживаемые торговые пары
     */
    @Operation(
        summary = "Получить поддерживаемые торговые пары",
        description = "Возвращает список всех поддерживаемых торговых пар для биржи",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список получен"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Биржа не найдена")
        }
    )
    @GetMapping("/{name}/pairs")
    public ResponseEntity<ApiResponse<List<String>>> getSupportedTradingPairs(
            @Parameter(description = "Название биржи", example = "BINANCE")
            @PathVariable String name,
            HttpServletRequest request) {
        
        log.debug("Запрос торговых пар для биржи: {}", name);
        
        try {
            List<String> pairs = cryptoFacade.getSupportedTradingPairs(name);
            
            return ResponseEntity.ok(
                ApiResponse.success(pairs, 
                    String.format("Получено %d торговых пар", pairs.size()))
                    .withTraceId(getTraceId(request))
            );
        } catch (IllegalArgumentException e) {
            log.error("Ошибка получения торговых пар для биржи {}: {}", name, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Получить статистику биржи
     */
    @Operation(
        summary = "Получить статистику биржи",
        description = "Возвращает статистическую информацию о бирже",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Статистика получена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Биржа не найдена")
        }
    )
    @GetMapping("/{name}/stats")
    public ResponseEntity<ApiResponse<ApiExchangeStatsDto>> getExchangeStatistics(
            @Parameter(description = "Название биржи", example = "BINANCE")
            @PathVariable String name,
            HttpServletRequest request) {
        
        log.debug("Запрос статистики для биржи: {}", name);
        
        try {
            Map<String, Object> stats = cryptoFacade.getExchangeStatistics(name);
            ApiExchangeStatsDto statsDto = ApiExchangeStatsDto.from(name, stats);
            
            return ResponseEntity.ok(
                ApiResponse.success(statsDto, "Статистика биржи получена")
                    .withTraceId(getTraceId(request))
            );
        } catch (IllegalArgumentException e) {
            log.error("Ошибка получения статистики биржи {}: {}", name, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    private String getTraceId(HttpServletRequest request) {
        return request.getHeader("X-Trace-ID");
    }
} 