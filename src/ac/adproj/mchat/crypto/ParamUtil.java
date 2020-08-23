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

/**
 * Utility class of parameters in crypto services.
 *
 * @author Andy Cheung
 */
public final class ParamUtil {
    private ParamUtil() {
        throw new UnsupportedOperationException("No instance! ");
    }

    /**
     * Calculate the IV value according to UUID String.
     *
     * @param str    The String
     * @param length Length of IV.
     * @return Calculated IV.
     * @implNote Use the String.hashCode() algorithm to generate the hash of a character,
     *          run 'length' times. Then concatenate them.
     */
    public static byte[] getIVFromString(String str, int length) {
        byte[] ivBytes = new byte[length];

        for (int i = 0; i < length; i++) {
            byte val = (byte) (str.charAt(i) * 31);
            val += Math.pow(val, str.length() - 1);
            ivBytes[i] = val;
        }

        return ivBytes;
    }
}
