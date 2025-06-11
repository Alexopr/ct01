package com.ct01.subscription.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Конфигурация для Subscription Domain
 */
@Configuration
@ComponentScan(basePackages = "com.ct01.subscription")
@EnableJpaRepositories(basePackages = "com.ct01.subscription.infrastructure.repository")
public class SubscriptionDomainConfig {
} 
