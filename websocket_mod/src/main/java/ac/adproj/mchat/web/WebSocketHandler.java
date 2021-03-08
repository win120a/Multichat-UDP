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

package ac.adproj.mchat.web;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import ac.adproj.mchat.handler.MessageType;
import ac.adproj.mchat.handler.MessageTypeConstants;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import ac.adproj.mchat.listener.ServerListener;
import ac.adproj.mchat.model.ProtocolStrings;
import ac.adproj.mchat.service.MessageDistributor;
import ac.adproj.mchat.service.UserManager;
import ac.adproj.mchat.service.MessageDistributor.SubscriberCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ac.adproj.mchat.handler.MessageType.*;

/**
 * WebSocket message (connection) handler.
 * 
 * @author Andy Cheung
 * @since 2020/5/18
 */
public class WebSocketHandler implements WebSocketListener {
    private Session session;
    private String uuid;
    private String nickname;

    private static Set<WebSocketHandler> connections;

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketHandler.class);
    
    public static boolean isConnected() {
        return !connections.isEmpty();
    }

    static {
        connections = Collections.synchronizedSet(new HashSet<>(16));
        nameBindings = new ConcurrentHashMap<>(16);
        MessageDistributor.getInstance().registerSubscriber(new WebSocketBridge());
    }

    public WebSocketHandler() {
        uuid = UUID.randomUUID().toString();
    }

    
    public static class WebSocketBridge implements SubscriberCallback {
        private static final Logger WS_BRIDGE_LOG = LoggerFactory.getLogger(WebSocketBridge.class);
        @Override
        public void onMessageReceived(String uiMessage) {
            String name = uiMessage.split("\\s*:\\s*")[0];
            
            for (WebSocketHandler conn : connections) {
                if (conn.nickname.equals(name) || conn.uuid.equals(name)) {
                    continue;
                }
                
                try {
                    conn.session.getRemote().sendString(uiMessage);
                } catch (IOException e) {
                    WS_BRIDGE_LOG.error(String.format("Bridge - Send Message failed. [To UUID = %s]", conn.uuid), e);
                }
            }
        }
    }

    // UUID, Name
    private static Map<String, String> nameBindings;

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        /* only interested in text messages */
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        connections.remove(this);
        nameBindings.remove(uuid);
        UserManager.getInstance().undoReserveName(nickname);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        LOG.error(String.format("WebSocket protocol error. [UUID=%s]", uuid), cause);
    }

    @Override
    public void onWebSocketText(String message) {
        switch (MessageType.getMessageType(message)) {
            case DEBUG:
                break;

            case INCOMING_MESSAGE:
                String protoMsg = message.replace(uuid, nickname);
                
                try {
                    MessageDistributor.getInstance().sendRawProtocolMessage(protoMsg);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();

                    LOG.error(String.format("Error in sending message to Message Distributor. [UUID=%s]", uuid), e1);
                }
                
                broadcastMessage(protoMsg);
                break;

            case REGISTER:
                Map<String, String> result = REGISTER.tokenize(message);

                if (UserManager.getInstance().reserveName(result.get(MessageTypeConstants.UUID))) {
                    nameBindings.put(result.get(MessageTypeConstants.UUID), result.get(MessageTypeConstants.USERNAME));
                }

                nickname = result.get(MessageTypeConstants.USERNAME);

                break;

            case USERNAME_QUERY_REQUEST:
                String name = USERNAME_QUERY_REQUEST.tokenize(message).get(MessageTypeConstants.USERNAME);

                try {
                    session.getRemote()
                            .sendString(UserManager.getInstance().containsName(name) ? ProtocolStrings.USER_NAME_DUPLICATED
                                    : ProtocolStrings.USER_NAME_NOT_EXIST);
                } catch (IOException e) {
                    LOG.error(String.format("User query protocol error. [UUID=%s]", uuid), e);
                }

                break;

            case UNKNOWN:
            default:
                broadcastMessage(message);
                break;
        }
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        connections.add(this);

        try {
            session.getRemote().sendString(ProtocolStrings.WEBSOCKET_UUID_HEADER + uuid + ProtocolStrings.WEBSOCKET_UUID_TAIL);
        } catch (IOException e) {
            LOG.error(String.format("WebSocket protocol error. [UUID=%s]", uuid), e);
        }
    }

    private void broadcastMessage(String message) {
        if ((session != null) && (session.isOpen())) {
            for (WebSocketHandler h : connections) {
                if (h != this) {
                    try {
                        h.session.getRemote().sendString(message);
                    } catch (IOException e) {
                        LOG.error(String.format("Send WebSocket message error. [UUID=%s]", uuid), e);
                    }
                }
            }
        }
        
        try {
            ServerListener.getInstance().sendCommunicationData(message, ProtocolStrings.BROADCAST_MESSAGE_UUID);
        } catch (IOException e) {
            LOG.error(String.format("Send UDP message error. [UUID=%s]", uuid), e);
        }
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        WebSocketHandler other = (WebSocketHandler) obj;
        return Objects.equals(uuid, other.uuid);
    }
}
