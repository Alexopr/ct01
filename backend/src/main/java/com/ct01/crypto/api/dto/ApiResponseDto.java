package com.ct01.crypto.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Стандартизированный wrapper для API ответов
 */
@Schema(description = "Стандартный формат ответа API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponseDto<T>(
    
    @Schema(description = "Статус ответа", example = "success")
    String status,
    
    @Schema(description = "Сообщение о результате операции", example = "Операция выполнена успешно")
    String message,
    
    @Schema(description = "Данные ответа")
    T data,
    
    @Schema(description = "Информация об ошибках")
    List<ApiErrorDto> errors,
    
    @Schema(description = "Время ответа")
    LocalDateTime timestamp,
    
    @Schema(description = "Путь API запроса", example = "/api/v1/crypto/coins")
    String path
) {
    
    /**
     * Создание успешного ответа с данными
     */
    public static <T> ApiResponseDto<T> success(T data, String message) {
        return new ApiResponseDto<>(
            "success",
            message,
            data,
            null,
            LocalDateTime.now(),
            null
        );
    }
    
    /**
     * Создание успешного ответа без данных
     */
    public static <T> ApiResponseDto<T> success(String message) {
        return success(null, message);
    }
    
    /**
     * Создание ответа с ошибкой
     */
    public static <T> ApiResponseDto<T> error(String message, List<ApiErrorDto> errors) {
        return new ApiResponseDto<>(
            "error",
            message,
            null,
            errors,
            LocalDateTime.now(),
            null
        );
    }
    
    /**
     * Создание ответа с одной ошибкой
     */
    public static <T> ApiResponseDto<T> error(String message, String errorCode, String errorDetail) {
        return error(message, List.of(new ApiErrorDto(errorCode, errorDetail, null)));
    }
    
    /**
     * Добавление пути запроса к ответу
     */
    public ApiResponseDto<T> withPath(String path) {
        return new ApiResponseDto<>(status, message, data, errors, timestamp, path);
    }
}

/**
 * DTO для представления ошибки в API
 */
@Schema(description = "Информация об ошибке")
record ApiErrorDto(
    
    @Schema(description = "Код ошибки", example = "VALIDATION_ERROR")
    String code,
    
    @Schema(description = "Описание ошибки", example = "Поле 'symbol' обязательно для заполнения")
    String message,
    
    @Schema(description = "Поле, связанное с ошибкой", example = "symbol")
    String field
) {} 
