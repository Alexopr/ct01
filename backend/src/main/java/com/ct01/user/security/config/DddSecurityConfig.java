package com.ct01.user.security.config;

import com.ct01.user.security.infrastructure.DddAuthenticationProvider;
import com.ct01.user.security.infrastructure.DddUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * DDD Security Configuration
 * Современная конфигурация безопасности для DDD архитектуры
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class DddSecurityConfig {
    
    @Value("${app.security.session-timeout:3600}")
    private int sessionTimeout;
    
    @Value("${app.security.bcrypt-strength:12}")
    private int bcryptStrength;
    
    private final DddUserDetailsService dddUserDetailsService;
    private final DddAuthenticationProvider dddAuthenticationProvider;
    
    /**
     * Основная цепочка фильтров безопасности
     */
    @Bean
    @Primary
    public SecurityFilterChain dddSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            // CORS конфигурация
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF конфигурация
            .csrf(csrf -> csrf
                .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                // DDD API endpoints не требуют CSRF для REST API
                .ignoringRequestMatchers("/api/v1/auth/**")
                .ignoringRequestMatchers("/api/v1/users/**") 
                .ignoringRequestMatchers("/api/v1/coins/**")
                .ignoringRequestMatchers("/api/v1/exchanges/**")
                .ignoringRequestMatchers("/api/v1/settings/**")
                // Legacy endpoints (будут удалены после миграции)
                .ignoringRequestMatchers("/api/auth/**")
            )
            
            // Авторизация запросов
            .authorizeHttpRequests(auth -> auth
                // Публичные эндпоинты аутентификации
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll() // Legacy compatibility
                
                // Публичные эндпоинты для криптовалют
                .requestMatchers("/api/v1/coins/search").permitAll()
                .requestMatchers("/api/v1/coins/active").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/coins/*/price").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/coins/*/price/*").permitAll()
                
                // Публичные эндпоинты для бирж
                .requestMatchers("/api/v1/exchanges/status").permitAll()
                
                // Публичные эндпоинты для анализа
                .requestMatchers("/api/v1/analysis/health").permitAll()
                .requestMatchers("/api/v1/analysis/discover/test").permitAll()
                
                // WebSocket подключения
                .requestMatchers("/websocket/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                
                // Мониторинг
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/v1/prices/status").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/prices/update").authenticated()
                
                // Защищенные DDD API эндпоинты
                .requestMatchers("/api/v1/users/**").authenticated()
                .requestMatchers("/api/v1/settings/**").authenticated()
                .requestMatchers("/api/v1/coins/**").authenticated()
                .requestMatchers("/api/v1/exchanges/**").authenticated()
                .requestMatchers("/api/v1/analysis/**").authenticated()
                .requestMatchers("/api/v1/subscription/**").authenticated()
                
                // Документация
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/favicon.ico", "/error").permitAll()
                
                // Все остальные запросы требуют аутентификации
                .anyRequest().authenticated()
            )
            
            // Управление сессиями
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(5)
                .and()
                .sessionFixation().migrateSession()
                .invalidSessionUrl("/api/v1/auth/login")
            )
            
            // DDD Authentication Provider
            .authenticationProvider(dddAuthenticationProvider)
            
            // Security Context Repository
            .securityContext(context -> context
                .securityContextRepository(securityContextRepository())
            )
            
            .build();
    }
    
    /**
     * CORS конфигурация для DDD API
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Разрешительная конфигурация для разработки
        // TODO: Ограничить в production
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * Password Encoder с настраиваемой сложностью
     */
    @Bean
    @Primary
    public PasswordEncoder dddPasswordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }
    
    /**
     * Security Context Repository для управления сессиями
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
    
    /**
     * Authentication Manager для программного доступа к аутентификации
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
} 