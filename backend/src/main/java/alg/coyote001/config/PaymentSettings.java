package alg.coyote001.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Payment settings configuration
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentSettings {
    
    @JsonProperty("accepted_currencies")
    private List<String> acceptedCurrencies;
    
    @JsonProperty("payment_provider")
    private String paymentProvider;
    
    @JsonProperty("trial_period_days")
    private int trialPeriodDays;
    
    @JsonProperty("refund_period_days")
    private int refundPeriodDays;
    
    @JsonProperty("auto_renewal")
    private boolean autoRenewal;
    
    @JsonProperty("grace_period_days")
    private int gracePeriodDays;
    
    // Constructors
    public PaymentSettings() {}
    
    // Getters and Setters
    public List<String> getAcceptedCurrencies() {
        return acceptedCurrencies;
    }
    
    public void setAcceptedCurrencies(List<String> acceptedCurrencies) {
        this.acceptedCurrencies = acceptedCurrencies;
    }
    
    public String getPaymentProvider() {
        return paymentProvider;
    }
    
    public void setPaymentProvider(String paymentProvider) {
        this.paymentProvider = paymentProvider;
    }
    
    public int getTrialPeriodDays() {
        return trialPeriodDays;
    }
    
    public void setTrialPeriodDays(int trialPeriodDays) {
        this.trialPeriodDays = trialPeriodDays;
    }
    
    public int getRefundPeriodDays() {
        return refundPeriodDays;
    }
    
    public void setRefundPeriodDays(int refundPeriodDays) {
        this.refundPeriodDays = refundPeriodDays;
    }
    
    public boolean isAutoRenewal() {
        return autoRenewal;
    }
    
    public void setAutoRenewal(boolean autoRenewal) {
        this.autoRenewal = autoRenewal;
    }
    
    public int getGracePeriodDays() {
        return gracePeriodDays;
    }
    
    public void setGracePeriodDays(int gracePeriodDays) {
        this.gracePeriodDays = gracePeriodDays;
    }
    
    /**
     * Check if a currency is accepted
     */
    public boolean isCurrencyAccepted(String currency) {
        return acceptedCurrencies != null && acceptedCurrencies.contains(currency.toUpperCase());
    }
} 