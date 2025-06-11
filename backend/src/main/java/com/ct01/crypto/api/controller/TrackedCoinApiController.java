package com.ct01.crypto.api.controller;

import com.ct01.crypto.api.dto.*;
import com.ct01.crypto.application.facade.CryptoApplicationFacade;
import com.ct01.crypto.application.dto.CoinResult;
import com.ct01.crypto.domain.TrackedCoin;
import com.ct01.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API контроллер для операций с отслеживаемыми монетами
 * Версия API: v1
 */
@RestController
@RequestMapping("/api/v1/crypto/tracked-coins")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tracked Coins API v1", description = "API для работы с отслеживаемыми криптовалютными монетами")
public class TrackedCoinApiController {
    
    private final CryptoApplicationFacade cryptoFacade;
    
    /**
     * Получить все отслеживаемые монеты
     */
    @Operation(
        summary = "Получить все отслеживаемые монеты",
        description = "Возвращает список всех активно отслеживаемых криптовалютных монет",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список получен")
        }
    )
    @GetMapping
    public ApiResponse<List<ApiTrackedCoinDto>> getAllTrackedCoins() {
        log.debug("Запрос всех отслеживаемых монет");
        
        List<TrackedCoin> trackedCoins = cryptoFacade.getActiveTrackedCoins();
        
        List<ApiTrackedCoinDto> dtos = trackedCoins.stream()
            .map(ApiTrackedCoinDto::from)
            .collect(Collectors.toList());
        
        return ApiResponse.success(dtos, 
            String.format("Получено %d отслеживаемых монет", dtos.size()));
    }
    
    /**
     * Получить отслеживаемую монету по ID
     */
    @Operation(
        summary = "Получить отслеживаемую монету по ID",
        description = "Возвращает информацию об отслеживаемой монете по её ID",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Монета найдена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Монета не найдена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректный ID")
        }
    )
    @GetMapping("/{id}")
    public ApiResponse<ApiTrackedCoinDto> getTrackedCoinById(
            @Parameter(description = "ID отслеживаемой монеты", example = "1")
            @PathVariable Long id) {
        
        log.debug("Запрос отслеживаемой монеты с ID: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID монеты должен быть положительным числом");
        }
        
        return cryptoFacade.getTrackedCoinById(id)
            .map(coin -> ApiResponse.success(
                ApiTrackedCoinDto.from(coin), 
                "Отслеживаемая монета найдена"))
            .orElseThrow(() -> new EntityNotFoundException(
                "Отслеживаемая монета с ID " + id + " не найдена"));
    }
    
    /**
     * Получить отслеживаемые монеты по символу
     */
    @Operation(
        summary = "Получить отслеживаемые монеты по символу",
        description = "Возвращает все отслеживаемые монеты с указанным символом",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Монеты получены"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректный символ")
        }
    )
    @GetMapping("/symbol/{symbol}")
    public ApiResponse<List<ApiTrackedCoinDto>> getTrackedCoinsBySymbol(
            @Parameter(description = "Символ монеты", example = "BTC")
            @PathVariable String symbol) {
        
        log.debug("Запрос отслеживаемых монет с символом: {}", symbol);
        
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Символ монеты не может быть пустым");
        }
        
        List<TrackedCoin> trackedCoins = cryptoFacade.getTrackedCoinsBySymbol(symbol.toUpperCase());
        
        List<ApiTrackedCoinDto> dtos = trackedCoins.stream()
            .map(ApiTrackedCoinDto::from)
            .collect(Collectors.toList());
        
        String message = dtos.isEmpty() 
            ? "Отслеживаемые монеты с символом '" + symbol + "' не найдены"
            : String.format("Найдено %d отслеживаемых монет с символом '%s'", dtos.size(), symbol);
        
        return ApiResponse.success(dtos, message);
    }
    
    /**
     * Получить отслеживаемые монеты по бирже
     */
    @Operation(
        summary = "Получить отслеживаемые монеты по бирже",
        description = "Возвращает все отслеживаемые монеты для указанной биржи",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Монеты получены"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректная биржа")
        }
    )
    @GetMapping("/exchange/{exchange}")
    public ApiResponse<List<ApiTrackedCoinDto>> getTrackedCoinsByExchange(
            @Parameter(description = "Название биржи", example = "BINANCE")
            @PathVariable String exchange) {
        
        log.debug("Запрос отслеживаемых монет для биржи: {}", exchange);
        
        if (exchange == null || exchange.trim().isEmpty()) {
            throw new IllegalArgumentException("Название биржи не может быть пустым");
        }
        
        List<TrackedCoin> trackedCoins = cryptoFacade.getTrackedCoinsByExchange(exchange.toUpperCase());
        
        List<ApiTrackedCoinDto> dtos = trackedCoins.stream()
            .map(ApiTrackedCoinDto::from)
            .collect(Collectors.toList());
        
        String message = dtos.isEmpty() 
            ? "Отслеживаемые монеты для биржи '" + exchange + "' не найдены"
            : String.format("Найдено %d отслеживаемых монет для биржи '%s'", dtos.size(), exchange);
        
        return ApiResponse.success(dtos, message);
    }
    
    /**
     * Добавить монету для отслеживания
     */
    @Operation(
        summary = "Добавить монету для отслеживания",
        description = "Добавляет новую монету в список отслеживаемых",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Монета добавлена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Монета уже отслеживается")
        }
    )
    @PostMapping
    public ApiResponse<ApiTrackedCoinDto> addTrackedCoin(
            @RequestBody ApiTrackedCoinCreateDto createDto) {
        
        log.debug("Добавление монеты для отслеживания: {}", createDto);
        
        CoinResult.TrackedCoinOperationResult result = cryptoFacade.addTrackedCoin(
            createDto.getSymbol(),
            createDto.getQuoteCurrency(),
            createDto.getExchange(),
            createDto.getPriority()
        );
        
        if (!result.success()) {
            if (result.errorCode() != null && result.errorCode().contains("ALREADY_EXISTS")) {
                throw new IllegalStateException(result.message());
            }
            throw new IllegalArgumentException(result.message());
        }
        
        return ApiResponse.success(
            ApiTrackedCoinDto.from(result.trackedCoin()),
            "Монета успешно добавлена для отслеживания"
        );
    }
} 
