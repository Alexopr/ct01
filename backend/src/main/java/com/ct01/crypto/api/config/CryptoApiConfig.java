package com.ct01.crypto.api.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для API слоя криптомодуля
 */
@Configuration
@ComponentScan(basePackages = {
    "com.ct01.crypto.api.controller"
})
public class CryptoApiConfig {
    
    // Конфигурация для контроллеров API
    // Spring Boot автоматически зарегистрирует все @RestController из пакета
} 
