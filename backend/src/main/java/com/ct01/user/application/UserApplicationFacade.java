package com.ct01.user.application;

import com.ct01.core.application.ApplicationFacade;
import com.ct01.core.domain.UserId;
import com.ct01.user.api.dto.TelegramAuthDto;
import com.ct01.user.application.command.ActivateUserCommand;
import com.ct01.user.application.command.CreateUserCommand;
import com.ct01.user.application.dto.AuthResult;
import com.ct01.user.application.query.GetUserQuery;
import com.ct01.user.application.usecase.ActivateUserUseCase;
import com.ct01.user.application.usecase.CreateUserUseCase;
import com.ct01.user.application.usecase.GetUserUseCase;
import com.ct01.user.application.usecase.auth.AuthenticateUserUseCase;
import com.ct01.user.application.command.AuthenticateUserCommand;
import com.ct01.user.domain.repository.UserRepository;
import com.ct01.user.domain.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Application Facade для User Domain
 * Предоставляет единую точку входа для всех операций с пользователями
 */
@Service
public class UserApplicationFacade implements ApplicationFacade {
    
    private final CreateUserUseCase createUserUseCase;
    private final ActivateUserUseCase activateUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final UserRepository userRepository;
    
    public UserApplicationFacade(
            CreateUserUseCase createUserUseCase,
            ActivateUserUseCase activateUserUseCase,
            GetUserUseCase getUserUseCase,
            AuthenticateUserUseCase authenticateUserUseCase,
            UserRepository userRepository) {
        this.createUserUseCase = createUserUseCase;
        this.activateUserUseCase = activateUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.userRepository = userRepository;
    }
    
    /**
     * Создает нового пользователя
     */
    public UserId createUser(CreateUserCommand command) {
        return createUserUseCase.execute(command);
    }
    
    /**
     * Активирует пользователя
     */
    public void activateUser(ActivateUserCommand command) {
        activateUserUseCase.execute(command);
    }
    
    /**
     * Получает пользователя по ID
     */
    public User getUser(GetUserQuery query) {
        return getUserUseCase.execute(query);
    }
    
    /**
     * Получает пользователя по ID (упрощенный метод)
     */
    public User getUser(UserId userId) {
        return getUserUseCase.execute(new GetUserQuery(userId));
    }
    
    /**
     * Получает пользователя по ID (возвращает Optional)
     */
    public Optional<User> getUserById(Long id) {
        try {
            User user = getUserUseCase.execute(new GetUserQuery(new UserId(id)));
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Поиск пользователя по email
     */
    public Optional<User> findUserByEmail(String email) {
        try {
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Аутентификация пользователя по email и паролю
     */
    public AuthResult.LoginResult authenticate(String email, String password, String clientIp) {
        try {
            AuthenticateUserCommand command = new AuthenticateUserCommand(email, password, clientIp);
            return authenticateUserUseCase.execute(command);
        } catch (Exception e) {
            return AuthResult.LoginResult.failure("Ошибка аутентификации", "AUTHENTICATION_ERROR");
        }
    }
    
    /**
     * Регистрация нового пользователя
     */
    public AuthResult.RegistrationResult registerUser(String email, String password, String name) {
        // TODO: Реализовать регистрацию
        // Пока возвращаем заглушку для компиляции
        return AuthResult.RegistrationResult.failure("Регистрация не реализована", "NOT_IMPLEMENTED");
    }
    
    /**
     * Аутентификация через Telegram
     */
    public AuthResult.TelegramAuthResult authenticateViaTelegram(TelegramAuthDto telegramData) {
        // TODO: Реализовать Telegram аутентификацию
        // Пока возвращаем заглушку для компиляции
        return AuthResult.TelegramAuthResult.failure("Telegram аутентификация не реализована", "NOT_IMPLEMENTED");
    }
    
    /**
     * Аннулирование сессий пользователя
     */
    public void invalidateUserSessions(Long userId) {
        // TODO: Реализовать аннулирование сессий
        // Пока пустая реализация
    }
} 
