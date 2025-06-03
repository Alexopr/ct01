package alg.coyote001.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class for standardized error handling across the application
 */
@Slf4j
public class ErrorHandlingUtils {
    
    // ===== HTTP Error Classification =====
    
    /**
     * Check if HTTP error is retryable (5xx, 429, 503)
     */
    public static boolean isRetryableHttpError(Throwable error) {
        if (error instanceof WebClientResponseException webClientError) {
            int status = webClientError.getStatusCode().value();
            return status >= 500 || status == 429 || status == 503;
        }
        return false;
    }
    
    /**
     * Check if error is a rate limit error (429)
     */
    public static boolean isRateLimitError(Throwable error) {
        if (error instanceof WebClientResponseException webClientError) {
            return webClientError.getStatusCode().value() == 429;
        }
        
        return error.getMessage() != null && 
               error.getMessage().toLowerCase().contains("rate limit");
    }
    
    /**
     * Check if error is a temporary network issue
     */
    public static boolean isNetworkError(Throwable error) {
        if (error == null || error.getMessage() == null) {
            return false;
        }
        
        String message = error.getMessage().toLowerCase();
        return message.contains("timeout") || 
               message.contains("connection") ||
               message.contains("network") ||
               message.contains("socket");
    }
    
    /**
     * Check if error indicates external service unavailability
     */
    public static boolean isServiceUnavailableError(Throwable error) {
        if (error instanceof WebClientResponseException webClientError) {
            int status = webClientError.getStatusCode().value();
            return status == 503 || status == 502 || status == 504;
        }
        return false;
    }
    
    // ===== Error Severity Classification =====
    
    /**
     * Determine error severity level
     */
    public static ErrorSeverity getErrorSeverity(Throwable error) {
        if (isRateLimitError(error)) {
            return ErrorSeverity.WARNING;
        }
        
        if (isNetworkError(error) || isRetryableHttpError(error)) {
            return ErrorSeverity.WARNING;
        }
        
        if (error instanceof IllegalArgumentException || error instanceof BusinessException) {
            return ErrorSeverity.INFO;
        }
        
        if (error instanceof WebClientResponseException webClientError) {
            int status = webClientError.getStatusCode().value();
            if (status >= 400 && status < 500) {
                return ErrorSeverity.WARNING;
            }
            if (status >= 500) {
                return ErrorSeverity.ERROR;
            }
        }
        
        return ErrorSeverity.ERROR;
    }
    
    // ===== Safe Execution Wrappers =====
    
    /**
     * Execute operation with standardized error handling
     * @param operation operation to execute
     * @param operationName descriptive name for logging
     * @param defaultValue value to return on error
     * @return operation result or default value
     */
    public static <T> T executeWithErrorHandling(
            Supplier<T> operation, 
            String operationName, 
            T defaultValue) {
        
        try {
            return operation.get();
        } catch (Exception e) {
            logError(operationName, e);
            return defaultValue;
        }
    }
    
    /**
     * Execute operation with standardized error handling and transformation
     * @param operation operation to execute
     * @param operationName descriptive name for logging
     * @param errorMapper function to transform error to result
     * @return operation result or transformed error
     */
    public static <T> T executeWithErrorMapping(
            Supplier<T> operation,
            String operationName,
            Function<Throwable, T> errorMapper) {
        
        try {
            return operation.get();
        } catch (Exception e) {
            logError(operationName, e);
            return errorMapper.apply(e);
        }
    }
    
    /**
     * Execute void operation with standardized error handling
     * @param operation operation to execute
     * @param operationName descriptive name for logging
     * @return true if successful, false if error occurred
     */
    public static boolean executeVoidWithErrorHandling(
            Runnable operation,
            String operationName) {
        
        try {
            operation.run();
            return true;
        } catch (Exception e) {
            logError(operationName, e);
            return false;
        }
    }
    
    // ===== Error Logging =====
    
    /**
     * Log error with appropriate level based on severity
     * @param operationName descriptive name of the operation
     * @param error the error that occurred
     */
    public static void logError(String operationName, Throwable error) {
        ErrorSeverity severity = getErrorSeverity(error);
        String message = formatErrorMessage(operationName, error);
        
        switch (severity) {
            case INFO:
                log.info(message);
                break;
            case WARNING:
                log.warn(message);
                break;
            case ERROR:
            default:
                log.error(message, error);
                break;
        }
    }
    
    /**
     * Format error message in standardized way
     */
    private static String formatErrorMessage(String operationName, Throwable error) {
        String errorType = error.getClass().getSimpleName();
        String errorMessage = error.getMessage();
        
        if (error instanceof WebClientResponseException webClientError) {
            return String.format("Failed %s: HTTP %d - %s", 
                operationName, webClientError.getStatusCode().value(), errorMessage);
        }
        
        return String.format("Failed %s: %s - %s", operationName, errorType, errorMessage);
    }
    
    // ===== Business Exception Helpers =====
    
    /**
     * Create standardized business exception with error code
     */
    public static BusinessException createBusinessException(
            String message, 
            String errorCode, 
            HttpStatus httpStatus) {
        
        return new BusinessException(message, httpStatus, errorCode);
    }
    
    /**
     * Create validation error
     */
    public static BusinessException createValidationError(String field, String message) {
        return new BusinessException(
            String.format("Validation failed for field '%s': %s", field, message),
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR"
        );
    }
    
    /**
     * Create not found error
     */
    public static BusinessException createNotFoundError(String entityType, String identifier) {
        return new BusinessException(
            String.format("%s not found: %s", entityType, identifier),
            HttpStatus.NOT_FOUND,
            "NOT_FOUND"
        );
    }
    
    /**
     * Create external service error
     */
    public static BusinessException createExternalServiceError(String serviceName, Throwable cause) {
        BusinessException exception = new BusinessException(
            String.format("External service '%s' is unavailable", serviceName),
            HttpStatus.SERVICE_UNAVAILABLE,
            "EXTERNAL_SERVICE_ERROR"
        );
        exception.initCause(cause);
        return exception;
    }
    
    // ===== Error Severity Enum =====
    
    public enum ErrorSeverity {
        INFO,
        WARNING,
        ERROR
    }
} 