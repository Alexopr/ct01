package com.ct01.crypto.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO для представления результатов с пагинацией
 */
@Schema(description = "Результат запроса с поддержкой пагинации")
public record ApiPagedResult<T>(
    
    @Schema(description = "Содержимое страницы")
    List<T> content,
    
    @Schema(description = "Номер текущей страницы (начиная с 0)", example = "0")
    int pageNumber,
    
    @Schema(description = "Размер страницы", example = "20")
    int pageSize,
    
    @Schema(description = "Общее количество элементов", example = "150")
    long totalElements,
    
    @Schema(description = "Общее количество страниц", example = "8")
    int totalPages,
    
    @Schema(description = "Является ли текущая страница первой", example = "true")
    boolean isFirst,
    
    @Schema(description = "Является ли текущая страница последней", example = "false")
    boolean isLast,
    
    @Schema(description = "Есть ли следующая страница", example = "true")
    boolean hasNext,
    
    @Schema(description = "Есть ли предыдущая страница", example = "false")
    boolean hasPrevious
) {
    
    /**
     * Конструктор для создания с основными параметрами
     */
    public ApiPagedResult(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages) {
        this(
            content,
            pageNumber,
            pageSize,
            totalElements,
            totalPages,
            pageNumber == 0,
            pageNumber >= totalPages - 1,
            pageNumber < totalPages - 1,
            pageNumber > 0
        );
    }
} 
