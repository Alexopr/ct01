package alg.coyote001.service;

import alg.coyote001.model.User;
import alg.coyote001.dto.UserUpdateDto;
import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long id);
    User getUserByUsername(String username);
    User createUser(User user);
    User updateUser(Long id, UserUpdateDto dto);
    void deleteUser(Long id);
    List<User> searchUsers(String keyword);
} 