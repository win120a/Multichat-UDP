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

package ac.adproj.mchat.crypto;

import ac.adproj.mchat.util.CommonSpiFactory;

import javax.crypto.BadPaddingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.security.Key;

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
     *
     * @param base64Message The encrypted Base64 result.
     * @return Decrypted Message.
     * @throws InvalidKeyException If the key belong to the service is invalid.
     *
     * @throws BadPaddingException  If a particular padding mechanism is expected for the
     *                              input data but the data is not padded properly.
     *                              Also Thrown if IV or key isn't correct.
     */
    String decryptMessageFromBase64String(String base64Message) throws InvalidKeyException, BadPaddingException;

    static SymmetricCryptoService getInstance(Key key, byte[] iv) {
        Class<? extends SymmetricCryptoService> cls = CommonSpiFactory.getServiceImplementation(SymmetricCryptoService.class, NullCryptoServiceImpl.class).getClass();

        Constructor<? extends SymmetricCryptoService> ctor = null;

        try {
            ctor = cls.getDeclaredConstructor(Key.class, byte[].class);

            return ctor.newInstance(key, iv);
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();

            return new NullCryptoServiceImpl();
        }
    }
}
