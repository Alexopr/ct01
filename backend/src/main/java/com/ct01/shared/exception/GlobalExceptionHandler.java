package com.ct01.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Унифицированный глобальный обработчик исключений для всей системы CT.01
 * Заменяет все ранее существующие обработчики ошибок и обеспечивает
 * консистентный формат ответов во всех модулях
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ===== Валидация данных =====

    /**
     * Обработка ошибок валидации (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            String errorMessage = error.getDefaultMessage();
            
            fieldErrors.add(ErrorResponse.FieldError.builder()
                .field(fieldName)
                .rejectedValue(rejectedValue)
                .message(errorMessage)
                .errorCode(ApiErrorCode.VALIDATION_FIELD_INVALID.getCode())
                .build());
        });

        ErrorResponse errorResponse = ErrorResponse.validationError(
            "Ошибка валидации данных", fieldErrors)
            .withPath(getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("Validation error on path {}: {}", getPath(request), fieldErrors);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Обработка ошибок валидации параметров
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            Object rejectedValue = violation.getInvalidValue();
            String errorMessage = violation.getMessage();
            
            fieldErrors.add(ErrorResponse.FieldError.builder()
                .field(fieldName)
                .rejectedValue(rejectedValue)
                .message(errorMessage)
                .errorCode(ApiErrorCode.VALIDATION_FIELD_INVALID.getCode())
                .build());
        }

        ErrorResponse errorResponse = ErrorResponse.validationError(
            "Ошибка валидации параметров", fieldErrors)
            .withPath(getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("Constraint violation on path {}: {}", getPath(request), fieldErrors);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Обработка ошибок привязки данных
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex, WebRequest request) {
        
        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            if (error instanceof FieldError fieldError) {
                fieldErrors.add(ErrorResponse.FieldError.builder()
                    .field(fieldError.getField())
                    .rejectedValue(fieldError.getRejectedValue())
                    .message(fieldError.getDefaultMessage())
                    .errorCode(ApiErrorCode.VALIDATION_FIELD_INVALID.getCode())
                    .build());
            }
        });

        ErrorResponse errorResponse = ErrorResponse.validationError(
            "Ошибка привязки данных", fieldErrors)
            .withPath(getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("Bind error on path {}: {}", getPath(request), fieldErrors);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    // ===== Аутентификация и авторизация =====

    /**
     * Обработка ошибок аутентификации
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication Failed",
            ApiErrorCode.AUTH_INVALID_CREDENTIALS,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("Authentication failed on path {}: {}", getPath(request), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Обработка ошибок авторизации
     */
    @ExceptionHandler({AccessDeniedException.class, java.nio.file.AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            Exception ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.FORBIDDEN.value(),
            "Access Denied",
            ApiErrorCode.AUTH_ACCESS_DENIED,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("Access denied on path {}: {}", getPath(request), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    // ===== HTTP ошибки =====

    /**
     * Обработка ошибок "ресурс не найден"
     */
    @ExceptionHandler({EntityNotFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            Exception ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ApiErrorCode.NOT_FOUND,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("Resource not found on path {}: {}", getPath(request), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Обработка ошибок неподдерживаемого HTTP метода
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            "Method Not Allowed",
            ApiErrorCode.METHOD_NOT_ALLOWED,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("Method not allowed on path {}: {} not supported", getPath(request), ex.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    /**
     * Обработка ошибок неподдерживаемого Media Type
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            "Unsupported Media Type",
            ApiErrorCode.BAD_REQUEST,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("Unsupported media type on path {}: {}", getPath(request), ex.getContentType());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }

    // ===== Обработка неверных данных =====

    /**
     * Обработка ошибок чтения HTTP сообщений
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.BAD_REQUEST.value(),
            "Malformed JSON",
            ApiErrorCode.BAD_REQUEST,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("Malformed JSON on path {}: {}", getPath(request), ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Обработка ошибок типов аргументов
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Parameter Type",
            ApiErrorCode.VALIDATION_FIELD_INVALID,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("Type mismatch on path {}: parameter '{}' with value '{}' could not be converted to {}",
            getPath(request), ex.getName(), ex.getValue(), ex.getRequiredType());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Обработка ошибок отсутствующих параметров
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(
            MissingServletRequestParameterException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.BAD_REQUEST.value(),
            "Missing Parameter",
            ApiErrorCode.VALIDATION_FIELD_REQUIRED,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("Missing parameter on path {}: {} of type {}", 
            getPath(request), ex.getParameterName(), ex.getParameterType());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    // ===== Обработка бизнес-логики =====

    /**
     * Обработка IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Argument",
            ApiErrorCode.BAD_REQUEST,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("Invalid argument on path {}: {}", getPath(request), ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Обработка IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.CONFLICT.value(),
            "Illegal State",
            ApiErrorCode.CONFLICT,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("Illegal state on path {}: {}", getPath(request), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    // ===== Обработка базы данных =====

    /**
     * Обработка ошибок нарушения ограничений БД
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.CONFLICT.value(),
            "Data Integrity Violation",
            ApiErrorCode.DATABASE_CONSTRAINT_VIOLATION,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.error("Data integrity violation on path {}: {}", getPath(request), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    // ===== Обработка внешних сервисов =====

    /**
     * Обработка ошибок внешних API
     */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponseException(
            WebClientResponseException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.BAD_GATEWAY.value(),
            "External Service Error",
            ApiErrorCode.EXTERNAL_API_ERROR,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.error("External service error on path {}: HTTP {} - {}", 
            getPath(request), ex.getStatusCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    /**
     * Обработка таймаутов
     */
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeoutException(
            TimeoutException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.REQUEST_TIMEOUT.value(),
            "Request Timeout",
            ApiErrorCode.EXTERNAL_TIMEOUT,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.error("Timeout on path {}: {}", getPath(request), ex.getMessage());
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse);
    }

    // ===== Обработка файлов =====

    /**
     * Обработка ошибок превышения размера файла
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.PAYLOAD_TOO_LARGE.value(),
            "File Size Exceeded",
            ApiErrorCode.FILE_SIZE_EXCEEDED,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("File size exceeded on path {}: max size {}", getPath(request), ex.getMaxUploadSize());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    // ===== Обработка форматов данных =====

    /**
     * Обработка ошибок парсинга дат
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParseException(
            DateTimeParseException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Date Format",
            ApiErrorCode.VALIDATION_DATE_INVALID,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.warn("Date parse error on path {}: {}", getPath(request), ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    // ===== Общая обработка =====

    /**
     * Обработка RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            ApiErrorCode.INTERNAL_ERROR,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.error("Runtime exception on path {}: ", getPath(request), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Обработка всех остальных исключений
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.from(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Unexpected Error",
            ApiErrorCode.INTERNAL_ERROR,
            getPath(request))
            .withTraceId(getTraceId(request));

        log.error("Unexpected exception on path {}: ", getPath(request), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // ===== Утилиты =====

    /**
     * Извлечение пути запроса
     */
    private String getPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.startsWith("uri=") ? description.substring(4) : description;
    }

    /**
     * Извлечение или генерация trace ID
     */
    private String getTraceId(WebRequest request) {
        // Попытка получить trace ID из headers или MDC
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null) {
            traceId = java.util.UUID.randomUUID().toString().substring(0, 8);
        }
        return traceId;
    }
} 
