package alg.coyote001.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import alg.coyote001.service.TelegramAuthService;
import alg.coyote001.service.UserService;
import alg.coyote001.dto.TelegramAuthDto;
import alg.coyote001.dto.UserRegistrationDto;
import alg.coyote001.entity.User;
import alg.coyote001.security.RateLimitService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import org.springframework.security.web.csrf.CsrfToken;

/**
 * REST контроллер для обработки аутентификации и авторизации пользователей.
 * Поддерживает стандартную аутентификацию по логину/паролю и авторизацию через Telegram.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final TelegramAuthService telegramAuthService;
    private final UserService userService;
    private final RateLimitService rateLimitService;

    public AuthController(AuthenticationManager authenticationManager, TelegramAuthService telegramAuthService, UserService userService, RateLimitService rateLimitService) {
        this.authenticationManager = authenticationManager;
        this.telegramAuthService = telegramAuthService;
        this.userService = userService;
        this.rateLimitService = rateLimitService;
    }

    /**
     * Получение CSRF токена для защиты от CSRF атак.
     * 
     * @param request HTTP запрос
     * @return ResponseEntity с CSRF токеном и именем заголовка
     */
    @GetMapping("/csrf")
    public ResponseEntity<?> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            return ResponseEntity.ok(new CsrfResponse(csrfToken.getToken(), csrfToken.getHeaderName()));
        }
        return ResponseEntity.ok(new CsrfResponse(null, "X-XSRF-TOKEN"));
    }

    /**
     * Аутентификация пользователя по логину и паролю.
     * Включает защиту от превышения лимита попыток входа по IP адресу.
     * 
     * @param request данные для входа (логин и пароль)
     * @param httpRequest HTTP запрос для получения IP адреса
     * @return ResponseEntity с результатом аутентификации
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        // Rate limiting по IP адресу
        String clientIp = getClientIpAddress(httpRequest);
        if (!rateLimitService.tryConsumeAuth(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse("Слишком много попыток входа. Попробуйте позже."));
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            httpRequest.getSession(true);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            httpRequest.getSession().setAttribute("SPRING_SECURITY_CONTEXT", new SecurityContextImpl(authentication));
            
            // Сбрасываем лимит при успешной авторизации
            rateLimitService.resetAuthLimit(clientIp);
            
            return ResponseEntity.ok().build();
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Неверные учетные данные"));
        }
    }

    /**
     * Регистрация нового пользователя.
     * Автоматически назначает роль USER всем новым пользователям.
     * 
     * @param registrationDto данные для регистрации
     * @param httpRequest HTTP запрос для получения IP адреса
     * @return ResponseEntity с результатом регистрации
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDto registrationDto, HttpServletRequest httpRequest) {
        // Rate limiting по IP адресу
        String clientIp = getClientIpAddress(httpRequest);
        if (!rateLimitService.tryConsumeAuth(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse("Слишком много попыток регистрации. Попробуйте позже."));
        }
        
        try {
            // Регистрация пользователя с автоматическим назначением роли USER
            alg.coyote001.dto.UserDto userDto = userService.registerUser(registrationDto);
            
            // Сбрасываем лимит при успешной регистрации
            rateLimitService.resetAuthLimit(clientIp);
            
            // Возвращаем данные созданного пользователя
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistrationResponse(
                    convertToSimpleUserDto(userDto),
                    "Пользователь успешно зарегистрирован с ролью USER"
                ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            String username = userDetails.getUsername();
            // roles без префикса ROLE_
            var roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .toList();
            // email можно получить через UserService, если нужно
            return ResponseEntity.ok(new UserDto(username, null, roles));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknown principal type");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/telegram")
    public ResponseEntity<?> telegramAuth(@Valid @RequestBody TelegramAuthDto telegramData, HttpServletRequest httpRequest) {
        // Rate limiting по IP адресу для Telegram авторизации
        String clientIp = getClientIpAddress(httpRequest);
        if (!rateLimitService.tryConsumeAuth(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse("Слишком много попыток авторизации. Попробуйте позже."));
        }
        
        try {
            // Аутентификация через Telegram
            User user = telegramAuthService.authenticateUser(telegramData);
            
            // Создание Spring Security Authentication
            var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
            
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername() != null ? user.getUsername() : user.getTelegramId().toString(),
                null,
                authorities
            );
            
            // Установка сессии
            httpRequest.getSession(true);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            httpRequest.getSession().setAttribute("SPRING_SECURITY_CONTEXT", new SecurityContextImpl(authentication));
            
            // Сбрасываем лимит при успешной авторизации
            rateLimitService.resetAuthLimit(clientIp);
            
            // Возвращаем данные пользователя
            return ResponseEntity.ok(new TelegramAuthResponse(
                convertToUserDto(user),
                "Успешная авторизация через Telegram"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    private UserDto convertToUserDto(User user) {
        var roles = user.getRoles().stream()
            .map(role -> role.getName())
            .collect(Collectors.toList());
            
        return new UserDto(
            user.getUsername() != null ? user.getUsername() : user.getTelegramId().toString(),
            user.getEmail(),
            roles
        );
    }
    
    private UserDto convertToSimpleUserDto(alg.coyote001.dto.UserDto userDto) {
        return new UserDto(
            userDto.getUsername(),
            userDto.getEmail(),
            userDto.getRoleNames().stream().toList()
        );
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    public static class CsrfResponse {
        public String token;
        public String headerName;
        public CsrfResponse(String token, String headerName) {
            this.token = token;
            this.headerName = headerName;
        }
    }

    public static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class UserDto {
        public String username;
        public String email;
        public java.util.List<String> roles;
        public UserDto(String username, String email, java.util.List<String> roles) {
            this.username = username;
            this.email = email;
            this.roles = roles;
        }
    }
    
    public static class TelegramAuthResponse {
        public UserDto user;
        public String message;
        public TelegramAuthResponse(UserDto user, String message) {
            this.user = user;
            this.message = message;
        }
    }
    
    public static class RegistrationResponse {
        public UserDto user;
        public String message;
        public RegistrationResponse(UserDto user, String message) {
            this.user = user;
            this.message = message;
        }
    }
    
    public static class ErrorResponse {
        public String message;
        public ErrorResponse(String message) {
            this.message = message;
        }
    }
} 