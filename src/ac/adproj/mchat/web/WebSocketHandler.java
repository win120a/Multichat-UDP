package ac.adproj.mchat.web;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

public class WebSocketHandler implements WebSocketListener {
    private Session session;
    private String uuid;
    private static Set<WebSocketHandler> connections;

    static {
        connections = Collections.synchronizedSet(new HashSet<>());
    }
    
    {
        uuid = UUID.randomUUID().toString();
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        /* only interested in text messages */
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        connections.remove(this);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace(System.err);
    }

    @Override
    public void onWebSocketText(String message) {
        if ((session != null) && (session.isOpen())) {
            for (WebSocketHandler h : connections) {
                if (h != this) {
                    try {
                        h.session.getRemote().sendString(message);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        connections.add(this);
    }
}
