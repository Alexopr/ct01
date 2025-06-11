package com.ct01.admin.domain;

import com.ct01.core.domain.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AdminRepository - Интерфейс репозитория для Admin Domain
 * 
 * Определяет контракт для сохранения и получения административных операций
 */
public interface AdminRepository {
    
    /**
     * Сохранить административную операцию
     */
    Admin save(Admin admin);
    
    /**
     * Найти административную операцию по ID
     */
    Optional<Admin> findById(AdminId adminId);
    
    /**
     * Найти все административные операции пользователя
     */
    List<Admin> findByUserId(UserId userId);
    
    /**
     * Найти административные операции пользователя с пагинацией
     */
    Page<Admin> findByUserId(UserId userId, Pageable pageable);
    
    /**
     * Найти все административные операции по статусу
     */
    List<Admin> findByStatus(AdminStatus status);
    
    /**
     * Найти административные операции по статусу с пагинацией
     */
    Page<Admin> findByStatus(AdminStatus status, Pageable pageable);
    
    /**
     * Найти административные операции пользователя по статусу
     */
    List<Admin> findByUserIdAndStatus(UserId userId, AdminStatus status);
    
    /**
     * Найти административные операции по типу действия
     */
    List<Admin> findByActionType(AdminActionType actionType);
    
    /**
     * Найти административные операции по типу действия с пагинацией
     */
    Page<Admin> findByActionType(AdminActionType actionType, Pageable pageable);
    
    /**
     * Найти административные операции по категории
     */
    List<Admin> findByActionCategory(String category);
    
    /**
     * Найти административные операции за период
     */
    List<Admin> findByExecutedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Найти административные операции за период с пагинацией
     */
    Page<Admin> findByExecutedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    /**
     * Найти критические административные операции
     */
    List<Admin> findCriticalOperations();
    
    /**
     * Найти критические административные операции с пагинацией
     */
    Page<Admin> findCriticalOperations(Pageable pageable);
    
    /**
     * Найти активные административные операции (PENDING, EXECUTING)
     */
    List<Admin> findActiveOperations();
    
    /**
     * Найти активные административные операции пользователя
     */
    List<Admin> findActiveOperationsByUserId(UserId userId);
    
    /**
     * Найти проваленные операции для повторной попытки
     */
    List<Admin> findFailedOperationsForRetry(LocalDateTime olderThan);
    
    /**
     * Найти операции для очистки (старые завершенные операции)
     */
    List<Admin> findOperationsForCleanup(LocalDateTime olderThan);
    
    /**
     * Подсчитать количество операций пользователя за период
     */
    long countByUserIdAndExecutedAtBetween(UserId userId, LocalDateTime start, LocalDateTime end);
    
    /**
     * Подсчитать количество операций по статусу
     */
    long countByStatus(AdminStatus status);
    
    /**
     * Подсчитать количество критических операций за период
     */
    long countCriticalOperationsBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Проверить существует ли активная операция данного типа для пользователя
     */
    boolean existsActiveOperationByUserIdAndActionType(UserId userId, AdminActionType actionType);
    
    /**
     * Найти самую недавнюю операцию пользователя
     */
    Optional<Admin> findMostRecentByUserId(UserId userId);
    
    /**
     * Найти самую недавнюю операцию пользователя по типу
     */
    Optional<Admin> findMostRecentByUserIdAndActionType(UserId userId, AdminActionType actionType);
    
    /**
     * Получить статистику операций пользователя
     */
    AdminOperationStatistics getOperationStatistics(UserId userId);
    
    /**
     * Получить общую статистику операций за период
     */
    AdminOperationStatistics getOperationStatistics(LocalDateTime start, LocalDateTime end);
    
    /**
     * Получить топ пользователей по количеству операций
     */
    List<UserOperationSummary> getTopUsersByOperationCount(LocalDateTime start, LocalDateTime end, int limit);
    
    /**
     * Получить статистику операций по типам
     */
    List<ActionTypeStatistics> getOperationStatisticsByActionType(LocalDateTime start, LocalDateTime end);
    
    /**
     * Удалить административную операцию
     */
    void delete(Admin admin);
    
    /**
     * Удалить операции старше указанной даты
     */
    void deleteOlderThan(LocalDateTime cutoffDate);
    
    /**
     * Проверить существование операции
     */
    boolean existsById(AdminId adminId);
} 
