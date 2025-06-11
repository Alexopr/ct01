package com.ct01.crypto.api.controller;

import com.ct01.crypto.api.dto.*;
import com.ct01.crypto.application.facade.CryptoApplicationFacade;
import com.ct01.crypto.application.dto.CoinResult;
import com.ct01.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API контроллер для операций с данными о ценах
 * Версия API: v1
 */
@RestController
@RequestMapping("/api/v1/crypto/prices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Prices API v1", description = "API для работы с данными о ценах криптовалют")
public class PriceApiController {
    
    private final CryptoApplicationFacade cryptoFacade;
    
    /**
     * Получить текущую цену монеты на конкретной бирже
     */
    @Operation(
        summary = "Получить текущую цену",
        description = "Возвращает текущую цену криптовалютной монеты на указанной бирже",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Цена получена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Данные не найдены"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректные параметры")
        }
    )
    @GetMapping("/current/{symbol}")
    public ApiResponse<ApiPriceDataDto> getCurrentPrice(
            @Parameter(description = "Символ монеты", example = "BTC")
            @PathVariable String symbol,
            @Parameter(description = "Котируемая валюта", example = "USDT")
            @RequestParam String quoteCurrency,
            @Parameter(description = "Биржа", example = "BINANCE")
            @RequestParam String exchange) {
        
        log.debug("Получение текущей цены: {}/{} на бирже {}", symbol, quoteCurrency, exchange);
        
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Символ монеты не может быть пустым");
        }
        if (quoteCurrency == null || quoteCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Котируемая валюта не может быть пустой");
        }
        if (exchange == null || exchange.trim().isEmpty()) {
            throw new IllegalArgumentException("Биржа не может быть пустой");
        }
        
        CoinResult.PriceResult result = cryptoFacade.getCurrentPrice(
            symbol.toUpperCase(), 
            quoteCurrency.toUpperCase(), 
            exchange.toUpperCase()
        );
        
        if (!result.success()) {
            throw new EntityNotFoundException(String.format(
                "Текущая цена для %s/%s на бирже %s не найдена", 
                symbol.toUpperCase(), quoteCurrency.toUpperCase(), exchange.toUpperCase()));
        }
        
        ApiPriceDataDto priceDto = ApiPriceDataDto.from(result.priceHistory());
        return ApiResponse.success(priceDto, "Текущая цена получена");
    }
    
    /**
     * Получить исторические данные о ценах
     */
    @Operation(
        summary = "Получить исторические данные",
        description = "Возвращает исторические данные о ценах за указанный период",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Исторические данные получены"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректные параметры")
        }
    )
    @GetMapping("/historical/{symbol}")
    public ApiResponse<List<ApiPriceDataDto>> getHistoricalData(
            @Parameter(description = "Символ монеты", example = "BTC")
            @PathVariable String symbol,
            @Parameter(description = "Котируемая валюта", example = "USDT")
            @RequestParam String quoteCurrency,
            @Parameter(description = "Биржа", example = "BINANCE")
            @RequestParam String exchange,
            @Parameter(description = "Начальная дата и время", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
            @Parameter(description = "Конечная дата и время", example = "2024-01-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime,
            @Parameter(description = "Максимальное количество записей", example = "100")
            @RequestParam(defaultValue = "100") int limit) {
        
        log.debug("Получение исторических данных {}/{} на {} с {} по {}", 
                 symbol, quoteCurrency, exchange, fromTime, toTime);
        
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Символ монеты не может быть пустым");
        }
        if (quoteCurrency == null || quoteCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Котируемая валюта не может быть пустой");
        }
        if (exchange == null || exchange.trim().isEmpty()) {
            throw new IllegalArgumentException("Биржа не может быть пустой");
        }
        if (fromTime == null) {
            throw new IllegalArgumentException("Начальная дата не может быть пустой");
        }
        if (toTime == null) {
            throw new IllegalArgumentException("Конечная дата не может быть пустой");
        }
        if (fromTime.isAfter(toTime)) {
            throw new IllegalArgumentException("Начальная дата не может быть больше конечной");
        }
        if (limit <= 0 || limit > 1000) {
            throw new IllegalArgumentException("Лимит должен быть от 1 до 1000");
        }
        
        CoinResult.PriceListResult result = cryptoFacade.getHistoricalData(
            symbol.toUpperCase(),
            quoteCurrency.toUpperCase(),
            exchange.toUpperCase(),
            fromTime,
            toTime,
            limit
        );
        
        if (result.success()) {
            List<ApiPriceDataDto> historicalData = result.priceHistories().stream()
                .map(ApiPriceDataDto::from)
                .collect(Collectors.toList());
            
            return ApiResponse.success(historicalData,
                String.format("Получено %d исторических записей", historicalData.size()));
        } else {
            return ApiResponse.success(List.of(), "Исторические данные не найдены");
        }
    }
} 
