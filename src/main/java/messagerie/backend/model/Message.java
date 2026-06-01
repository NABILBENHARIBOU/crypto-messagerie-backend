package messagerie.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_sender_id", columnList = "sender_id"),
    @Index(name = "idx_recipient_id", columnList = "recipient_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String encryptedContent;

    @Column(nullable = false)
    private String encryptionAlgorithm;

    @Column(columnDefinition = "BLOB")
    private byte[] iv;

    @Column(columnDefinition = "BLOB")
    private byte[] authTag;

    @Column
    private String mediaUrl;

    @Column
    private String mediaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime readAt;

    @Column
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false)
    private Boolean isFavorite = false;

    @Column
    private LocalDateTime editedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = MessageStatus.SENT;
        isDeleted = false;
        isFavorite = false;
    }

    public enum MessageStatus {
        SENT, DELIVERED, READ, FAILED, DELETED
    }
}
