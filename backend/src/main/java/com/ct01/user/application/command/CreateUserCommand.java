package com.ct01.user.application.command;

/**
 * Команда для создания нового пользователя
 */
public record CreateUserCommand(
    String username,
    String email,
    String password,
    String fullName
) {
    
    public CreateUserCommand {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username не может быть пустым");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password не может быть пустым");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("FullName не может быть пустым");
        }
    }
} 
