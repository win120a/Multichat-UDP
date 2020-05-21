package ac.adproj.mchat.listener;

import ac.adproj.mchat.handler.MessageType;
import ac.adproj.mchat.handler.ServerMessageHandler;
import ac.adproj.mchat.service.UserManager;

public class WebSocketBridge {
    private static ServerListener listener;
    private static ServerMessageHandler NULL_MSG_HANDLER = new ServerMessageHandler(null);
    
    public static MessageType getMessageTypeFacade(String message) {
        return NULL_MSG_HANDLER.getMessageType(message);
    }
    
    public static boolean reserveName(String name) {
        return UserManager.getInstance().reserveName(name);
    }
}