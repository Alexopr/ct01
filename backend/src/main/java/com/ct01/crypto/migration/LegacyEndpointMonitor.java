package com.ct01.crypto.migration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Система мониторинга использования legacy endpoints
 * Отслеживает метрики для принятия решений о депрекации
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LegacyEndpointMonitor {
    
    private final MeterRegistry meterRegistry;
    
    // Счетчики использования по endpoints
    private final Map<String, Counter> endpointCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> responseTimers = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> uniqueClientsPerEndpoint = new ConcurrentHashMap<>();
    
    // Общие метрики
    private final Counter totalLegacyRequests;
    private final Counter deprecationWarningsShown;
    
    public LegacyEndpointMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.totalLegacyRequests = Counter.builder("legacy.api.requests.total")
                .description("Общее количество запросов к legacy API")
                .register(meterRegistry);
        this.deprecationWarningsShown = Counter.builder("legacy.api.deprecation.warnings.total")
                .description("Количество показанных предупреждений о депрекации")
                .register(meterRegistry);
    }
    
    /**
     * Записать использование legacy endpoint
     */
    @Async
    public void recordLegacyEndpointUsage(String endpoint, String method, String clientId, 
                                        String userAgent, long responseTimeMs) {
        String endpointKey = method + " " + endpoint;
        
        // Увеличиваем счетчик для конкретного endpoint
        Counter counter = endpointCounters.computeIfAbsent(endpointKey, key ->
                Counter.builder("legacy.api.endpoint.requests")
                        .tag("endpoint", endpoint)
                        .tag("method", method)
                        .description("Запросы к конкретному legacy endpoint")
                        .register(meterRegistry)
        );
        counter.increment();
        
        // Записываем время ответа
        Timer timer = responseTimers.computeIfAbsent(endpointKey, key ->
                Timer.builder("legacy.api.endpoint.response.time")
                        .tag("endpoint", endpoint)
                        .tag("method", method)
                        .description("Время ответа legacy endpoint")
                        .register(meterRegistry)
        );
        timer.record(responseTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        // Отслеживаем уникальных клиентов
        String clientKey = endpointKey + ":" + clientId;
        uniqueClientsPerEndpoint.computeIfAbsent(clientKey, k -> new AtomicLong(0)).incrementAndGet();
        
        // Общий счетчик
        totalLegacyRequests.increment();
        
        // Логирование
        log.debug("Legacy API usage recorded: {} {} by client {} ({}ms)", 
                method, endpoint, clientId, responseTimeMs);
        
        // Детальная информация для анализа
        if (log.isTraceEnabled()) {
            log.trace("Legacy API details - Endpoint: {}, Client: {}, UserAgent: {}, ResponseTime: {}ms",
                    endpointKey, clientId, userAgent, responseTimeMs);
        }
    }
    
    /**
     * Записать показ предупреждения о депрекации
     */
    @Async
    public void recordDeprecationWarning(String endpoint, String method, String clientId) {
        deprecationWarningsShown.increment();
        
        // Метрика по конкретному endpoint
        Counter.builder("legacy.api.deprecation.warnings.endpoint")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .description("Предупреждения о депрекации для конкретного endpoint")
                .register(meterRegistry)
                .increment();
        
        log.info("Deprecation warning shown for {} {} to client {}", method, endpoint, clientId);
    }
    
    /**
     * Записать попытку доступа к отключенному endpoint
     */
    @Async
    public void recordDisabledEndpointAccess(String endpoint, String method, String clientId) {
        Counter.builder("legacy.api.disabled.access.attempts")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .description("Попытки доступа к отключенным legacy endpoints")
                .register(meterRegistry)
                .increment();
        
        log.warn("Access attempt to disabled legacy endpoint: {} {} by client {}", 
                method, endpoint, clientId);
    }
    
    /**
     * Записать успешный редирект на новый endpoint
     */
    @Async
    public void recordSuccessfulRedirect(String legacyEndpoint, String newEndpoint, String clientId) {
        Counter.builder("legacy.api.redirects.successful")
                .tag("legacy.endpoint", legacyEndpoint)
                .tag("new.endpoint", newEndpoint)
                .description("Успешные редиректы с legacy на новые endpoints")
                .register(meterRegistry)
                .increment();
        
        log.info("Successful redirect from {} to {} for client {}", 
                legacyEndpoint, newEndpoint, clientId);
    }
    
    /**
     * Получить статистику использования legacy API
     */
    public LegacyUsageStats getLegacyUsageStats() {
        LegacyUsageStats stats = new LegacyUsageStats();
        
        // Общие метрики
        stats.setTotalRequests((long) totalLegacyRequests.count());
        stats.setTotalWarningsShown((long) deprecationWarningsShown.count());
        stats.setUniqueEndpoints(endpointCounters.size());
        
        // Подсчитываем уникальных клиентов
        long uniqueClients = uniqueClientsPerEndpoint.keySet().stream()
                .map(key -> key.split(":")[1]) // Извлекаем clientId
                .distinct()
                .count();
        stats.setUniqueClients(uniqueClients);
        
        // Самые популярные endpoints
        Map<String, Long> popularEndpoints = new ConcurrentHashMap<>();
        endpointCounters.forEach((endpoint, counter) -> 
                popularEndpoints.put(endpoint, (long) counter.count()));
        stats.setPopularEndpoints(popularEndpoints);
        
        stats.setLastUpdated(LocalDateTime.now());
        
        return stats;
    }
    
    /**
     * Получить детальную статистику по конкретному endpoint
     */
    public EndpointUsageDetails getEndpointDetails(String endpoint, String method) {
        String endpointKey = method + " " + endpoint;
        
        EndpointUsageDetails details = new EndpointUsageDetails();
        details.setEndpoint(endpoint);
        details.setMethod(method);
        
        Counter counter = endpointCounters.get(endpointKey);
        details.setRequestCount(counter != null ? (long) counter.count() : 0);
        
        Timer timer = responseTimers.get(endpointKey);
        if (timer != null) {
            details.setAverageResponseTime(timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            details.setMaxResponseTime(timer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
        }
        
        // Подсчитываем уникальных клиентов для этого endpoint
        long uniqueClients = uniqueClientsPerEndpoint.keySet().stream()
                .filter(key -> key.startsWith(endpointKey + ":"))
                .count();
        details.setUniqueClients(uniqueClients);
        
        return details;
    }
    
    /**
     * Проверить, нужно ли показать предупреждение о депрекации
     */
    public boolean shouldShowDeprecationWarning(String endpoint, String clientId) {
        // Логика определения частоты предупреждений
        // Можно настроить: показывать каждый N-й раз, или раз в день для клиента, etc.
        
        String key = endpoint + ":" + clientId;
        AtomicLong requestCount = uniqueClientsPerEndpoint.get(key);
        
        if (requestCount == null) {
            return true; // Первый запрос - показываем предупреждение
        }
        
        // Показываем предупреждение каждый 10-й запрос от клиента
        return requestCount.get() % 10 == 1;
    }
    
    /**
     * Статистика использования legacy API
     */
    public static class LegacyUsageStats {
        private Long totalRequests;
        private Long totalWarningsShown;
        private Integer uniqueEndpoints;
        private Long uniqueClients;
        private Map<String, Long> popularEndpoints;
        private LocalDateTime lastUpdated;
        
        // Getters and Setters
        public Long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(Long totalRequests) { this.totalRequests = totalRequests; }
        
        public Long getTotalWarningsShown() { return totalWarningsShown; }
        public void setTotalWarningsShown(Long totalWarningsShown) { this.totalWarningsShown = totalWarningsShown; }
        
        public Integer getUniqueEndpoints() { return uniqueEndpoints; }
        public void setUniqueEndpoints(Integer uniqueEndpoints) { this.uniqueEndpoints = uniqueEndpoints; }
        
        public Long getUniqueClients() { return uniqueClients; }
        public void setUniqueClients(Long uniqueClients) { this.uniqueClients = uniqueClients; }
        
        public Map<String, Long> getPopularEndpoints() { return popularEndpoints; }
        public void setPopularEndpoints(Map<String, Long> popularEndpoints) { this.popularEndpoints = popularEndpoints; }
        
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }
    
    /**
     * Детальная информация об использовании endpoint
     */
    public static class EndpointUsageDetails {
        private String endpoint;
        private String method;
        private Long requestCount;
        private Double averageResponseTime;
        private Double maxResponseTime;
        private Long uniqueClients;
        
        // Getters and Setters
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        
        public Long getRequestCount() { return requestCount; }
        public void setRequestCount(Long requestCount) { this.requestCount = requestCount; }
        
        public Double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(Double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public Double getMaxResponseTime() { return maxResponseTime; }
        public void setMaxResponseTime(Double maxResponseTime) { this.maxResponseTime = maxResponseTime; }
        
        public Long getUniqueClients() { return uniqueClients; }
        public void setUniqueClients(Long uniqueClients) { this.uniqueClients = uniqueClients; }
    }
} 