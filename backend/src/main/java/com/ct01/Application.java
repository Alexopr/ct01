package com.ct01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * CT.01 Application Main Class
 * 
 * Главный класс приложения для торгового трекера криптовалют
 * с DDD (Domain-Driven Design) архитектурой
 * 
 * Поддерживаемые домены:
 * - Core: базовые компоненты и конфигурации
 * - User: управление пользователями, роли, права
 * - Subscription: подписки и лимиты использования
 * - Crypto: криптовалютные данные и трекинг
 * - Market: рыночные данные и цены
 * - Notification: система уведомлений
 * - Admin: административные функции
 */
@SpringBootApplication(
    scanBasePackages = {
        "com.ct01"           // DDD пакет с полной функциональностью
    }
)
@EnableJpaRepositories(
    basePackages = {
        "com.ct01.**.infrastructure.persistence" // DDD repositories
    }
)
@EntityScan(
    basePackages = {
        "com.ct01.**.infrastructure.persistence" // DDD JPA entities
    }
)
@EnableCaching
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
public class Application {

    public static void main(String[] args) {
        // Установка профиля по умолчанию
        System.setProperty("spring.profiles.default", "dev");
        
        // Запуск приложения
        SpringApplication app = new SpringApplication(Application.class);
        
        // Дополнительные настройки для DDD архитектуры
        app.setAdditionalProfiles("ddd"); // Профиль для DDD конфигураций
        
        app.run(args);
    }
} 
