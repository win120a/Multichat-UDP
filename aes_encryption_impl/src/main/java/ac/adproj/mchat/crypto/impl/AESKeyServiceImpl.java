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

package ac.adproj.mchat.crypto.impl;

import ac.adproj.mchat.crypto.key.AbstractSymmetricKeyService;

import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * AES Implementation of Key Service.
 * 
 * @author Andy Cheung
 */
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
