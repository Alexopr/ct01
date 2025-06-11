package com.ct01.user.infrastructure.mapper;

import com.ct01.user.domain.Permission;
import com.ct01.user.domain.Role;
import com.ct01.user.infrastructure.entity.PermissionJpaEntity;
import com.ct01.user.infrastructure.entity.RoleJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между Role domain и JPA entities
 */
@Component
public class RoleMapper {
    
    private final PermissionMapper permissionMapper;
    
    public RoleMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }
    
    /**
     * Преобразует Role domain объект в JPA entity
     */
    public RoleJpaEntity toJpaEntity(Role role) {
        RoleJpaEntity entity = new RoleJpaEntity(
            role.getId().toString(),
            role.getName(),
            role.getDescription(),
            role.getCreatedAt()
        );
        
        // Преобразуем permissions
        Set<PermissionJpaEntity> permissionEntities = role.getPermissions().stream()
            .map(permissionMapper::toJpaEntity)
            .collect(Collectors.toSet());
        entity.setPermissions(permissionEntities);
        
        return entity;
    }
    
    /**
     * Преобразует JPA entity в Role domain объект
     */
    public Role toDomain(RoleJpaEntity entity) {
        // Преобразуем permissions
        Set<Permission> permissions = entity.getPermissions().stream()
            .map(permissionMapper::toDomain)
            .collect(Collectors.toSet());
        
        return new Role(
            UUID.fromString(entity.getId()),
            entity.getName(),
            entity.getDescription(),
            entity.getCreatedAt(),
            permissions
        );
    }
} 
