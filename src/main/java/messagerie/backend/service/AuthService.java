package messagerie.backend.service;

import messagerie.backend.dto.AuthDtos.AuthResponse;
import messagerie.backend.dto.AuthDtos.LoginRequest;
import messagerie.backend.dto.AuthDtos.RegisterRequest;
import messagerie.backend.dto.AuthDtos.UserResponse;
import messagerie.backend.exception.UnauthorizedException;
import messagerie.backend.security.JwtTokenProvider;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            UserResponse user = userService.findByEmail(request.email());
            
            if (user.blocked()) {
                throw new UnauthorizedException("User account is blocked");
            }

            if (!userService.verifyPassword(request.email(), request.password())) {
                throw new UnauthorizedException("Invalid email or password");
            }

            userService.updateLastLogin(user.id());

            String accessToken = jwtTokenProvider.generateToken(request.email(), user.role());
            String refreshToken = jwtTokenProvider.generateRefreshToken(request.email());
            
            return new AuthResponse(accessToken, refreshToken, user, user.twoFactorEnabled(), "Bearer", 86400000);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    public AuthResponse register(RegisterRequest request) {
        UserResponse user = userService.register(
            request.firstName(),
            request.lastName(),
            request.email(),
            request.password(),
            request.phone(),
            request.twoFactorEnabled()
        );

        String accessToken = jwtTokenProvider.generateToken(request.email(), user.role());
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.email());
        
        return new AuthResponse(accessToken, refreshToken, user, user.twoFactorEnabled(), "Bearer", 86400000);
    }

    public List<UserResponse> users() {
        return userService.findAll();
    }

    public UserResponse findByEmail(String email) {
        return userService.findByEmail(email);
    }

    public UserResponse setBlocked(long id, boolean blocked) {
        return userService.setBlocked(id, blocked);
    }

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        UserResponse user = userService.findByEmail(email);

        String newAccessToken = jwtTokenProvider.generateToken(email, user.role());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

        return new AuthResponse(newAccessToken, newRefreshToken, user, user.twoFactorEnabled(), "Bearer", 86400000);
    }
}

