package alg.coyote001.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for validating required environment variables
 * and centralizing security-related configuration properties.
 */
@Configuration
@Slf4j
public class EnvironmentConfig {

    @Value("${TELEGRAM_BOT_TOKEN}")
    private String telegramBotToken;

    @Value("${app.telegram.bot.name}")
    private String telegramBotName;

    /**
     * Validates that all required environment variables are set
     * and have non-empty values.
     * 
     * @throws IllegalStateException if required environment variables are missing
     */
    @PostConstruct
    public void validateEnvironmentVariables() {
        log.info("Validating required environment variables...");
        
        if (!StringUtils.hasText(telegramBotToken)) {
            throw new IllegalStateException(
                "TELEGRAM_BOT_TOKEN environment variable is required but not set. " +
                "Please set this environment variable before starting the application."
            );
        }
        
        if (!StringUtils.hasText(telegramBotName)) {
            log.warn("Telegram bot name is not configured, using default value");
        }
        
        log.info("Environment validation completed successfully");
        log.info("Telegram bot configured: {}", telegramBotName);
    }

    /**
     * Gets the configured Telegram bot token.
     * 
     * @return the Telegram bot token
     */
    public String getTelegramBotToken() {
        return telegramBotToken;
    }

    /**
     * Gets the configured Telegram bot name.
     * 
     * @return the Telegram bot name
     */
    public String getTelegramBotName() {
        return telegramBotName;
    }
} 