package ac.adproj.mchat.web;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import ac.adproj.mchat.protocol.ServerListener;

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
    public static class MessageHandler extends HttpServlet {
        @Override
        public void service(HttpServletRequest request, HttpServletResponse response) {
            response.setContentType("text/html; charset=utf-8");

            // Declare response status code
            response.setStatus(HttpServletResponse.SC_OK);

            // Write back response
            try {
                response.getWriter().print(chattingHTML);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    @SuppressWarnings("serial")
    public static class WebSocketHandlerFacade extends WebSocketServlet {
        @Override
        public void configure(WebSocketServletFactory arg0) {
            // TODO Auto-generated method stub
            arg0.register(WebSocketHandler.class);
        }
        
    }

    public void start(int port) throws Exception {
        serverThread = new Thread(() -> {
            server = new Server(port);
            
            ServletHandler handler = new ServletHandler();
            handler.addServletWithMapping(WebSocketHandlerFacade.class, "/acmcs/wshandler");
            handler.addServletWithMapping(MessageHandler.class, "/acmcs/index.jsp");
            handler.addServletWithMapping(MessageHandler.class, "/acmcs");
            
            server.setHandler(handler);

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
