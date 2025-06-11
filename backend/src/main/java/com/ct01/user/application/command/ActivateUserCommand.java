package com.ct01.user.application.command;

import com.ct01.core.domain.UserId;

/**
 * Команда для активации пользователя
 */
public record ActivateUserCommand(UserId userId) {
    
    public ActivateUserCommand {
        if (userId == null) {
            throw new IllegalArgumentException("UserId не может быть null");
        }
    }
} 
