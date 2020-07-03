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
     * 用户名查重请求消息
     */
    USERNAME_QUERY_REQUEST,

    /**
     * 用于向客户端通知非法密钥的信号字
     */
    INVALID_KEY,
    
    /**
     * 未知
     */
    UNKNOWN;
    
    /**
     * 获取消息类型（对象）。
     * 
     * @param message 协议消息
     * @return 对应的 MessageType 对象。
     */
    public static MessageType getMessageType(String message) {
        if (message.startsWith(Protocol.CONNECTING_GREET_LEFT_HALF)) {
            return REGISTER;
        } else if (message.startsWith(Protocol.DEBUG_MODE_STRING)) {
            return DEBUG;
        } else if (message.startsWith(Protocol.NOTIFY_LOGOFF_HEADER)) {
            return LOGOFF;
        } else if (message.startsWith(Protocol.MESSAGE_HEADER_LEFT_HALF)) {
            return INCOMING_MESSAGE;
        } else if (message.startsWith(Protocol.CHECK_DUPLICATE_REQUEST_HEADER)) {
            return USERNAME_QUERY_REQUEST;
        } else if (message.startsWith(Protocol.INVALID_KEY_NOTIFYING_STRING_HEADER)) {
            return INVALID_KEY;
        }
        return MessageType.UNKNOWN;
    }

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
            case USERNAME_QUERY_REQUEST:
                ret = of("username", message.replace(Protocol.CHECK_DUPLICATE_REQUEST_HEADER, ""));
                break;
            case UNKNOWN:
            default:
                ret = Collections.emptyMap();
                break;
        }

        return ret;
    }

    /**
     * 根据一对参数返回只读 Map。（类似于 Java 9 中 Map.of 方法的作用）
     * @param <K> 键类型
     * @param <V> 值类型
     * @param k1 第一个键 
     * @param v1 第一个值
     * @return 内含 [k1, v1] 一个元素的只读的 Map
     */
    private <K, V> Map<K, V> of(K k1, V v1) {
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
    private <K, V> Map<K, V> of(K k1, V v1, K k2, V v2) {
        HashMap<K, V> hm = new HashMap<>(2);

        hm.put(k1, v1);
        hm.put(k2, v2);

        return Collections.unmodifiableMap(hm);
    }
}
