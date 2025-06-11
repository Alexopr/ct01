package com.ct01.user.application.usecase;

import com.ct01.core.application.UseCase;
import com.ct01.core.domain.Email;
import com.ct01.core.domain.UserId;
import com.ct01.shared.exception.DomainException;
import com.ct01.user.application.command.CreateUserCommand;
import com.ct01.user.domain.User;
import com.ct01.user.domain.UserRepository;
import com.ct01.user.domain.Username;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case для создания нового пользователя
 */
@Service
@Transactional
public class CreateUserUseCase implements UseCase<CreateUserCommand, UserId> {
    
    private final UserRepository userRepository;
    
    public CreateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserId execute(CreateUserCommand command) {
        Username username = Username.of(command.username());
        Email email = Email.of(command.email());
        
        // Проверяем уникальность username и email
        if (userRepository.existsByUsername(username)) {
            throw DomainException.conflict("Пользователь с таким username уже существует");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw DomainException.conflict("Пользователь с таким email уже существует");
        }
        
        // Создаем нового пользователя
        User user = User.create(username, email, command.password(), command.fullName());
        
        // Сохраняем пользователя
        User savedUser = userRepository.save(user);
        
        return savedUser.getId();
    }
} 
