package com.ct01.crypto.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * API DTO для представления базовой информации о монете
 */
@Schema(description = "Базовая информация о криптовалютной монете")
public record ApiCoinDto(
    
    @Schema(description = "Символ монеты (например, BTC, ETH)", example = "BTC")
    @NotBlank(message = "Символ обязателен")
    @Size(min = 2, max = 20, message = "Символ должен содержать от 2 до 20 символов")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Символ может содержать только заглавные буквы и цифры")
    String symbol,
    
    @Schema(description = "Название монеты", example = "Bitcoin")
    @NotBlank(message = "Название обязательно")
    @Size(min = 2, max = 100, message = "Название должно содержать от 2 до 100 символов")
    String name
) {
    
    /**
     * Фабричный метод для создания API DTO из domain объекта
     */
    public static ApiCoinDto from(com.ct01.crypto.domain.Coin domainCoin) {
        return new ApiCoinDto(
            domainCoin.getSymbol(),
            domainCoin.getName()
        );
    }
    
    /**
     * Конвертация в domain объект
     */
    public com.ct01.crypto.domain.Coin toDomain() {
        return new com.ct01.crypto.domain.Coin(symbol, name);
    }
} 
