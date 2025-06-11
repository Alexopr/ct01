package com.ct01.migration.domain;

import com.ct01.core.domain.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для планов миграции
 */
public interface MigrationPlanRepository extends Repository<MigrationPlan, MigrationPlanId> {
    
    /**
     * Найти план миграции по ID
     */
    Optional<MigrationPlan> findById(MigrationPlanId id);
    
    /**
     * Сохранить план миграции
     */
    MigrationPlan save(MigrationPlan migrationPlan);
    
    /**
     * Удалить план миграции
     */
    void delete(MigrationPlan migrationPlan);
    
    /**
     * Найти планы миграции по статусу
     */
    List<MigrationPlan> findByStatus(MigrationStatus status);
    
    /**
     * Найти планы миграции по стратегии
     */
    List<MigrationPlan> findByStrategy(MigrationStrategy strategy);
    
    /**
     * Найти активные планы миграции (RUNNING, ROLLBACK)
     */
    List<MigrationPlan> findActivePlans();
    
    /**
     * Найти планы миграции, созданные в период
     */
    List<MigrationPlan> findCreatedBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Найти планы миграции, завершенные в период
     */
    List<MigrationPlan> findCompletedBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Найти неудачные планы миграции для повторного выполнения
     */
    List<MigrationPlan> findFailedPlansForRetry(LocalDateTime olderThan);
    
    /**
     * Найти планы миграции по названию (частичное совпадение)
     */
    List<MigrationPlan> findByNameContaining(String namePattern);
    
    /**
     * Проверить существование активного плана миграции
     */
    boolean hasActiveMigration();
    
    /**
     * Подсчитать планы миграции по статусу
     */
    long countByStatus(MigrationStatus status);
    
    /**
     * Найти последний выполненный план миграции
     */
    Optional<MigrationPlan> findLastCompleted();
    
    /**
     * Найти планы миграции, готовые к выполнению
     */
    List<MigrationPlan> findReadyToExecute();
} 