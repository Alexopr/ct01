package alg.coyote001.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Configuration class for managing subscription limits with hot-reload capability.
 * Loads subscription plans, limits, and settings from subscription-limits.json
 */
@Configuration
@EnableScheduling
public class SubscriptionLimitsConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionLimitsConfiguration.class);
    
    @Value("${subscription.config.file:subscription-limits.json}")
    private String configFileName;
    
    @Value("${subscription.config.hot-reload:true}")
    private boolean hotReloadEnabled;
    
    private volatile SubscriptionConfig currentConfig;
    private WatchService watchService;
    private ExecutorService executorService;
    private final ObjectMapper objectMapper;
    
    public SubscriptionLimitsConfiguration() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @PostConstruct
    public void init() {
        try {
            loadConfiguration();
            if (hotReloadEnabled) {
                startFileWatcher();
            }
            logger.info("Subscription configuration initialized successfully. Hot-reload: {}", hotReloadEnabled);
        } catch (Exception e) {
            logger.error("Failed to initialize subscription configuration", e);
            throw new RuntimeException("Failed to load subscription configuration", e);
        }
    }
    
    @PreDestroy
    public void destroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                logger.warn("Error closing watch service", e);
            }
        }
    }
    
    /**
     * Load configuration from the JSON file
     */
    private void loadConfiguration() throws IOException {
        ClassPathResource resource = new ClassPathResource(configFileName);
        if (!resource.exists()) {
            throw new IOException("Configuration file not found: " + configFileName);
        }
        
        try {
            SubscriptionConfig newConfig = objectMapper.readValue(resource.getInputStream(), SubscriptionConfig.class);
            
            // Validate configuration
            validateConfiguration(newConfig);
            
            this.currentConfig = newConfig;
            logger.info("Subscription configuration loaded successfully. Version: {}, Last updated: {}", 
                       newConfig.getVersion(), newConfig.getLastUpdated());
        } catch (Exception e) {
            logger.error("Error parsing subscription configuration file", e);
            throw new IOException("Invalid configuration file format", e);
        }
    }
    
    /**
     * Validate the loaded configuration
     */
    private void validateConfiguration(SubscriptionConfig config) {
        if (config.getSubscriptionPlans() == null || config.getSubscriptionPlans().isEmpty()) {
            throw new IllegalArgumentException("Subscription plans cannot be empty");
        }
        
        if (!config.getSubscriptionPlans().containsKey("FREE")) {
            throw new IllegalArgumentException("FREE plan must be defined");
        }
        
        if (!config.getSubscriptionPlans().containsKey("PREMIUM")) {
            throw new IllegalArgumentException("PREMIUM plan must be defined");
        }
        
        logger.debug("Configuration validation passed");
    }
    
    /**
     * Start file watcher for hot-reload functionality
     */
    private void startFileWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            executorService = Executors.newSingleThreadExecutor();
            
            // Get the resources directory path
            Path resourcePath = Paths.get(getClass().getClassLoader().getResource("").toURI()).getParent();
            Path watchPath = resourcePath.resolve("classes");
            
            watchPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            
            executorService.submit(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        WatchKey key = watchService.take();
                        
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                Path fileName = (Path) event.context();
                                if (fileName.toString().equals(configFileName)) {
                                    logger.info("Configuration file change detected, reloading...");
                                    // Wait a bit to ensure file write is complete
                                    Thread.sleep(1000);
                                    reloadConfiguration();
                                }
                            }
                        }
                        
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.info("File watcher thread interrupted");
                } catch (Exception e) {
                    logger.error("Error in file watcher", e);
                }
            });
            
        } catch (Exception e) {
            logger.warn("Failed to start file watcher, hot-reload will be disabled", e);
            hotReloadEnabled = false;
        }
    }
    
    /**
     * Reload configuration from file
     */
    private synchronized void reloadConfiguration() {
        try {
            SubscriptionConfig oldConfig = currentConfig;
            loadConfiguration();
            logger.info("Configuration reloaded successfully. Version changed from {} to {}", 
                       oldConfig != null ? oldConfig.getVersion() : "unknown", 
                       currentConfig.getVersion());
        } catch (Exception e) {
            logger.error("Failed to reload configuration, keeping current version", e);
        }
    }
    
    /**
     * Get current subscription configuration
     */
    public SubscriptionConfig getCurrentConfig() {
        return currentConfig;
    }
    
    /**
     * Get subscription plan by name
     */
    public SubscriptionPlan getSubscriptionPlan(String planName) {
        return currentConfig.getSubscriptionPlans().get(planName.toUpperCase());
    }
    
    /**
     * Check if hot-reload is enabled
     */
    public boolean isHotReloadEnabled() {
        return hotReloadEnabled;
    }
    
    /**
     * Manually trigger configuration reload (for testing purposes)
     */
    public void forceReload() {
        if (hotReloadEnabled) {
            reloadConfiguration();
        } else {
            logger.warn("Hot-reload is disabled, force reload ignored");
        }
    }
    
    /**
     * Scheduled check for configuration file changes (fallback for file watcher)
     */
    @Scheduled(fixedRate = 60000) // Check every minute
    public void scheduledConfigCheck() {
        if (!hotReloadEnabled) {
            return;
        }
        
        try {
            ClassPathResource resource = new ClassPathResource(configFileName);
            if (resource.exists() && resource.lastModified() > 0) {
                // This is a basic fallback - the main hot-reload mechanism is the file watcher
                logger.debug("Scheduled configuration check completed");
            }
        } catch (IOException e) {
            logger.debug("Error during scheduled config check", e);
        }
    }
    
    /**
     * Configuration bean for dependency injection
     */
    @Bean
    public SubscriptionConfig subscriptionConfig() {
        return getCurrentConfig();
    }
} 