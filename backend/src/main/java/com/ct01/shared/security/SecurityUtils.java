package com.ct01.shared.security;

import com.ct01.user.api.dto.CsrfTokenDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.UUID;

/**
 * Утилиты для работы с безопасностью
 */
public class SecurityUtils {
    
    private static final String AUTH_COOKIE_NAME = "CT_AUTH_TOKEN";
    private static final int COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7 дней
    
    /**
     * Получить ID текущего аутентифицированного пользователя
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                // Здесь должна быть логика извлечения user ID из UserDetails
                // Пока возвращаем заглушку
                return 1L; // TODO: Реализовать получение реального ID пользователя
            }
        }
        return null;
    }
    
    /**
     * Получить текущую аутентификацию
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    
    /**
     * Проверить аутентифицирован ли пользователь
     */
    public static boolean isAuthenticated() {
        Authentication auth = getCurrentAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }
    
    /**
     * Генерировать CSRF токен
     */
    public static CsrfTokenDto generateCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        
        if (csrfToken != null) {
            return new CsrfTokenDto(
                csrfToken.getToken(),
                csrfToken.getHeaderName(),
                csrfToken.getParameterName()
            );
        }
        
        // Генерируем простой токен если CSRF не настроен
        String token = UUID.randomUUID().toString();
        return new CsrfTokenDto(token, "X-XSRF-TOKEN", "_csrf");
    }
    
    /**
     * Установить cookie аутентификации
     */
    public static void setAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Только для HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setAttribute("SameSite", "Strict");
        
        response.addCookie(cookie);
    }
    
    /**
     * Очистить cookie аутентификации
     */
    public static void clearAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Удаляем cookie
        
        response.addCookie(cookie);
    }
    
    /**
     * Получить IP адрес клиента
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 