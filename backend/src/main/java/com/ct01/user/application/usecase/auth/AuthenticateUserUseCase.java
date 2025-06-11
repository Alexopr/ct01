package com.ct01.user.application.usecase.auth;

import com.ct01.core.application.UseCase;
import com.ct01.user.application.command.AuthenticateUserCommand;
import com.ct01.user.application.dto.AuthResult;
import com.ct01.user.domain.User;
import com.ct01.user.domain.repository.UserRepository;
import com.ct01.user.security.domain.service.AuthenticationDomainService;
import com.ct01.user.security.application.UserSecurityApplicationService;
import com.ct01.user.security.domain.event.AuthenticationFailedEvent;
import com.ct01.user.security.domain.event.UserAuthenticatedEvent;
import com.ct01.core.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use Case для аутентификации пользователя
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthenticateUserUseCase implements UseCase<AuthenticateUserCommand, AuthResult.LoginResult> {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationDomainService authenticationDomainService;
    private final UserSecurityApplicationService securityService;
    private final DomainEventPublisher eventPublisher;
    
    @Override
    public AuthResult.LoginResult execute(AuthenticateUserCommand command) {
        log.info("Authenticating user: {}", command.email());
        
        try {
            // Поиск пользователя по email
            User user = userRepository.findByEmail(command.email())
                .orElse(null);
            
            if (user == null) {
                log.warn("User not found: {}", command.email());
                
                // Записываем неудачную попытку для несуществующего пользователя
                securityService.recordAuthenticationFailure(
                    command.clientIp(), 
                    command.email(), 
                    null, 
                    AuthenticationFailedEvent.FailureReason.USER_NOT_FOUND
                );
                
                return AuthResult.LoginResult.failure(
                    "Пользователь не найден", 
                    "USER_NOT_FOUND"
                );
            }
            
            // Проверяем лимиты аутентификации ПЕРЕД проверкой пароля
            UserSecurityApplicationService.ValidationResult validationResult = 
                securityService.validateAuthenticationAttempt(command.clientIp(), command.email(), user.getId());
            
            if (!validationResult.isAllowed()) {
                log.warn("Authentication validation failed for user: {} from IP: {}, reason: {}", 
                    command.email(), command.clientIp(), validationResult.getReason());
                
                return AuthResult.LoginResult.failure(
                    validationResult.getReason(), 
                    validationResult.getType().name()
                );
            }
            
            // Проверка активности пользователя
            if (!user.isActive()) {
                log.warn("User is inactive: {}", command.email());
                
                securityService.recordAuthenticationFailure(
                    command.clientIp(), 
                    command.email(), 
                    user.getId(), 
                    AuthenticationFailedEvent.FailureReason.ACCOUNT_DISABLED
                );
                
                return AuthResult.LoginResult.failure(
                    "Аккаунт пользователя неактивен", 
                    "USER_INACTIVE"
                );
            }
            
            // Проверка пароля
            if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
                log.warn("Invalid password for user: {}", command.email());
                
                // Записываем неудачную попытку через новый сервис
                securityService.recordAuthenticationFailure(
                    command.clientIp(), 
                    command.email(), 
                    user.getId(), 
                    AuthenticationFailedEvent.FailureReason.INVALID_CREDENTIALS
                );
                
                return AuthResult.LoginResult.failure(
                    "Неверный пароль", 
                    "INVALID_PASSWORD"
                );
            }
            
            // Обновляем время последнего входа
            user.updateLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            // Генерируем токен сессии
            String sessionToken = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24); // 24 часа
            
            // Сбрасываем лимиты аутентификации при успешном входе
            securityService.resetAuthenticationLimits(command.email(), user.getId());
            
            // Публикуем событие успешной аутентификации
            UserAuthenticatedEvent authEvent = new UserAuthenticatedEvent(
                user.getId(), 
                command.email(), 
                command.clientIp(), 
                sessionToken
            );
            eventPublisher.publish(authEvent);
            
            log.info("Authentication successful for user: {}", command.email());
            
            return AuthResult.LoginResult.success(
                user, 
                sessionToken, 
                expiresAt, 
                UUID.randomUUID().toString() // Session ID
            );
            
        } catch (Exception e) {
            log.error("Authentication error for user {}: {}", command.email(), e.getMessage(), e);
            return AuthResult.LoginResult.failure(
                "Ошибка аутентификации", 
                "AUTHENTICATION_ERROR"
            );
        }
    }
} 