package ui.fx;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;

    private PasswordUtil() {
    }

    public static boolean passwordMatches(String password, String saltBase64, String expectedHashBase64) {
        if (password == null || saltBase64 == null || expectedHashBase64 == null) {
            return false;
        }

        try {
            String actualHash = hashPassword(password, saltBase64);
            return MessageDigest.isEqual(
                    actualHash.getBytes("UTF-8"),
                    expectedHashBase64.getBytes("UTF-8")
            );
        } catch (Exception e) {
            System.out.println("Password check failed: " + e.getMessage());
            return false;
        }
    }

    public static String hashPassword(String password, String saltBase64)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }
}
