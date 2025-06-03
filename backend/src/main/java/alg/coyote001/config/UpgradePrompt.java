package alg.coyote001.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Upgrade prompt configuration
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpgradePrompt {
    
    private String message;
    private String action;
    
    // Constructors
    public UpgradePrompt() {}
    
    public UpgradePrompt(String message, String action) {
        this.message = message;
        this.action = action;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
} 