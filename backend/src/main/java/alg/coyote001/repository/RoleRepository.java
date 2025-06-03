package alg.coyote001.repository;

import alg.coyote001.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Role entity operations
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find role by name
     */
    Optional<Role> findByName(String name);
    
    /**
     * Check if role name exists
     */
    boolean existsByName(String name);
    
    /**
     * Find active roles
     */
    List<Role> findByIsActiveTrue();
    
    /**
     * Find roles by priority range
     */
    List<Role> findByPriorityBetween(Integer minPriority, Integer maxPriority);
    
    /**
     * Find roles with specific permission
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);
    
    /**
     * Find roles ordered by priority
     */
    List<Role> findAllByOrderByPriorityDesc();
} 