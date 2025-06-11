package com.ct01.user.application.usecase;

import com.ct01.core.application.UseCase;
import com.ct01.shared.exception.DomainException;
import com.ct01.user.application.command.ActivateUserCommand;
import com.ct01.user.domain.User;
import com.ct01.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case для активации пользователя
 */
@Service
@Transactional
public class ActivateUserUseCase implements UseCase<ActivateUserCommand, Void> {
    
    private final UserRepository userRepository;
    
    public ActivateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public Void execute(ActivateUserCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> DomainException.aggregateNotFound("User", command.userId()));
        
        user.activate();
        userRepository.save(user);
        
        return null;
    }
} 
