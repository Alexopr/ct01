package com.ct01.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для CSRF токена
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "CSRF токен для защиты от CSRF атак")
public class CsrfTokenDto {
    
    @Schema(description = "CSRF токен", example = "abcd1234-ef56-7890-ghij-klmnopqrstuv")
    private String token;
    
    @Schema(description = "Имя заголовка для передачи токена", example = "X-XSRF-TOKEN")
    private String headerName;
    
    @Schema(description = "Имя параметра для передачи токена", example = "_csrf")
    private String parameterName;
} 