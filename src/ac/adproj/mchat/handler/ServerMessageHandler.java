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

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.SocketAddress;
import ac.adproj.mchat.listener.ServerListener;
import ac.adproj.mchat.model.Protocol;
import ac.adproj.mchat.model.User;
import ac.adproj.mchat.service.UserManager;

import static ac.adproj.mchat.handler.MessageType.*;

/**
 * 服务端消息处理类。
 * 
 * @author Andy Cheung
 * @since 2020/4/26
 */
public class ServerMessageHandler implements Handler {
    private UserManager userManager = UserManager.getInstance();
    private ServerListener listener;

    public ServerMessageHandler(ServerListener listener) {
        super();
        this.listener = listener;
    }

    @Override
    public String handleMessage(String message, SocketAddress address) {
        switch (getMessageType(message)) {
            case REGISTER:
                // 用户注册
                String[] data = message.replace(Protocol.CONNECTING_GREET_LEFT_HALF, "")
                        .replace(Protocol.CONNECTING_GREET_RIGHT_HALF, "").split(Protocol.CONNECTING_GREET_MIDDLE_HALF);

                String uuid = data[0];
                String name = data[1];

                User userObject = new User(uuid, address, name);

                userManager.register(userObject);

                return "Client: " + uuid + " (" + name + ") Connected.";

            case DEBUG:
                // 调试模式
                System.out.println(userManager.toString());
                return "";

            case LOGOFF:
                // 客户端请求注销
                SoftReference<String> targetUuid = new SoftReference<String>(
                        message.replace(Protocol.NOTIFY_LOGOFF_HEADER, ""));

                try {
                    System.out.println("Disconnecting: " + targetUuid.get());
                    listener.logoff(targetUuid.get());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return "Client: " + message.replace(Protocol.NOTIFY_LOGOFF_HEADER, "") + " Disconnected.";

            case INCOMING_MESSAGE:
                // 收到消息
                String[] msgData = message.replace(Protocol.MESSAGE_HEADER_LEFT_HALF, "")
                        .replace(Protocol.MESSAGE_HEADER_RIGHT_HALF, "").split(Protocol.MESSAGE_HEADER_MIDDLE_HALF);

                final int vaildDataArrayLength = 2;

                if (msgData.length < vaildDataArrayLength) {
                    return "";
                }

                String fromUuid = msgData[0];
                String messageText = msgData[1];

                if (!userManager.containsUuid(fromUuid)) {
                    // 不接收没有注册机器的任何信息
                    return "";
                }

                String nameOnlyProtocolMessage = message.replace(fromUuid, userManager.getName(fromUuid));

                for (User u : userManager.userProfileValueSet()) {
                    if (!fromUuid.equals(u.getUuid())) {
                        listener.sendCommunicationData(nameOnlyProtocolMessage, u.getUuid());
                        System.out.println("Forwarding message to " + u.getUuid() + " message: " + messageText);
                    }
                }

                message = userManager.getName(fromUuid) + ": " + messageText;
                break;

            case UNKNOWN:
            default:
                return message;
        }

        return message;
    }

    @Override
    public MessageType getMessageType(String message) {
        if (message.startsWith(Protocol.CONNECTING_GREET_LEFT_HALF)) {
            return MessageType.REGISTER;
        } else if (message.startsWith(Protocol.DEBUG_MODE_STRING)) {
            return MessageType.DEBUG;
        } else if (message.startsWith(Protocol.NOTIFY_LOGOFF_HEADER)) {
            return MessageType.LOGOFF;
        } else if (message.startsWith(Protocol.MESSAGE_HEADER_LEFT_HALF)) {
            return MessageType.INCOMING_MESSAGE;
        }
        return MessageType.UNKNOWN;
    }
}