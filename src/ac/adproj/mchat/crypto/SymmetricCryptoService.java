package ac.adproj.mchat.crypto;

import java.security.InvalidKeyException;

/**
 * Represents a symmetric crypto service that can be used to encrypt text messages,
 * and produce Base64 result.
 *
 * @author Andy Cheung
 */
public interface SymmetricCryptoService {
    /**
     * Encrypt a text message to Base64 string.
     *
     * @param message The raw text message.
     * @return The encrypted Base64 result.
     * @throws InvalidKeyException If the key belong to the service is invalid.
     */
    String encryptMessageToBase64String(String message) throws InvalidKeyException;

    /**
     * Decrypt the encrypted Base64 message to raw message.
     * @param base64Message The encrypted Base64 result.
     * @return Decrypted Message.
     * @throws InvalidKeyException If the key belong to the service is invalid.
     */
    String decryptMessageFromBase64String(String base64Message) throws InvalidKeyException;
}
