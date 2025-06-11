package com.ct01.user.security.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DDD Authentication Provider
 * Мост между Spring Security и DDD User domain
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DddAuthenticationProvider extends DaoAuthenticationProvider {
    
    private final DddUserDetailsService dddUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Инициализация провайдера аутентификации
     */
    @jakarta.annotation.PostConstruct
    public void initializeProvider() {
        setUserDetailsService(dddUserDetailsService);
        setPasswordEncoder(passwordEncoder);
        log.info("DDD Authentication Provider initialized");
    }
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        
        log.info("DDD: Authenticating user: {}", username);
        
        try {
            // Загружаем пользователя через DDD UserDetailsService
            UserDetails userDetails = dddUserDetailsService.loadUserByUsername(username);
            
            // Проверяем пароль
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                log.warn("DDD: Bad credentials for user: {}", username);
                throw new BadCredentialsException("Неверный пароль");
            }
            
            // Проверяем статус аккаунта
            if (!userDetails.isEnabled()) {
                log.warn("DDD: User account is disabled: {}", username);
                throw new BadCredentialsException("Аккаунт пользователя отключен");
            }
            
            if (!userDetails.isAccountNonLocked()) {
                log.warn("DDD: User account is locked: {}", username);
                throw new BadCredentialsException("Аккаунт пользователя заблокирован");
            }
            
            if (!userDetails.isAccountNonExpired()) {
                log.warn("DDD: User account is expired: {}", username);
                throw new BadCredentialsException("Срок действия аккаунта истек");
            }
            
            if (!userDetails.isCredentialsNonExpired()) {
                log.warn("DDD: User credentials are expired: {}", username);
                throw new BadCredentialsException("Срок действия учетных данных истек");
            }
            
            log.info("DDD: Authentication successful for user: {} with authorities: {}", 
                    username, userDetails.getAuthorities());
            
            // Создаем успешный токен аутентификации
            UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                userDetails, 
                null, // Очищаем пароль из токена
                userDetails.getAuthorities()
            );
            
            result.setDetails(authentication.getDetails());
            return result;
            
        } catch (AuthenticationException e) {
            log.error("DDD: Authentication failed for user {}: {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("DDD: Unexpected error during authentication for user {}: {}", username, e.getMessage(), e);
            throw new BadCredentialsException("Ошибка аутентификации", e);
        }
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        // Поддерживаем стандартную аутентификацию по логину/паролю
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
} 