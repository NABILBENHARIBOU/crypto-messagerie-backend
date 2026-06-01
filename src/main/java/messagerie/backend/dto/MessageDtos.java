package messagerie.backend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class MessageDtos {
    private MessageDtos() {
    }

    public record SendMessageRequest(
        @NotNull Long recipientId,
        @NotBlank String encryptedContent,
        String encryptionAlgorithm,
        byte[] iv,
        byte[] authTag,
        String mediaUrl,
        String mediaType
    ) {
    }

    public record MessageUpdateRequest(
        @NotBlank String encryptedContent,
        String encryptionAlgorithm,
        byte[] iv,
        byte[] authTag,
        String mediaUrl,
        String mediaType
    ) {
    }

    public record MessageResponse(
        Long id,
        Long senderId,
        Long recipientId,
        String encryptedContent,
        String encryptionAlgorithm,
        String status,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        LocalDateTime editedAt,
        Boolean isFavorite,
        Boolean isDeleted
    ) {
    }
}
