package alg.coyote001.controller;

import alg.coyote001.dto.UserDto;
import alg.coyote001.dto.RoleDto;
import alg.coyote001.entity.User;
import alg.coyote001.service.UserService;
import alg.coyote001.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for user and role management with @PreAuthorize security
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class UserManagementController {
    
    private final UserService userService;
    private final RoleService roleService;
    
    /**
     * Get all users (requires USER_READ permission)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination");
        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get user by ID (users can view their own profile or need USER_READ permission)
     */
    @GetMapping("/{userId}")
    @PreAuthorize("@userService.isCurrentUser(#userId, authentication.name) or hasAuthority('USER_READ') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        log.debug("Fetching user by ID: {}", userId);
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Create new user (requires USER_WRITE permission)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('USER_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Creating new user: {}", userDto.getUsername());
        UserDto created = userService.createUser(userDto);
        return ResponseEntity.ok(created);
    }
    
    /**
     * Update user (users can update their own profile or need USER_WRITE permission)
     */
    @PutMapping("/{userId}")
    @PreAuthorize("@userService.isCurrentUser(#userId, authentication.name) or hasAuthority('USER_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserDto userDto) {
        log.info("Updating user: {}", userId);
        UserDto updated = userService.updateUser(userId, userDto);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Delete user (requires USER_DELETE permission, cannot delete self)
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_DELETE') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal String username) {
        
        // Prevent self-deletion
        User currentUser = userService.findByUsername(username);
        if (currentUser.getId().equals(userId)) {
            log.warn("User {} attempted to delete their own account", username);
            return ResponseEntity.badRequest().build();
        }
        
        log.info("Deleting user: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Assign role to user (requires USER_WRITE permission)
     */
    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('USER_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> assignRole(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        log.info("Assigning role {} to user {}", roleId, userId);
        UserDto updated = userService.assignRole(userId, roleId);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Remove role from user (requires USER_WRITE permission)
     */
    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('USER_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> removeRole(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);
        UserDto updated = userService.removeRole(userId, roleId);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Get all roles (requires ROLE_READ permission)
     */
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_READ') or hasRole('ADMIN')")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        log.debug("Fetching all roles");
        List<RoleDto> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
    
    /**
     * Create new role (requires ROLE_WRITE permission)
     */
    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleDto roleDto) {
        log.info("Creating new role: {}", roleDto.getName());
        RoleDto created = roleService.createRole(roleDto);
        return ResponseEntity.ok(created);
    }
    
    /**
     * Update role (requires ROLE_WRITE permission)
     */
    @PutMapping("/roles/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<RoleDto> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleDto roleDto) {
        log.info("Updating role: {}", roleId);
        RoleDto updated = roleService.updateRole(roleId, roleDto);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Delete role (requires ROLE_DELETE permission)
     */
    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_DELETE') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        log.info("Deleting role: {}", roleId);
        roleService.deleteRole(roleId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get user statistics (requires USER_READ permission)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        log.debug("Fetching user statistics");
        Map<String, Object> stats = userService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Change user password (users can change their own password)
     */
    @PostMapping("/{userId}/change-password")
    @PreAuthorize("@userService.isCurrentUser(#userId, authentication.name) or hasAuthority('USER_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> passwordData) {
        
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Current password and new password are required"));
        }
        
        boolean success = userService.changePassword(userId, currentPassword, newPassword);
        
        if (success) {
            log.info("Password changed successfully for user: {}", userId);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } else {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Current password is incorrect"));
        }
    }
    
    /**
     * Upgrade user to PREMIUM role (requires ADMIN role or special authorization for payment system)
     */
    @PutMapping("/{userId}/upgrade-to-premium")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> upgradeToPremium(@PathVariable Long userId) {
        log.info("Upgrading user {} to PREMIUM role", userId);
        
        try {
            UserDto updatedUser = userService.upgradeToPremium(userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "User successfully upgraded to PREMIUM",
                "user", updatedUser,
                "success", true
            ));
        } catch (RuntimeException e) {
            log.error("Failed to upgrade user {} to PREMIUM: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", e.getMessage(),
                    "success", false
                ));
        }
    }
} 