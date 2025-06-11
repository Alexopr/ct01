package com.ct01.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для аутентификации через Telegram
 */
@Data
@Schema(description = "Данные для аутентификации через Telegram")
public class TelegramAuthDto {
    
    @NotNull(message = "Telegram ID не может быть null")
    @Schema(description = "ID пользователя в Telegram", example = "123456789", required = true)
    private Long telegramId;
    
    @NotBlank(message = "Username не может быть пустым")
    @Schema(description = "Username в Telegram", example = "johndoe", required = true)
    private String username;
    
    @Schema(description = "Имя пользователя в Telegram", example = "John")
    private String firstName;
    
    @Schema(description = "Фамилия пользователя в Telegram", example = "Doe")
    private String lastName;
    
    @Schema(description = "Фото профиля в Telegram")
    private String photoUrl;
    
    @NotBlank(message = "Hash не может быть пустым")
    @Schema(description = "Hash для верификации данных от Telegram", required = true)
    private String hash;
    
    @NotNull(message = "Auth date не может быть null")
    @Schema(description = "Дата аутентификации в Telegram", example = "1640995200", required = true)
    private Long authDate;
} 