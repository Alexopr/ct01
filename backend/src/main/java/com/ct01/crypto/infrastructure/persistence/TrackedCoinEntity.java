package com.ct01.crypto.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * JPA Entity для отслеживаемых криптовалют
 */
@Entity
@Table(name = "tracked_coin")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackedCoinEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol;

    @Column(name = "name")
    private String name;

    @ElementCollection(targetClass = Exchange.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "tracked_coin_exchanges", joinColumns = @JoinColumn(name = "tracked_coin_id"))
    @Column(name = "exchange")
    private Set<Exchange> exchanges;

    @ElementCollection
    @CollectionTable(name = "tracked_coin_quote_currencies", joinColumns = @JoinColumn(name = "tracked_coin_id"))
    @Column(name = "quote_currency")
    private Set<String> quoteCurrencies;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "websocket_enabled")
    private Boolean websocketEnabled;

    @Column(name = "polling_interval_seconds")
    private Integer pollingIntervalSeconds;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Exchange {
        BINANCE,
        BYBIT,
        OKX
    }
} 