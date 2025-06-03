package alg.coyote001.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entity representing a cryptocurrency coin that is being tracked
 */
@Entity
@Table(name = "tracked_coins")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackedCoin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Base symbol of the coin (e.g., BTC, ETH, ADA)
     */
    @Column(name = "symbol", nullable = false, unique = true, length = 20)
    private String symbol;
    
    /**
     * Full name of the coin (e.g., Bitcoin, Ethereum, Cardano)
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * Set of exchanges to track this coin on
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "tracked_coin_exchanges",
        joinColumns = @JoinColumn(name = "coin_id")
    )
    @Column(name = "exchange")
    @Enumerated(EnumType.STRING)
    private Set<Exchange> exchanges;
    
    /**
     * Set of quote currencies to track (e.g., USDT, BTC, ETH)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "tracked_coin_quotes",
        joinColumns = @JoinColumn(name = "coin_id")
    )
    @Column(name = "quote_currency")
    private Set<String> quoteCurrencies;
    
    /**
     * Whether this coin is currently being actively tracked
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * Polling interval in seconds for this specific coin
     * If null, uses default interval
     */
    @Column(name = "polling_interval_seconds")
    private Integer pollingIntervalSeconds;
    
    /**
     * Whether to enable WebSocket real-time updates for this coin
     */
    @Column(name = "websocket_enabled", nullable = false)
    @Builder.Default
    private Boolean websocketEnabled = true;
    
    /**
     * Priority level for tracking (higher number = higher priority)
     */
    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 1;
    
    /**
     * Additional metadata/notes about this coin
     */
    @Column(name = "notes", length = 500)
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Enum for supported exchanges
     */
    public enum Exchange {
        BINANCE,
        BYBIT,
        OKX
    }
} 