package org.mcgill.ca.it.security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class KeyCipherUtils {
    private static final String VECTOR = "RandomInitVector";
    private static final String ENCRYPTION = "AES";
    private static final String ENCRYPTION_LIST = "AES/CBC/PKCS5PADDING";

    /**
     * Decrypt an encrypted message with a key.
     *
     * @param key       : the key
     * @param encrypted : the ciphered message
     * @return the decrypted message or null if an error occurred.
     */
    public static String decrypt(String key, String encrypted) {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(VECTOR.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ENCRYPTION);

        Cipher cipher;

        try {
            cipher = Cipher.getInstance(ENCRYPTION_LIST);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));

            return new String(original);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }
}
