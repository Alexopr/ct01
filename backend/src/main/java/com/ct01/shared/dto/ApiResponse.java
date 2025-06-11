package com.ct01.shared.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Унифицированный класс для всех успешных ответов API
 * Используется во всех модулях системы CT.01 для обеспечения консистентности
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Стандартный ответ API")
public class ApiResponse<T> {
    
    /**
     * Индикатор успешности операции
     */
    @Schema(description = "Статус выполнения операции", example = "true")
    @Builder.Default
    private boolean success = true;
    
    /**
     * Данные ответа
     */
    @Schema(description = "Данные ответа")
    private T data;
    
    /**
     * Сообщение о результате операции
     */
    @Schema(description = "Сообщение о результате", example = "Операция выполнена успешно")
    private String message;
    
    /**
     * Время создания ответа
     */
    @Schema(description = "Время создания ответа", example = "2024-01-15T10:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Версия API
     */
    @Schema(description = "Версия API", example = "v1")
    @Builder.Default
    private String apiVersion = "v1";
    
    /**
     * ID трейса для отслеживания запроса
     */
    @Schema(description = "ID трейса", example = "abc123-def456-ghi789")
    private String traceId;
    
    /**
     * Метаданные ответа (пагинация, общее количество и т.д.)
     */
    @Schema(description = "Метаданные ответа")
    private Metadata metadata;
    
    /**
     * Метаданные для ответов с пагинацией и дополнительной информацией
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Метаданные ответа")
    public static class Metadata {
        
        /**
         * Общее количество элементов
         */
        @Schema(description = "Общее количество элементов", example = "100")
        private Long totalElements;
        
        /**
         * Общее количество страниц
         */
        @Schema(description = "Общее количество страниц", example = "10")
        private Integer totalPages;
        
        /**
         * Текущая страница (начиная с 0)
         */
        @Schema(description = "Номер текущей страницы", example = "0")
        private Integer currentPage;
        
        /**
         * Размер страницы
         */
        @Schema(description = "Размер страницы", example = "10")
        private Integer pageSize;
        
        /**
         * Есть ли следующая страница
         */
        @Schema(description = "Есть ли следующая страница", example = "true")
        private Boolean hasNext;
        
        /**
         * Есть ли предыдущая страница
         */
        @Schema(description = "Есть ли предыдущая страница", example = "false")
        private Boolean hasPrevious;
        
        /**
         * Дополнительная информация
         */
        @Schema(description = "Дополнительная информация")
        private Object additional;
    }
    
    // ===== Статические методы для создания ответов =====
    
    /**
     * Создать успешный ответ с данными
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message("Операция выполнена успешно")
            .build();
    }
    
    /**
     * Создать успешный ответ с данными и сообщением
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .build();
    }
    
    /**
     * Создать успешный ответ без данных
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
            .success(true)
            .message("Операция выполнена успешно")
            .build();
    }
    
    /**
     * Создать успешный ответ без данных с сообщением
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .build();
    }
    
    /**
     * Создать ответ с данными и метаданными (для пагинации)
     */
    public static <T> ApiResponse<T> success(T data, Metadata metadata) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message("Операция выполнена успешно")
            .metadata(metadata)
            .build();
    }
    
    /**
     * Создать ответ с данными, сообщением и метаданными
     */
    public static <T> ApiResponse<T> success(T data, String message, Metadata metadata) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .metadata(metadata)
            .build();
    }
    
    /**
     * Создать метаданные из Spring Page
     */
    public static Metadata fromPage(org.springframework.data.domain.Page<?> page) {
        return Metadata.builder()
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .currentPage(page.getNumber())
            .pageSize(page.getSize())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
    
    /**
     * Добавить trace ID к ответу
     */
    public ApiResponse<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
    
    /**
     * Добавить метаданные к ответу
     */
    public ApiResponse<T> withMetadata(Metadata metadata) {
        this.metadata = metadata;
        return this;
    }
    
    /**
     * Добавить версию API к ответу
     */
    public ApiResponse<T> withApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }
} 
