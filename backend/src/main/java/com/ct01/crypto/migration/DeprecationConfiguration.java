package com.ct01.crypto.migration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация для deprecation interceptor
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "legacy.api.deprecation.enabled", havingValue = "true", matchIfMissing = true)
public class DeprecationConfiguration implements WebMvcConfigurer {
    
    private final DeprecationInterceptor deprecationInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(deprecationInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                    "/api/v1/crypto/**",        // Новые DDD endpoints
                    "/api/v1/system/migration/**",  // Migration endpoints
                    "/api/v1/actuator/**"       // Actuator endpoints
                );
    }
} 