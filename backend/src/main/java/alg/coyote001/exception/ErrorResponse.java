package alg.coyote001.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Стандартный ответ об ошибке для API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    /**
     * Временная метка ошибки
     */
    private LocalDateTime timestamp;
    
    /**
     * HTTP статус код
     */
    private int status;
    
    /**
     * Тип ошибки
     */
    private String error;
    
    /**
     * Сообщение об ошибке
     */
    private String message;
    
    /**
     * Дополнительные детали (например, поля валидации)
     */
    private Map<String, Object> details;
    
    /**
     * Путь запроса где произошла ошибка
     */
    private String path;
    
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
} 