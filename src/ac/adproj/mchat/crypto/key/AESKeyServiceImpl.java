package ac.adproj.mchat.crypto.key;

import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class AESKeyServiceImpl extends AbstractSymmetricKeyService {
    @Override
    public Key generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);

            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException ignore) {
            // Shouldn't happen.
            throw new AssertionError("No such algorithm for AES! ");
        }

    }
}
