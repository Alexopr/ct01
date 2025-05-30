package alg.coyote001.dto;

import alg.coyote001.model.User;
import alg.coyote001.model.Role;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {
    public static UserDto toDto(User user) {
        if (user == null) return null;
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRoles() != null ? user.getRoles().stream().map(Role::name).collect(Collectors.toSet()) : null,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) return null;
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRoles(dto.getRoles() != null ? dto.getRoles().stream().map(Role::valueOf).collect(Collectors.toSet()) : null);
        user.setCreatedAt(dto.getCreatedAt());
        user.setUpdatedAt(dto.getUpdatedAt());
        return user;
    }
} 