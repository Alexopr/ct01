package alg.coyote001.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * System settings configuration
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemSettings {
    
    @JsonProperty("data_retention")
    private DataRetention dataRetention;
    
    private Performance performance;
    private Security security;
    
    // Constructors
    public SystemSettings() {}
    
    // Getters and Setters
    public DataRetention getDataRetention() {
        return dataRetention;
    }
    
    public void setDataRetention(DataRetention dataRetention) {
        this.dataRetention = dataRetention;
    }
    
    public Performance getPerformance() {
        return performance;
    }
    
    public void setPerformance(Performance performance) {
        this.performance = performance;
    }
    
    public Security getSecurity() {
        return security;
    }
    
    public void setSecurity(Security security) {
        this.security = security;
    }
    
    /**
     * Data retention settings
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataRetention {
        @JsonProperty("free_plan_days")
        private int freePlanDays;
        
        @JsonProperty("pro_plan_days")
        private int proPlanDays;
        
        @JsonProperty("logs_retention_days")
        private int logsRetentionDays;
        
        // Getters and Setters
        public int getFreePlanDays() {
            return freePlanDays;
        }
        
        public void setFreePlanDays(int freePlanDays) {
            this.freePlanDays = freePlanDays;
        }
        
        public int getProPlanDays() {
            return proPlanDays;
        }
        
        public void setProPlanDays(int proPlanDays) {
            this.proPlanDays = proPlanDays;
        }
        
        public int getLogsRetentionDays() {
            return logsRetentionDays;
        }
        
        public void setLogsRetentionDays(int logsRetentionDays) {
            this.logsRetentionDays = logsRetentionDays;
        }
    }
    
    /**
     * Performance settings
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Performance {
        @JsonProperty("cache_ttl_seconds")
        private int cacheTtlSeconds;
        
        @JsonProperty("batch_processing_size")
        private int batchProcessingSize;
        
        @JsonProperty("max_concurrent_requests")
        private int maxConcurrentRequests;
        
        // Getters and Setters
        public int getCacheTtlSeconds() {
            return cacheTtlSeconds;
        }
        
        public void setCacheTtlSeconds(int cacheTtlSeconds) {
            this.cacheTtlSeconds = cacheTtlSeconds;
        }
        
        public int getBatchProcessingSize() {
            return batchProcessingSize;
        }
        
        public void setBatchProcessingSize(int batchProcessingSize) {
            this.batchProcessingSize = batchProcessingSize;
        }
        
        public int getMaxConcurrentRequests() {
            return maxConcurrentRequests;
        }
        
        public void setMaxConcurrentRequests(int maxConcurrentRequests) {
            this.maxConcurrentRequests = maxConcurrentRequests;
        }
    }
    
    /**
     * Security settings
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Security {
        @JsonProperty("rate_limit_window_minutes")
        private int rateLimitWindowMinutes;
        
        @JsonProperty("max_failed_login_attempts")
        private int maxFailedLoginAttempts;
        
        @JsonProperty("session_timeout_hours")
        private int sessionTimeoutHours;
        
        // Getters and Setters
        public int getRateLimitWindowMinutes() {
            return rateLimitWindowMinutes;
        }
        
        public void setRateLimitWindowMinutes(int rateLimitWindowMinutes) {
            this.rateLimitWindowMinutes = rateLimitWindowMinutes;
        }
        
        public int getMaxFailedLoginAttempts() {
            return maxFailedLoginAttempts;
        }
        
        public void setMaxFailedLoginAttempts(int maxFailedLoginAttempts) {
            this.maxFailedLoginAttempts = maxFailedLoginAttempts;
        }
        
        public int getSessionTimeoutHours() {
            return sessionTimeoutHours;
        }
        
        public void setSessionTimeoutHours(int sessionTimeoutHours) {
            this.sessionTimeoutHours = sessionTimeoutHours;
        }
    }
} 