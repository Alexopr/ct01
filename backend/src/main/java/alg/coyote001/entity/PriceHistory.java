package alg.coyote001.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity модель для истории цен криптовалют
 */
@Entity
@Table(name = "price_history", indexes = {
        @Index(name = "idx_price_coin_exchange_time", columnList = "coin_id, exchange_id, timestamp"),
        @Index(name = "idx_price_coin_time", columnList = "coin_id, timestamp"),
        @Index(name = "idx_price_exchange_time", columnList = "exchange_id, timestamp"),
        @Index(name = "idx_price_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Ссылка на монету
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_id", nullable = false)
    private Coin coin;
    
    /**
     * Ссылка на биржу
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_id", nullable = false)
    private Exchange exchange;
    
    /**
     * Торговая пара (например: BTC/USDT)
     */
    @Column(nullable = false, length = 20)
    private String tradingPair;
    
    /**
     * Котируемая валюта (например: USDT, BTC)
     */
    @Column(length = 10)
    private String quoteCurrency;
    
    /**
     * Временная метка цены
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * Цена открытия (если это свеча)
     */
    @Column(precision = 20, scale = 8)
    private BigDecimal openPrice;
    
    /**
     * Максимальная цена (если это свеча)
     */
    @Column(precision = 20, scale = 8)
    private BigDecimal highPrice;
    
    /**
     * Минимальная цена (если это свеча)
     */
    @Column(precision = 20, scale = 8)
    private BigDecimal lowPrice;
    
    /**
     * Цена закрытия или текущая цена
     */
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal closePrice;
    
    /**
     * Объем торгов за период
     */
    @Column(precision = 30, scale = 8)
    private BigDecimal volume;
    
    /**
     * Объем торгов в USD
     */
    @Column(precision = 20, scale = 2)
    private BigDecimal volumeUsd;
    
    /**
     * Количество сделок за период
     */
    private Long tradeCount;
    
    /**
     * Цена покупки (bid)
     */
    @Column(precision = 20, scale = 8)
    private BigDecimal bidPrice;
    
    /**
     * Цена продажи (ask)
     */
    @Column(precision = 20, scale = 8)
    private BigDecimal askPrice;
    
    /**
     * Спред (разница между ask и bid)
     */
    @Column(precision = 20, scale = 8)
    private BigDecimal spread;
    
    /**
     * Изменение цены за 24 часа в процентах
     */
    @Column(precision = 8, scale = 4)
    private BigDecimal change24h;
    
    /**
     * Тип записи данных
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceType priceType;
    
    /**
     * Интервал времени для свечных данных
     */
    @Enumerated(EnumType.STRING)
    private TimeInterval timeInterval;
    
    /**
     * Дата создания записи
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Enum для типа ценовых данных
     */
    public enum PriceType {
        TICKER,     // Текущая цена (real-time)
        CANDLE_1M,  // Свеча 1 минута
        CANDLE_5M,  // Свеча 5 минут
        CANDLE_15M, // Свеча 15 минут
        CANDLE_1H,  // Свеча 1 час
        CANDLE_4H,  // Свеча 4 часа
        CANDLE_1D,  // Свеча 1 день
        CANDLE_1W,  // Свеча 1 неделя
        CANDLE_1MO  // Свеча 1 месяц
    }
    
    /**
     * Enum для интервалов времени
     */
    public enum TimeInterval {
        ONE_MINUTE("1m"),
        FIVE_MINUTES("5m"),
        FIFTEEN_MINUTES("15m"),
        THIRTY_MINUTES("30m"),
        ONE_HOUR("1h"),
        FOUR_HOURS("4h"),
        ONE_DAY("1d"),
        ONE_WEEK("1w"),
        ONE_MONTH("1M");
        
        private final String value;
        
        TimeInterval(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static TimeInterval fromValue(String value) {
            for (TimeInterval interval : TimeInterval.values()) {
                if (interval.value.equals(value)) {
                    return interval;
                }
            }
            throw new IllegalArgumentException("Unknown time interval: " + value);
        }
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        
        // Вычисляем спред если есть bid и ask
        if (bidPrice != null && askPrice != null) {
            this.spread = askPrice.subtract(bidPrice);
        }
    }
    
    /**
     * Проверить является ли запись актуальной (не старше 5 минут для тикеров)
     */
    public boolean isRecent() {
        if (priceType == PriceType.TICKER) {
            return timestamp.isAfter(LocalDateTime.now().minusMinutes(5));
        }
        return true; // Для свечных данных актуальность определяется по-другому
    }
    
    /**
     * Получить основную цену для отображения
     */
    public BigDecimal getDisplayPrice() {
        return closePrice != null ? closePrice : 
               (openPrice != null ? openPrice : bidPrice);
    }
    
    /**
     * Get price for API compatibility (returns closePrice)
     */
    public BigDecimal getPrice() {
        return this.closePrice;
    }
    
    /**
     * Set price for API compatibility (sets closePrice)
     */
    public void setPrice(BigDecimal price) {
        this.closePrice = price;
    }
} 