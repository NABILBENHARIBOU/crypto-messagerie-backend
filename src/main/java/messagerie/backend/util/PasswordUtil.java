package messagerie.backend.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    private static final int SALT_LENGTH = 16;

    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

            // Combine salt and hashed password, then encode to Base64
            byte[] saltAndPassword = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, saltAndPassword, 0, salt.length);
            System.arraycopy(hashedPassword, 0, saltAndPassword, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(saltAndPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            byte[] decoded = Base64.getDecoder().decode(hashedPassword);
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(decoded, 0, salt, 0, SALT_LENGTH);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedInput = md.digest(password.getBytes(StandardCharsets.UTF_8));

            for (int i = 0; i < hashedInput.length; i++) {
                if (hashedInput[i] != decoded[SALT_LENGTH + i]) {
                    return false;
                }
            }
            return true;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
