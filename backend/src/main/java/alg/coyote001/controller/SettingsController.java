package alg.coyote001.controller;

import alg.coyote001.dto.SettingsDto;
import alg.coyote001.entity.User;
import alg.coyote001.service.SettingsService;
import alg.coyote001.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for system settings management with role-based access
 */
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class SettingsController {
    
    private final SettingsService settingsService;
    private final UserService userService;
    
    /**
     * Get all settings (filtered by user permissions)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAllSettings(
            @AuthenticationPrincipal String username) {
        
        User user = userService.findByUsername(username);
        Map<String, Object> settings = settingsService.getSettingsForUser(user);
        
        return ResponseEntity.ok(settings);
    }
    
    /**
     * Get specific setting category
     */
    @GetMapping("/{category}")
    @PreAuthorize("@settingsService.hasPermissionForCategory(authentication.name, #category, 'READ')")
    public ResponseEntity<Map<String, Object>> getSettingsByCategory(
            @PathVariable String category) {
        
        log.debug("Fetching settings for category: {}", category);
        Map<String, Object> settings = settingsService.getSettingsByCategory(category);
        
        return ResponseEntity.ok(settings);
    }
    
    /**
     * Update settings
     */
    @PutMapping("/{category}")
    @PreAuthorize("@settingsService.hasPermissionForCategory(authentication.name, #category, 'WRITE')")
    public ResponseEntity<Map<String, Object>> updateSettings(
            @PathVariable String category,
            @RequestBody Map<String, Object> settings,
            @AuthenticationPrincipal String username) {
        
        log.info("Updating settings for category: {} by user: {}", category, username);
        Map<String, Object> updated = settingsService.updateSettingsByCategory(category, settings);
        
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Get trading settings
     */
    @GetMapping("/trading")
    @PreAuthorize("hasAuthority('TRADING_READ') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTradingSettings() {
        
        log.debug("Fetching trading settings");
        Map<String, Object> settings = settingsService.getTradingSettings();
        
        return ResponseEntity.ok(settings);
    }
    
    /**
     * Update trading settings
     */
    @PutMapping("/trading")
    @PreAuthorize("hasAuthority('TRADING_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateTradingSettings(
            @RequestBody Map<String, Object> settings,
            @AuthenticationPrincipal String username) {
        
        log.info("Updating trading settings by user: {}", username);
        Map<String, Object> updated = settingsService.updateTradingSettings(settings);
        
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Get exchange settings
     */
    @GetMapping("/exchanges")
    @PreAuthorize("hasAuthority('EXCHANGE_READ') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getExchangeSettings() {
        
        log.debug("Fetching exchange settings");
        Map<String, Object> settings = settingsService.getExchangeSettings();
        
        return ResponseEntity.ok(settings);
    }
    
    /**
     * Update exchange settings
     */
    @PutMapping("/exchanges")
    @PreAuthorize("hasAuthority('EXCHANGE_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateExchangeSettings(
            @RequestBody Map<String, Object> settings,
            @AuthenticationPrincipal String username) {
        
        log.info("Updating exchange settings by user: {}", username);
        Map<String, Object> updated = settingsService.updateExchangeSettings(settings);
        
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Get notification settings
     */
    @GetMapping("/notifications")
    @PreAuthorize("hasAuthority('NOTIFICATION_READ') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getNotificationSettings() {
        
        log.debug("Fetching notification settings");
        Map<String, Object> settings = settingsService.getNotificationSettings();
        
        return ResponseEntity.ok(settings);
    }
    
    /**
     * Update notification settings
     */
    @PutMapping("/notifications")
    @PreAuthorize("hasAuthority('NOTIFICATION_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateNotificationSettings(
            @RequestBody Map<String, Object> settings,
            @AuthenticationPrincipal String username) {
        
        log.info("Updating notification settings by user: {}", username);
        Map<String, Object> updated = settingsService.updateNotificationSettings(settings);
        
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Get system settings (admin only)
     */
    @GetMapping("/system")
    @PreAuthorize("hasAuthority('SYSTEM_READ') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemSettings() {
        
        log.debug("Fetching system settings");
        Map<String, Object> settings = settingsService.getSystemSettings();
        
        return ResponseEntity.ok(settings);
    }
    
    /**
     * Update system settings (admin only)
     */
    @PutMapping("/system")
    @PreAuthorize("hasAuthority('SYSTEM_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateSystemSettings(
            @RequestBody Map<String, Object> settings,
            @AuthenticationPrincipal String username) {
        
        log.info("Updating system settings by user: {}", username);
        Map<String, Object> updated = settingsService.updateSystemSettings(settings);
        
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Reset settings to default
     */
    @PostMapping("/{category}/reset")
    @PreAuthorize("@settingsService.hasPermissionForCategory(authentication.name, #category, 'WRITE')")
    public ResponseEntity<Map<String, Object>> resetSettings(
            @PathVariable String category,
            @AuthenticationPrincipal String username) {
        
        log.info("Resetting settings for category: {} by user: {}", category, username);
        Map<String, Object> defaultSettings = settingsService.resetToDefault(category);
        
        return ResponseEntity.ok(defaultSettings);
    }
    
    /**
     * Check user permissions for a category
     */
    @GetMapping("/permissions/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkPermissions(
            @PathVariable String category,
            @AuthenticationPrincipal String username) {
        
        User user = userService.findByUsername(username);
        Map<String, Object> permissions = settingsService.getUserPermissionsForCategory(user, category);
        
        return ResponseEntity.ok(permissions);
    }
} 