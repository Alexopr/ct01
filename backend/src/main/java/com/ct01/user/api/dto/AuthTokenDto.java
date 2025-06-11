package com.ct01.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для токена аутентификации
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Токен аутентификации и информация о сессии")
public class AuthTokenDto {
    
    @Schema(description = "Токен доступа", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "Тип токена", example = "Bearer")
    private String tokenType = "Bearer";
    
    @Schema(description = "Время истечения токена")
    private LocalDateTime expiresAt;
    
    @Schema(description = "Информация о пользователе")
    private ApiUserDto user;
    
    @Schema(description = "ID сессии", example = "sess_1234567890")
    private String sessionId;
    
    public AuthTokenDto(String accessToken, LocalDateTime expiresAt, ApiUserDto user) {
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
        this.user = user;
    }
    
    /**
     * Создает DTO из результата аутентификации
     */
    public static AuthTokenDto from(com.ct01.user.application.dto.AuthResult.LoginResult result) {
        return new AuthTokenDto(
            result.token(),
            result.expiresAt(),
            ApiUserDto.from(result.user()),
            result.sessionId()
        );
    }
    
    /**
     * Создает DTO из результата регистрации
     */
    public static AuthTokenDto from(com.ct01.user.application.dto.AuthResult.RegistrationResult result) {
        return new AuthTokenDto(
            result.token(),
            result.expiresAt(),
            ApiUserDto.from(result.user()),
            result.sessionId()
        );
    }
    
    /**
     * Создает DTO из результата Telegram аутентификации
     */
    public static AuthTokenDto from(com.ct01.user.application.dto.AuthResult.TelegramAuthResult result) {
        return new AuthTokenDto(
            result.token(),
            result.expiresAt(),
            ApiUserDto.from(result.user()),
            result.sessionId()
        );
    }
} 