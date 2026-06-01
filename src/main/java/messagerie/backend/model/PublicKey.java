package messagerie.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "public_keys", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_key_algorithm", columnList = "key_algorithm")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String publicKeyPem;

    @Column(nullable = false)
    private String keyAlgorithm;

    @Column(nullable = false)
    private String keyFormat;

    @Column(nullable = false)
    private Integer keySize;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean active;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        active = true;
    }
}
