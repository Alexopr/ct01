package com.ct01.user.application.usecase;

import com.ct01.core.application.UseCase;
import com.ct01.shared.exception.DomainException;
import com.ct01.user.application.query.GetUserQuery;
import com.ct01.user.domain.User;
import com.ct01.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case для получения пользователя по ID
 */
@Service
@Transactional(readOnly = true)
public class GetUserUseCase implements UseCase<GetUserQuery, User> {
    
    private final UserRepository userRepository;
    
    public GetUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public User execute(GetUserQuery query) {
        return userRepository.findById(query.userId())
            .orElseThrow(() -> DomainException.aggregateNotFound("User", query.userId()));
    }
} 
