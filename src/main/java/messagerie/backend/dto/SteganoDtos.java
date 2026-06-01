package messagerie.backend.dto;

public final class SteganoDtos {
    private SteganoDtos() {
    }

    public record SteganoTextRequest(String message, String key) {
    }

    public record SteganoTextResponse(String message, String status) {
    }
}
