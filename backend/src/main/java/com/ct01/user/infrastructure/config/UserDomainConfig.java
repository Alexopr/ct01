package com.ct01.user.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Конфигурация для User Domain
 */
@Configuration
@ComponentScan(basePackages = "com.ct01.user")
@EnableJpaRepositories(basePackages = "com.ct01.user.infrastructure.repository")
public class UserDomainConfig {
} 
