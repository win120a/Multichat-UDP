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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * The AES implementation of SymmetricCryptoService.
 *
 * @author Andy Cheung
 * @implNote This implementation uses CFB mode, and PKCS #5 Padding.
 */
public class AESCryptoServiceImpl implements SymmetricCryptoService {
    private final Key key;
    private final IvParameterSpec ips;

    /**
     * Valid IV byte array length, which is 16.
     */
    private static final int VALID_IV_LENGTH = 16;

    /**
     * Initializes this object with specified key and a random initial vector.
     *
     * @param key The secret key.
     */
    public AESCryptoServiceImpl(Key key) {
        this.key = key;

        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);

        this.ips = new IvParameterSpec(iv);
    }

    /**
     * Initializes this object with specified key and initial vector.
     *
     * @param key The secret key.
     * @param iv The initial vector.
     */
    public AESCryptoServiceImpl(Key key, byte[] iv) {
        if (iv.length != VALID_IV_LENGTH) {
            throw new IllegalArgumentException("IV length should be 16.");
        }

        this.key = key;
        this.ips = new IvParameterSpec(iv);
    }

    /**
     * Shortcut of Cipher.getInstance("AES/CFB/PKCS5Padding").
     *
     * @return The Cipher object.
     */
    private Cipher initCipher() {
        try {
            return Cipher.getInstance("AES/CFB/PKCS5Padding");
        } catch (NoSuchPaddingException | NoSuchAlgorithmException ignored) {
            // Shouldn't happen.
            throw new AssertionError(ignored);
        }
    }

    @Override
    public String encryptMessageToBase64String(String message) throws InvalidKeyException {
        byte[] plainText = message.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = initCipher();

        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, ips);
        } catch (InvalidAlgorithmParameterException ignored) {
            // Shouldn't happen.
            throw new AssertionError(ignored);
        }

        byte[] cipherText;

        try {
            cipherText = cipher.doFinal(plainText);
        } catch (IllegalBlockSizeException | BadPaddingException ignored) {
            // Shouldn't happen in encryption.
            throw new AssertionError(ignored);
        }

        return Base64.getEncoder().encodeToString(cipherText);
    }

    @Override
    public String decryptMessageFromBase64String(String base64Message) throws InvalidKeyException, BadPaddingException {
        Cipher c1 = initCipher();

        try {
            c1.init(Cipher.DECRYPT_MODE, key, ips);
        } catch (InvalidAlgorithmParameterException ignored) {
            // Shouldn't happen, since the IV value is checked by constructor.
            throw new AssertionError(ignored);
        }

        try {
            return StandardCharsets.UTF_8.decode(
                    ByteBuffer.wrap(c1.doFinal(Base64.getDecoder().decode(base64Message)))).toString();
        } catch (IllegalBlockSizeException ignored) {
            // Shouldn't happen.
            throw new AssertionError(ignored);
        }
    }

    public byte[] getIV() {
        return ips.getIV();
    }
}
