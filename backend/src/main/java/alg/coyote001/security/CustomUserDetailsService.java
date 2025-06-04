package alg.coyote001.security;

import alg.coyote001.entity.User;
import alg.coyote001.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
        
        log.info("User found: {}, status: {}, roles: {}", 
                user.getUsername(), 
                user.getStatus(), 
                user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()));
        
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
        
        log.info("Authorities granted: {}", authorities);
        
        String password = user.getPassword() != null ? user.getPassword() : "";
        log.info("Password hash length: {}, starts with: {}", 
                password.length(), 
                password.length() > 10 ? password.substring(0, 10) : password);
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                password,
                authorities
        );
    }
} 