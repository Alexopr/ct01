package alg.coyote001.service;

import alg.coyote001.model.User;
import alg.coyote001.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import alg.coyote001.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import alg.coyote001.dto.UserUpdateDto;
import alg.coyote001.model.Role;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, Validator validator, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Override
    public User createUser(User user) {
        validate(user);
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DataIntegrityViolationException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DataIntegrityViolationException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    @CachePut(value = "users", key = "#id")
    public User updateUser(Long id, UserUpdateDto dto) {
        User existing = getUserById(id);
        if (dto.getUsername() != null && !existing.getUsername().equals(dto.getUsername()) && userRepository.existsByUsername(dto.getUsername())) {
            throw new DataIntegrityViolationException("Username already exists");
        }
        if (dto.getEmail() != null && !existing.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new DataIntegrityViolationException("Email already exists");
        }
        if (dto.getUsername() != null) existing.setUsername(dto.getUsername());
        if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
        if (dto.getRoles() != null) existing.setRoles(dto.getRoles().stream().map(Role::valueOf).collect(java.util.stream.Collectors.toSet()));
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        validate(existing);
        return userRepository.save(existing);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword);
    }

    private void validate(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
} 