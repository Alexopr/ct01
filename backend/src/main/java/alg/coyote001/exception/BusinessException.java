package alg.coyote001.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Исключение для бизнес-логики приложения
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final HttpStatus status;
    private final String error;
    private final Map<String, Object> details;
    
    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.error = "Business Logic Error";
        this.details = null;
    }
    
    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.error = "Business Logic Error";
        this.details = null;
    }
    
    public BusinessException(String message, String error) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.error = error;
        this.details = null;
    }
    
    public BusinessException(String message, HttpStatus status, String error) {
        super(message);
        this.status = status;
        this.error = error;
        this.details = null;
    }
    
    public BusinessException(String message, HttpStatus status, String error, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.error = error;
        this.details = details;
    }
    
    /**
     * Создать исключение для "сущность не найдена"
     */
    public static BusinessException notFound(String entityName, Object id) {
        return new BusinessException(
            String.format("%s with id %s not found", entityName, id),
            HttpStatus.NOT_FOUND,
            "Entity Not Found"
        );
    }
    
    /**
     * Создать исключение для конфликта данных
     */
    public static BusinessException conflict(String message) {
        return new BusinessException(message, HttpStatus.CONFLICT, "Data Conflict");
    }
    
    /**
     * Создать исключение для недостатка прав
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(message, HttpStatus.FORBIDDEN, "Access Denied");
    }
} 