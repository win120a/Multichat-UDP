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

package ac.adproj.mchat.model;

/**
 * 连接监听器接口。
 * 
 * @author Andy Cheung
 * @since 2020/4/26
 * @see ProtocolStrings
 * @see AutoCloseable
 */
public interface Listener extends AutoCloseable {
    /**
     * 向指定端发送信息。
     * 
     * @param message 消息内容
     * @param uuid 客户端标识 （UUID）
     */
    void sendMessage(String message, String uuid);

    /**
     * 向指定端发送协议信息。
     * 
     * @param text 协议消息体
     * @param uuid 客户端标识 （UUID）
     */
    void sendCommunicationData(String text, String uuid);
    
    /**
     * 确定在端间是否有连接。
     * 
     * @return 如在端间有连接，返回true
     */
    boolean isConnected();
}
