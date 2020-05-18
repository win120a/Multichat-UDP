package ac.adproj.mchat.listener;

import javax.websocket.Session;

import ac.adproj.mchat.model.Protocol;

public class ServerListenerFacade {
    private ServerListener listener;
    
    public ServerListenerFacade(ServerListener listener) {
        this.listener = listener;
    }
    
    public void register(String name, Session session) {
    }
    
    public void sendMessage(String msg) {
        // UDP 端
        listener.sendCommunicationData(msg, Protocol.BROADCAST_MESSAGE_UUID);
    }
}
