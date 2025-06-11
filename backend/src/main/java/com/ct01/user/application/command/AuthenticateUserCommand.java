package com.ct01.user.application.command;

import com.ct01.core.application.Command;

/**
 * Команда для аутентификации пользователя
 */
public record AuthenticateUserCommand(
    String email,
    String password,
    String clientIp
) implements Command {
} 