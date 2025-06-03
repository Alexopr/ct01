package alg.coyote001.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity модель для криптовалют
 */
@Entity
@Table(name = "coins", indexes = {
        @Index(name = "idx_coin_symbol", columnList = "symbol"),
        @Index(name = "idx_coin_status", columnList = "status"),
        @Index(name = "idx_coin_symbol_status", columnList = "symbol, status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Символ монеты (BTC, ETH, SOL, etc.)
     */
    @Column(nullable = false, unique = true, length = 10)
    private String symbol;
    
    /**
     * Полное название монеты
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * Логотип или иконка (URL)
     */
    @Column(length = 500)
    private String iconUrl;
    
    /**
     * Описание монеты
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Официальный сайт
     */
    @Column(length = 500)
    private String websiteUrl;
    
    /**
     * Whitepaper URL
     */
    @Column(length = 500)
    private String whitepaperUrl;
    
    /**
     * Максимальное предложение монет
     */
    @Column(precision = 30, scale = 8)
    private BigDecimal maxSupply;
    
    /**
     * Текущее предложение монет
     */
    @Column(precision = 30, scale = 8)
    private BigDecimal circulatingSupply;
    
    /**
     * Рыночная капитализация в USD
     */
    @Column(precision = 20, scale = 2)
    private BigDecimal marketCap;
    
    /**
     * Рейтинг по рыночной капитализации
     */
    private Integer marketRank;
    
    /**
     * Статус монеты
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoinStatus status;
    
    /**
     * Приоритет отслеживания (0-10, где 10 - максимальный приоритет)
     */
    @Column(nullable = false)
    private Integer priority = 5;
    
    /**
     * Категории монеты (DeFi, NFT, Gaming, etc.)
     */
    @ElementCollection
    @CollectionTable(
            name = "coin_categories",
            joinColumns = @JoinColumn(name = "coin_id")
    )
    @Column(name = "category")
    private List<String> categories;
    
    /**
     * Дата создания записи
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Дата последнего обновления
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Дата последнего обновления данных с внешних источников
     */
    private LocalDateTime lastSyncAt;
    
    /**
     * Enum для статуса монеты
     */
    public enum CoinStatus {
        ACTIVE,     // Активная торговля
        INACTIVE,   // Временно неактивна
        DELISTED,   // Исключена с бирж
        DEPRECATED  // Устарела/заменена
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Get active status for API compatibility
     */
    public Boolean getIsActive() {
        return this.status == CoinStatus.ACTIVE;
    }
    
    /**
     * Set active status for API compatibility
     */
    public void setIsActive(Boolean isActive) {
        if (isActive != null) {
            this.status = isActive ? CoinStatus.ACTIVE : CoinStatus.INACTIVE;
        }
    }
} 