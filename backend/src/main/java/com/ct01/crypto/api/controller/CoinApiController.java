package com.ct01.crypto.api.controller;

import com.ct01.crypto.api.dto.*;
import com.ct01.crypto.application.facade.CryptoApplicationFacade;
import com.ct01.crypto.application.dto.CoinResult;
import com.ct01.crypto.domain.Coin;
import com.ct01.crypto.infrastructure.service.ExchangeAdapterService;
import com.ct01.shared.dto.ApiResponse;
import com.ct01.shared.exception.ApiErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API контроллер для операций с монетами
 * Версия API: v1
 * Использует унифицированную систему обработки ошибок
 */
@RestController
@RequestMapping("/api/v1/crypto/coins")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Coins API v1", description = "API для работы с базовой информацией о криптовалютных монетах")
public class CoinApiController {
    
    private final CryptoApplicationFacade cryptoFacade;
    private final ExchangeAdapterService exchangeAdapterService;
    
    /**
     * Получить информацию о монете по символу
     */
    @Operation(
        summary = "Получить монету по символу",
        description = "Возвращает информацию о криптовалютной монете по её символу (например, BTC, ETH)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Монета найдена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Монета не найдена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректный символ")
        }
    )
    @GetMapping("/{symbol}")
    public ResponseEntity<ApiResponse<ApiCoinDto>> getCoinBySymbol(
            @Parameter(description = "Символ монеты", example = "BTC")
            @PathVariable String symbol,
            HttpServletRequest request) {
        
        log.debug("Запрос информации о монете с символом: {}", symbol);
        
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Символ монеты не может быть пустым");
        }
        
        return cryptoFacade.getCoinBySymbol(symbol.toUpperCase())
            .map(coin -> {
                ApiCoinDto coinDto = ApiCoinDto.from(coin);
                return ResponseEntity.ok(
                    ApiResponse.success(coinDto, "Информация о монете получена")
                        .withTraceId(getTraceId(request))
                );
            })
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                "Монета с символом '" + symbol + "' не найдена"));
    }
    
    /**
     * Поиск монет по частичному совпадению символа или названия
     */
    @Operation(
        summary = "Поиск монет",
        description = "Поиск криптовалютных монет по частичному совпадению символа или названия",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Результаты поиска получены"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректные параметры поиска")
        }
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ApiCoinDto>>> searchCoins(
            @Parameter(description = "Поисковый запрос", example = "bit")
            @RequestParam String query,
            @Parameter(description = "Максимальное количество результатов", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        
        log.debug("Поиск монет по запросу: '{}', лимит: {}", query, limit);
        
        if (query == null || query.trim().length() < 2) {
            throw new IllegalArgumentException("Поисковый запрос должен содержать минимум 2 символа");
        }
        
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("Лимит должен быть от 1 до 100");
        }
        
        CoinResult.CoinSearchResult searchResult = cryptoFacade.searchCoins(query.trim());
        
        List<ApiCoinDto> coins = searchResult.coins().stream()
            .limit(limit)
            .map(ApiCoinDto::from)
            .collect(Collectors.toList());
        
        String message = coins.isEmpty() 
            ? "Монеты не найдены по запросу '" + query + "'"
            : String.format("Найдено %d монет по запросу '%s'", coins.size(), query);
        
        return ResponseEntity.ok(
            ApiResponse.success(coins, message)
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить список всех доступных монет
     */
    @Operation(
        summary = "Получить все доступные монеты",
        description = "Возвращает список всех доступных криптовалютных монет в системе",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список монет получен")
        }
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<ApiCoinDto>>> getAllCoins(
            HttpServletRequest request) {
        
        log.debug("Запрос списка всех доступных монет");
        
        // Получаем все отслеживаемые монеты и извлекаем базовую информацию
        List<com.ct01.crypto.domain.TrackedCoin> trackedCoins = cryptoFacade.getActiveTrackedCoins();
        
        List<ApiCoinDto> coins = trackedCoins.stream()
            .map(trackedCoin -> new ApiCoinDto(trackedCoin.getSymbol(), trackedCoin.getName()))
            .distinct()
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(
            ApiResponse.success(coins, 
                String.format("Получено %d доступных монет", coins.size()))
                .withTraceId(getTraceId(request))
        );
    }
    
    /**
     * Получить текущую цену монеты с всех бирж
     */
    @Operation(
        summary = "Получить актуальные цены с бирж",
        description = "Получает текущую цену указанной монеты со всех доступных бирж",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Цены получены"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Цены не найдены")
        }
    )
    @GetMapping("/{symbol}/prices")
    public ResponseEntity<ApiResponse<List<ApiPriceDto>>> getCurrentPrices(
            @Parameter(description = "Символ монеты", example = "BTC")
            @PathVariable String symbol,
            HttpServletRequest request) {
        
        log.debug("Запрос цен для символа: {}", symbol);
        
        return exchangeAdapterService.fetchTickerFromAllExchanges(symbol)
            .map(priceHistory -> ApiPriceDto.from(priceHistory))
            .collectList()
            .map(prices -> {
                if (prices.isEmpty()) {
                    throw new jakarta.persistence.EntityNotFoundException(
                        "Цены для символа '" + symbol + "' не найдены на биржах");
                }
                
                return ResponseEntity.ok(
                    ApiResponse.success(prices, 
                        String.format("Получено %d цен для %s", prices.size(), symbol))
                        .withTraceId(getTraceId(request))
                );
            })
            .block(); // Blocking for REST API compatibility
    }
    
    /**
     * Получить лучшую цену для монеты
     */
    @Operation(
        summary = "Получить лучшую цену",
        description = "Возвращает лучшую доступную цену для указанной монеты",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Лучшая цена найдена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Цена не найдена")
        }
    )
    @GetMapping("/{symbol}/best-price")
    public ResponseEntity<ApiResponse<ApiPriceDto>> getBestPrice(
            @Parameter(description = "Символ монеты", example = "BTC")
            @PathVariable String symbol,
            HttpServletRequest request) {
        
        log.debug("Запрос лучшей цены для символа: {}", symbol);
        
        return exchangeAdapterService.getBestPrice(symbol)
            .map(priceHistory -> {
                ApiPriceDto priceDto = ApiPriceDto.from(priceHistory);
                return ResponseEntity.ok(
                    ApiResponse.success(priceDto, 
                        "Лучшая цена для " + symbol + " получена")
                        .withTraceId(getTraceId(request))
                );
            })
            .switchIfEmpty(
                // Fallback на migration adapter если новые адаптеры не работают
                migrationAdapter.getCurrentPriceWithFallback(symbol)
                    .map(priceHistory -> {
                        ApiPriceDto priceDto = ApiPriceDto.from(priceHistory);
                        return ResponseEntity.ok(
                            ApiResponse.success(priceDto, 
                                "Цена для " + symbol + " получена (legacy fallback)")
                                .withTraceId(getTraceId(request))
                        );
                    })
            )
            .blockOptional() // Blocking for REST API compatibility
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                "Цена для символа '" + symbol + "' не найдена"));
    }
    
    /**
     * Получить статус бирж
     */
    @Operation(
        summary = "Статус бирж",
        description = "Получить текущий статус доступности всех подключенных бирж",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Статус бирж получен")
        }
    )
    @GetMapping("/exchanges/status")
    public ResponseEntity<ApiResponse<ExchangeStatusDto>> getExchangeStatus(
            HttpServletRequest request) {
        
        log.debug("Запрос статуса бирж");
        
        return exchangeAdapterService.getExchangeStatus()
            .map(statusMap -> {
                ExchangeStatusDto statusDto = new ExchangeStatusDto(statusMap);
                return ResponseEntity.ok(
                    ApiResponse.success(statusDto, "Статус бирж получен")
                        .withTraceId(getTraceId(request))
                );
            })
            .block(); // Blocking for REST API compatibility
    }
    
    /**
     * Получить поддерживаемые символы для биржи
     */
    @Operation(
        summary = "Поддерживаемые символы биржи",
        description = "Получить список всех поддерживаемых торговых пар для указанной биржи",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Символы получены"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Биржа не найдена")
        }
    )
    @GetMapping("/exchanges/{exchange}/symbols")
    public ResponseEntity<ApiResponse<List<String>>> getSupportedSymbols(
            @Parameter(description = "Название биржи", example = "BINANCE")
            @PathVariable String exchange,
            HttpServletRequest request) {
        
        log.debug("Запрос поддерживаемых символов для биржи: {}", exchange);
        
        if (!exchangeAdapterService.isExchangeAvailable(exchange)) {
            throw new jakarta.persistence.EntityNotFoundException(
                "Биржа '" + exchange + "' недоступна или не поддерживается");
        }
        
        return exchangeAdapterService.getSupportedSymbols(exchange)
            .map(symbols -> ResponseEntity.ok(
                ApiResponse.success(symbols, 
                    String.format("Получено %d символов для биржи %s", symbols.size(), exchange))
                    .withTraceId(getTraceId(request))
            ))
            .block(); // Blocking for REST API compatibility
    }
    
    // ===== Утилиты =====
    
    /**
     * Извлечение или генерация trace ID
     */
    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null) {
            traceId = java.util.UUID.randomUUID().toString().substring(0, 8);
        }
        return traceId;
    }
} 
