package com.ct01.migration.infrastructure.repository;

import com.ct01.migration.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory реализация репозитория планов миграции
 */
@Repository
@Slf4j
public class InMemoryMigrationPlanRepository implements MigrationPlanRepository {
    
    private final Map<MigrationPlanId, MigrationPlan> plans = new ConcurrentHashMap<>();
    
    @Override
    public Optional<MigrationPlan> findById(MigrationPlanId id) {
        return Optional.ofNullable(plans.get(id));
    }
    
    @Override
    public MigrationPlan save(MigrationPlan migrationPlan) {
        plans.put(migrationPlan.getId(), migrationPlan);
        log.debug("Сохранен план миграции: {}", migrationPlan.getId());
        return migrationPlan;
    }
    
    @Override
    public void delete(MigrationPlan migrationPlan) {
        plans.remove(migrationPlan.getId());
        log.debug("Удален план миграции: {}", migrationPlan.getId());
    }
    
    @Override
    public List<MigrationPlan> findByStatus(MigrationStatus status) {
        return plans.values().stream()
                   .filter(plan -> plan.getStatus() == status)
                   .collect(Collectors.toList());
    }
    
    @Override
    public List<MigrationPlan> findByStrategy(MigrationStrategy strategy) {
        return plans.values().stream()
                   .filter(plan -> plan.getStrategy() == strategy)
                   .collect(Collectors.toList());
    }
    
    @Override
    public List<MigrationPlan> findActivePlans() {
        return plans.values().stream()
                   .filter(plan -> plan.getStatus() == MigrationStatus.RUNNING || 
                                  plan.getStatus() == MigrationStatus.ROLLBACK)
                   .collect(Collectors.toList());
    }
    
    @Override
    public List<MigrationPlan> findCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return plans.values().stream()
                   .filter(plan -> plan.getCreatedAt().isAfter(startDate) && 
                                  plan.getCreatedAt().isBefore(endDate))
                   .collect(Collectors.toList());
    }
    
    @Override
    public List<MigrationPlan> findCompletedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return plans.values().stream()
                   .filter(plan -> plan.getStatus() == MigrationStatus.COMPLETED &&
                                  plan.getCompletedAt() != null &&
                                  plan.getCompletedAt().isAfter(startDate) && 
                                  plan.getCompletedAt().isBefore(endDate))
                   .collect(Collectors.toList());
    }
    
    @Override
    public List<MigrationPlan> findFailedPlansForRetry(LocalDateTime olderThan) {
        return plans.values().stream()
                   .filter(plan -> plan.getStatus() == MigrationStatus.FAILED &&
                                  plan.getCreatedAt().isBefore(olderThan))
                   .collect(Collectors.toList());
    }
    
    @Override
    public List<MigrationPlan> findByNameContaining(String namePattern) {
        String pattern = namePattern.toLowerCase();
        return plans.values().stream()
                   .filter(plan -> plan.getName().toLowerCase().contains(pattern))
                   .collect(Collectors.toList());
    }
    
    @Override
    public boolean hasActiveMigration() {
        return plans.values().stream()
                   .anyMatch(plan -> plan.getStatus() == MigrationStatus.RUNNING || 
                                    plan.getStatus() == MigrationStatus.ROLLBACK);
    }
    
    @Override
    public long countByStatus(MigrationStatus status) {
        return plans.values().stream()
                   .filter(plan -> plan.getStatus() == status)
                   .count();
    }
    
    @Override
    public Optional<MigrationPlan> findLastCompleted() {
        return plans.values().stream()
                   .filter(plan -> plan.getStatus() == MigrationStatus.COMPLETED)
                   .filter(plan -> plan.getCompletedAt() != null)
                   .max(Comparator.comparing(MigrationPlan::getCompletedAt));
    }
    
    @Override
    public List<MigrationPlan> findReadyToExecute() {
        return plans.values().stream()
                   .filter(plan -> plan.getStatus() == MigrationStatus.PLANNED)
                   .collect(Collectors.toList());
    }
    
    @Override
    public List<MigrationPlan> findAll() {
        return new ArrayList<>(plans.values());
    }
    
    @Override
    public void deleteById(MigrationPlanId id) {
        plans.remove(id);
        log.debug("Удален план миграции по ID: {}", id);
    }
    
    @Override
    public boolean existsById(MigrationPlanId id) {
        return plans.containsKey(id);
    }
    
    @Override
    public long count() {
        return plans.size();
    }
    
    /**
     * Очистить все планы (для тестирования)
     */
    public void clear() {
        plans.clear();
        log.debug("Очищены все планы миграции");
    }
    
    /**
     * Получить статистику по планам
     */
    public Map<MigrationStatus, Long> getStatusStatistics() {
        return Arrays.stream(MigrationStatus.values())
                    .collect(Collectors.toMap(
                        status -> status,
                        status -> countByStatus(status)
                    ));
    }
} 