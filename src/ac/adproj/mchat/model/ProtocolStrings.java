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
 * 协议字符串常量类。
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
     * 聊天服务器端口。
     */
    public static final int SERVER_PORT = 10240;
    
    /**
     * 用户名查询服务端口。
     */
    public static final int SERVER_CHECK_DUPLICATE_PORT = 10241;
    
    /**
     * 客户端的默认端口。
     */
    public static final int CLIENT_DEFAULT_PORT = 10242;
    
    /**
     * NIO 缓冲区大小。
     */
    public static final int BUFFER_SIZE = 1024;
    
    // << CONNECT >>(UUID)>>>>(Name)<< CONNECT >>
    
    /**
     * 用户注册协议消息的左半部分。
     * 消息格式：&lt;&lt; CONNECT &gt;&gt;(UUID)&gt;&gt;&gt;&gt;(用户名)&lt;&lt; CONNECT &gt;&gt;
     */
    public static final String CONNECTING_GREET_LEFT_HALF = "<< CONNECT >>";
    
    /**
     * 用户注册协议消息的中间部分。
     * 消息格式：&lt;&lt; CONNECT &gt;&gt;(UUID)&gt;&gt;&gt;&gt;(用户名)&lt;&lt; CONNECT &gt;&gt;
     */
    public static final String CONNECTING_GREET_MIDDLE_HALF = ">>>>>";
    
    /**
     * 用户注册协议消息的右半部分。
     * 消息格式：&lt;&lt; CONNECT &gt;&gt;(UUID)&gt;&gt;&gt;&gt;(用户名)&lt;&lt; CONNECT &gt;&gt;
     */
    public static final String CONNECTING_GREET_RIGHT_HALF = "<< CONNECT >>";
    
    /**
     * 客户端通知注销的请求头。
     */
    public static final String NOTIFY_LOGOFF_HEADER = "<< LOGOFF >>";
    
    // << MESSAGE >>> <<<< (UUID) >>>> << MESSAGE >> (messageContent)
    /**
     * 信息包的左半部分。
     * 格式： &lt;&lt; MESSAGE &gt;&gt;&gt; &lt;&lt;&lt;&lt; (UUID) &gt;&gt;&gt;&gt; &lt;&lt; MESSAGE &gt;&gt; (内容)
     */
    public static final String MESSAGE_HEADER_LEFT_HALF = "<< MESSAGE >>> <<<<";
    
    /**
     * 信息包的中间部分。
     * 格式： &lt;&lt; MESSAGE &gt;&gt;&gt; &lt;&lt;&lt;&lt; (UUID) &gt;&gt;&gt;&gt; &lt;&lt; MESSAGE &gt;&gt; (内容)
     */
    public static final String MESSAGE_HEADER_MIDDLE_HALF = ">>>>>";
    
    /**
     * 信息包的右半部分。
     * 格式： &lt;&lt; MESSAGE &gt;&gt;&gt; &lt;&lt;&lt;&lt; (UUID) &gt;&gt;&gt;&gt; &lt;&lt; MESSAGE &gt;&gt; (内容)
     */
    public static final String MESSAGE_HEADER_RIGHT_HALF = " << MESSAGE >>";
    
    /**
     * 调试模式的信号字。目前是让服务器把用户档案打印出来。
     */
    public static final String DEBUG_MODE_STRING = "/// DEBUG ///";
    
    /**
     * 调试模式的信号字。目前是让服务器把用户档案打印出来。
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
     * WebSocket 客户端注册请求头 - 左半
     */
    public static final String WEBSOCKET_UUID_HEADER = "<WS><<";

    /**
     * WebSocket 客户端注册请求头 - 右半
     */
    public static final String WEBSOCKET_UUID_TAIL = ">>";

    // << IK >> (UUID)
    /**
     * 服务器通知客户端密钥出错的信号字
     */
    public static final String INVALID_KEY_NOTIFYING_STRING_HEADER = "<< INVALID_KEY >> ";
}
