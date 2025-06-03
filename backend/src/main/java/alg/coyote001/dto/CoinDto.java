package alg.coyote001.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO for basic coin operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinDto {
    
    private Long id;
    
    @NotBlank(message = "Symbol is required")
    @Size(min = 2, max = 20, message = "Symbol must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Symbol must contain only uppercase letters and numbers")
    private String symbol;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 