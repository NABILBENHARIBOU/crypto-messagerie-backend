package messagerie.backend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import messagerie.backend.dto.SteganoDtos.SteganoTextRequest;
import messagerie.backend.dto.SteganoDtos.SteganoTextResponse;

@Service
public class SteganoService {
    private static final byte[] MAGIC = "SCHT1".getBytes(StandardCharsets.US_ASCII);
    private static final int HEADER_BYTES = MAGIC.length + Integer.BYTES;

    public SteganoTextResponse encode(SteganoTextRequest request) {
        String message = request.message() == null ? "" : request.message();
        return new SteganoTextResponse(message, "encoded-demo");
    }

    public SteganoTextResponse decode(SteganoTextRequest request) {
        String key = request.key() == null || request.key().isBlank() ? "no-key" : request.key();
        return new SteganoTextResponse("Decoded secret with key: " + key, "decoded-demo");
    }

    public byte[] encodeImage(MultipartFile imageFile, String message, String key) {
        BufferedImage image = readPng(imageFile);
        byte[] payload = preparePayload(message, key);
        int capacityBits = image.getWidth() * image.getHeight() * 3;

        if (payload.length * Byte.SIZE > capacityBits) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Message is too large for this image capacity"
            );
        }

        BufferedImage encoded = copyAsArgb(image);
        writePayload(encoded, payload);
        return writePng(encoded);
    }

    public SteganoTextResponse decodeImage(MultipartFile imageFile, String key) {
        BufferedImage image = readPng(imageFile);
        byte[] header = readBytes(image, HEADER_BYTES);

        if (!Arrays.equals(Arrays.copyOf(header, MAGIC.length), MAGIC)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No SecureChat payload found in this image");
        }

        int length = ByteBuffer.wrap(header, MAGIC.length, Integer.BYTES).getInt();
        if (length < 0 || length > maxPayloadBytes(image) - HEADER_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid hidden payload length");
        }

        byte[] encryptedPayload = readBytes(image, HEADER_BYTES + length);
        byte[] messageBytes = Arrays.copyOfRange(encryptedPayload, HEADER_BYTES, HEADER_BYTES + length);
        byte[] decoded = applyKeyStream(messageBytes, key);

        return new SteganoTextResponse(new String(decoded, StandardCharsets.UTF_8), "decoded");
    }

    private byte[] preparePayload(String message, String key) {
        byte[] rawMessage = (message == null ? "" : message).getBytes(StandardCharsets.UTF_8);
        byte[] protectedMessage = applyKeyStream(rawMessage, key);

        ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES + protectedMessage.length);
        buffer.put(MAGIC);
        buffer.putInt(protectedMessage.length);
        buffer.put(protectedMessage);
        return buffer.array();
    }

    private BufferedImage readPng(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PNG image is required");
        }

        try {
            BufferedImage image = ImageIO.read(imageFile.getInputStream());
            if (image == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is not a readable image");
            }
            return image;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read uploaded image", exception);
        }
    }

    private BufferedImage copyAsArgb(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.getGraphics().drawImage(source, 0, 0, null);
        return copy;
    }

    private void writePayload(BufferedImage image, byte[] payload) {
        int bitIndex = 0;
        int totalBits = payload.length * Byte.SIZE;

        for (int y = 0; y < image.getHeight() && bitIndex < totalBits; y++) {
            for (int x = 0; x < image.getWidth() && bitIndex < totalBits; x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                int red = (argb >>> 16) & 0xFF;
                int green = (argb >>> 8) & 0xFF;
                int blue = argb & 0xFF;

                red = writeBit(red, payload, bitIndex++);
                if (bitIndex < totalBits) {
                    green = writeBit(green, payload, bitIndex++);
                }
                if (bitIndex < totalBits) {
                    blue = writeBit(blue, payload, bitIndex++);
                }

                image.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
            }
        }
    }

    private int writeBit(int channel, byte[] payload, int bitIndex) {
        int byteIndex = bitIndex / Byte.SIZE;
        int bitInByte = 7 - (bitIndex % Byte.SIZE);
        int bit = (payload[byteIndex] >>> bitInByte) & 1;
        return (channel & 0xFE) | bit;
    }

    private byte[] readBytes(BufferedImage image, int bytesToRead) {
        int totalBits = bytesToRead * Byte.SIZE;
        byte[] output = new byte[bytesToRead];
        int bitIndex = 0;

        for (int y = 0; y < image.getHeight() && bitIndex < totalBits; y++) {
            for (int x = 0; x < image.getWidth() && bitIndex < totalBits; x++) {
                int argb = image.getRGB(x, y);
                int[] channels = {
                    (argb >>> 16) & 0xFF,
                    (argb >>> 8) & 0xFF,
                    argb & 0xFF,
                };

                for (int channel : channels) {
                    if (bitIndex >= totalBits) {
                        break;
                    }
                    int byteIndex = bitIndex / Byte.SIZE;
                    output[byteIndex] = (byte) ((output[byteIndex] << 1) | (channel & 1));
                    bitIndex++;
                }
            }
        }

        return output;
    }

    private int maxPayloadBytes(BufferedImage image) {
        return (image.getWidth() * image.getHeight() * 3) / Byte.SIZE;
    }

    private byte[] writePng(BufferedImage image) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not encode PNG output", exception);
        }
    }

    private byte[] applyKeyStream(byte[] input, String key) {
        if (key == null || key.isBlank()) {
            return input;
        }

        byte[] digest = sha256(key);
        byte[] output = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = (byte) (input[i] ^ digest[i % digest.length]);
        }
        return output;
    }

    private byte[] sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
