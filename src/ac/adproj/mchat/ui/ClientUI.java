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

import static ac.adproj.mchat.ui.CommonDialogs.errorDialog;
import static ac.adproj.mchat.ui.CommonDialogs.inputDialog;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.eclipse.swt.widgets.Display;

import ac.adproj.mchat.listener.ClientListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端界面。
 * 
 * @author Andy Cheung
 */
public class ClientUI extends BaseChattingUI {
    private ClientListener listener;

    private static final Logger LOG = LoggerFactory.getLogger(ClientUI.class);
    
    public void initListener(byte[] ipAddress, int port, String userName, String keyFile) throws IOException {
        if (!ClientListener.checkNameDuplicates(ipAddress, userName)) {
            listener = new ClientListener(this, this::appendMessageDisplay, ipAddress, port, userName, keyFile);
        } else {
            errorDialog("用户名重复了！");
            initListener(ipAddress, port, getUserName(), keyFile);
        }
    }

    @Override
    protected void handleSendMessage(String text) {
        listener.sendMessage(text);
        appendMessageDisplay(listener.getUserName() + ": " + text);
    }

    @Override
    protected void handleLogoff() {
        try {
            listener.logoff();
        } catch (IOException e) {
            LOG.error("Logoff failed.", e);

            getDisplay().syncExec(() -> {
                CommonDialogs.errorDialog("注销异常：" + e.getMessage());
            });
        }
        
        send.setEnabled(false);
        logoff.setEnabled(false);
    }
    
    /**
     * 向用户获取用户名。
     * 
     * @return 用户名
     */
    private static String getUserName() {
        return inputDialog("请输入用户名", "必须输入用户名！");
    }

    public static void main(String[] args) throws IOException {
        ClientUI ui = new ClientUI();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String date = sdf.format(new Date());

        Handler fh = new FileHandler("%t/" + String.format("ACMCS_UDP_C_%s.log", date));
        java.util.logging.Logger.getLogger("").addHandler(fh);

        ui.setText(ui.getText() + " - C");
        
        ui.open();
        
        ClientConfigurationDialog.StatusWrapper cfd = ClientConfigurationDialog.showDialog();
        
        if (cfd == null) {
            System.exit(-1);
        }
        
        ui.initListener(cfd.ip, cfd.port, cfd.nickname, cfd.keyFile);

        Display d = ui.getDisplay();
        
        ui.setActive();

        while (!ui.isDisposed()) {
            if (!d.readAndDispatch()) {
                d.sleep();
            }
        }
        
        try {
            ui.listener.close();
        } catch (Exception ignored) {
            // ignore
        }
    }

}
