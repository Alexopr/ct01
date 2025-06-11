package com.ct01.user.application.dto;

import com.ct01.user.domain.User;

import java.time.LocalDateTime;

/**
 * Результаты операций аутентификации
 */
public class AuthResult {
    
    /**
     * Результат операции входа в систему
     */
    public record LoginResult(
            boolean success,
            String message,
            String errorCode,
            User user,
            String token,
            LocalDateTime expiresAt,
            String sessionId
    ) {
        public static LoginResult success(User user, String token, LocalDateTime expiresAt, String sessionId) {
            return new LoginResult(true, "Успешная аутентификация", null, user, token, expiresAt, sessionId);
        }
        
        public static LoginResult failure(String message, String errorCode) {
            return new LoginResult(false, message, errorCode, null, null, null, null);
        }
    }
    
    /**
     * Результат операции регистрации
     */
    public record RegistrationResult(
            boolean success,
            String message,
            String errorCode,
            User user,
            String token,
            LocalDateTime expiresAt,
            String sessionId
    ) {
        public static RegistrationResult success(User user, String token, LocalDateTime expiresAt, String sessionId) {
            return new RegistrationResult(true, "Пользователь успешно зарегистрирован", null, user, token, expiresAt, sessionId);
        }
        
        public static RegistrationResult failure(String message, String errorCode) {
            return new RegistrationResult(false, message, errorCode, null, null, null, null);
        }
    }
    
    /**
     * Результат Telegram аутентификации
     */
    public record TelegramAuthResult(
            boolean success,
            String message,
            String errorCode,
            User user,
            String token,
            LocalDateTime expiresAt,
            String sessionId
    ) {
        public static TelegramAuthResult success(User user, String token, LocalDateTime expiresAt, String sessionId) {
            return new TelegramAuthResult(true, "Telegram аутентификация успешна", null, user, token, expiresAt, sessionId);
        }
        
        public static TelegramAuthResult failure(String message, String errorCode) {
            return new TelegramAuthResult(false, message, errorCode, null, null, null, null);
        }
    }
} 