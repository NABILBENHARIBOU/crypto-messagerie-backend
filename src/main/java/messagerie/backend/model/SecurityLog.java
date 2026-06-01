package messagerie.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_log_type", columnList = "log_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogType logType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String ipAddress;

    @Column
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogSeverity severity;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum LogType {
        LOGIN, LOGOUT, FAILED_LOGIN, TWO_FA_ENABLED, TWO_FA_DISABLED,
        USER_CREATED, USER_DELETED, USER_BLOCKED, USER_UNBLOCKED,
        PASSWORD_CHANGED, ENCRYPTION_KEY_GENERATED, SUSPICIOUS_ACTIVITY
    }

    public enum LogSeverity {
        INFO, WARNING, CRITICAL
    }
}
