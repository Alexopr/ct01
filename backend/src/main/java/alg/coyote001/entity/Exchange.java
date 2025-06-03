package alg.coyote001.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity модель для криптовалютных бирж
 */
@Entity
@Table(name = "exchanges", indexes = {
        @Index(name = "idx_exchange_name", columnList = "name"),
        @Index(name = "idx_exchange_status", columnList = "status"),
        @Index(name = "idx_exchange_enabled", columnList = "enabled")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Exchange {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Название биржи (Bybit, Binance, OKX, etc.)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    /**
     * Отображаемое название биржи
     */
    @Column(nullable = false, length = 100)
    private String displayName;
    
    /**
     * Базовый URL API биржи
     */
    @Column(nullable = false, length = 500)
    private String baseApiUrl;
    
    /**
     * WebSocket URL для real-time данных
     */
    @Column(length = 500)
    private String websocketUrl;
    
    /**
     * Документация API
     */
    @Column(length = 500)
    private String apiDocumentationUrl;
    
    /**
     * API ключ (зашифрован)
     */
    @Column(length = 1000)
    private String apiKey;
    
    /**
     * Secret ключ (зашифрован)
     */
    @Column(length = 1000)
    private String apiSecret;
    
    /**
     * Passphrase для некоторых бирж (зашифрован)
     */
    @Column(length = 1000)
    private String passphrase;
    
    /**
     * Максимальное количество запросов в минуту
     */
    @Column(nullable = false)
    private Integer maxRequestsPerMinute = 60;
    
    /**
     * Максимальное количество подключений WebSocket
     */
    private Integer maxWebsocketConnections = 10;
    
    /**
     * Текущее количество используемых подключений
     */
    private Integer currentConnections = 0;
    
    /**
     * Тайм-аут для HTTP запросов (в миллисекундах)
     */
    private Integer requestTimeoutMs = 30000;
    
    /**
     * Включена ли биржа для использования
     */
    @Column(nullable = false)
    private Boolean enabled = true;
    
    /**
     * Статус биржи
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeStatus status = ExchangeStatus.ACTIVE;
    
    /**
     * Приоритет биржи (1-10, где 10 - максимальный приоритет)
     */
    @Column(nullable = false)
    private Integer priority = 5;
    
    /**
     * Поддерживаемые торговые пары
     */
    @ElementCollection
    @CollectionTable(
            name = "exchange_supported_pairs",
            joinColumns = @JoinColumn(name = "exchange_id")
    )
    @Column(name = "trading_pair")
    private java.util.List<String> supportedTradingPairs;
    
    /**
     * Конфигурация специфичная для биржи
     */
    @ElementCollection
    @CollectionTable(
            name = "exchange_config",
            joinColumns = @JoinColumn(name = "exchange_id")
    )
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value")
    private Map<String, String> configuration;
    
    /**
     * Последняя проверка health check
     */
    private LocalDateTime lastHealthCheck;
    
    /**
     * Статус последней проверки
     */
    private Boolean lastHealthStatus;
    
    /**
     * Сообщение об ошибке (если есть)
     */
    @Column(length = 1000)
    private String lastErrorMessage;
    
    /**
     * Количество ошибок подряд
     */
    private Integer consecutiveErrors = 0;
    
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
     * Enum для статуса биржи
     */
    public enum ExchangeStatus {
        ACTIVE,        // Активна и работает
        MAINTENANCE,   // На техническом обслуживании
        ERROR,         // Есть проблемы с подключением
        DISABLED       // Отключена администратором
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
     * Проверить доступность подключения
     */
    public boolean canMakeConnection() {
        return enabled && 
               status == ExchangeStatus.ACTIVE && 
               (currentConnections == null || currentConnections < maxWebsocketConnections);
    }
    
    /**
     * Увеличить счетчик соединений
     */
    public void incrementConnections() {
        if (currentConnections == null) {
            currentConnections = 0;
        }
        currentConnections++;
    }
    
    /**
     * Уменьшить счетчик соединений
     */
    public void decrementConnections() {
        if (currentConnections != null && currentConnections > 0) {
            currentConnections--;
        }
    }
    
    /**
     * Get active status for API compatibility
     */
    public Boolean getIsActive() {
        return this.enabled && this.status == ExchangeStatus.ACTIVE;
    }
    
    /**
     * Set active status for API compatibility
     */
    public void setIsActive(Boolean isActive) {
        if (isActive != null) {
            this.enabled = isActive;
            this.status = isActive ? ExchangeStatus.ACTIVE : ExchangeStatus.DISABLED;
        }
    }
    
    /**
     * Get API URL for API compatibility
     */
    public String getApiUrl() {
        return this.baseApiUrl;
    }
    
    /**
     * Set API URL for API compatibility
     */
    public void setApiUrl(String apiUrl) {
        this.baseApiUrl = apiUrl;
    }
} 