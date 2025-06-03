package alg.coyote001.repository;

import alg.coyote001.entity.SubscriptionUsage;
import alg.coyote001.entity.SubscriptionUsage.ResetPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с использованием лимитов подписки
 */
@Repository
public interface SubscriptionUsageRepository extends JpaRepository<SubscriptionUsage, Long> {
    
    /**
     * Найти запись использования для конкретного пользователя, модуля и ресурса на определенную дату
     */
    @Query("SELECT su FROM SubscriptionUsage su WHERE su.user.id = :userId " +
           "AND su.moduleName = :moduleName " +
           "AND su.resourceType = :resourceType " +
           "AND su.usageDate = :usageDate")
    Optional<SubscriptionUsage> findByUserAndModuleAndResourceAndDate(
            @Param("userId") Long userId,
            @Param("moduleName") String moduleName, 
            @Param("resourceType") String resourceType,
            @Param("usageDate") LocalDate usageDate);
    
    /**
     * Найти все записи использования для пользователя на определенную дату
     */
    @Query("SELECT su FROM SubscriptionUsage su WHERE su.user.id = :userId " +
           "AND su.usageDate = :usageDate")
    List<SubscriptionUsage> findByUserAndDate(@Param("userId") Long userId, 
                                              @Param("usageDate") LocalDate usageDate);
    
    /**
     * Найти все записи использования для пользователя и модуля
     */
    @Query("SELECT su FROM SubscriptionUsage su WHERE su.user.id = :userId " +
           "AND su.moduleName = :moduleName " +
           "ORDER BY su.usageDate DESC")
    List<SubscriptionUsage> findByUserAndModule(@Param("userId") Long userId, 
                                                @Param("moduleName") String moduleName);
    
    /**
     * Найти записи, которые нужно сбросить для определенного периода
     */
    @Query("SELECT su FROM SubscriptionUsage su WHERE su.resetPeriod = :resetPeriod " +
           "AND su.usageDate < :cutoffDate")
    List<SubscriptionUsage> findUsageToReset(@Param("resetPeriod") ResetPeriod resetPeriod,
                                             @Param("cutoffDate") LocalDate cutoffDate);
    
    /**
     * Получить текущее использование для пользователя по модулю
     */
    @Query("SELECT su FROM SubscriptionUsage su WHERE su.user.id = :userId " +
           "AND su.moduleName = :moduleName " +
           "AND su.usageDate >= :fromDate " +
           "ORDER BY su.usageDate DESC, su.resourceType")
    List<SubscriptionUsage> findCurrentUsageByUserAndModule(@Param("userId") Long userId,
                                                             @Param("moduleName") String moduleName,
                                                             @Param("fromDate") LocalDate fromDate);
    
    /**
     * Получить суммарное использование ресурса за период
     */
    @Query("SELECT COALESCE(SUM(su.usedCount), 0) FROM SubscriptionUsage su " +
           "WHERE su.user.id = :userId " +
           "AND su.moduleName = :moduleName " +
           "AND su.resourceType = :resourceType " +
           "AND su.usageDate BETWEEN :startDate AND :endDate")
    Integer getTotalUsageForPeriod(@Param("userId") Long userId,
                                   @Param("moduleName") String moduleName,
                                   @Param("resourceType") String resourceType,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
    
    /**
     * Получить использование ресурса на сегодня
     */
    @Query("SELECT COALESCE(su.usedCount, 0) FROM SubscriptionUsage su " +
           "WHERE su.user.id = :userId " +
           "AND su.moduleName = :moduleName " +
           "AND su.resourceType = :resourceType " +
           "AND su.usageDate = :today")
    Integer getTodayUsage(@Param("userId") Long userId,
                          @Param("moduleName") String moduleName,
                          @Param("resourceType") String resourceType,
                          @Param("today") LocalDate today);
    
    /**
     * Увеличить счетчик использования
     */
    @Modifying
    @Query("UPDATE SubscriptionUsage su SET su.usedCount = su.usedCount + :increment, " +
           "su.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE su.user.id = :userId " +
           "AND su.moduleName = :moduleName " +
           "AND su.resourceType = :resourceType " +
           "AND su.usageDate = :usageDate")
    int incrementUsage(@Param("userId") Long userId,
                       @Param("moduleName") String moduleName,
                       @Param("resourceType") String resourceType,
                       @Param("usageDate") LocalDate usageDate,
                       @Param("increment") Integer increment);
    
    /**
     * Сбросить счетчики для определенного периода
     */
    @Modifying
    @Query("UPDATE SubscriptionUsage su SET su.usedCount = 0, " +
           "su.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE su.resetPeriod = :resetPeriod " +
           "AND su.usageDate < :cutoffDate")
    int resetUsageForPeriod(@Param("resetPeriod") ResetPeriod resetPeriod,
                            @Param("cutoffDate") LocalDate cutoffDate);
    
    /**
     * Удалить старые записи использования
     */
    @Modifying
    @Query("DELETE FROM SubscriptionUsage su WHERE su.usageDate < :cutoffDate")
    int deleteOldUsageRecords(@Param("cutoffDate") LocalDate cutoffDate);
    
    /**
     * Найти пользователей, превысивших лимит по модулю
     */
    @Query("SELECT DISTINCT su.user.id FROM SubscriptionUsage su " +
           "WHERE su.moduleName = :moduleName " +
           "AND su.usageDate = :usageDate " +
           "AND su.usedCount >= su.limitCount")
    List<Long> findUsersExceedingLimit(@Param("moduleName") String moduleName,
                                       @Param("usageDate") LocalDate usageDate);
    
    /**
     * Получить статистику использования по модулю
     */
    @Query("SELECT su.resourceType, AVG(CAST(su.usedCount AS DOUBLE) / su.limitCount * 100) " +
           "FROM SubscriptionUsage su " +
           "WHERE su.moduleName = :moduleName " +
           "AND su.usageDate BETWEEN :startDate AND :endDate " +
           "GROUP BY su.resourceType")
    List<Object[]> getUsageStatsByModule(@Param("moduleName") String moduleName,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
    
    /**
     * Проверить, превышен ли лимит для пользователя
     */
    @Query("SELECT COUNT(su) > 0 FROM SubscriptionUsage su " +
           "WHERE su.user.id = :userId " +
           "AND su.moduleName = :moduleName " +
           "AND su.resourceType = :resourceType " +
           "AND su.usageDate = :usageDate " +
           "AND su.usedCount >= su.limitCount")
    boolean isLimitExceeded(@Param("userId") Long userId,
                            @Param("moduleName") String moduleName,
                            @Param("resourceType") String resourceType,
                            @Param("usageDate") LocalDate usageDate);
    
    /**
     * Получить оставшееся количество для ресурса
     */
    @Query("SELECT CASE WHEN su.limitCount > su.usedCount " +
           "THEN su.limitCount - su.usedCount ELSE 0 END " +
           "FROM SubscriptionUsage su " +
           "WHERE su.user.id = :userId " +
           "AND su.moduleName = :moduleName " +
           "AND su.resourceType = :resourceType " +
           "AND su.usageDate = :usageDate")
    Optional<Integer> getRemainingUsage(@Param("userId") Long userId,
                                        @Param("moduleName") String moduleName,
                                        @Param("resourceType") String resourceType,
                                        @Param("usageDate") LocalDate usageDate);
} 