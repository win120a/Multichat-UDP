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

package ac.adproj.mchat.ui;

import ac.adproj.mchat.crypto.key.AESKeyServiceImpl;
import ac.adproj.mchat.listener.ServerListener;
import ac.adproj.mchat.model.ProtocolStrings;
import ac.adproj.mchat.service.MessageDistributor;
import ac.adproj.mchat.web.WebServerStarter;
import ac.adproj.mchat.web.WebSocketHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;

/**
 * 服务器端界面。
 * 
 * @author Andy Cheung
 */
public class ServerUI extends BaseChattingUI {
    private ServerListener listener;

    private static final Logger LOG = LoggerFactory.getLogger(ServerUI.class);

    private void initListener(Key key) throws IOException {
        MessageDistributor.getInstance().registerSubscriber(message -> {
            this.getDisplay().asyncExec(() -> appendMessageDisplay(message));
        });
        
        listener = ServerListener.getInstance();

        listener.setKey(key);
    }

    @Override
    protected void handleSendMessage(String text) {
        if (!listener.isConnected() && !WebSocketHandler.isConnected()) {
            MessageDialog.openError(ServerUI.this, "出错", "没有客户端登录。");
            return;
        }
        
        listener.sendMessage(text, ProtocolStrings.BROADCAST_MESSAGE_UUID);
    }

    @Override
    protected void handleLogoff() {
        if (!listener.isConnected()) {
            MessageDialog.openError(ServerUI.this, "出错", "没有客户端登录。");
            return;
        }
        
        try {
            listener.logoffAll();
        } catch (IOException e) {
            LOG.error("Failed to send logoff message", e);
        }
    }

    public static void main(String[] args) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String date = sdf.format(new Date());

        Handler fh = new FileHandler("%t/" + String.format("ACMCS_UDP_S_%s.log", date));
        java.util.logging.Logger.getLogger("").addHandler(fh);

        ServerUI ui = new ServerUI();

        ui.setText(ui.getText() + " - S");
        ui.logoff.setText("注销全部客户端");

        ServerConfigurationDialog.StatusWrapper cfd = ServerConfigurationDialog.showDialog();
        
        if (cfd == null) {
            System.exit(-1);
        }
        
        ui.initListener(new AESKeyServiceImpl().readKeyFromFile(cfd.getKeyFile()));
        
        Display d = ui.getDisplay();
        
        WebServerStarter s = new WebServerStarter();
        s.start(8090);

        ui.open();
        ui.layout();

        while (!ui.isDisposed()) {
            if (!d.readAndDispatch()) {
                d.sleep();
            }
        }
        
        s.close();
        ui.listener.close();
    }

}
