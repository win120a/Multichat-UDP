/*
    Copyright (C) 2011-2020 Andy Cheung

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package ac.adproj.mchat.crypto.key;

import ac.adproj.mchat.util.CommonSpiFactory;

import java.io.IOException;
import java.security.Key;

/**
 * Represents a symmetric key service that can generate, save and retrieve keys based on file storage.
 *
 * @author Andy Cheung
 */
public interface SymmetricKeyService {
    /**
     * Generates a secret key.
     *
     * @return The generated key.
     */
    Key generateKey();

    /**
     * Generates a secret key and save it to a file.
     *
     * @param fileName Target file name.
     * @return The generated key.
     * @throws IOException If I/O Error occurred.
     */
    Key generateKeyAndStoreToFile(String fileName) throws IOException;

    /**
     * Stores specified secret key to a file.
     *
     * @param key      The key object.
     * @param fileName Target file name.
     * @throws IOException If I/O Error occurred.
     */
    void storeKeyToFile(Key key, String fileName) throws IOException;

    /**
     * Reads key from specified file.
     *
     * @param fileName The file name.
     * @return The key.
     * @throws IOException If I/O Error occurred.
     */
    Key readKeyFromFile(String fileName) throws IOException;

    static SymmetricKeyService getInstance() {
        return CommonSpiFactory.getServiceImplementation(SymmetricKeyService.class, NullSymmetricKeyServiceImpl.class);
    }
}
