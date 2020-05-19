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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import ac.adproj.mchat.listener.ServerListener;

public class WebStarter implements AutoCloseable {
    private static ServerListener listener;
    
    private static final String chattingHTML = "<!DOCTYPE html>\r\n" + "<html>\r\n" + "<head>\r\n"
            + "    <title>WebSocket client</title>\r\n" + "    <script>\r\n"
            + "        var ws = new WebSocket(\"ws://127.0.0.1:8090/acmcs/wshandler\");\r\n" + "\r\n"
            + "        ws.onopen = function(){\r\n" + "        };\r\n" + "\r\n"
            + "        ws.onmessage = function(message){\r\n"
            + "            document.getElementById(\"chatlog\").textContent += message.data + \"\\n\";\r\n"
            + "        };\r\n" + "        function postToServer(){\r\n"
            + "            ws.send(document.getElementById(\"msg\").value);\r\n"
            + "            document.getElementById(\"msg\").value = \"\";\r\n" + "        }\r\n" + "\r\n"
            + "        function closeConnect(){\r\n" + "            ws.close();\r\n" + "        }\r\n"
            + "    </script>\r\n" + "</head>\r\n" + "<body>\r\n"
            + "    <textarea id=\"chatlog\" readonly></textarea><br/>\r\n"
            + "    <input id=\"msg\" type=\"text\" />\r\n"
            + "    <button type=\"submit\" id=\"sendButton\" onClick=\"postToServer()\">Send!</button>\r\n"
            + "    <button type=\"submit\" id=\"sendButton\" onClick=\"closeConnect()\">End</button>\r\n"
            + "</body>\r\n" + "</body>\r\n" + "</html>";

    private Server server = null;
    private Thread serverThread;
    
    @SuppressWarnings("serial")
    public static class WebSocketHandlerFacade extends WebSocketServlet {
        @Override
        public void configure(WebSocketServletFactory arg0) {
            arg0.register(WebSocketHandler.class);
        }
    }

    public void start(int port) throws Exception {
        serverThread = new Thread(() -> {
            server = new Server(port);
            
            WebAppContext webapp = new WebAppContext();
            webapp.setContextPath("/acmcs");
            webapp.setWar(System.getProperty("user.dir") + "\\webClient.war");
            webapp.addServlet(WebSocketHandlerFacade.class, "/wshandler");
            
            server.setHandler(webapp);

            try {
                server.start();
                server.join();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

    public static void main(String[] args) throws Exception {
        System.out.println(System.getProperty("user.dir"));
        WebStarter i = new WebStarter();
        i.start(8090);
        i.serverThread.join();
        i.close();
    }

    @Override
    public void close() throws Exception {
        server.stop();
    }
}
