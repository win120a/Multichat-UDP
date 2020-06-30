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

package ac.adproj.mchat.test;

import ac.adproj.mchat.crypto.AESCryptoServiceImpl;
import ac.adproj.mchat.crypto.key.AESKeyServiceImpl;
import ac.adproj.mchat.crypto.key.SymmetricKeyService;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;

import static org.junit.Assert.assertEquals;

public class CryptoServiceTest {
    private static Key key;
    private static SymmetricKeyService ks;
    private static final byte[] iv = new byte[] {-97, 22, 51, 32, -88, 107, -112, -13, -59, 123, -5, 84, -87, 61, -109, -76};
    private static final String MESSAGE = "11111111111";

    @BeforeClass
    public static void test() throws IOException {
        ks = new AESKeyServiceImpl();
        key = ks.generateKeyAndStoreToFile("D:\\keys.key");
    }

    @Test
    public void testEncrypt() throws InvalidKeyException, IOException, BadPaddingException {
        var cs = new AESCryptoServiceImpl(key, iv);

        String cryptoText = cs.encryptMessageToBase64String(MESSAGE);

        Key k2 = ks.readKeyFromFile("D:\\keys.key");

        cs = new AESCryptoServiceImpl(k2, iv);

        assertEquals(MESSAGE, cs.decryptMessageFromBase64String(cryptoText));
    }
}
