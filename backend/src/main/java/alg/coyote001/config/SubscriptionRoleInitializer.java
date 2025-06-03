package alg.coyote001.config;

import alg.coyote001.service.SubscriptionRoleIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialize subscription roles on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionRoleInitializer implements CommandLineRunner {
    
    private final SubscriptionRoleIntegrationService integrationService;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing subscription roles on startup...");
        integrationService.initializeSubscriptionRoles();
        log.info("Subscription roles initialization completed");
    }
} 