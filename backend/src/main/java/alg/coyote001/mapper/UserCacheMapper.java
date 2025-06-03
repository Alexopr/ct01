package alg.coyote001.mapper;

import alg.coyote001.dto.UserCacheDto;
import alg.coyote001.entity.User;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Маппер для преобразования User в UserCacheDto для кэширования.
 */
@Component
public class UserCacheMapper {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public UserCacheDto toCache(User user) {
        if (user == null) {
            return null;
        }
        Set<String> roles = user.getRoles() != null
            ? user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet())
            : null;
        return new UserCacheDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            roles,
            user.getCreatedAt() != null ? user.getCreatedAt().format(ISO_FORMATTER) : null,
            user.getUpdatedAt() != null ? user.getUpdatedAt().format(ISO_FORMATTER) : null,
            "1.0"
        );
    }
    
    // Note: We don't convert back from cache to entity
    // Cache is read-only, entity data comes from database
} 