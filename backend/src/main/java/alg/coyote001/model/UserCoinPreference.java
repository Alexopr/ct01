package alg.coyote001.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import alg.coyote001.entity.User;
import alg.coyote001.entity.Coin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity модель для пользовательских предпочтений отслеживания монет
 */
@Entity
@Table(name = "user_coin_preferences", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "coin_id"}),
       indexes = {
        @Index(name = "idx_user_coin_user", columnList = "user_id"),
        @Index(name = "idx_user_coin_coin", columnList = "coin_id"),
        @Index(name = "idx_user_coin_favorite", columnList = "user_id, is_favorite"),
        @Index(name = "idx_user_coin_alerts", columnList = "user_id, has_alerts")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoinPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Ссылка на пользователя
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Ссылка на монету
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_id", nullable = false)
    private Coin coin;
    
    /**
     * Помечена ли монета как избранная
     */
    @Column(nullable = false)
    private Boolean isFavorite = false;
    
    /**
     * Включены ли уведомления для этой монеты
     */
    @Column(nullable = false)
    private Boolean notificationsEnabled = true;
    
    /**
     * Есть ли активные алерты для этой монеты
     */
    @Column(nullable = false)
    private Boolean hasAlerts = false;
    
    /**
     * Порог изменения цены для уведомлений (в процентах)
     */
    @Column(precision = 8, scale = 4)
    private BigDecimal priceChangeThreshold;
    
    /**
     * Целевая цена для покупки
     */
    @Column(precision = 20, scale = 8)
    private BigDecimal targetBuyPrice;
    
    /**
     * Целевая цена для продажи
     */
    @Column(precision = 20, scale = 8)
    private BigDecimal targetSellPrice;
    
    /**
     * Количество монет в портфеле пользователя
     */
    @Column(precision = 30, scale = 8)
    private BigDecimal portfolioAmount;
    
    /**
     * Средняя цена покупки
     */
    @Column(precision = 20, scale = 8)
    private BigDecimal averageBuyPrice;
    
    /**
     * Предпочитаемые биржи для отслеживания
     */
    @ElementCollection
    @CollectionTable(
            name = "user_preferred_exchanges",
            joinColumns = @JoinColumn(name = "user_coin_preference_id")
    )
    @Column(name = "exchange_name")
    private List<String> preferredExchanges;
    
    /**
     * Настройки отображения
     */
    @Embedded
    private DisplaySettings displaySettings;
    
    /**
     * Настройки уведомлений
     */
    @Embedded
    private NotificationSettings notificationSettings;
    
    /**
     * Дата добавления в отслеживание
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Дата последнего обновления
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Дата последней проверки цены
     */
    private LocalDateTime lastPriceCheck;
    
    /**
     * Дата последнего уведомления
     */
    private LocalDateTime lastNotificationSent;
    
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
     * Встраиваемый класс для настроек отображения
     */
    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisplaySettings {
        
        /**
         * Показывать ли изменения цены
         */
        @Column(name = "show_price_change")
        private Boolean showPriceChange = true;
        
        /**
         * Показывать ли объем торгов
         */
        @Column(name = "show_volume")
        private Boolean showVolume = true;
        
        /**
         * Показывать ли рыночную капитализацию
         */
        @Column(name = "show_market_cap")
        private Boolean showMarketCap = false;
        
        /**
         * Предпочтительная валюта для отображения
         */
        @Column(name = "display_currency", length = 10)
        private String displayCurrency = "USD";
        
        /**
         * Количество знаков после запятой
         */
        @Column(name = "decimal_places")
        private Integer decimalPlaces = 4;
    }
    
    /**
     * Встраиваемый класс для настроек уведомлений
     */
    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationSettings {
        
        /**
         * Уведомления о достижении целевой цены
         */
        @Column(name = "notify_target_price")
        private Boolean notifyTargetPrice = true;
        
        /**
         * Уведомления о значительных изменениях цены
         */
        @Column(name = "notify_price_change")
        private Boolean notifyPriceChange = true;
        
        /**
         * Уведомления о высоком объеме торгов
         */
        @Column(name = "notify_volume_spike")
        private Boolean notifyVolumeSpike = false;
        
        /**
         * Уведомления по email
         */
        @Column(name = "email_notifications")
        private Boolean emailNotifications = true;
        
        /**
         * Push уведомления
         */
        @Column(name = "push_notifications")
        private Boolean pushNotifications = false;
        
        /**
         * Максимальная частота уведомлений (в минутах)
         */
        @Column(name = "notification_frequency")
        private Integer notificationFrequencyMinutes = 60;
    }
    
    /**
     * Проверить нужно ли отправить уведомление о изменении цены
     */
    public boolean shouldNotifyPriceChange(BigDecimal currentPrice, BigDecimal previousPrice) {
        if (!notificationsEnabled || notificationSettings == null || !notificationSettings.notifyPriceChange) {
            return false;
        }
        
        if (priceChangeThreshold == null || previousPrice == null || currentPrice == null) {
            return false;
        }
        
        // Проверяем частоту уведомлений
        if (lastNotificationSent != null && notificationSettings.notificationFrequencyMinutes != null) {
            LocalDateTime minTimeForNextNotification = lastNotificationSent
                    .plusMinutes(notificationSettings.notificationFrequencyMinutes);
            if (LocalDateTime.now().isBefore(minTimeForNextNotification)) {
                return false;
            }
        }
        
        // Вычисляем процентное изменение
        BigDecimal change = currentPrice.subtract(previousPrice)
                .divide(previousPrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .abs();
        
        return change.compareTo(priceChangeThreshold) >= 0;
    }
    
    /**
     * Проверить нужно ли отправить уведомление о достижении целевой цены
     */
    public boolean shouldNotifyTargetPrice(BigDecimal currentPrice) {
        if (!notificationsEnabled || notificationSettings == null || !notificationSettings.notifyTargetPrice) {
            return false;
        }
        
        if (currentPrice == null) {
            return false;
        }
        
        // Проверяем целевую цену покупки (цена упала до целевого уровня)
        if (targetBuyPrice != null && currentPrice.compareTo(targetBuyPrice) <= 0) {
            return true;
        }
        
        // Проверяем целевую цену продажи (цена выросла до целевого уровня)
        if (targetSellPrice != null && currentPrice.compareTo(targetSellPrice) >= 0) {
            return true;
        }
        
        return false;
    }
} 