package alg.coyote001.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Subscription limits configuration for different modules
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionLimits {
    
    @JsonProperty("twitter_tracker")
    private TwitterTrackerLimits twitterTracker;
    
    @JsonProperty("telegram_tracker")
    private TelegramTrackerLimits telegramTracker;
    
    @JsonProperty("market_analytics")
    private MarketAnalyticsLimits marketAnalytics;
    
    @JsonProperty("smart_money_tracking")
    private SmartMoneyTrackingLimits smartMoneyTracking;
    
    private GeneralLimits general;
    
    // Constructors
    public SubscriptionLimits() {}
    
    // Getters and Setters
    public TwitterTrackerLimits getTwitterTracker() {
        return twitterTracker;
    }
    
    public void setTwitterTracker(TwitterTrackerLimits twitterTracker) {
        this.twitterTracker = twitterTracker;
    }
    
    public TelegramTrackerLimits getTelegramTracker() {
        return telegramTracker;
    }
    
    public void setTelegramTracker(TelegramTrackerLimits telegramTracker) {
        this.telegramTracker = telegramTracker;
    }
    
    public MarketAnalyticsLimits getMarketAnalytics() {
        return marketAnalytics;
    }
    
    public void setMarketAnalytics(MarketAnalyticsLimits marketAnalytics) {
        this.marketAnalytics = marketAnalytics;
    }
    
    public SmartMoneyTrackingLimits getSmartMoneyTracking() {
        return smartMoneyTracking;
    }
    
    public void setSmartMoneyTracking(SmartMoneyTrackingLimits smartMoneyTracking) {
        this.smartMoneyTracking = smartMoneyTracking;
    }
    
    public GeneralLimits getGeneral() {
        return general;
    }
    
    public void setGeneral(GeneralLimits general) {
        this.general = general;
    }
    
    /**
     * Twitter Tracker specific limits
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TwitterTrackerLimits {
        @JsonProperty("max_accounts")
        private int maxAccounts;
        
        @JsonProperty("max_alerts_per_day")
        private int maxAlertsPerDay;
        
        @JsonProperty("historical_data_days")
        private int historicalDataDays;
        
        @JsonProperty("sentiment_analysis")
        private boolean sentimentAnalysis;
        
        private boolean categorization;
        
        @JsonProperty("real_time_notifications")
        private boolean realTimeNotifications;
        
        @JsonProperty("email_alerts")
        private boolean emailAlerts;
        
        @JsonProperty("advanced_filters")
        private boolean advancedFilters;
        
        @JsonProperty("analytics_dashboard")
        private boolean analyticsDashboard;
        
        // Getters and Setters
        public int getMaxAccounts() { return maxAccounts; }
        public void setMaxAccounts(int maxAccounts) { this.maxAccounts = maxAccounts; }
        
        public int getMaxAlertsPerDay() { return maxAlertsPerDay; }
        public void setMaxAlertsPerDay(int maxAlertsPerDay) { this.maxAlertsPerDay = maxAlertsPerDay; }
        
        public int getHistoricalDataDays() { return historicalDataDays; }
        public void setHistoricalDataDays(int historicalDataDays) { this.historicalDataDays = historicalDataDays; }
        
        public boolean isSentimentAnalysis() { return sentimentAnalysis; }
        public void setSentimentAnalysis(boolean sentimentAnalysis) { this.sentimentAnalysis = sentimentAnalysis; }
        
        public boolean isCategorization() { return categorization; }
        public void setCategorization(boolean categorization) { this.categorization = categorization; }
        
        public boolean isRealTimeNotifications() { return realTimeNotifications; }
        public void setRealTimeNotifications(boolean realTimeNotifications) { this.realTimeNotifications = realTimeNotifications; }
        
        public boolean isEmailAlerts() { return emailAlerts; }
        public void setEmailAlerts(boolean emailAlerts) { this.emailAlerts = emailAlerts; }
        
        public boolean isAdvancedFilters() { return advancedFilters; }
        public void setAdvancedFilters(boolean advancedFilters) { this.advancedFilters = advancedFilters; }
        
        public boolean isAnalyticsDashboard() { return analyticsDashboard; }
        public void setAnalyticsDashboard(boolean analyticsDashboard) { this.analyticsDashboard = analyticsDashboard; }
    }
    
    /**
     * Telegram Tracker specific limits
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelegramTrackerLimits {
        @JsonProperty("max_channels")
        private int maxChannels;
        
        @JsonProperty("max_alerts_per_day")
        private int maxAlertsPerDay;
        
        @JsonProperty("historical_data_days")
        private int historicalDataDays;
        
        @JsonProperty("ocr_enabled")
        private boolean ocrEnabled;
        
        @JsonProperty("audio_transcription")
        private boolean audioTranscription;
        
        @JsonProperty("translation_enabled")
        private boolean translationEnabled;
        
        @JsonProperty("sentiment_analysis")
        private boolean sentimentAnalysis;
        
        @JsonProperty("advanced_filters")
        private boolean advancedFilters;
        
        @JsonProperty("channel_analytics")
        private boolean channelAnalytics;
        
        // Getters and Setters
        public int getMaxChannels() { return maxChannels; }
        public void setMaxChannels(int maxChannels) { this.maxChannels = maxChannels; }
        
        public int getMaxAlertsPerDay() { return maxAlertsPerDay; }
        public void setMaxAlertsPerDay(int maxAlertsPerDay) { this.maxAlertsPerDay = maxAlertsPerDay; }
        
        public int getHistoricalDataDays() { return historicalDataDays; }
        public void setHistoricalDataDays(int historicalDataDays) { this.historicalDataDays = historicalDataDays; }
        
        public boolean isOcrEnabled() { return ocrEnabled; }
        public void setOcrEnabled(boolean ocrEnabled) { this.ocrEnabled = ocrEnabled; }
        
        public boolean isAudioTranscription() { return audioTranscription; }
        public void setAudioTranscription(boolean audioTranscription) { this.audioTranscription = audioTranscription; }
        
        public boolean isTranslationEnabled() { return translationEnabled; }
        public void setTranslationEnabled(boolean translationEnabled) { this.translationEnabled = translationEnabled; }
        
        public boolean isSentimentAnalysis() { return sentimentAnalysis; }
        public void setSentimentAnalysis(boolean sentimentAnalysis) { this.sentimentAnalysis = sentimentAnalysis; }
        
        public boolean isAdvancedFilters() { return advancedFilters; }
        public void setAdvancedFilters(boolean advancedFilters) { this.advancedFilters = advancedFilters; }
        
        public boolean isChannelAnalytics() { return channelAnalytics; }
        public void setChannelAnalytics(boolean channelAnalytics) { this.channelAnalytics = channelAnalytics; }
    }
    
    /**
     * Market Analytics specific limits
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MarketAnalyticsLimits {
        @JsonProperty("price_listing_tracking")
        private boolean priceListingTracking;
        
        @JsonProperty("volume_tracking")
        private boolean volumeTracking;
        
        @JsonProperty("liquidity_tracking")
        private boolean liquidityTracking;
        
        @JsonProperty("detection_analytics")
        private boolean detectionAnalytics;
        
        @JsonProperty("max_tracked_contracts")
        private int maxTrackedContracts;
        
        @JsonProperty("alert_frequency_minutes")
        private int alertFrequencyMinutes;
        
        @JsonProperty("historical_data_days")
        private int historicalDataDays;
        
        @JsonProperty("advanced_charts")
        private boolean advancedCharts;
        
        @JsonProperty("custom_indicators")
        private boolean customIndicators;
        
        @JsonProperty("portfolio_tracking")
        private boolean portfolioTracking;
        
        // Getters and Setters
        public boolean isPriceListingTracking() { return priceListingTracking; }
        public void setPriceListingTracking(boolean priceListingTracking) { this.priceListingTracking = priceListingTracking; }
        
        public boolean isVolumeTracking() { return volumeTracking; }
        public void setVolumeTracking(boolean volumeTracking) { this.volumeTracking = volumeTracking; }
        
        public boolean isLiquidityTracking() { return liquidityTracking; }
        public void setLiquidityTracking(boolean liquidityTracking) { this.liquidityTracking = liquidityTracking; }
        
        public boolean isDetectionAnalytics() { return detectionAnalytics; }
        public void setDetectionAnalytics(boolean detectionAnalytics) { this.detectionAnalytics = detectionAnalytics; }
        
        public int getMaxTrackedContracts() { return maxTrackedContracts; }
        public void setMaxTrackedContracts(int maxTrackedContracts) { this.maxTrackedContracts = maxTrackedContracts; }
        
        public int getAlertFrequencyMinutes() { return alertFrequencyMinutes; }
        public void setAlertFrequencyMinutes(int alertFrequencyMinutes) { this.alertFrequencyMinutes = alertFrequencyMinutes; }
        
        public int getHistoricalDataDays() { return historicalDataDays; }
        public void setHistoricalDataDays(int historicalDataDays) { this.historicalDataDays = historicalDataDays; }
        
        public boolean isAdvancedCharts() { return advancedCharts; }
        public void setAdvancedCharts(boolean advancedCharts) { this.advancedCharts = advancedCharts; }
        
        public boolean isCustomIndicators() { return customIndicators; }
        public void setCustomIndicators(boolean customIndicators) { this.customIndicators = customIndicators; }
        
        public boolean isPortfolioTracking() { return portfolioTracking; }
        public void setPortfolioTracking(boolean portfolioTracking) { this.portfolioTracking = portfolioTracking; }
    }
    
    /**
     * Smart Money Tracking specific limits
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SmartMoneyTrackingLimits {
        private boolean enabled;
        
        @JsonProperty("max_wallets")
        private int maxWallets;
        
        @JsonProperty("pattern_analysis")
        private boolean patternAnalysis;
        
        @JsonProperty("flow_analysis")
        private boolean flowAnalysis;
        
        @JsonProperty("correlation_analysis")
        private boolean correlationAnalysis;
        
        @JsonProperty("portfolio_analysis")
        private boolean portfolioAnalysis;
        
        @JsonProperty("historical_data_days")
        private int historicalDataDays;
        
        @JsonProperty("copy_trading_alerts")
        private boolean copyTradingAlerts;
        
        @JsonProperty("whale_movement_alerts")
        private boolean whaleMovementAlerts;
        
        @JsonProperty("early_detection")
        private boolean earlyDetection;
        
        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public int getMaxWallets() { return maxWallets; }
        public void setMaxWallets(int maxWallets) { this.maxWallets = maxWallets; }
        
        public boolean isPatternAnalysis() { return patternAnalysis; }
        public void setPatternAnalysis(boolean patternAnalysis) { this.patternAnalysis = patternAnalysis; }
        
        public boolean isFlowAnalysis() { return flowAnalysis; }
        public void setFlowAnalysis(boolean flowAnalysis) { this.flowAnalysis = flowAnalysis; }
        
        public boolean isCorrelationAnalysis() { return correlationAnalysis; }
        public void setCorrelationAnalysis(boolean correlationAnalysis) { this.correlationAnalysis = correlationAnalysis; }
        
        public boolean isPortfolioAnalysis() { return portfolioAnalysis; }
        public void setPortfolioAnalysis(boolean portfolioAnalysis) { this.portfolioAnalysis = portfolioAnalysis; }
        
        public int getHistoricalDataDays() { return historicalDataDays; }
        public void setHistoricalDataDays(int historicalDataDays) { this.historicalDataDays = historicalDataDays; }
        
        public boolean isCopyTradingAlerts() { return copyTradingAlerts; }
        public void setCopyTradingAlerts(boolean copyTradingAlerts) { this.copyTradingAlerts = copyTradingAlerts; }
        
        public boolean isWhaleMovementAlerts() { return whaleMovementAlerts; }
        public void setWhaleMovementAlerts(boolean whaleMovementAlerts) { this.whaleMovementAlerts = whaleMovementAlerts; }
        
        public boolean isEarlyDetection() { return earlyDetection; }
        public void setEarlyDetection(boolean earlyDetection) { this.earlyDetection = earlyDetection; }
    }
    
    /**
     * General limits that apply across all modules
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeneralLimits {
        @JsonProperty("api_requests_per_day")
        private int apiRequestsPerDay;
        
        @JsonProperty("historical_data_retention_days")
        private int historicalDataRetentionDays;
        
        @JsonProperty("concurrent_connections")
        private int concurrentConnections;
        
        @JsonProperty("export_data")
        private boolean exportData;
        
        @JsonProperty("custom_filters")
        private boolean customFilters;
        
        @JsonProperty("advanced_search")
        private boolean advancedSearch;
        
        @JsonProperty("priority_support")
        private boolean prioritySupport;
        
        @JsonProperty("ads_enabled")
        private boolean adsEnabled;
        
        // Getters and Setters
        public int getApiRequestsPerDay() { return apiRequestsPerDay; }
        public void setApiRequestsPerDay(int apiRequestsPerDay) { this.apiRequestsPerDay = apiRequestsPerDay; }
        
        public int getHistoricalDataRetentionDays() { return historicalDataRetentionDays; }
        public void setHistoricalDataRetentionDays(int historicalDataRetentionDays) { this.historicalDataRetentionDays = historicalDataRetentionDays; }
        
        public int getConcurrentConnections() { return concurrentConnections; }
        public void setConcurrentConnections(int concurrentConnections) { this.concurrentConnections = concurrentConnections; }
        
        public boolean isExportData() { return exportData; }
        public void setExportData(boolean exportData) { this.exportData = exportData; }
        
        public boolean isCustomFilters() { return customFilters; }
        public void setCustomFilters(boolean customFilters) { this.customFilters = customFilters; }
        
        public boolean isAdvancedSearch() { return advancedSearch; }
        public void setAdvancedSearch(boolean advancedSearch) { this.advancedSearch = advancedSearch; }
        
        public boolean isPrioritySupport() { return prioritySupport; }
        public void setPrioritySupport(boolean prioritySupport) { this.prioritySupport = prioritySupport; }
        
        public boolean isAdsEnabled() { return adsEnabled; }
        public void setAdsEnabled(boolean adsEnabled) { this.adsEnabled = adsEnabled; }
    }
} 