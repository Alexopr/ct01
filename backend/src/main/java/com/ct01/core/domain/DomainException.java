package com.ct01.core.domain;

/**
 * Базовое исключение для доменного слоя
 */
public class DomainException extends RuntimeException {
    
    private final String errorCode;
    
    public DomainException(String message) {
        super(message);
        this.errorCode = "DOMAIN_ERROR";
    }
    
    public DomainException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DOMAIN_ERROR";
    }
    
    public DomainException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
} 
