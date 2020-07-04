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

import ac.adproj.mchat.service.CommonThreadPool;
import ac.adproj.mchat.web.res.WebClientLoader;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket 服务器和 WebSocket 客户端 （嵌入式 Jetty 服务器）的启动代码所在类。
 * 
 * @author Andy Cheung
 * @since 2020/5/24 (originally 2020/5/18)
 */
public class WebServerStarter implements AutoCloseable {
    private Server server = null;
    private Thread serverThread;

    private static final Logger LOG = LoggerFactory.getLogger(WebServerStarter.class);

    /**
     * WebSocket 服务的 (Servlet) 外观类，主要作用是包装 WebSocketHandler 类，供 Jetty 使用。
     * 
     * @author Andy Cheung
     */
    @SuppressWarnings("serial")
    private static final class WebSocketHandlerFacade extends WebSocketServlet {
        @Override
        public void configure(WebSocketServletFactory arg0) {
            arg0.register(WebSocketHandler.class);
        }
    }
    
    /**
     * 启动嵌入式 Jetty 容器。
     * 
     * @param port HTTP 端口
     */
    public void start(int port) {
        CommonThreadPool.execute(() -> {
            serverThread = Thread.currentThread();
            
            server = new Server(port);
            
            WebAppContext webapp = new WebAppContext();
            webapp.setContextPath("/acmcs");
            webapp.setWar(WebClientLoader.getWebappWarPath());
            webapp.addServlet(new ServletHolder(new WebSocketHandlerFacade()), "/wshandler");
            
            server.setHandler(webapp);

            try {
                server.start();
                server.join();
            } catch (Exception e) {
                LOG.error("Error when starting web server.", e);
            }
        }, "Jetty HTTP Server");
    }
    
    public static void main(String[] args) throws Exception {
        try(WebServerStarter i = new WebServerStarter()) {
            i.start(8090);
            i.serverThread.join();
        }
    }

    @Override
    public void close() throws Exception {
        server.stop();
        serverThread.interrupt();
    }
}
