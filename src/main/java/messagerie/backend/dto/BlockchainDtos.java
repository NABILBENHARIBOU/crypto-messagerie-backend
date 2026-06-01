package messagerie.backend.dto;

import java.time.Instant;

public final class BlockchainDtos {
    private BlockchainDtos() {
    }

    public record MineBlockRequest(String event, String category) {
    }

    public record BlockResponse(
        int index,
        String hash,
        String previousHash,
        Instant timestamp,
        String event,
        long nonce,
        int difficulty,
        boolean valid,
        String category,
        boolean genesis
    ) {
    }

    public record ChainValidationResponse(boolean valid, int blocks) {
    }
}
