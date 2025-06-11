package com.ct01.shared.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Унифицированный класс для представления ошибок в API
 * Используется во всех модулях системы CT.01 для обеспечения консистентности
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Стандартный ответ об ошибке")
public class ErrorResponse {
    
    /**
     * Время возникновения ошибки
     */
    @Schema(description = "Время возникновения ошибки", example = "2024-01-15T10:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * HTTP статус код
     */
    @Schema(description = "HTTP статус код", example = "400")
    private int status;
    
    /**
     * Краткое описание типа ошибки
     */
    @Schema(description = "Тип ошибки", example = "Bad Request")
    private String error;
    
    /**
     * Основное сообщение об ошибке
     */
    @Schema(description = "Сообщение об ошибке", example = "Неверный формат запроса")
    private String message;
    
    /**
     * Путь запроса, на котором произошла ошибка
     */
    @Schema(description = "Путь запроса", example = "/api/v1/crypto/coins")
    private String path;
    
    /**
     * Уникальный код ошибки
     */
    @Schema(description = "Код ошибки", example = "VAL_001")
    private String errorCode;
    
    /**
     * Дополнительные детали ошибки (например, ошибки валидации полей)
     */
    @Schema(description = "Дополнительные детали ошибки")
    private Map<String, Object> details;
    
    /**
     * Список отдельных ошибок (для случаев множественных ошибок валидации)
     */
    @Schema(description = "Список отдельных ошибок")
    private List<FieldError> fieldErrors;
    
    /**
     * ID трейса для отслеживания ошибки в логах
     */
    @Schema(description = "ID трейса", example = "abc123-def456-ghi789")
    private String traceId;
    
    /**
     * Версия API
     */
    @Schema(description = "Версия API", example = "v1")
    @Builder.Default
    private String apiVersion = "v1";
    
    /**
     * Информация об ошибке поля валидации
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Ошибка валидации поля")
    public static class FieldError {
        
        /**
         * Название поля
         */
        @Schema(description = "Название поля", example = "email")
        private String field;
        
        /**
         * Отклоненное значение
         */
        @Schema(description = "Отклоненное значение", example = "invalid-email")
        private Object rejectedValue;
        
        /**
         * Сообщение об ошибке
         */
        @Schema(description = "Сообщение об ошибке", example = "Неверный формат email")
        private String message;
        
        /**
         * Код ошибки валидации
         */
        @Schema(description = "Код ошибки", example = "VAL_003")
        private String errorCode;
    }
    
    // ===== Статические методы для создания ошибок =====
    
    /**
     * Создать простой ответ об ошибке
     */
    public static ErrorResponse of(int status, String error, String message) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status)
            .error(error)
            .message(message)
            .build();
    }
    
    /**
     * Создать ответ об ошибке с кодом
     */
    public static ErrorResponse of(int status, String error, String message, ApiErrorCode errorCode) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status)
            .error(error)
            .message(message)
            .errorCode(errorCode.getCode())
            .build();
    }
    
    /**
     * Создать ответ об ошибке с деталями
     */
    public static ErrorResponse of(int status, String error, String message, Map<String, Object> details) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status)
            .error(error)
            .message(message)
            .details(details)
            .build();
    }
    
    /**
     * Создать ответ об ошибке с полным контекстом
     */
    public static ErrorResponse of(int status, String error, String message, ApiErrorCode errorCode, 
                                 String path, String traceId) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status)
            .error(error)
            .message(message)
            .errorCode(errorCode.getCode())
            .path(path)
            .traceId(traceId)
            .build();
    }
    
    /**
     * Создать ответ об ошибке валидации
     */
    public static ErrorResponse validationError(String message, List<FieldError> fieldErrors) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(400)
            .error("Validation Failed")
            .message(message)
            .errorCode(ApiErrorCode.VALIDATION_ERROR.getCode())
            .fieldErrors(fieldErrors)
            .build();
    }
    
    /**
     * Создать ответ об ошибке для исключения с кодом
     */
    public static ErrorResponse from(int status, String error, ApiErrorCode errorCode, String path) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status)
            .error(error)
            .message(errorCode.getDefaultMessage())
            .errorCode(errorCode.getCode())
            .path(path)
            .build();
    }
    
    /**
     * Добавить путь к ответу
     */
    public ErrorResponse withPath(String path) {
        this.path = path;
        return this;
    }
    
    /**
     * Добавить trace ID к ответу
     */
    public ErrorResponse withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
    
    /**
     * Добавить детали к ответу
     */
    public ErrorResponse withDetails(Map<String, Object> details) {
        this.details = details;
        return this;
    }
} 
