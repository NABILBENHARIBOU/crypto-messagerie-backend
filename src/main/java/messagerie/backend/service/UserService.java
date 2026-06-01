package messagerie.backend.service;

import messagerie.backend.dto.AuthDtos.UserResponse;
import messagerie.backend.exception.ResourceNotFoundException;
import messagerie.backend.exception.UserAlreadyExistsException;
import messagerie.backend.model.User;
import messagerie.backend.repository.UserRepository;
import messagerie.backend.util.PasswordUtil;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse register(String firstName, String lastName, String email, String password, String phone, boolean twoFactorEnabled) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setPhone(phone);
        user.setRole(User.UserRole.USER);
        user.setTwoFactorEnabled(twoFactorEnabled);
        user.setBlocked(false);

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return mapToUserResponse(user);
    }

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return mapToUserResponse(user);
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
            .map(this::mapToUserResponse)
            .toList();
    }

    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public UserResponse setBlocked(Long id, boolean blocked) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setBlocked(blocked);
        User updated = userRepository.save(user);
        return mapToUserResponse(updated);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        userRepository.delete(user);
    }

    public boolean verifyPassword(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return PasswordUtil.verifyPassword(password, user.getPasswordHash());
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRole().name(),
            user.isTwoFactorEnabled(),
            user.isBlocked()
        );
    }
}
