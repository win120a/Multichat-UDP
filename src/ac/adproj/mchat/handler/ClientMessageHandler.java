package ac.adproj.mchat.handler;

import java.net.SocketAddress;
import java.util.function.Consumer;

import ac.adproj.mchat.model.Protocol;
import static ac.adproj.mchat.handler.MessageType.*;

/**
 * 客户端消息处理类。
 * 
 * @author Andy Cheung
 */
public class ClientMessageHandler implements Handler {
    private Consumer<Void> forceLogoffCallback;

    public ClientMessageHandler(Consumer<Void> forceLogoffCallback) {
        super();
        this.forceLogoffCallback = forceLogoffCallback;
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
                forceLogoffCallback.accept(null);
                message = "Server closed the connection.";
                break;

            case UNKNOWN:
            default:
                message = "";
                break;
        }

        return message;
    }

    @Override
    public MessageType getMessageType(String message) {
        if (message.startsWith(Protocol.MESSAGE_HEADER_LEFT_HALF)) {
            return INCOMING_MESSAGE;
        } else if (message.startsWith(Protocol.CONNECTING_GREET_LEFT_HALF)) {
            return REGISTER;
        } else if (message.startsWith(Protocol.NOTIFY_LOGOFF_HEADER)) {
            return LOGOFF;
        } else {
            return UNKNOWN;
        }
    }
}