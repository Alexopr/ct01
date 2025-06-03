package alg.coyote001.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Main configuration class for subscription system
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionConfig {
    
    @JsonProperty("subscription_plans")
    private Map<String, SubscriptionPlan> subscriptionPlans;
    
    @JsonProperty("payment_settings")
    private PaymentSettings paymentSettings;
    
    @JsonProperty("upgrade_prompts")
    private Map<String, UpgradePrompt> upgradePrompts;
    
    @JsonProperty("notification_settings")
    private NotificationSettings notificationSettings;
    
    @JsonProperty("system_settings")
    private SystemSettings systemSettings;
    
    @JsonProperty("module_availability")
    private Map<String, ModuleAvailability> moduleAvailability;
    
    private String version;
    
    @JsonProperty("last_updated")
    private Instant lastUpdated;
    
    @JsonProperty("hot_reload_enabled")
    private boolean hotReloadEnabled;
    
    // Constructors
    public SubscriptionConfig() {}
    
    // Getters and Setters
    public Map<String, SubscriptionPlan> getSubscriptionPlans() {
        return subscriptionPlans;
    }
    
    public void setSubscriptionPlans(Map<String, SubscriptionPlan> subscriptionPlans) {
        this.subscriptionPlans = subscriptionPlans;
    }
    
    public PaymentSettings getPaymentSettings() {
        return paymentSettings;
    }
    
    public void setPaymentSettings(PaymentSettings paymentSettings) {
        this.paymentSettings = paymentSettings;
    }
    
    public Map<String, UpgradePrompt> getUpgradePrompts() {
        return upgradePrompts;
    }
    
    public void setUpgradePrompts(Map<String, UpgradePrompt> upgradePrompts) {
        this.upgradePrompts = upgradePrompts;
    }
    
    public NotificationSettings getNotificationSettings() {
        return notificationSettings;
    }
    
    public void setNotificationSettings(NotificationSettings notificationSettings) {
        this.notificationSettings = notificationSettings;
    }
    
    public SystemSettings getSystemSettings() {
        return systemSettings;
    }
    
    public void setSystemSettings(SystemSettings systemSettings) {
        this.systemSettings = systemSettings;
    }
    
    public Map<String, ModuleAvailability> getModuleAvailability() {
        return moduleAvailability;
    }
    
    public void setModuleAvailability(Map<String, ModuleAvailability> moduleAvailability) {
        this.moduleAvailability = moduleAvailability;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Instant getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public boolean isHotReloadEnabled() {
        return hotReloadEnabled;
    }
    
    public void setHotReloadEnabled(boolean hotReloadEnabled) {
        this.hotReloadEnabled = hotReloadEnabled;
    }
    
    /**
     * Get subscription plan by name (case insensitive)
     */
    public SubscriptionPlan getPlan(String planName) {
        return subscriptionPlans.get(planName.toUpperCase());
    }
    
    /**
     * Check if a module is available for a specific plan
     */
    public boolean isModuleAvailable(String moduleName, String planName) {
        ModuleAvailability module = moduleAvailability.get(moduleName);
        if (module == null) {
            return false;
        }
        
        return "FREE".equalsIgnoreCase(planName) ? module.isFree() : module.isPremium();
    }
    
    /**
     * Get upgrade prompt by key
     */
    public UpgradePrompt getUpgradePrompt(String key) {
        return upgradePrompts.get(key);
    }
} 