package alg.coyote001.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Notification settings configuration
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationSettings {
    
    @JsonProperty("rate_limits")
    private RateLimits rateLimits;
    
    private Map<String, Boolean> channels;
    
    // Constructors
    public NotificationSettings() {}
    
    // Getters and Setters
    public RateLimits getRateLimits() {
        return rateLimits;
    }
    
    public void setRateLimits(RateLimits rateLimits) {
        this.rateLimits = rateLimits;
    }
    
    public Map<String, Boolean> getChannels() {
        return channels;
    }
    
    public void setChannels(Map<String, Boolean> channels) {
        this.channels = channels;
    }
    
    /**
     * Check if a notification channel is enabled
     */
    public boolean isChannelEnabled(String channelName) {
        return channels != null && channels.getOrDefault(channelName, false);
    }
    
    /**
     * Rate limits for notifications
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RateLimits {
        @JsonProperty("free_daily_notifications")
        private int freeDailyNotifications;
        
        @JsonProperty("pro_daily_notifications")
        private int proDailyNotifications;
        
        @JsonProperty("email_cooldown_minutes")
        private int emailCooldownMinutes;
        
        @JsonProperty("browser_notification_cooldown_seconds")
        private int browserNotificationCooldownSeconds;
        
        // Getters and Setters
        public int getFreeDailyNotifications() {
            return freeDailyNotifications;
        }
        
        public void setFreeDailyNotifications(int freeDailyNotifications) {
            this.freeDailyNotifications = freeDailyNotifications;
        }
        
        public int getProDailyNotifications() {
            return proDailyNotifications;
        }
        
        public void setProDailyNotifications(int proDailyNotifications) {
            this.proDailyNotifications = proDailyNotifications;
        }
        
        public int getEmailCooldownMinutes() {
            return emailCooldownMinutes;
        }
        
        public void setEmailCooldownMinutes(int emailCooldownMinutes) {
            this.emailCooldownMinutes = emailCooldownMinutes;
        }
        
        public int getBrowserNotificationCooldownSeconds() {
            return browserNotificationCooldownSeconds;
        }
        
        public void setBrowserNotificationCooldownSeconds(int browserNotificationCooldownSeconds) {
            this.browserNotificationCooldownSeconds = browserNotificationCooldownSeconds;
        }
    }
} 