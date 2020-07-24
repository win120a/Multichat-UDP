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

import ac.adproj.mchat.listener.ServerListener;
import ac.adproj.mchat.model.ProtocolStrings;
import ac.adproj.mchat.model.User;
import ac.adproj.mchat.service.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.SocketAddress;
import java.util.Map;

import static ac.adproj.mchat.handler.MessageType.*;

/**
 * 服务端消息处理类。
 *
 * @author Andy Cheung
 * @since 2020/4/26
 */
public class ServerMessageHandler implements Handler {
    private static final Logger LOG = LoggerFactory.getLogger(ServerMessageHandler.class);
    private final UserManager userManager = UserManager.getInstance();
    private final ServerListener listener;

    public ServerMessageHandler(ServerListener listener) {
        super();
        this.listener = listener;
    }

    @Override
    public String handleMessage(String message, SocketAddress address) {
        switch (getMessageType(message)) {
            case REGISTER:
                // 用户注册
                Map<String, String> data = REGISTER.tokenize(message);
                User userObject = new User(data.get(MessageTypeConstants.UUID),
                                            address, data.get(MessageTypeConstants.USERNAME));

                userManager.register(userObject);

                LOG.debug("[UDP] Registering, UUID = {}, Nickname = {}", userObject.getUuid(), userObject.getName());

                return "Client: " + data.get(MessageTypeConstants.UUID) +
                            " (" + data.get(MessageTypeConstants.USERNAME) + ") Connected.";

            case DEBUG:
                // 调试模式
                LOG.debug("[UDP] Users: {}", userManager);
                return "";

            case NOTIFY_LOGOFF:
                // 客户端请求注销
                SoftReference<String> targetUuid = new SoftReference<>(NOTIFY_LOGOFF.tokenize(message).get(MessageTypeConstants.UUID));

                try {
                    LOG.debug("[UDP] Disconnecting, UUID = {}.", targetUuid.get());
                    listener.logoff(targetUuid.get());
                } catch (IOException e) {
                    LOG.warn(String.format("[UDP] Disconnecting failed, UUID = %s.", targetUuid.get()), e);
                }

                return "Client: " + message.replace(ProtocolStrings.NOTIFY_LOGOFF_HEADER, "") + " Disconnected.";

            case INCOMING_MESSAGE:
                // 收到消息

                Map<String, String> msgData = INCOMING_MESSAGE.tokenize(message);

                if (msgData.size() == 0) {
                    return "";
                }

                String fromUuid = msgData.get(MessageTypeConstants.UUID);
                String messageText = msgData.get(MessageTypeConstants.MESSAGE_TEXT);

                if (!userManager.containsUuid(fromUuid)) {
                    // 不接收没有注册机器的任何信息
                    return "";
                }

                String nameOnlyProtocolMessage = message.replace(fromUuid, userManager.getName(fromUuid));

                for (User u : userManager.userProfileValueSet()) {
                    if (!fromUuid.equals(u.getUuid())) {
                        listener.sendCommunicationData(nameOnlyProtocolMessage, u.getUuid());
                        LOG.debug("Forwarding message to {}, message: {}", u.getUuid(), messageText);
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
}