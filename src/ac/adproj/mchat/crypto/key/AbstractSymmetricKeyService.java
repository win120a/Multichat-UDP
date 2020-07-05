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

import java.io.*;
import java.security.Key;

/**
 * 密钥服务的抽象模板类。
 * 
 * @author Andy Cheung
 */
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
        try (ObjectInputStream o = new ObjectInputStream(new FileInputStream(fileName))) {
            return (Key) o.readObject();
        } catch (ClassNotFoundException e) {
            // Shouldn't happen.
            throw new AssertionError("No such class for 'Key'! ");
        }
    }

}
