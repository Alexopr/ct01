package com.ct01.user.api.dto;

import com.ct01.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для информации о пользователе в API ответах
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о пользователе")
public class ApiUserDto {
    
    @Schema(description = "ID пользователя", example = "1")
    private Long id;
    
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;
    
    @Schema(description = "Имя пользователя", example = "Иван Иванов")
    private String name;
    
    @Schema(description = "Роли пользователя", example = "[\"USER\", \"PREMIUM\"]")
    private List<String> roles;
    
    @Schema(description = "Активен ли пользователь", example = "true")
    private boolean active;
    
    @Schema(description = "Дата создания аккаунта")
    private LocalDateTime createdAt;
    
    @Schema(description = "Последний вход в систему")
    private LocalDateTime lastLoginAt;
    
    /**
     * Создает DTO из доменной модели User
     */
    public static ApiUserDto from(User user) {
        ApiUserDto dto = new ApiUserDto();
        dto.setId(user.getId().getValue());
        dto.setEmail(user.getEmail().getValue());
        dto.setName(user.getFullName());
        dto.setActive(user.getStatus().isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        
        // Преобразуем роли из domain модели
        dto.setRoles(user.getRoles().stream()
            .map(role -> role.getName())
            .toList());
        
        return dto;
    }
} 