package com.ct01.user.infrastructure.repository;

import com.ct01.core.domain.Email;
import com.ct01.core.domain.UserId;
import com.ct01.user.domain.User;
import com.ct01.user.domain.UserRepository;
import com.ct01.user.domain.UserStatus;
import com.ct01.user.domain.Username;
import com.ct01.user.infrastructure.entity.UserJpaEntity;
import com.ct01.user.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация UserRepository через JPA
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final UserJpaRepository jpaRepository;
    private final UserMapper userMapper;
    
    public UserRepositoryImpl(UserJpaRepository jpaRepository, UserMapper userMapper) {
        this.jpaRepository = jpaRepository;
        this.userMapper = userMapper;
    }
    
    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findByIdWithRoles(id.value())
            .map(userMapper::toDomain);
    }
    
    @Override
    public Optional<User> findByUsername(Username username) {
        return jpaRepository.findByUsernameWithRoles(username.value())
            .map(userMapper::toDomain);
    }
    
    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.value())
            .map(userMapper::toDomain);
    }
    
    @Override
    public Optional<User> findByTelegramId(Long telegramId) {
        return jpaRepository.findByTelegramId(telegramId)
            .map(userMapper::toDomain);
    }
    
    @Override
    public boolean existsByUsername(Username username) {
        return jpaRepository.existsByUsername(username.value());
    }
    
    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }
    
    @Override
    public boolean existsByTelegramId(Long telegramId) {
        return jpaRepository.existsByTelegramId(telegramId);
    }
    
    @Override
    public List<User> findByStatus(UserStatus status) {
        return jpaRepository.findByStatus(userMapper.toJpaEnum(status))
            .stream()
            .map(userMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByCreatedAtBetween(startDate, endDate)
            .stream()
            .map(userMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<User> findInactiveUsers(LocalDateTime threshold) {
        return jpaRepository.findInactiveUsers(threshold)
            .stream()
            .map(userMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public User save(User user) {
        UserJpaEntity entity = userMapper.toJpaEntity(user);
        UserJpaEntity savedEntity = jpaRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }
    
    @Override
    public void delete(User user) {
        jpaRepository.deleteById(user.getId().value());
    }
    
    @Override
    public void deleteById(UserId id) {
        jpaRepository.deleteById(id.value());
    }
    
    @Override
    public List<User> findAll() {
        return jpaRepository.findAll()
            .stream()
            .map(userMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
    
    @Override
    public boolean existsById(UserId id) {
        return jpaRepository.existsById(id.value());
    }
} 
