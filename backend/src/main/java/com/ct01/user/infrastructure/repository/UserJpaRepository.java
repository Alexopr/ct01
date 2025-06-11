package com.ct01.user.infrastructure.repository;

import com.ct01.user.infrastructure.entity.UserJpaEntity;
import com.ct01.user.infrastructure.entity.UserStatusJpaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository для User
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, String> {
    
    Optional<UserJpaEntity> findByUsername(String username);
    
    Optional<UserJpaEntity> findByEmail(String email);
    
    Optional<UserJpaEntity> findByTelegramId(Long telegramId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByTelegramId(Long telegramId);
    
    List<UserJpaEntity> findByStatus(UserStatusJpaEnum status);
    
    @Query("SELECT u FROM UserJpaEntity u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<UserJpaEntity> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT u FROM UserJpaEntity u WHERE u.lastLoginAt < :threshold")
    List<UserJpaEntity> findInactiveUsers(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT u FROM UserJpaEntity u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<UserJpaEntity> findByIdWithRoles(@Param("id") String id);
    
    @Query("SELECT u FROM UserJpaEntity u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<UserJpaEntity> findByUsernameWithRoles(@Param("username") String username);
} 
