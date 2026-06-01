package messagerie.backend.dto;

import java.time.Instant;

public final class AdminDtos {
    private AdminDtos() {
    }

    public record AdminStatsResponse(
        int totalUsers,
        int activeUsers,
        int messagesToday,
        int failedLogins
    ) {
    }

    public record SecurityLogResponse(
        long id,
        String title,
        String description,
        String ipAddress,
        String userAgent,
        String severity,
        Instant createdAt
    ) {
    }
}
