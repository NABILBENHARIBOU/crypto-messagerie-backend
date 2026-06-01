package messagerie.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
    ) {
    }

    public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        @NotBlank String password,
        String phone,
        boolean twoFactorEnabled
    ) {
    }

    public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user,
        boolean twoFactorRequired,
        String tokenType,
        long expiresIn
    ) {
    }

    public record UserResponse(
        long id,
        String firstName,
        String lastName,
        String email,
        String role,
        boolean twoFactorEnabled,
        boolean blocked
    ) {
    }
}
