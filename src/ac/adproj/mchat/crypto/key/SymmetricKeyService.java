package ac.adproj.mchat.crypto.key;

import java.io.IOException;
import java.security.Key;

/**
 * Represents a symmetric key service that can generate, save and retrieve keys based on file storage.
 *
 * @author Andy Cheung
 */
public interface SymmetricKeyService {
    /**
     * Generate a secret key.
     *
     * @return The generated key.
     */
    Key generateKey();

    /**
     * Generate a secret key and save it to a file.
     *
     * @param fileName Target file name.
     * @return The generated key.
     * @throws IOException If I/O Error occurred.
     */
    Key generateKeyAndStoreToFile(String fileName) throws IOException;

    /**
     * Store specified secret key to a file.
     * @param key The key object.
     * @param fileName Target file name.
     * @throws IOException If I/O Error occurred.
     */
    void storeKeyToFile(Key key, String fileName) throws IOException;

    /**
     * Read key from specified file.
     *
     * @param fileName The file name.
     * @return The key.
     * @throws IOException If I/O Error occurred.
     */
    Key readKeyFromFile(String fileName) throws IOException;
}
