package com.ct01.user.domain;

import com.ct01.core.domain.AggregateRoot;
import com.ct01.core.domain.DomainEvent;
import com.ct01.core.domain.Email;
import com.ct01.core.domain.UserId;
import com.ct01.shared.exception.DomainException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * User Aggregate Root.
 * Инкапсулирует всю бизнес-логику, связанную с пользователем.
 */
public class User implements AggregateRoot<UserId> {
    
    private UserId id;
    private Username username;
    private Email email;
    private Password password;
    private String fullName;
    private String firstName;
    private String lastName;
    private Long telegramId;
    private String telegramUsername;
    private String photoUrl;
    private Long authDate;
    private Set<Role> roles;
    private UserStatus status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Для доменных событий
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();
    
    // Конструктор для создания нового пользователя
    private User(Username username, Email email, Password password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.status = UserStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.roles = Set.of(); // Пустой набор ролей по умолчанию
        
        checkInvariants();
        
        // Добавляем событие создания пользователя
        addEvent(new UserCreatedEvent(this.id, username.value(), email.value()));
    }
    
    // Конструктор для восстановления из базы данных
    public User(UserId id, Username username, Email email, Password password, 
                String fullName, String firstName, String lastName, 
                Long telegramId, String telegramUsername, String photoUrl, Long authDate,
                Set<Role> roles, UserStatus status, LocalDateTime lastLoginAt,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.telegramId = telegramId;
        this.telegramUsername = telegramUsername;
        this.photoUrl = photoUrl;
        this.authDate = authDate;
        this.roles = roles != null ? roles : Set.of();
        this.status = status;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        
        checkInvariants();
    }
    
    /**
     * Создать нового пользователя
     */
    public static User create(Username username, Email email, String rawPassword, String fullName) {
        // Валидируем сырой пароль
        Password.validateRawPassword(rawPassword);
        
        // Здесь должно быть хэширование пароля через сервис
        // Пока создаем с placeholder'ом
        Password hashedPassword = Password.fromHash("$2a$10$placeholder_hash_will_be_replaced");
        
        return new User(username, email, hashedPassword, fullName);
    }
    
    /**
     * Активировать пользователя
     */
    public void activate() {
        if (!status.canBeActivated()) {
            throw DomainException.invalidState("User", status.name(), "activate");
        }
        
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        
        addEvent(new UserActivatedEvent(this.id, this.username.value()));
        checkInvariants();
    }
    
    /**
     * Заблокировать пользователя
     */
    public void block(String reason) {
        if (!status.canBeBlocked()) {
            throw DomainException.invalidState("User", status.name(), "block");
        }
        
        this.status = UserStatus.BLOCKED;
        this.updatedAt = LocalDateTime.now();
        
        addEvent(new UserBlockedEvent(this.id, this.username.value(), reason));
        checkInvariants();
    }
    
        /**
     * Обновить информацию о последнем входе
     */
    public void recordLogin() {
        if (!status.canLogin()) {
            throw DomainException.invalidState("User", status.name(), "login");
        }

        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        addEvent(new UserLoggedInEvent(this.id, this.username.value()));
    }
    
    /**
     * Обновить время последнего входа (для использования в security)
     */
    public void updateLastLoginAt(LocalDateTime loginTime) {
        if (loginTime == null) {
            loginTime = LocalDateTime.now();
        }
        
        this.lastLoginAt = loginTime;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Изменить пароль
     */
    public void changePassword(String currentRawPassword, String newRawPassword) {
        // Здесь должна быть проверка текущего пароля через сервис
        Password.validateRawPassword(newRawPassword);
        
        // Здесь должно быть хэширование нового пароля
        this.password = Password.fromHash("$2a$10$new_placeholder_hash");
        this.updatedAt = LocalDateTime.now();
        
        addEvent(new UserPasswordChangedEvent(this.id, this.username.value()));
        checkInvariants();
    }
    
    /**
     * Обновить профиль пользователя
     */
    public void updateProfile(String fullName, String firstName, String lastName) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            this.fullName = fullName.trim();
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            this.firstName = firstName.trim();
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            this.lastName = lastName.trim();
        }
        
        this.updatedAt = LocalDateTime.now();
        checkInvariants();
    }
    
    /**
     * Связать с Telegram аккаунтом
     */
    public void linkTelegram(Long telegramId, String telegramUsername, String photoUrl, Long authDate) {
        if (telegramId == null || telegramId <= 0) {
            throw new IllegalArgumentException("Telegram ID должен быть положительным числом");
        }
        
        this.telegramId = telegramId;
        this.telegramUsername = telegramUsername;
        this.photoUrl = photoUrl;
        this.authDate = authDate;
        this.updatedAt = LocalDateTime.now();
        
        addEvent(new UserTelegramLinkedEvent(this.id, this.username.value(), telegramId));
        checkInvariants();
    }
    
    /**
     * Назначить роли пользователю
     */
    public void assignRoles(Set<Role> newRoles) {
        if (newRoles == null) {
            throw new IllegalArgumentException("Роли не могут быть null");
        }
        
        this.roles = Set.copyOf(newRoles);
        this.updatedAt = LocalDateTime.now();
        
        addEvent(new UserRolesChangedEvent(this.id, this.username.value(), 
            newRoles.stream().map(Role::getName).toList()));
        checkInvariants();
    }
    
    /**
     * Проверить, есть ли у пользователя определенное право
     */
    public boolean hasPermission(String permissionName) {
        return roles.stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(permission -> permission.getName().equals(permissionName));
    }
    
    /**
     * Проверить, есть ли у пользователя определенная роль
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }
    
    @Override
    public UserId getId() {
        return id;
    }
    
    @Override
    public void checkInvariants() {
        if (username == null) {
            throw DomainException.invariantViolated("User должен иметь username");
        }
        if (email == null) {
            throw DomainException.invariantViolated("User должен иметь email");
        }
        if (password == null) {
            throw DomainException.invariantViolated("User должен иметь пароль");
        }
        if (status == null) {
            throw DomainException.invariantViolated("User должен иметь статус");
        }
        if (roles == null) {
            throw DomainException.invariantViolated("User должен иметь набор ролей (может быть пустым)");
        }
        if (createdAt == null) {
            throw DomainException.invariantViolated("User должен иметь дату создания");
        }
        if (updatedAt == null) {
            throw DomainException.invariantViolated("User должен иметь дату обновления");
        }
        if (updatedAt.isBefore(createdAt)) {
            throw DomainException.invariantViolated("Дата обновления не может быть раньше даты создания");
        }
    }
    
    @Override
    public List<DomainEvent> getUncommittedEvents() {
        return Collections.unmodifiableList(uncommittedEvents);
    }
    
    @Override
    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }
    
    @Override
    public void addEvent(DomainEvent event) {
        uncommittedEvents.add(event);
    }
    
    // Геттеры
    public Username getUsername() { return username; }
    public Email getEmail() { return email; }
    public Password getPassword() { return password; }
    public String getPasswordHash() { return password != null ? password.getValue() : null; }
    public String getFullName() { return fullName; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Long getTelegramId() { return telegramId; }
    public String getTelegramUsername() { return telegramUsername; }
    public String getPhotoUrl() { return photoUrl; }
    public Long getAuthDate() { return authDate; }
    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }
    public UserStatus getStatus() { return status; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
} 
