package alg.coyote001.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class TestController {
    
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/password-check")
    public String checkPassword(@RequestParam String password, @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(password, hash);
        log.info("Password check: password='{}', hash starts with='{}', matches={}", 
                password, 
                hash.length() > 10 ? hash.substring(0, 10) : hash, 
                matches);
        return "Password matches: " + matches;
    }
    
    @GetMapping("/encode-password")
    public String encodePassword(@RequestParam String password) {
        String encoded = passwordEncoder.encode(password);
        log.info("Encoded password '{}' to hash starting with: {}", password, encoded.substring(0, 10));
        return "Encoded hash: " + encoded;
    }
} 