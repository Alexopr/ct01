package alg.coyote001.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

/**
 * Production-ready DTO for Redis caching
 * - No sensitive data (passwords, etc.)
 * - ISO string dates for compatibility
 * - Versioning for future migrations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCacheDto {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    @JsonProperty("created_at")
    private String createdAt; // ISO string format
    @JsonProperty("updated_at")
    private String updatedAt; // ISO string format
    private String version = "1.0"; // For cache migrations
} 