package alg.coyote001.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for Settings operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsDto {
    
    @NotBlank(message = "Settings category is required")
    @Size(min = 2, max = 50, message = "Settings category must be between 2 and 50 characters")
    private String category;
    
    @NotNull(message = "Settings map cannot be null")
    private Map<String, Object> settings;
    
    @Size(max = 100, message = "Updated by field cannot exceed 100 characters")
    private String updatedBy;
    
    private LocalDateTime updatedAt;
    
    @Size(max = 200, message = "Permissions field cannot exceed 200 characters")
    private String permissions; // User's permission level for this category
} 