package com.ct01.user.infrastructure.mapper;

import com.ct01.core.domain.Email;
import com.ct01.core.domain.UserId;
import com.ct01.user.domain.Password;
import com.ct01.user.domain.Role;
import com.ct01.user.domain.User;
import com.ct01.user.domain.UserStatus;
import com.ct01.user.domain.Username;
import com.ct01.user.infrastructure.entity.RoleJpaEntity;
import com.ct01.user.infrastructure.entity.UserJpaEntity;
import com.ct01.user.infrastructure.entity.UserStatusJpaEnum;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между User domain и JPA entities
 */
@Component
public class UserMapper {
    
    private final RoleMapper roleMapper;
    
    public UserMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }
    
    /**
     * Преобразует User domain объект в JPA entity
     */
    public UserJpaEntity toJpaEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity(
            user.getId().value(),
            user.getUsername().value(),
            user.getEmail().value(),
            user.getPassword().value(),
            user.getFullName(),
            toJpaEnum(user.getStatus()),
            user.getCreatedAt()
        );
        
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setLastLoginAt(user.getLastLoginAt());
        entity.setTelegramId(user.getTelegramId());
        entity.setTelegramUsername(user.getTelegramUsername());
        
        // Преобразуем роли
        Set<RoleJpaEntity> roleEntities = user.getRoles().stream()
            .map(roleMapper::toJpaEntity)
            .collect(Collectors.toSet());
        entity.setRoles(roleEntities);
        
        return entity;
    }
    
    /**
     * Преобразует JPA entity в User domain объект
     */
    public User toDomain(UserJpaEntity entity) {
        // Преобразуем роли
        Set<Role> roles = entity.getRoles().stream()
            .map(roleMapper::toDomain)
            .collect(Collectors.toSet());
        
        return new User(
            UserId.of(entity.getId()),
            Username.of(entity.getUsername()),
            Email.of(entity.getEmail()),
            Password.of(entity.getPassword()),
            entity.getFullName(),
            fromJpaEnum(entity.getStatus()),
            entity.getTelegramId(),
            entity.getTelegramUsername(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getLastLoginAt(),
            roles
        );
    }
    
    /**
     * Преобразует UserStatus domain в JPA enum
     */
    public UserStatusJpaEnum toJpaEnum(UserStatus status) {
        return switch (status) {
            case PENDING -> UserStatusJpaEnum.PENDING;
            case ACTIVE -> UserStatusJpaEnum.ACTIVE;
            case BLOCKED -> UserStatusJpaEnum.BLOCKED;
        };
    }
    
    /**
     * Преобразует JPA enum в UserStatus domain
     */
    public UserStatus fromJpaEnum(UserStatusJpaEnum status) {
        return switch (status) {
            case PENDING -> UserStatus.PENDING;
            case ACTIVE -> UserStatus.ACTIVE;
            case BLOCKED -> UserStatus.BLOCKED;
        };
    }
} 
