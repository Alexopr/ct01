package com.ct01.user.infrastructure.mapper;

import com.ct01.user.domain.Permission;
import com.ct01.user.infrastructure.entity.PermissionJpaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Маппер для преобразования между Permission domain и JPA entities
 */
@Component
public class PermissionMapper {
    
    /**
     * Преобразует Permission domain объект в JPA entity
     */
    public PermissionJpaEntity toJpaEntity(Permission permission) {
        return new PermissionJpaEntity(
            permission.getId().toString(),
            permission.getName(),
            permission.getDescription(),
            permission.getCreatedAt()
        );
    }
    
    /**
     * Преобразует JPA entity в Permission domain объект
     */
    public Permission toDomain(PermissionJpaEntity entity) {
        return new Permission(
            UUID.fromString(entity.getId()),
            entity.getName(),
            entity.getDescription(),
            entity.getCreatedAt()
        );
    }
} 
