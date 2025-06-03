package alg.coyote001.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Module availability configuration
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModuleAvailability {
    
    private boolean free;
    private boolean premium;
    private String status;
    
    // Constructors
    public ModuleAvailability() {}
    
    public ModuleAvailability(boolean free, boolean premium, String status) {
        this.free = free;
        this.premium = premium;
        this.status = status;
    }
    
    // Getters and Setters
    public boolean isFree() {
        return free;
    }
    
    public void setFree(boolean free) {
        this.free = free;
    }
    
    public boolean isPremium() {
        return premium;
    }
    
    public void setPremium(boolean premium) {
        this.premium = premium;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Check if module is available for a specific plan
     */
    public boolean isAvailableForPlan(String planName) {
        return "FREE".equalsIgnoreCase(planName) ? free : premium;
    }
    
    /**
     * Check if module is active
     */
    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }
    
    /**
     * Check if module is in development
     */
    public boolean isInDevelopment() {
        return "development".equalsIgnoreCase(status);
    }
} 