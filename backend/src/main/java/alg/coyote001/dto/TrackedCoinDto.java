package alg.coyote001.dto;

import alg.coyote001.entity.TrackedCoin;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for TrackedCoin API operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackedCoinDto {
    
    private Long id;
    
    @NotBlank(message = "Symbol is required")
    @Size(min = 2, max = 20, message = "Symbol must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Symbol must contain only uppercase letters and numbers")
    private String symbol;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotEmpty(message = "At least one exchange must be specified")
    private Set<TrackedCoin.Exchange> exchanges;
    
    @NotEmpty(message = "At least one quote currency must be specified")
    @JsonProperty("quoteCurrencies")
    private Set<@Pattern(regexp = "^[A-Z0-9]+$", message = "Quote currency must contain only uppercase letters and numbers") String> quoteCurrencies;
    
    @JsonProperty("isActive")
    @Builder.Default
    private Boolean isActive = true;
    
    @Min(value = 1, message = "Polling interval must be at least 1 second")
    @Max(value = 86400, message = "Polling interval cannot exceed 24 hours (86400 seconds)")
    private Integer pollingIntervalSeconds;
    
    @JsonProperty("websocketEnabled")
    @Builder.Default
    private Boolean websocketEnabled = true;
    
    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 10, message = "Priority cannot exceed 10")
    @Builder.Default
    private Integer priority = 1;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convert DTO to Entity
     */
    public TrackedCoin toEntity() {
        return TrackedCoin.builder()
                .id(this.id)
                .symbol(this.symbol)
                .name(this.name)
                .exchanges(this.exchanges)
                .quoteCurrencies(this.quoteCurrencies)
                .isActive(this.isActive)
                .pollingIntervalSeconds(this.pollingIntervalSeconds)
                .websocketEnabled(this.websocketEnabled)
                .priority(this.priority)
                .notes(this.notes)
                .build();
    }
    
    /**
     * Convert Entity to DTO
     */
    public static TrackedCoinDto fromEntity(TrackedCoin entity) {
        return TrackedCoinDto.builder()
                .id(entity.getId())
                .symbol(entity.getSymbol())
                .name(entity.getName())
                .exchanges(entity.getExchanges())
                .quoteCurrencies(entity.getQuoteCurrencies())
                .isActive(entity.getIsActive())
                .pollingIntervalSeconds(entity.getPollingIntervalSeconds())
                .websocketEnabled(entity.getWebsocketEnabled())
                .priority(entity.getPriority())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
} 