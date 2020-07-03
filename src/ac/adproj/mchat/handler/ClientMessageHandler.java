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

import java.net.SocketAddress;
import java.util.function.Consumer;

import ac.adproj.mchat.model.Protocol;
import ac.adproj.mchat.ui.CommonDialogs;

import static ac.adproj.mchat.handler.MessageType.*;

/**
 * 客户端消息处理类。
 * 
 * @author Andy Cheung
 */
public class ClientMessageHandler implements Handler {
    private Consumer<Boolean> logoffCallback;

    public ClientMessageHandler(Consumer<Boolean> logoffCallback) {
        super();
        this.logoffCallback = logoffCallback;
    }

    @Override
    public String handleMessage(String message, SocketAddress address) {

        switch (getMessageType(message)) {
            case INCOMING_MESSAGE:
                message = message.replace(Protocol.MESSAGE_HEADER_LEFT_HALF, "")
                        .replace(Protocol.MESSAGE_HEADER_RIGHT_HALF, "")
                        .replace(Protocol.MESSAGE_HEADER_MIDDLE_HALF, ": ");
                break;

            case REGISTER:
                message = "";
                break;

            case LOGOFF:
                logoffCallback.accept(true);
                message = "Server closed the connection.";
                break;

            case INVALID_KEY:
                logoffCallback.accept(false);

                message = "The key you specified is NOT valid, please re-open the " +
                        "app and specify the same key as server configuration.";

                CommonDialogs.errorDialog(message);

                break;

            case UNKNOWN:
            default:
                message = "";
                break;
        }

        return message;
    }
}