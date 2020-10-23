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

/**
 * Providing some methods to generate immutable collections. (Intended for backward capability)
 *
 * @author Andy Cheung
 */
public final class CollectionUtils {
    private CollectionUtils() { }

    /**
     * Returns an empty map.
     *
     * @param <K> Type of key.
     * @param <V> Type of value.
     * @return An immutable empty map.
     */
    public static <K, V> Map<K, V> mapOf() {
        return Collections.emptyMap();
    }

    /**
     * Returns an empty map.
     *
     * @param <K> Type of key.
     * @param <V> Type of value.
     * @return An immutable empty map.
     */
    public static <K, V> Map<K, V> emptyMap() {
        return Collections.emptyMap();
    }

    /**
     * Returns a read-only Map according to parameters (like Map.of in Java 9).
     *
     * @param <K> Type of key.
     * @param <V> Type of value.
     * @param k1 The first key.
     * @param v1 The first value.
     * @return An read-only map that contains only one element [(K1, V1)].
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        HashMap<K, V> hm = new HashMap<>(1);

        hm.put(k1, v1);

        return Collections.unmodifiableMap(hm);
    }

    /**
     * Returns a read-only Map according to parameters (like Map.of in Java 9).
     *
     * @param <K> Type of key.
     * @param <V> Type of value.
     * @param k1 The first key.
     * @param v1 The first value.
     * @param k2 The second key.
     * @param v2 The second value.
     * @return An read-only map that contains two elements [(K1, V1), (K2, V2)].
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        HashMap<K, V> hm = new HashMap<>(2);

        hm.put(k1, v1);
        hm.put(k2, v2);

        return Collections.unmodifiableMap(hm);
    }
}
