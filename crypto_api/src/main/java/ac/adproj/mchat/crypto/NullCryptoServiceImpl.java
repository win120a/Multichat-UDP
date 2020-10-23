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
import java.security.InvalidKeyException;

/**
 * The Null implementation of SymmetricCryptoService, which always returns the original string.
 *
 * @author Andy Cheung
 */
public class NullCryptoServiceImpl implements SymmetricCryptoService {
    @Override
    public String encryptMessageToBase64String(String message) throws InvalidKeyException {
        return message;
    }

    @Override
    public String decryptMessageFromBase64String(String base64Message) throws InvalidKeyException, BadPaddingException {
        return base64Message;
    }
}
