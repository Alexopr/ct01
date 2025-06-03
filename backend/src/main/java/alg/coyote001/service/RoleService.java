package alg.coyote001.service;

import alg.coyote001.dto.RoleDto;
import alg.coyote001.entity.Role;
import alg.coyote001.entity.Permission;
import alg.coyote001.repository.RoleRepository;
import alg.coyote001.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for role management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    /**
     * Get all roles
     */
    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Get role by ID
     */
    @Transactional(readOnly = true)
    public RoleDto getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        return convertToDto(role);
    }
    
    /**
     * Create new role
     */
    public RoleDto createRole(RoleDto roleDto) {
        // Check if role name already exists
        if (roleRepository.findByName(roleDto.getName()).isPresent()) {
            throw new RuntimeException("Role name already exists: " + roleDto.getName());
        }
        
        Role role = Role.builder()
            .name(roleDto.getName())
            .description(roleDto.getDescription())
            .priority(roleDto.getPriority() != null ? roleDto.getPriority() : 1)
            .isActive(roleDto.getIsActive() != null ? roleDto.getIsActive() : true)
            .build();
        
        // Set permissions if provided
        if (roleDto.getPermissionIds() != null) {
            Set<Permission> permissions = roleDto.getPermissionIds().stream()
                .map(permissionId -> permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId)))
                .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }
        
        Role saved = roleRepository.save(role);
        log.info("Created new role: {}", saved.getName());
        
        return convertToDto(saved);
    }
    
    /**
     * Update role
     */
    public RoleDto updateRole(Long roleId, RoleDto roleDto) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        
        // Update basic fields
        if (roleDto.getName() != null && !roleDto.getName().equals(role.getName())) {
            if (roleRepository.findByName(roleDto.getName()).isPresent()) {
                throw new RuntimeException("Role name already exists: " + roleDto.getName());
            }
            role.setName(roleDto.getName());
        }
        
        if (roleDto.getDescription() != null) {
            role.setDescription(roleDto.getDescription());
        }
        
        if (roleDto.getPriority() != null) {
            role.setPriority(roleDto.getPriority());
        }
        
        if (roleDto.getIsActive() != null) {
            role.setIsActive(roleDto.getIsActive());
        }
        
        // Update permissions if provided
        if (roleDto.getPermissionIds() != null) {
            Set<Permission> permissions = roleDto.getPermissionIds().stream()
                .map(permissionId -> permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId)))
                .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }
        
        Role saved = roleRepository.save(role);
        log.info("Updated role: {}", saved.getName());
        
        return convertToDto(saved);
    }
    
    /**
     * Delete role
     */
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        
        roleRepository.delete(role);
        log.info("Deleted role: {}", role.getName());
    }
    
    /**
     * Get active roles
     */
    @Transactional(readOnly = true)
    public List<RoleDto> getActiveRoles() {
        return roleRepository.findByIsActiveTrue().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert Role entity to DTO
     */
    public RoleDto convertToDto(Role role) {
        return RoleDto.builder()
            .id(role.getId())
            .name(role.getName())
            .description(role.getDescription())
            .permissionIds(role.getPermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toSet()))
            .permissionNames(role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet()))
            .priority(role.getPriority())
            .isActive(role.getIsActive())
            .createdAt(role.getCreatedAt())
            .updatedAt(role.getUpdatedAt())
            .build();
    }
} 