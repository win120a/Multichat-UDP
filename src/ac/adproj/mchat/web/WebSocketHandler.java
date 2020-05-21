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
import java.util.Set;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import ac.adproj.mchat.handler.ServerMessageHandler;
import ac.adproj.mchat.listener.ServerListener;
import ac.adproj.mchat.listener.WebSocketBridge;
import ac.adproj.mchat.service.UserManager;

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
        broadcastMessage(message);
        
        switch(WebSocketBridge.getMessageTypeFacade(message)) {
            case DEBUG:
                break;
            case INCOMING_MESSAGE:
                
                break;
            case LOGOFF:
                break;
            case REGISTER:
                break;
            case UNKNOWN:
                break;
            default:
                break;
        }
    }

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        connections.add(this);
        
        broadcastMessage(uuid);
    }
    
    private void broadcastMessage(String message) {
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
}
