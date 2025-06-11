package com.ct01.websocket.infrastructure.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Роутер трафика для направления клиентов между legacy и новой DDD системой
 */
@Component
public class TrafficRouter {
    
    private static final Logger logger = LoggerFactory.getLogger(TrafficRouter.class);
    
    // Feature toggle для включения новой системы
    @Value("${websocket.migration.ddd-system-enabled:false}")
    private boolean dddSystemEnabled;
    
    // Процент трафика, направляемого в новую систему
    @Value("${websocket.migration.ddd-traffic-percentage:0}")
    private int dddTrafficPercentage;
    
    // Паттерны User-Agent для принудительного использования новой системы
    private final Set<Pattern> forceDddPatterns = ConcurrentHashMap.newKeySet();
    
    // Клиенты, принудительно переведенные на legacy
    private final Set<String> forceLegacyClients = ConcurrentHashMap.newKeySet();
    
    // Клиенты, принудительно переведенные на DDD
    private final Set<String> forceDddClients = ConcurrentHashMap.newKeySet();
    
    public TrafficRouter() {
        // Современные браузеры автоматически используют новую систему
        forceDddPatterns.add(Pattern.compile(".*Chrome/([1-9][0-9][0-9]|[1-9][0-9]{3,}).*")); // Chrome 100+
        forceDddPatterns.add(Pattern.compile(".*Firefox/([1-9][0-9][0-9]|[1-9][0-9]{3,}).*")); // Firefox 100+
        forceDddPatterns.add(Pattern.compile(".*Edge/([1-9][0-9][0-9]|[1-9][0-9]{3,}).*")); // Edge 100+
    }
    
    /**
     * Определяет, какую систему использовать для данного клиента
     */
    public RoutingDecision routeClient(String clientId, String userAgent, String clientIp) {
        
        // Если DDD система отключена, используем legacy
        if (!dddSystemEnabled) {
            logger.debug("DDD system disabled, routing {} to legacy", clientId);
            return new RoutingDecision(RoutingTarget.LEGACY, "DDD system disabled");
        }
        
        // Проверяем принудительные настройки клиента
        if (forceDddClients.contains(clientId)) {
            logger.debug("Client {} forced to DDD system", clientId);
            return new RoutingDecision(RoutingTarget.DDD, "Client forced to DDD");
        }
        
        if (forceLegacyClients.contains(clientId)) {
            logger.debug("Client {} forced to legacy system", clientId);
            return new RoutingDecision(RoutingTarget.LEGACY, "Client forced to legacy");
        }
        
        // Проверяем User-Agent паттерны
        if (userAgent != null) {
            for (Pattern pattern : forceDddPatterns) {
                if (pattern.matcher(userAgent).matches()) {
                    logger.debug("Client {} routed to DDD based on User-Agent pattern", clientId);
                    return new RoutingDecision(RoutingTarget.DDD, "Modern browser detected");
                }
            }
        }
        
        // Используем процентное распределение трафика
        int hash = Math.abs(clientId.hashCode()) % 100;
        if (hash < dddTrafficPercentage) {
            logger.debug("Client {} routed to DDD system (percentage: {}%)", clientId, dddTrafficPercentage);
            return new RoutingDecision(RoutingTarget.DDD, "Traffic percentage routing");
        }
        
        logger.debug("Client {} routed to legacy system", clientId);
        return new RoutingDecision(RoutingTarget.LEGACY, "Default routing");
    }
    
    /**
     * Принудительно направляет клиента в DDD систему
     */
    public void forceToDdd(String clientId) {
        forceDddClients.add(clientId);
        forceLegacyClients.remove(clientId);
        logger.info("Client {} forced to DDD system", clientId);
    }
    
    /**
     * Принудительно направляет клиента в legacy систему
     */
    public void forceToLegacy(String clientId) {
        forceLegacyClients.add(clientId);
        forceDddClients.remove(clientId);
        logger.info("Client {} forced to legacy system", clientId);
    }
    
    /**
     * Убирает принудительную настройку для клиента
     */
    public void removeForcing(String clientId) {
        boolean removedFromDdd = forceDddClients.remove(clientId);
        boolean removedFromLegacy = forceLegacyClients.remove(clientId);
        
        if (removedFromDdd || removedFromLegacy) {
            logger.info("Removed forcing for client {}", clientId);
        }
    }
    
    /**
     * Обновляет процент трафика для DDD системы
     */
    public void updateDddTrafficPercentage(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Traffic percentage must be between 0 and 100");
        }
        
        this.dddTrafficPercentage = percentage;
        logger.info("Updated DDD traffic percentage to {}%", percentage);
    }
    
    /**
     * Включает/выключает DDD систему
     */
    public void setDddSystemEnabled(boolean enabled) {
        this.dddSystemEnabled = enabled;
        logger.info("DDD system {}", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Добавляет паттерн User-Agent для принудительного использования DDD
     */
    public void addForceDddPattern(String pattern) {
        forceDddPatterns.add(Pattern.compile(pattern));
        logger.info("Added force DDD pattern: {}", pattern);
    }
    
    /**
     * Возвращает статистику роутинга
     */
    public RoutingStats getRoutingStats() {
        return new RoutingStats(
            dddSystemEnabled,
            dddTrafficPercentage,
            forceDddClients.size(),
            forceLegacyClients.size(),
            forceDddPatterns.size()
        );
    }
}

/**
 * Результат решения о роутинге
 */
class RoutingDecision {
    private final RoutingTarget target;
    private final String reason;
    
    public RoutingDecision(RoutingTarget target, String reason) {
        this.target = target;
        this.reason = reason;
    }
    
    public RoutingTarget getTarget() {
        return target;
    }
    
    public String getReason() {
        return reason;
    }
    
    public boolean useDddSystem() {
        return target == RoutingTarget.DDD;
    }
}

/**
 * Целевая система для роутинга
 */
enum RoutingTarget {
    LEGACY,
    DDD
}

/**
 * Статистика роутинга
 */
record RoutingStats(
    boolean dddSystemEnabled,
    int dddTrafficPercentage,
    int forcedDddClients,
    int forcedLegacyClients,
    int forceDddPatterns
) {} 