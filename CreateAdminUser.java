import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilitaire pour créer un utilisateur admin
 * Cet outil génère un hash de mot de passe sécurisé pour insérer manuellement dans la base de données
 */
public class CreateAdminUser {

    private static final int SALT_LENGTH = 16;

    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

            byte[] saltAndPassword = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, saltAndPassword, 0, salt.length);
            System.arraycopy(hashedPassword, 0, saltAndPassword, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(saltAndPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java CreateAdminUser <firstName> <lastName> <email> <password>");
            System.out.println("\nExample: java CreateAdminUser Admin User admin@example.com password123");
            System.exit(1);
        }

        String firstName = args[0];
        String lastName = args[1];
        String email = args[2];
        String password = args[3];

        String passwordHash = hashPassword(password);
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println("\n=== Admin User Creation ===\n");
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name: " + lastName);
        System.out.println("Email: " + email);
        System.out.println("Password Hash: " + passwordHash);
        System.out.println("\nPassword Hash (for database): " + passwordHash);

        System.out.println("\n\n=== SQL INSERT STATEMENT ===\n");
        String sqlInsert = String.format(
            "INSERT INTO user (first_name, last_name, email, password_hash, phone, role, blocked, two_factor_enabled, created_at, updated_at) " +
            "VALUES ('%s', '%s', '%s', '%s', '', 'ADMIN', false, false, '%s', '%s');",
            firstName.replace("'", "\\'"),
            lastName.replace("'", "\\'"),
            email.replace("'", "\\'"),
            passwordHash.replace("'", "\\'"),
            timestamp,
            timestamp
        );

        System.out.println(sqlInsert);
        System.out.println("\n\n=== COPY THE SQL STATEMENT ABOVE AND EXECUTE IN MYSQL ===\n");
    }
}
