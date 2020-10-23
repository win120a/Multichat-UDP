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
 * Constants of protocol.
 *  
 * @author Andy Cheung
 */
public final class ProtocolStrings {

    /**
     * NO-OP Constructor.
     */
    private ProtocolStrings() {
        throw new AssertionError("No 'ProtocolStrings' instance for you! ");
    }

    /**
     * UDP Chatting server port.
     */
    public static final int SERVER_PORT = 10240;
    
    /**
     * User name query service port.
     */
    public static final int SERVER_CHECK_DUPLICATE_PORT = 10241;
    
    /**
     * Default port of UDP client.
     */
    public static final int CLIENT_DEFAULT_PORT = 10242;
    
    /**
     * Size of NIO buffers.
     */
    public static final int BUFFER_SIZE = 1024;
    
    // << CONNECT >>(UUID)>>>>(Name)<< CONNECT >>

    /**
     * <p>The left half of user registering message.</p>
     * <br />
     * <p>Message format: << CONNECT >>(UUID)>>>>(Name)<< CONNECT >></p>
     */
    public static final String CONNECTING_GREET_LEFT_HALF = "<< CONNECT >>";

    /**
     * <p>The middle half of user registering message.</p>
     * <br />
     * <p>Message format: << CONNECT >>(UUID)>>>>(Name)<< CONNECT >></p>
     */
    public static final String CONNECTING_GREET_MIDDLE_HALF = ">>>>>";

    /**
     * <p>The right half of user registering message.</p>
     * <br />
     * <p>Message format: << CONNECT >>(UUID)>>>>(Name)<< CONNECT >></p>
     */
    public static final String CONNECTING_GREET_RIGHT_HALF = "<< CONNECT >>";

    /**
     * <p>The header of user logoff message.</p>
     * <br />
     * <p>Message format: << DISCONNECT >>(UUID)</p>
     */
    public static final String NOTIFY_LOGOFF_HEADER = "<< LOGOFF >>";

    // << MESSAGE >>> <<<< (UUID) >>>> << MESSAGE >> (messageContent)

    /**
     * <p>Left part of protocol string of incoming message.</p>
     * <br />
     * <p>Format: << MESSAGE >>> <<<< (UUID) >>>> << MESSAGE >> (messageContent)</p>
     */
    public static final String MESSAGE_HEADER_LEFT_HALF = "<< MESSAGE >>> <<<<";

    /**
     * <p>Middle part of protocol string of incoming message.</p>
     * <br />
     * <p>Format: << MESSAGE >>> <<<< (UUID) >>>> << MESSAGE >> (messageContent)</p>
     */
    public static final String MESSAGE_HEADER_MIDDLE_HALF = ">>>>>";

    /**
     * <p>Right part of protocol string of incoming message.</p>
     * <br />
     * <p>Format: << MESSAGE >>> <<<< (UUID) >>>> << MESSAGE >> (messageContent)</p>
     */
    public static final String MESSAGE_HEADER_RIGHT_HALF = " << MESSAGE >>";

    /**
     * The debug signal. Currently, when server listener receive the message, it prints the user list.
     */
    public static final String DEBUG_MODE_STRING = "/// DEBUG ///";
    
    /**
     * 服务器广播用 UUID。
     */
    public static final String BROADCAST_MESSAGE_UUID = "SERVER";
    
    // <<< DUP ? >>> (Name)
    /**
     * 用户名查询服务请求头。
     */
    public static final String CHECK_DUPLICATE_REQUEST_HEADER = "<<< DUP ? >>> ";
    
    /**
     * 用户名查询服务响应 - 用户名已经占用
     */
    public static final String USER_NAME_DUPLICATED = ">>> DUPLICATED <<< ";
    
    /**
     * 用户名查询服务响应 - 用户名没有占用
     */
    public static final String USER_NAME_NOT_EXIST = "<<< Clear >>>";

    /**
     * Header of WebSocket user registering message.
     */
    public static final String WEBSOCKET_UUID_HEADER = "<WS><<";

    /**
     * Tail of WebSocket user registering message.
     */
    public static final String WEBSOCKET_UUID_TAIL = ">>";

    // << IK >> (UUID)
    /**
     * 服务器通知客户端密钥出错的信号字
     */
    public static final String INVALID_KEY_NOTIFYING_STRING_HEADER = "<< INVALID_KEY >> ";
}
