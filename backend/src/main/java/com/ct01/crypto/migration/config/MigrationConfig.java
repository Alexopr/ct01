package com.ct01.crypto.migration.config;

import com.ct01.crypto.migration.MigrationService;
import com.ct01.crypto.migration.LegacyApiAdapter;
import com.ct01.crypto.application.facade.CryptoApplicationFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Конфигурация для компонентов миграции
 */
@Configuration
public class MigrationConfig {
    
    /**
     * Сервис управления миграцией
     */
    @Bean
    public MigrationService migrationService() {
        return new MigrationService();
    }
    
    /**
     * Адаптер для Legacy API
     */
    @Bean
    public LegacyApiAdapter legacyApiAdapter(CryptoApplicationFacade cryptoApplicationFacade,
                                           MigrationService migrationService) {
        return new LegacyApiAdapter(cryptoApplicationFacade, migrationService);
    }
} 
