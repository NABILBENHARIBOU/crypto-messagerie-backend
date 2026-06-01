package messagerie.backend.controller;

import messagerie.backend.dto.AuthDtos.UserResponse;
import messagerie.backend.exception.UnauthorizedException;
import messagerie.backend.service.AuthService;
import messagerie.backend.service.UserService;
import messagerie.backend.util.SecurityUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        // Users can only access their own profile, except admins
        if (!SecurityUtil.isAdmin() && !id.equals(getUserIdFromEmail())) {
            throw new UnauthorizedException("You don't have permission to access this user");
        }
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        // Users can only access their own email, except admins
        if (!SecurityUtil.isAdmin() && !email.equals(SecurityUtil.getCurrentUserEmail())) {
            throw new UnauthorizedException("You don't have permission to access this user");
        }
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<UserResponse> blockUser(@PathVariable Long id) {
        // Users cannot block/unblock, only admins can
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Only admins can block users");
        }
        return ResponseEntity.ok(userService.setBlocked(id, true));
    }

    @PutMapping("/{id}/unblock")
    public ResponseEntity<UserResponse> unblockUser(@PathVariable Long id) {
        // Users cannot block/unblock, only admins can
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Only admins can unblock users");
        }
        return ResponseEntity.ok(userService.setBlocked(id, false));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Users can only delete their own account, admins can delete anyone
        if (!SecurityUtil.isAdmin() && !id.equals(getUserIdFromEmail())) {
            throw new UnauthorizedException("You can only delete your own account");
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromEmail() {
        String email = SecurityUtil.getCurrentUserEmail();
        if (email != null) {
            UserResponse user = authService.findByEmail(email);
            return user.id();
        }
        throw new UnauthorizedException("User not found");
    }
}
