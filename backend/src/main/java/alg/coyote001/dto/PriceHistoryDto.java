package alg.coyote001.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for price history data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryDto {
    
    private Long id;
    
    @NotBlank(message = "Coin symbol is required")
    @Size(min = 2, max = 20, message = "Coin symbol must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Coin symbol must contain only uppercase letters and numbers")
    private String coinSymbol;
    
    @NotBlank(message = "Coin name is required")
    @Size(min = 2, max = 100, message = "Coin name must be between 2 and 100 characters")
    private String coinName;
    
    @NotBlank(message = "Exchange name is required")
    @Size(min = 2, max = 50, message = "Exchange name must be between 2 and 50 characters")
    private String exchangeName;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @PositiveOrZero(message = "Volume must be zero or positive")
    private BigDecimal volume;
    
    @NotBlank(message = "Quote currency is required")
    @Size(min = 2, max = 10, message = "Quote currency must be between 2 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Quote currency must contain only uppercase letters and numbers")
    private String quoteCurrency;
    
    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;
} 