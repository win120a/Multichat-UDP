package ac.adproj.mchat.crypto.key;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;

public abstract class AbstractSymmetricKeyService implements SymmetricKeyService {

    @Override
    public void storeKeyToFile(Key key, String fileName) throws IOException {
        try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(fileName))) {
            o.writeObject(key);
        }
    }

    @Override
    public Key generateKeyAndStoreToFile(String fileName) throws IOException {
        Key k = this.generateKey();

        storeKeyToFile(k, fileName);
        
        return k;
    }

    @Override
    public Key readKeyFromFile(String fileName) throws IOException {
        try (ObjectInputStream o = new ObjectInputStream(new FileInputStream(fileName));) {
            return (Key) o.readObject();
        } catch (ClassNotFoundException e) {
            // Shouldn't happen.
            throw new AssertionError("No such class for 'Key'! ");
        }
    }

}
