package com.ct01.user.security.infrastructure;

import com.ct01.core.domain.UserId;
import com.ct01.user.application.UserApplicationFacade;
import com.ct01.user.domain.Permission;
import com.ct01.user.domain.Role;
import com.ct01.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DDD-совместимый UserDetailsService
 * Загружает пользователей через UserApplicationFacade
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DddUserDetailsService implements UserDetailsService {
    
    private final UserApplicationFacade userApplicationFacade;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("DDD: Attempting to load user by email: {}", username);
        
        try {
            // Пытаемся найти пользователя через DDD Application Facade
            User user = userApplicationFacade.findUserByEmail(username)
                .orElseThrow(() -> {
                    log.error("DDD: User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
            
            log.info("DDD: User found: {}, status: {}, roles count: {}", 
                    user.getEmail().getValue(), 
                    user.isActive() ? "ACTIVE" : "INACTIVE",
                    user.getRoles().size());
            
            // Собираем authorities из ролей пользователя
            Collection<GrantedAuthority> authorities = buildAuthorities(user);
            
            log.info("DDD: Authorities granted: {}", authorities);
            
            // Проверяем пароль
            String password = user.getPasswordHash() != null ? user.getPasswordHash() : "";
            log.info("DDD: Password hash exists: {}, length: {}", 
                    password.length() > 0, 
                    password.length());
            
            // Создаем Spring Security UserDetails
            return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail().getValue())
                .password(password)
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.isActive()) // Заблокирован если неактивен
                .credentialsExpired(false)
                .disabled(!user.isActive()) // Отключен если неактивен
                .build();
                
        } catch (Exception e) {
            log.error("DDD: Error loading user {}: {}", username, e.getMessage(), e);
            throw new UsernameNotFoundException("Failed to load user: " + username, e);
        }
    }
    
    /**
     * Строит authorities из ролей и разрешений пользователя
     */
    private Collection<GrantedAuthority> buildAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // Добавляем роли с префиксом ROLE_
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            
            // Добавляем разрешения из роли
            for (Permission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }
        
        // Если нет ролей, добавляем базовую роль USER
        if (authorities.isEmpty()) {
            log.warn("DDD: User {} has no roles, adding default ROLE_USER", user.getEmail().getValue());
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        return authorities;
    }
    
    /**
     * Загружает пользователя по ID для программного использования
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.info("DDD: Attempting to load user by ID: {}", userId);
        
        try {
            User user = userApplicationFacade.getUser(new UserId(userId));
            return loadUserByUsername(user.getEmail().getValue());
        } catch (Exception e) {
            log.error("DDD: Error loading user by ID {}: {}", userId, e.getMessage(), e);
            throw new UsernameNotFoundException("Failed to load user by ID: " + userId, e);
        }
    }
} 