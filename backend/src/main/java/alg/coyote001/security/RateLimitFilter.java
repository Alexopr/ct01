package alg.coyote001.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final RateLimitService rateLimitService;
    
    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String clientIp = getClientIpAddress(request);
        String path = request.getRequestURI();
        
        // Пропускаем rate limiting для статических ресурсов и некоторых путей
        if (shouldSkipRateLimit(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Применяем общий rate limiting
        if (!rateLimitService.tryConsumeGeneral(clientIp)) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Слишком много запросов. Попробуйте позже.\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean shouldSkipRateLimit(String path) {
        return path.startsWith("/static/") || 
               path.startsWith("/css/") || 
               path.startsWith("/js/") || 
               path.startsWith("/images/") ||
               path.equals("/favicon.ico") ||
               path.startsWith("/actuator/health");
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 