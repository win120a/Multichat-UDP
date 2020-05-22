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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import ac.adproj.mchat.listener.ServerListener;
import ac.adproj.mchat.web.res.WebClientLoader;

public class WebStarter implements AutoCloseable {
    private static ServerListener listener;

    private Server server = null;
    private Thread serverThread;
    
    @SuppressWarnings("serial")
    public static class WebSocketHandlerFacade extends WebSocketServlet {
        @Override
        public void configure(WebSocketServletFactory arg0) {
            arg0.register(WebSocketHandler.class);
        }
    }
    
    @SuppressWarnings("serial")
    public static class PageRedirector extends HttpServlet {
        @Override
        public void service(HttpServletRequest req, HttpServletResponse resp) {
            resp.setHeader("Location", req.getLocalAddr() + "/acmcs");
            resp.setStatus(HttpServletResponse.SC_FOUND);
            
            try {
                resp.flushBuffer();
            } catch (IOException e) {
                // ignore
            }
        }
    }
    
    public void start(int port) throws Exception {
        serverThread = new Thread(() -> {
            server = new Server(port);
            
            WebAppContext webapp = new WebAppContext();
            webapp.setContextPath("/acmcs");
            webapp.setWar(WebClientLoader.getWebappWarPath());
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
