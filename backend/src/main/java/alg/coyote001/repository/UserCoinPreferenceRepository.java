package alg.coyote001.repository;

import alg.coyote001.entity.Coin;
import alg.coyote001.entity.User;
import alg.coyote001.model.UserCoinPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с пользовательскими предпочтениями отслеживания монет
 */
@Repository
public interface UserCoinPreferenceRepository extends JpaRepository<UserCoinPreference, Long> {
    
    /**
     * Найти предпочтения пользователя для конкретной монеты
     */
    Optional<UserCoinPreference> findByUserAndCoin(User user, Coin coin);
    
    /**
     * Найти все предпочтения пользователя
     */
    List<UserCoinPreference> findByUser(User user);
    
    /**
     * Найти избранные монеты пользователя
     */
    List<UserCoinPreference> findByUserAndIsFavoriteTrue(User user);
    
    /**
     * Найти монеты с включенными уведомлениями
     */
    List<UserCoinPreference> findByUserAndNotificationsEnabledTrue(User user);
    
    /**
     * Найти монеты с активными алертами
     */
    List<UserCoinPreference> findByUserAndHasAlertsTrue(User user);
    
    /**
     * Найти все предпочтения с активными алертами
     */
    List<UserCoinPreference> findByHasAlertsTrue();
    
    /**
     * Найти предпочтения, которые давно не проверялись
     */
    @Query("SELECT ucp FROM UserCoinPreference ucp WHERE ucp.hasAlerts = true AND " +
           "(ucp.lastPriceCheck IS NULL OR ucp.lastPriceCheck < :threshold)")
    List<UserCoinPreference> findPreferencesNeedingPriceCheck(@Param("threshold") LocalDateTime threshold);
    
    /**
     * Найти предпочтения с целевыми ценами
     */
    @Query("SELECT ucp FROM UserCoinPreference ucp WHERE ucp.notificationsEnabled = true AND " +
           "(ucp.targetBuyPrice IS NOT NULL OR ucp.targetSellPrice IS NOT NULL)")
    List<UserCoinPreference> findPreferencesWithTargetPrices();
    
    /**
     * Найти предпочтения с настройками портфеля
     */
    @Query("SELECT ucp FROM UserCoinPreference ucp WHERE ucp.portfolioAmount IS NOT NULL AND ucp.portfolioAmount > 0")
    List<UserCoinPreference> findPreferencesWithPortfolio();
    
    /**
     * Найти предпочтения по предпочитаемой бирже
     */
    @Query("SELECT ucp FROM UserCoinPreference ucp JOIN ucp.preferredExchanges pe WHERE pe = :exchangeName")
    List<UserCoinPreference> findByPreferredExchange(@Param("exchangeName") String exchangeName);
    
    /**
     * Проверить существование предпочтения
     */
    boolean existsByUserAndCoin(User user, Coin coin);
    
    /**
     * Подсчитать количество избранных монет пользователя
     */
    @Query("SELECT COUNT(ucp) FROM UserCoinPreference ucp WHERE ucp.user = :user AND ucp.isFavorite = true")
    Long countFavoritesByUser(@Param("user") User user);
    
    /**
     * Подсчитать количество монет с алертами у пользователя
     */
    @Query("SELECT COUNT(ucp) FROM UserCoinPreference ucp WHERE ucp.user = :user AND ucp.hasAlerts = true")
    Long countAlertsbyUser(@Param("user") User user);
    
    /**
     * Найти топ отслеживаемые монеты (по количеству пользователей)
     */
    @Query("SELECT ucp.coin, COUNT(ucp) as userCount FROM UserCoinPreference ucp " +
           "GROUP BY ucp.coin ORDER BY userCount DESC")
    List<Object[]> findMostTrackedCoins();
    
    /**
     * Найти пользователей, отслеживающих конкретную монету
     */
    @Query("SELECT ucp.user FROM UserCoinPreference ucp WHERE ucp.coin = :coin")
    List<User> findUsersTrackingCoin(@Param("coin") Coin coin);
} 