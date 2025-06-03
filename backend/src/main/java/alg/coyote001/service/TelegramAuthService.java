package alg.coyote001.service;

import alg.coyote001.dto.TelegramAuthDto;
import alg.coyote001.entity.User;
import alg.coyote001.entity.Role;
import alg.coyote001.repository.UserRepository;
import alg.coyote001.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@Transactional
public class TelegramAuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    @Value("${app.telegram.bot.token:}")
    private String botToken;
    
    public TelegramAuthService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }
    
    /**
     * Аутентифицирует пользователя через Telegram
     */
    public User authenticateUser(TelegramAuthDto telegramData) {
        // 1. Валидация данных от Telegram
        if (!isValidTelegramAuth(telegramData)) {
            throw new RuntimeException("Недействительные данные авторизации Telegram");
        }
        
        // 2. Проверка времени авторизации (не старше 24 часов)
        long currentTime = System.currentTimeMillis() / 1000;
        if (currentTime - telegramData.getAuth_date() > 86400) {
            throw new RuntimeException("Данные авторизации устарели");
        }
        
        // 3. Поиск или создание пользователя
        return findOrCreateUser(telegramData);
    }
    
    /**
     * Валидация hash от Telegram
     */
    private boolean isValidTelegramAuth(TelegramAuthDto authData) {
        if (botToken == null || botToken.isEmpty()) {
            // В режиме разработки пропускаем валидацию
            return true;
        }
        
        try {
            // Создаем строку для проверки hash
            Map<String, String> dataMap = new TreeMap<>();
            dataMap.put("id", authData.getId().toString());
            dataMap.put("auth_date", authData.getAuth_date().toString());
            
            if (authData.getFirst_name() != null) {
                dataMap.put("first_name", authData.getFirst_name());
            }
            if (authData.getLast_name() != null) {
                dataMap.put("last_name", authData.getLast_name());
            }
            if (authData.getUsername() != null) {
                dataMap.put("username", authData.getUsername());
            }
            if (authData.getPhoto_url() != null) {
                dataMap.put("photo_url", authData.getPhoto_url());
            }
            
            // Создаем строку для подписи
            StringBuilder dataString = new StringBuilder();
            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                if (dataString.length() > 0) {
                    dataString.append("\n");
                }
                dataString.append(entry.getKey()).append("=").append(entry.getValue());
            }
            
            // Вычисляем hash
            String computedHash = computeHash(dataString.toString(), botToken);
            return computedHash.equals(authData.getHash());
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Вычисление hash для валидации
     */
    private String computeHash(String data, String botToken) throws NoSuchAlgorithmException, InvalidKeyException {
        // Создаем secret key из bot token
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] secretKey = sha256.digest(botToken.getBytes(StandardCharsets.UTF_8));
        
        // Вычисляем HMAC-SHA256
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        // Конвертируем в hex
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
    
    /**
     * Поиск или создание пользователя
     */
    private User findOrCreateUser(TelegramAuthDto telegramData) {
        Optional<User> existingUser = userRepository.findByTelegramId(telegramData.getId());
        
        if (existingUser.isPresent()) {
            // Обновляем существующего пользователя
            User user = existingUser.get();
            updateUserFromTelegram(user, telegramData);
            return userRepository.save(user);
        } else {
            // Создаем нового пользователя
            User newUser = createUserFromTelegram(telegramData);
            return userRepository.save(newUser);
        }
    }
    
    /**
     * Обновление пользователя данными из Telegram
     */
    private void updateUserFromTelegram(User user, TelegramAuthDto telegramData) {
        user.setTelegramId(telegramData.getId());
        user.setTelegramUsername(telegramData.getUsername());
        user.setFirstName(telegramData.getFirst_name());
        user.setLastName(telegramData.getLast_name());
        user.setPhotoUrl(telegramData.getPhoto_url());
        user.setAuthDate(telegramData.getAuth_date());
        
        // Обновляем username если его нет
        if (user.getUsername() == null && telegramData.getUsername() != null) {
            user.setUsername(telegramData.getUsername());
        }
    }
    
    /**
     * Создание нового пользователя из данных Telegram
     */
    private User createUserFromTelegram(TelegramAuthDto telegramData) {
        User user = new User();
        
        user.setTelegramId(telegramData.getId());
        user.setTelegramUsername(telegramData.getUsername());
        user.setUsername(telegramData.getUsername()); // может быть null
        user.setFirstName(telegramData.getFirst_name());
        user.setLastName(telegramData.getLast_name());
        user.setPhotoUrl(telegramData.getPhoto_url());
        user.setAuthDate(telegramData.getAuth_date());
        
        // Устанавливаем статус активного пользователя
        user.setStatus(User.UserStatus.ACTIVE);
        
        // Инициализируем коллекцию ролей
        user.setRoles(new HashSet<>());
        
        // Автоматическое назначение роли USER
        Role userRole = roleRepository.findByName("USER").orElse(null);
        if (userRole != null) {
            user.getRoles().add(userRole);
        }
        
        return user;
    }
} 