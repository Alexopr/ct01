package com.ct01.user.application.query;

import com.ct01.core.domain.UserId;

/**
 * Query для получения пользователя по ID
 */
public record GetUserQuery(UserId userId) {
    
    public GetUserQuery {
        if (userId == null) {
            throw new IllegalArgumentException("UserId не может быть null");
        }
    }
} 
