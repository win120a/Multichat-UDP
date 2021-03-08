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

import java.io.IOException;
import java.security.Key;

/**
 * The Null implementation of SymmetricCryptoService, which always returns the original string.
 *
 * @author Andy Cheung
 */
public class NullSymmetricKeyServiceImpl implements SymmetricKeyService {
    @Override
    public Key generateKey() {
        return null;
    }

    @Override
    public Key generateKeyAndStoreToFile(String fileName) throws IOException {
        return null;
    }

    @Override
    public void storeKeyToFile(Key key, String fileName) throws IOException {
        // Do nothing
    }

    @Override
    public Key readKeyFromFile(String fileName) throws IOException {
        return null;
    }
}
