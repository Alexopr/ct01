package alg.coyote001.controller;

import alg.coyote001.model.User;
import alg.coyote001.model.Role;
import alg.coyote001.service.UserService;
import alg.coyote001.dto.UserDto;
import alg.coyote001.dto.UserMapper;
import alg.coyote001.dto.UserUpdateDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        // Простая статистика для демонстрации
        List<User> users = userService.getAllUsers();
        long totalUsers = users.size();
        long telegramUsers = users.stream()
            .filter(user -> user.getTelegramId() != null)
            .count();
        long adminUsers = users.stream()
            .filter(user -> user.getRoles().contains(alg.coyote001.model.Role.ADMIN))
            .count();
            
        return ResponseEntity.ok(new AdminStats(totalUsers, telegramUsers, adminUsers));
    }

    @PostMapping("/users/{id}/roles")
    public ResponseEntity<UserDto> updateUserRoles(@PathVariable Long id, @RequestBody Set<String> roles) {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setRoles(roles);
        return ResponseEntity.ok(UserMapper.toDto(userService.updateUser(id, dto)));
    }

    // TODO: Add more admin-specific endpoints and audit logging if needed

    public static class AdminStats {
        public long totalUsers;
        public long telegramUsers;
        public long adminUsers;
        
        public AdminStats(long totalUsers, long telegramUsers, long adminUsers) {
            this.totalUsers = totalUsers;
            this.telegramUsers = telegramUsers;
            this.adminUsers = adminUsers;
        }
    }
} 