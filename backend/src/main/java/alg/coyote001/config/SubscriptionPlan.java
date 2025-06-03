package alg.coyote001.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Subscription plan configuration
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionPlan {
    
    private String name;
    private double price;
    private String currency;
    
    @JsonProperty("billing_cycle")
    private String billingCycle;
    
    @JsonProperty("trial_days")
    private int trialDays;
    
    @JsonProperty("refund_days")
    private int refundDays;
    
    private SubscriptionLimits limits;
    private List<String> features;
    
    // Constructors
    public SubscriptionPlan() {}
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getBillingCycle() {
        return billingCycle;
    }
    
    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }
    
    public int getTrialDays() {
        return trialDays;
    }
    
    public void setTrialDays(int trialDays) {
        this.trialDays = trialDays;
    }
    
    public int getRefundDays() {
        return refundDays;
    }
    
    public void setRefundDays(int refundDays) {
        this.refundDays = refundDays;
    }
    
    public SubscriptionLimits getLimits() {
        return limits;
    }
    
    public void setLimits(SubscriptionLimits limits) {
        this.limits = limits;
    }
    
    public List<String> getFeatures() {
        return features;
    }
    
    public void setFeatures(List<String> features) {
        this.features = features;
    }
    
    /**
     * Check if this is a free plan
     */
    public boolean isFree() {
        return price == 0.0;
    }
    
    /**
     * Check if trial is available
     */
    public boolean hasTrialPeriod() {
        return trialDays > 0;
    }
} 