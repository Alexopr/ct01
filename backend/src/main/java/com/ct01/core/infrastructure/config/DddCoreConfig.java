package com.ct01.core.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Базовая конфигурация для DDD компонентов.
 * Настраивает сканирование компонентов и базовые функциональности.
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {
    "com.ct01.core",
    "com.ct01.shared"
})
public class DddCoreConfig {
    
    // Базовые бины можно добавить здесь при необходимости
} 
