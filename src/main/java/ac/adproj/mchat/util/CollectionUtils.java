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

package ac.adproj.mchat.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CollectionUtils {
    private CollectionUtils() { }

    /**
     * 返回一个空 Map。
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 内含 [k1, v1] 一个元素的只读的 Map
     */
    public static <K, V> Map<K, V> mapOf() {
        return Collections.emptyMap();
    }

    /**
     * 返回一个空 Map。
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 内含 [k1, v1] 一个元素的只读的 Map
     */
    public static <K, V> Map<K, V> emptyMap() {
        return Collections.emptyMap();
    }

    /**
     * 根据一对参数返回只读 Map。（类似于 Java 9 中 Map.of 方法的作用）
     * @param <K> 键类型
     * @param <V> 值类型
     * @param k1 第一个键 
     * @param v1 第一个值
     * @return 内含 [k1, v1] 一个元素的只读的 Map
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        HashMap<K, V> hm = new HashMap<>(1);

        hm.put(k1, v1);

        return Collections.unmodifiableMap(hm);
    }

    /**
     * 根据两对参数返回只读 Map。（类似于 Java 9 中 Map.of 方法的作用）
     * @param <K> 键类型
     * @param <V> 值类型
     * @param k1 第一个键 
     * @param v1 第一个值
     * @param k2 第二个键 
     * @param v2 第二个值
     * @return 内含 [k1, v1], [k2, v2] 两个元素的只读的 Map
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        HashMap<K, V> hm = new HashMap<>(2);

        hm.put(k1, v1);
        hm.put(k2, v2);

        return Collections.unmodifiableMap(hm);
    }
}
