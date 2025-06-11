package com.ct01.crypto.migration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interceptor для обработки deprecation заголовков и мониторинга legacy endpoints
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "legacy.api.deprecation.enabled", havingValue = "true", matchIfMissing = true)
public class DeprecationInterceptor implements HandlerInterceptor {
    
    private final LegacyEndpointMonitor monitor;
    private final MigrationService migrationService;
    
    // Кэш для определения legacy endpoints
    private final Map<String, Boolean> legacyEndpointCache = new ConcurrentHashMap<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler) throws Exception {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // Проверяем, является ли это legacy endpoint
        if (isLegacyEndpoint(requestURI)) {
            String clientId = extractClientId(request);
            String userAgent = request.getHeader("User-Agent");
            
            // Проверяем, включен ли legacy API
            if (!migrationService.isLegacyApiEnabled()) {
                monitor.recordDisabledEndpointAccess(requestURI, method, clientId);
                response.setStatus(HttpServletResponse.SC_GONE);
                response.getWriter().write("{\"error\":\"This API has been deprecated and is no longer available\",\"migration_guide\":\"https://docs.api.company.com/migration\"}");
                return false;
            }
            
            // Записываем начало обработки
            request.setAttribute("legacy.request.start.time", System.currentTimeMillis());
            request.setAttribute("legacy.endpoint", requestURI);
            request.setAttribute("legacy.method", method);
            request.setAttribute("legacy.client.id", clientId);
            request.setAttribute("legacy.user.agent", userAgent);
            
            // Добавляем deprecation заголовки
            addDeprecationHeaders(response, requestURI, method);
            
            // Показываем предупреждение если нужно
            if (monitor.shouldShowDeprecationWarning(requestURI, clientId)) {
                monitor.recordDeprecationWarning(requestURI, method, clientId);
                addDeprecationWarningHeaders(response, requestURI);
            }
            
            log.debug("Processing legacy request: {} {} from client {}", method, requestURI, clientId);
        }
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                          Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        
        String requestURI = (String) request.getAttribute("legacy.endpoint");
        if (requestURI != null) {
            // Записываем статистику после обработки
            long startTime = (Long) request.getAttribute("legacy.request.start.time");
            long responseTime = System.currentTimeMillis() - startTime;
            
            String method = (String) request.getAttribute("legacy.method");
            String clientId = (String) request.getAttribute("legacy.client.id");
            String userAgent = (String) request.getAttribute("legacy.user.agent");
            
            monitor.recordLegacyEndpointUsage(requestURI, method, clientId, userAgent, responseTime);
        }
    }
    
    /**
     * Определить, является ли endpoint legacy
     */
    private boolean isLegacyEndpoint(String requestURI) {
        return legacyEndpointCache.computeIfAbsent(requestURI, uri -> {
            // Проверяем по шаблонам legacy endpoints
            return uri.startsWith("/api/v1/coins") && !uri.startsWith("/api/v1/crypto/") ||
                   uri.startsWith("/api/v1/tracked-coins") && !uri.startsWith("/api/v1/crypto/") ||
                   uri.startsWith("/api/v1/prices") && !uri.startsWith("/api/v1/crypto/") ||
                   uri.startsWith("/api/v1/exchanges") && !uri.startsWith("/api/v1/crypto/") ||
                   (uri.startsWith("/api/v1/system") && !uri.startsWith("/api/v1/system/migration")) ||
                   (uri.startsWith("/api/v1/subscriptions") && !uri.startsWith("/api/v1/crypto/")) ||
                   (uri.startsWith("/api/v1/users") && !uri.startsWith("/api/v1/crypto/")) ||
                   uri.startsWith("/api/v1/analysis");
        });
    }
    
    /**
     * Добавить основные deprecation заголовки
     */
    private void addDeprecationHeaders(HttpServletResponse response, String requestURI, String method) {
        response.setHeader("X-API-Deprecated", "true");
        response.setHeader("X-API-Deprecation-Date", "2024-01-01T00:00:00Z");
        response.setHeader("X-API-Removal-Date", getRemovalDate(requestURI));
        response.setHeader("X-API-Migration-Guide", "https://docs.api.company.com/migration");
        
        // Добавляем ссылку на новый endpoint
        String newEndpoint = getNewEndpoint(requestURI);
        if (newEndpoint != null) {
            response.setHeader("X-API-New-Endpoint", newEndpoint);
            response.setHeader("Link", "<" + newEndpoint + ">; rel=\"successor-version\"");
        }
        
        // Sunset заголовок (RFC 8594)
        response.setHeader("Sunset", getRemovalDate(requestURI));
        
        // Заголовок с предупреждением
        response.setHeader("Warning", "299 - \"This API is deprecated. Use " + newEndpoint + " instead.\"");
    }
    
    /**
     * Добавить дополнительные предупреждения
     */
    private void addDeprecationWarningHeaders(HttpServletResponse response, String requestURI) {
        response.setHeader("X-Deprecation-Warning", "This endpoint will be removed soon");
        response.setHeader("X-Deprecation-Details", "Consider migrating to the new API to avoid service interruption");
        response.setHeader("X-Support-Contact", "api-support@company.com");
        
        log.info("Deprecation warning added for legacy endpoint: {}", requestURI);
    }
    
    /**
     * Получить дату удаления endpoint
     */
    private String getRemovalDate(String requestURI) {
        // Определяем дату удаления в зависимости от endpoint
        if (requestURI.startsWith("/api/v1/coins") || requestURI.startsWith("/api/v1/tracked-coins")) {
            return "2024-06-01T00:00:00Z";
        } else if (requestURI.startsWith("/api/v1/exchanges")) {
            return "2024-03-01T00:00:00Z";
        } else {
            return "2024-09-01T00:00:00Z";
        }
    }
    
    /**
     * Получить новый endpoint для замены
     */
    private String getNewEndpoint(String requestURI) {
        if (requestURI.startsWith("/api/v1/coins")) {
            return requestURI.replace("/api/v1/coins", "/api/v1/crypto/coins");
        } else if (requestURI.startsWith("/api/v1/tracked-coins")) {
            return requestURI.replace("/api/v1/tracked-coins", "/api/v1/crypto/tracked-coins");
        } else if (requestURI.startsWith("/api/v1/prices")) {
            return requestURI.replace("/api/v1/prices", "/api/v1/crypto/prices");
        } else if (requestURI.startsWith("/api/v1/exchanges")) {
            return requestURI.replace("/api/v1/exchanges", "/api/v1/crypto/exchanges");
        } else if (requestURI.startsWith("/api/v1/analysis")) {
            return requestURI.replace("/api/v1/analysis", "/api/v1/admin/analysis");
        }
        return null;
    }
    
    /**
     * Извлечь идентификатор клиента из запроса
     */
    private String extractClientId(HttpServletRequest request) {
        // Пытаемся получить клиент ID из разных источников
        String clientId = request.getHeader("X-Client-ID");
        if (clientId != null) {
            return clientId;
        }
        
        clientId = request.getHeader("X-API-Key");
        if (clientId != null) {
            return "api-key-" + clientId.substring(0, Math.min(8, clientId.length()));
        }
        
        // Используем IP и User-Agent как fallback
        String userAgent = request.getHeader("User-Agent");
        String remoteAddr = request.getRemoteAddr();
        
        return "client-" + (remoteAddr + "-" + (userAgent != null ? userAgent : "unknown")).hashCode();
    }
    
    /**
     * Очистить кэш legacy endpoints (для тестирования)
     */
    public void clearCache() {
        legacyEndpointCache.clear();
        log.debug("Legacy endpoint cache cleared");
    }
} 