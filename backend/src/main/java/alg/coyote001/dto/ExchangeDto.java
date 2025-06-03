package alg.coyote001.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

/**
 * DTO for exchange operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeDto {
    
    private Long id;
    
    @NotBlank(message = "Exchange name is required")
    @Size(min = 2, max = 50, message = "Exchange name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_\\-]+$", message = "Exchange name must contain only letters, numbers, underscores, and hyphens")
    private String name;
    
    @NotBlank(message = "API URL is required")
    @Size(max = 500, message = "API URL cannot exceed 500 characters")
    @Pattern(regexp = "^https?://.*", message = "API URL must be a valid HTTP or HTTPS URL")
    private String apiUrl;
    
    @Size(max = 500, message = "WebSocket URL cannot exceed 500 characters")
    @Pattern(regexp = "^wss?://.*", message = "WebSocket URL must be a valid WS or WSS URL")
    private String websocketUrl;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 