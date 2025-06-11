package com.ct01.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Базовое исключение для доменных ошибок в DDD архитектуре.
 * Используется для нарушений бизнес-правил и инвариантов домена.
 */
@Getter
public class DomainException extends RuntimeException {
    
    private final HttpStatus status;
    private final String errorCode;
    private final Map<String, Object> details;
    
    public DomainException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = "DOMAIN_ERROR";
        this.details = null;
    }
    
    public DomainException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = "DOMAIN_ERROR";
        this.details = null;
    }
    
    public DomainException(String message, String errorCode) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public DomainException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public DomainException(String message, HttpStatus status, String errorCode, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.details = details;
    }
    
    /**
     * Создать исключение для агрегата не найден
     */
    public static DomainException aggregateNotFound(String aggregateName, Object id) {
        return new DomainException(
            String.format("%s with id %s not found", aggregateName, id),
            HttpStatus.NOT_FOUND,
            "AGGREGATE_NOT_FOUND"
        );
    }
    
    /**
     * Создать исключение для нарушения инварианта
     */
    public static DomainException invariantViolated(String message) {
        return new DomainException(message, HttpStatus.BAD_REQUEST, "INVARIANT_VIOLATED");
    }
    
    /**
     * Создать исключение для конфликта данных
     */
    public static DomainException conflict(String message) {
        return new DomainException(message, HttpStatus.CONFLICT, "DATA_CONFLICT");
    }
    
    /**
     * Создать исключение для недостатка прав
     */
    public static DomainException forbidden(String message) {
        return new DomainException(message, HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }
    
    /**
     * Создать исключение для невалидного состояния агрегата
     */
    public static DomainException invalidState(String aggregateName, String currentState, String operation) {
        return new DomainException(
            String.format("Cannot perform operation '%s' on %s in state '%s'", 
                operation, aggregateName, currentState),
            HttpStatus.BAD_REQUEST,
            "INVALID_STATE"
        );
    }
} 
