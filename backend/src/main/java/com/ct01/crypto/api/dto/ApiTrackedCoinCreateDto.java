package com.ct01.crypto.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * DTO для создания новой отслеживаемой монеты
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiTrackedCoinCreateDto {
    
    /**
     * Символ криптовалютной монеты (например: BTC, ETH)
     */
    @NotBlank(message = "Символ монеты не может быть пустым")
    @Size(min = 2, max = 10, message = "Символ монеты должен быть от 2 до 10 символов")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Символ монеты должен содержать только заглавные буквы и цифры")
    private String symbol;
    
    /**
     * Котируемая валюта (например: USDT, USD, BTC)
     */
    @NotBlank(message = "Котируемая валюта не может быть пустой")
    @Size(min = 2, max = 10, message = "Котируемая валюта должна быть от 2 до 10 символов")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Котируемая валюта должна содержать только заглавные буквы и цифры")
    private String quoteCurrency;
    
    /**
     * Биржа для отслеживания (например: BINANCE, COINBASE)
     */
    @NotBlank(message = "Биржа не может быть пустой")
    @Size(min = 2, max = 20, message = "Название биржи должно быть от 2 до 20 символов")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Название биржи должно содержать только заглавные буквы, цифры и подчеркивания")
    private String exchange;
    
    /**
     * Приоритет отслеживания (1 = высокий, 5 = низкий)
     */
    @Min(value = 1, message = "Приоритет не может быть меньше 1")
    @Max(value = 5, message = "Приоритет не может быть больше 5")
    private Integer priority = 3; // По умолчанию средний приоритет
} 
