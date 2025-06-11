package com.ct01.core.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

/**
 * Контроллер для проверки здоровья DDD архитектуры
 */
@RestController
@RequestMapping("/api/v1/core/health")
public class HealthController {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Проверка работы DDD архитектуры
     */
    @GetMapping("/ddd")
    public ResponseEntity<Map<String, Object>> checkDddHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // Основная информация
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("architecture", "DDD");
        health.put("application", "CT.01");
        
        // Проверка доменов
        Map<String, Boolean> domains = new HashMap<>();
        domains.put("core", checkDomainHealth("com.ct01.core"));
        domains.put("user", checkDomainHealth("com.ct01.user"));
        domains.put("crypto", checkDomainHealth("com.ct01.crypto"));
        domains.put("market", checkDomainHealth("com.ct01.market"));
        domains.put("subscription", checkDomainHealth("com.ct01.subscription"));
        domains.put("notification", checkDomainHealth("com.ct01.notification"));
        domains.put("admin", checkDomainHealth("com.ct01.admin"));
        
        health.put("domains", domains);
        
        // Информация о профилях
        health.put("activeProfiles", Arrays.asList(applicationContext.getEnvironment().getActiveProfiles()));
        
        // Статистика beans
        Map<String, Integer> beanStats = new HashMap<>();
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        beanStats.put("total", allBeanNames.length);
        
        int dddBeans = 0;
        int legacyBeans = 0;
        for (String beanName : allBeanNames) {
            if (beanName.contains("ct01")) {
                dddBeans++;
            } else if (beanName.contains("coyote001")) {
                legacyBeans++;
            }
        }
        
        beanStats.put("ddd", dddBeans);
        beanStats.put("legacy", legacyBeans);
        health.put("beans", beanStats);
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Простая проверка загрузки домена
     */
    private boolean checkDomainHealth(String packageName) {
        try {
            // Проверяем, что пакет существует и имеет хотя бы один bean
            String[] beanNames = applicationContext.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                try {
                    Class<?> beanClass = applicationContext.getBean(beanName).getClass();
                    if (beanClass.getPackage() != null && 
                        beanClass.getPackage().getName().startsWith(packageName)) {
                        return true;
                    }
                } catch (Exception e) {
                    // Игнорируем ошибки отдельных beans
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
} 
