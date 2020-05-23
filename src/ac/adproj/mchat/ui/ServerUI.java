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

import java.io.IOException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import ac.adproj.mchat.listener.ServerListener;
import ac.adproj.mchat.model.Protocol;
import ac.adproj.mchat.service.MessageDistributor;
import ac.adproj.mchat.web.WebStarter;

/**
 * 服务器端界面。
 * 
 * @author Andy Cheung
 */
public class ServerUI extends BaseChattingUI {
    private ServerListener listener;

    private void initListener() throws IOException {
        MessageDistributor.getInstance().registerSubscriber((message) -> {
            this.getDisplay().asyncExec(() -> appendMessageDisplay(message));
        });
        
        listener = ServerListener.getInstance();
    }

    @Override
    protected void handleSendMessage(String text) {
        if (!listener.isConnected()) {
            MessageDialog.openError(ServerUI.this, "出错", "没有客户端登录。");
            return;
        }
        
        listener.sendMessage(text, Protocol.BROADCAST_MESSAGE_UUID);
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
            e.printStackTrace();
            MessageDialog.openError(ServerUI.this, "出错", "注销失败：" + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        ServerUI ui = new ServerUI();

        ui.setText(ui.getText() + " - S");
        ui.logoff.setText("注销全部客户端");

        ui.initListener();
        
        Display d = ui.getDisplay();
        
        WebStarter s = new WebStarter();
        s.start(8090);

        ui.open();
        ui.layout();

        while (!ui.isDisposed()) {
            if (!d.readAndDispatch()) {
                d.sleep();
            }
        }
        
        ui.listener.close();
        s.close();
    }

}
