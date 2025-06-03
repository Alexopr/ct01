package alg.coyote001.service;

import alg.coyote001.dto.UserDto;
import alg.coyote001.dto.UserRegistrationDto;
import alg.coyote001.entity.User;
import alg.coyote001.entity.Role;
import alg.coyote001.repository.UserRepository;
import alg.coyote001.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for user management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Find user by username
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
    
    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Get all users with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(this::convertToDto);
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return convertToDto(user);
    }
    
    /**
     * Register new user with default USER role
     * This method is specifically for user registration and automatically assigns USER role
     */
    public UserDto registerUser(UserRegistrationDto registrationDto) {
        // Check if username or email already exists
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + registrationDto.getUsername());
        }
        
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + registrationDto.getEmail());
        }
        
        // Create user with default values
        User user = User.builder()
            .username(registrationDto.getUsername())
            .email(registrationDto.getEmail())
            .passwordHash(passwordEncoder.encode(registrationDto.getPassword()))
            .firstName(registrationDto.getFirstName())
            .lastName(registrationDto.getLastName())
            .status(User.UserStatus.ACTIVE)
            .build();
        
        // Always assign USER role (id: 1) for new registrations
        Role userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new RuntimeException("Default role 'USER' not found"));
        user.setRoles(Set.of(userRole));
        
        User saved = userRepository.save(user);
        log.info("Registered new user: {} with USER role", saved.getUsername());
        
        return convertToDto(saved);
    }
    
    /**
     * Create new user (for admin purposes with role selection)
     */
    public UserDto createUser(UserDto userDto) {
        // Check if username or email already exists
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + userDto.getUsername());
        }
        
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + userDto.getEmail());
        }
        
        User user = User.builder()
            .username(userDto.getUsername())
            .email(userDto.getEmail())
            .passwordHash(passwordEncoder.encode(userDto.getPassword()))
            .fullName(userDto.getFullName())
            .status(User.UserStatus.ACTIVE)
            .build();
        
        // Assign default role if none specified
        if (userDto.getRoleIds() == null || userDto.getRoleIds().isEmpty()) {
            Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role 'USER' not found"));
            user.setRoles(Set.of(defaultRole));
        } else {
            Set<Role> roles = userDto.getRoleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleId)))
                .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        
        User saved = userRepository.save(user);
        log.info("Created new user: {}", saved.getUsername());
        
        return convertToDto(saved);
    }
    
    /**
     * Update user
     */
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Update basic fields
        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists: " + userDto.getEmail());
            }
            user.setEmail(userDto.getEmail());
        }
        
        if (userDto.getFullName() != null) {
            user.setFullName(userDto.getFullName());
        }
        
        if (userDto.getStatus() != null) {
            user.setStatus(User.UserStatus.valueOf(userDto.getStatus()));
        }
        
        // Update roles if provided
        if (userDto.getRoleIds() != null) {
            Set<Role> roles = userDto.getRoleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleId)))
                .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        
        User saved = userRepository.save(user);
        log.info("Updated user: {}", saved.getUsername());
        
        return convertToDto(saved);
    }
    
    /**
     * Delete user
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        userRepository.delete(user);
        log.info("Deleted user: {}", user.getUsername());
    }
    
    /**
     * Assign role to user
     */
    public UserDto assignRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        
        user.getRoles().add(role);
        User saved = userRepository.save(user);
        
        log.info("Assigned role {} to user {}", role.getName(), user.getUsername());
        
        return convertToDto(saved);
    }
    
    /**
     * Remove role from user
     */
    public UserDto removeRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        
        user.getRoles().remove(role);
        User saved = userRepository.save(user);
        
        log.info("Removed role {} from user {}", role.getName(), user.getUsername());
        
        return convertToDto(saved);
    }
    
    /**
     * Change user password
     */
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return false;
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password changed for user: {}", user.getUsername());
        return true;
    }
    
    /**
     * Update last login time
     */
    public void updateLastLogin(String username) {
        User user = findByUsername(username);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(User.UserStatus.ACTIVE);
        long inactiveUsers = userRepository.countByStatus(User.UserStatus.INACTIVE);
        long blockedUsers = userRepository.countByStatus(User.UserStatus.BLOCKED);
        
        return Map.of(
            "totalUsers", totalUsers,
            "activeUsers", activeUsers,
            "inactiveUsers", inactiveUsers,
            "blockedUsers", blockedUsers,
            "recentLogins", userRepository.countUsersWithRecentLogin(LocalDateTime.now().minusDays(7))
        );
    }
    
    /**
     * Convert User entity to DTO
     */
    public UserDto convertToDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .status(user.getStatus().name())
            .roleIds(user.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet()))
            .roleNames(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()))
            .lastLoginAt(user.getLastLoginAt())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
    
    /**
     * Check if user exists
     */
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
    
    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    
    /**
     * Check if the given user ID belongs to the current authenticated user
     * Used for @PreAuthorize security checks
     */
    public boolean isCurrentUser(Long userId, String username) {
        if (userId == null || username == null) {
            return false;
        }
        
        User currentUser = findByUsername(username);
        return currentUser != null && currentUser.getId().equals(userId);
    }
    
    /**
     * Upgrade user to PREMIUM role
     * This method is idempotent - calling it multiple times will not create duplicate role assignments
     */
    public UserDto upgradeToPremium(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Find PREMIUM role
        Role premiumRole = roleRepository.findByName("PREMIUM")
            .orElseThrow(() -> new RuntimeException("PREMIUM role not found"));
        
        // Check if user already has PREMIUM role (idempotency)
        boolean hasRole = user.getRoles().stream()
            .anyMatch(role -> role.getName().equals("PREMIUM"));
        
        if (hasRole) {
            log.info("User {} already has PREMIUM role", user.getUsername());
            return convertToDto(user);
        }
        
        // Add PREMIUM role to user's existing roles
        user.getRoles().add(premiumRole);
        User saved = userRepository.save(user);
        
        log.info("Upgraded user {} to PREMIUM role", user.getUsername());
        
        return convertToDto(saved);
    }
} 