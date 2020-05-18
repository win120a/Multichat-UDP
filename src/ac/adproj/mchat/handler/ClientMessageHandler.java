package ac.adproj.mchat.handler;

import java.net.SocketAddress;
import java.util.function.Consumer;

import ac.adproj.mchat.model.Protocol;

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
        if (message.startsWith(Protocol.MESSAGE_HEADER_LEFT_HALF)) {
            message = message.replace(Protocol.MESSAGE_HEADER_LEFT_HALF, "")
                    .replace(Protocol.MESSAGE_HEADER_RIGHT_HALF, "")
                    .replace(Protocol.MESSAGE_HEADER_MIDDLE_HALF, ": ");

        } else if (message.startsWith(Protocol.CONNECTING_GREET_LEFT_HALF)) {
            return "";
        } else if (message.startsWith(Protocol.NOTIFY_LOGOFF_HEADER)) {
            forceLogoffCallback.accept(null);
            return "Server closed the connection.";
        }

        return message;
    }
}