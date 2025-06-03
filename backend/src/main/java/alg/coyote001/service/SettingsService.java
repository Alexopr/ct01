package alg.coyote001.service;

import alg.coyote001.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for application settings management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserService userService;
    
    private static final String SETTINGS_PREFIX = "settings:";
    
    /**
     * Get settings filtered by user permissions
     */
    public Map<String, Object> getSettingsForUser(User user) {
        Map<String, Object> settings = new HashMap<>();
        
        // Add settings based on user permissions
        if (user.hasPermission("TRADING_READ")) {
            settings.put("trading", getTradingSettings());
        }
        
        if (user.hasPermission("EXCHANGE_READ")) {
            settings.put("exchanges", getExchangeSettings());
        }
        
        if (user.hasPermission("NOTIFICATION_READ")) {
            settings.put("notifications", getNotificationSettings());
        }
        
        if (user.hasPermission("SYSTEM_READ") || user.hasRole("ADMIN")) {
            settings.put("system", getSystemSettings());
        }
        
        return settings;
    }
    
    /**
     * Get settings by category
     */
    public Map<String, Object> getSettingsByCategory(String category) {
        String key = SETTINGS_PREFIX + category;
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> settings = (Map<String, Object>) redisTemplate.opsForValue().get(key);
            
            if (settings != null) {
                log.debug("Retrieved settings for category: {}", category);
                return settings;
            }
        } catch (Exception e) {
            log.error("Error retrieving settings for category: {}", category, e);
        }
        
        // Return default settings if not found
        return getDefaultSettingsForCategory(category);
    }
    
    /**
     * Update settings by category
     */
    public Map<String, Object> updateSettingsByCategory(String category, Map<String, Object> settings) {
        String key = SETTINGS_PREFIX + category;
        
        try {
            redisTemplate.opsForValue().set(key, settings);
            log.info("Updated settings for category: {}", category);
            return settings;
        } catch (Exception e) {
            log.error("Error updating settings for category: {}", category, e);
            throw new RuntimeException("Failed to update settings", e);
        }
    }
    
    /**
     * Get trading settings
     */
    public Map<String, Object> getTradingSettings() {
        return getSettingsByCategory("trading");
    }
    
    /**
     * Update trading settings
     */
    public Map<String, Object> updateTradingSettings(Map<String, Object> settings) {
        return updateSettingsByCategory("trading", settings);
    }
    
    /**
     * Get exchange settings
     */
    public Map<String, Object> getExchangeSettings() {
        return getSettingsByCategory("exchanges");
    }
    
    /**
     * Update exchange settings
     */
    public Map<String, Object> updateExchangeSettings(Map<String, Object> settings) {
        return updateSettingsByCategory("exchanges", settings);
    }
    
    /**
     * Get notification settings
     */
    public Map<String, Object> getNotificationSettings() {
        return getSettingsByCategory("notifications");
    }
    
    /**
     * Update notification settings
     */
    public Map<String, Object> updateNotificationSettings(Map<String, Object> settings) {
        return updateSettingsByCategory("notifications", settings);
    }
    
    /**
     * Get system settings
     */
    public Map<String, Object> getSystemSettings() {
        return getSettingsByCategory("system");
    }
    
    /**
     * Update system settings
     */
    public Map<String, Object> updateSystemSettings(Map<String, Object> settings) {
        return updateSettingsByCategory("system", settings);
    }
    
    /**
     * Reset settings to default
     */
    public Map<String, Object> resetToDefault(String category) {
        Map<String, Object> defaultSettings = getDefaultSettingsForCategory(category);
        return updateSettingsByCategory(category, defaultSettings);
    }
    
    /**
     * Get user permissions for a category
     */
    public Map<String, Object> getUserPermissionsForCategory(User user, String category) {
        Map<String, Object> permissions = new HashMap<>();
        
        String categoryUpper = category.toUpperCase();
        permissions.put("category", category);
        permissions.put("canRead", user.hasPermission(categoryUpper + "_READ"));
        permissions.put("canWrite", user.hasPermission(categoryUpper + "_WRITE"));
        permissions.put("canDelete", user.hasPermission(categoryUpper + "_DELETE"));
        permissions.put("canAdmin", user.hasPermission(categoryUpper + "_ADMIN") || user.hasRole("ADMIN"));
        
        // Determine highest permission level
        String level = "NONE";
        if (user.hasPermission(categoryUpper + "_ADMIN") || user.hasRole("ADMIN")) {
            level = "ADMIN";
        } else if (user.hasPermission(categoryUpper + "_DELETE")) {
            level = "DELETE";
        } else if (user.hasPermission(categoryUpper + "_WRITE")) {
            level = "WRITE";
        } else if (user.hasPermission(categoryUpper + "_READ")) {
            level = "READ";
        }
        
        permissions.put("level", level);
        
        return permissions;
    }
    
    /**
     * Check if user has permission for category and action
     * Used by @PreAuthorize
     */
    public boolean hasPermissionForCategory(String username, String category, String action) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return false;
            }
            
            String permissionName = category.toUpperCase() + "_" + action.toUpperCase();
            return user.hasPermission(permissionName) || user.hasRole("ADMIN");
        } catch (Exception e) {
            log.error("Error checking permission for user: {}, category: {}, action: {}", 
                username, category, action, e);
            return false;
        }
    }
    
    /**
     * Get default settings for a category
     */
    private Map<String, Object> getDefaultSettingsForCategory(String category) {
        Map<String, Object> defaults = new HashMap<>();
        
        switch (category.toLowerCase()) {
            case "trading":
                defaults.put("autoTrade", false);
                defaults.put("riskLevel", "medium");
                defaults.put("maxPositionSize", 1000);
                defaults.put("stopLossPercentage", 5.0);
                defaults.put("takeProfitPercentage", 10.0);
                break;
                
            case "exchanges":
                defaults.put("defaultExchange", "binance");
                defaults.put("enabledExchanges", new String[]{"binance", "okx"});
                defaults.put("rateLimitEnabled", true);
                defaults.put("maxRequestsPerMinute", 100);
                break;
                
            case "notifications":
                defaults.put("emailEnabled", true);
                defaults.put("pushEnabled", false);
                defaults.put("priceAlerts", true);
                defaults.put("tradeAlerts", true);
                defaults.put("systemAlerts", true);
                break;
                
            case "system":
                defaults.put("maintenanceMode", false);
                defaults.put("logLevel", "INFO");
                defaults.put("cacheEnabled", true);
                defaults.put("sessionTimeout", 3600);
                break;
                
            default:
                defaults.put("initialized", true);
                break;
        }
        
        return defaults;
    }
} 