package messagerie.backend.controller;

import messagerie.backend.dto.AdminDtos.AdminStatsResponse;
import messagerie.backend.dto.AdminDtos.SecurityLogResponse;
import messagerie.backend.dto.AuthDtos.UserResponse;
import messagerie.backend.service.AdminService;
import messagerie.backend.service.AuthService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    private final AuthService authService;

    public AdminController(AdminService adminService, AuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> stats() {
        return ResponseEntity.ok(adminService.stats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> users() {
        return ResponseEntity.ok(authService.users());
    }

    @PutMapping("/users/{id}/block")
    public ResponseEntity<Map<String, Object>> blockUser(@PathVariable long id) {
        UserResponse user = authService.setBlocked(id, true);
        return ResponseEntity.ok(Map.of("id", user.id(), "blocked", user.blocked()));
    }

    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<Map<String, Object>> unblockUser(@PathVariable long id) {
        UserResponse user = authService.setBlocked(id, false);
        return ResponseEntity.ok(Map.of("id", user.id(), "blocked", user.blocked()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/logs")
    public ResponseEntity<Page<SecurityLogResponse>> logs(Pageable pageable) {
        return ResponseEntity.ok(adminService.logs(pageable));
    }
}
