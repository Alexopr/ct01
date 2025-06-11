package com.ct01.user.domain;

import com.ct01.core.domain.Email;
import com.ct01.core.domain.Repository;
import com.ct01.core.domain.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Repository интерфейс для User Aggregate.
 * Определяет контракт для доступа к данным пользователей.
 */
public interface UserRepository extends Repository<User, UserId> {
    
    /**
     * Найти пользователя по username
     * @param username имя пользователя
     * @return пользователь, если найден
     */
    Optional<User> findByUsername(Username username);
    
    /**
     * Найти пользователя по email
     * @param email email адрес
     * @return пользователь, если найден
     */
    Optional<User> findByEmail(Email email);
    
    /**
     * Найти пользователя по email (строковый вариант)
     * @param email email адрес как строка
     * @return пользователь, если найден
     */
    default Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return findByEmail(new Email(email));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Найти пользователя по Telegram ID
     * @param telegramId Telegram ID
     * @return пользователь, если найден
     */
    Optional<User> findByTelegramId(Long telegramId);
    
    /**
     * Найти всех пользователей с определенным статусом
     * @param status статус пользователей
     * @return список пользователей
     */
    List<User> findByStatus(UserStatus status);
    
    /**
     * Найти всех пользователей с определенной ролью
     * @param roleName название роли
     * @return список пользователей
     */
    List<User> findByRole(String roleName);
    
    /**
     * Проверить существование пользователя по username
     * @param username имя пользователя
     * @return true, если пользователь существует
     */
    boolean existsByUsername(Username username);
    
    /**
     * Проверить существование пользователя по email
     * @param email email адрес
     * @return true, если пользователь существует
     */
    boolean existsByEmail(Email email);
    
    /**
     * Проверить существование пользователя по Telegram ID
     * @param telegramId Telegram ID
     * @return true, если пользователь существует
     */
    boolean existsByTelegramId(Long telegramId);
    
    /**
     * Получить общее количество пользователей
     * @return количество пользователей
     */
    long count();
    
    /**
     * Получить количество активных пользователей
     * @return количество активных пользователей
     */
    long countByStatus(UserStatus status);
} 
