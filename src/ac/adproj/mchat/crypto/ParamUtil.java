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
 * 加密算法参数工具类
 *
 * @author Andy Cheung
 */
public final class ParamUtil {
    private ParamUtil() { throw new UnsupportedOperationException("No instance! "); }

    /**
     * 根据 UUID 字符串计算 IV 值。
     * @param str 字符串
     * @param length IV 的长度
     * @return 计算过的初始化向量
     * @implNote 直接取前 length 位，使用 String 的 hashCode 算法得出对应位 Hash，之后填充。
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
