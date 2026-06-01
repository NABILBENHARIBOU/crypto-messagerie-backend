package messagerie.backend.controller;

import jakarta.validation.Valid;
import messagerie.backend.dto.AuthDtos.*;
import messagerie.backend.dto.JwtDtos.*;
import messagerie.backend.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.token());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || !authService.validateToken(token)) {
            return ResponseEntity.ok(new TokenValidationResponse(false, null, null));
        }
        return ResponseEntity.ok(new TokenValidationResponse(false, null, null));
    }

    @GetMapping("/me")
    public ResponseEntity<List<UserResponse>> me() {
        List<UserResponse> users = authService.users();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp() {
        return ResponseEntity.ok(Map.of("verified", true));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}

