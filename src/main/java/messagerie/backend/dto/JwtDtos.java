package messagerie.backend.dto;

import jakarta.validation.constraints.NotBlank;

public final class JwtDtos {
    private JwtDtos() {
    }

    public record RefreshTokenRequest(
        @NotBlank String token
    ) {
    }

    public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
    ) {
    }

    public record TokenValidationResponse(
        boolean valid,
        String email,
        String role
    ) {
    }
}
