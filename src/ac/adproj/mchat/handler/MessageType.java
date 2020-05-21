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

package ac.adproj.mchat.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ac.adproj.mchat.model.Protocol;

/**
 * 消息类型枚举和消息元素分离器类。
 * 
 * @author Andy Cheung
 * @since 2020/5/19
 */
public enum MessageType {
    /**
     * 用户注册或连接上
     * 
     * data[0] = UUID; data[1] = User name
     */
    REGISTER,

    /**
     * 用户注销或断开
     */
    LOGOFF,

    /**
     * 用户消息
     */
    INCOMING_MESSAGE,

    /**
     * 调试模式
     */
    DEBUG,

    /**
     * 未知
     */
    UNKNOWN;

    /**
     * 消息元素分离方法。
     * 
     * @param message 协议消息
     * @return 分离后的消息元素。
     */
    public Map<String, String> tokenize(String message) {
        Map<String, String> ret = null;

        switch (this) {
            case REGISTER:
                String[] data = message.replace(Protocol.CONNECTING_GREET_LEFT_HALF, "")
                        .replace(Protocol.CONNECTING_GREET_RIGHT_HALF, "").split(Protocol.CONNECTING_GREET_MIDDLE_HALF);

                String uuid = data[0];
                String name = data[1];

                ret = of("uuid", uuid, "name", name);

                break;

            case LOGOFF:
                String targetUuid = message.replace(Protocol.NOTIFY_LOGOFF_HEADER, "");

                ret = of("uuid", targetUuid);
                
                break;

            case INCOMING_MESSAGE:
                String[] msgData = message.replace(Protocol.MESSAGE_HEADER_LEFT_HALF, "")
                        .replace(Protocol.MESSAGE_HEADER_RIGHT_HALF, "").split(Protocol.MESSAGE_HEADER_MIDDLE_HALF);

                final int vaildDataArrayLength = 2;

                if (msgData.length < vaildDataArrayLength) {
                    return Collections.emptyMap();
                }

                String fromUuid = msgData[0];
                String messageText = msgData[1];
                
                ret = of("uuid", fromUuid, "messageText", messageText);
                
                break;
                
            case UNKNOWN:
            default:
                ret = Collections.emptyMap();
                break;
        }

        return ret;
    }

    private <K, V> Map<K, V> of(K k1, V v1) {
        HashMap<K, V> hm = new HashMap<>();

        hm.put(k1, v1);

        return Collections.unmodifiableMap(hm);
    }

    private <K, V> Map<K, V> of(K k1, V v1, K k2, V v2) {
        HashMap<K, V> hm = new HashMap<>();

        hm.put(k1, v1);
        hm.put(k2, v2);

        return Collections.unmodifiableMap(hm);
    }
}
