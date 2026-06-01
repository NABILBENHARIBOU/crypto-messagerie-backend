package messagerie.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_blocks", indexes = {
    @Index(name = "idx_block_hash", columnList = "block_hash", unique = true),
    @Index(name = "idx_previous_hash", columnList = "previous_hash")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String blockHash;

    @Column(nullable = false)
    private String previousHash;

    @Column(nullable = false)
    private Long blockIndex;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String data;

    @Column(nullable = false)
    private Long timestamp;

    @Column(nullable = false)
    private Long nonce;

    @Column(nullable = false)
    private Integer difficulty;

    @Column(columnDefinition = "TEXT")
    private String minerAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        timestamp = System.currentTimeMillis() / 1000;
    }
}
