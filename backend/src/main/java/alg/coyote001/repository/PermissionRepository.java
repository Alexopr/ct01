package alg.coyote001.repository;

import alg.coyote001.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Permission entity operations
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * Find permission by name
     */
    Optional<Permission> findByName(String name);
    
    /**
     * Check if permission name exists
     */
    boolean existsByName(String name);
    
    /**
     * Find permissions by category
     */
    List<Permission> findByCategory(String category);
    
    /**
     * Find permissions by level
     */
    List<Permission> findByLevel(Permission.PermissionLevel level);
    
    /**
     * Find active permissions
     */
    List<Permission> findByIsActiveTrue();
    
    /**
     * Find permissions by category and level
     */
    List<Permission> findByCategoryAndLevel(String category, Permission.PermissionLevel level);
} 