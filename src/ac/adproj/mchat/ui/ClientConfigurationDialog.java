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

import ac.adproj.mchat.model.ProtocolStrings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;

public class ClientConfigurationDialog extends Shell {
    private final Text serverIP;
    private final Text port;
    private final Text nicknameField;
    private StatusWrapper status;
    private final Text keyFile;

    /**
     * Create the shell.
     *
     * @param display
     */
    public ClientConfigurationDialog(Display display) {
        super(display, SWT.CLOSE | SWT.TITLE | SWT.APPLICATION_MODAL);
        setLayout(new FormLayout());

        Label lblNewLabel = new Label(this, SWT.NONE);
        lblNewLabel.setAlignment(SWT.CENTER);
        FormData fd_lblNewLabel = new FormData();
        fd_lblNewLabel.left = new FormAttachment(0, 10);
        fd_lblNewLabel.top = new FormAttachment(0, 37);
        lblNewLabel.setLayoutData(fd_lblNewLabel);
        lblNewLabel.setText("\u670D\u52A1\u5668 IP");

        serverIP = new Text(this, SWT.BORDER);
        serverIP.setText("127.0.0.1");
        fd_lblNewLabel.right = new FormAttachment(serverIP, -14);
        FormData fd_serverIP = new FormData();
        fd_serverIP.top = new FormAttachment(0, 34);
        fd_serverIP.right = new FormAttachment(100, -10);
        fd_serverIP.left = new FormAttachment(0, 114);
        serverIP.setLayoutData(fd_serverIP);

        Button ok = new Button(this, SWT.NONE);
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int prt = Integer.parseInt(port.getText());

                boolean legalPort = port.getText().matches("\\d+") && prt > 0 && prt <= 65535;
                boolean legalIP = serverIP.getText().matches("\\d.+\\d.+\\d.+\\d+");

                if (legalPort && legalIP && nicknameField.getText().replaceAll("\\s+", "").isEmpty()) {
                    CommonDialogs.errorDialog("三者均不可空白，且前两者要按格式输入！");
                    return;
                }

                status = new StatusWrapper(serverIP.getText(), port.getText(), nicknameField.getText(), keyFile.getText());
                ClientConfigurationDialog.this.setVisible(false);
                dispose();
            }
        });
        FormData fd_ok = new FormData();
        fd_ok.bottom = new FormAttachment(100, -10);
        fd_ok.left = new FormAttachment(0, 161);
        ok.setLayoutData(fd_ok);
        ok.setText("\u786E\u5B9A");

        Button cancel = new Button(this, SWT.NONE);
        cancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dispose();
            }
        });
        fd_ok.right = new FormAttachment(100, -354);
        FormData fd_cancel = new FormData();
        fd_cancel.top = new FormAttachment(ok, 0, SWT.TOP);
        fd_cancel.left = new FormAttachment(ok, 85);
        fd_cancel.right = new FormAttachment(100, -148);
        cancel.setLayoutData(fd_cancel);
        cancel.setText("\u53D6\u6D88");

        Label label = new Label(this, SWT.NONE);
        label.setAlignment(SWT.CENTER);
        label.setText("\u5BA2\u6237\u673A\u7AEF\u53E3");
        FormData fd_label = new FormData();
        fd_label.top = new FormAttachment(lblNewLabel, 34);
        fd_label.right = new FormAttachment(lblNewLabel, 0, SWT.RIGHT);
        fd_label.left = new FormAttachment(0, 10);
        label.setLayoutData(fd_label);

        port = new Text(this, SWT.BORDER);
        port.setText(Integer.toString(ProtocolStrings.CLIENT_DEFAULT_PORT));
        FormData fd_port = new FormData();
        fd_port.top = new FormAttachment(label, -3, SWT.TOP);
        fd_port.left = new FormAttachment(serverIP, 0, SWT.LEFT);
        fd_port.right = new FormAttachment(serverIP, 0, SWT.RIGHT);
        port.setLayoutData(fd_port);

        Label label_1 = new Label(this, SWT.NONE);
        label_1.setAlignment(SWT.CENTER);
        label_1.setText("\u6635\u79F0");
        FormData fd_label_1 = new FormData();
        fd_label_1.top = new FormAttachment(label, 34);
        fd_label_1.right = new FormAttachment(lblNewLabel, 0, SWT.RIGHT);
        fd_label_1.left = new FormAttachment(0, 10);
        label_1.setLayoutData(fd_label_1);

        nicknameField = new Text(this, SWT.BORDER);
        FormData fd_nicknameField = new FormData();
        fd_nicknameField.top = new FormAttachment(label_1, -3, SWT.TOP);
        fd_nicknameField.left = new FormAttachment(serverIP, 0, SWT.LEFT);
        fd_nicknameField.right = new FormAttachment(serverIP, 0, SWT.RIGHT);
        nicknameField.setLayoutData(fd_nicknameField);

        Label label_2 = new Label(this, SWT.NONE);
        label_2.setText("\u5BC6\u94A5\u6587\u4EF6");
        label_2.setAlignment(SWT.CENTER);
        FormData fd_label_2 = new FormData();
        fd_label_2.right = new FormAttachment(lblNewLabel, 0, SWT.RIGHT);
        fd_label_2.left = new FormAttachment(lblNewLabel, 0, SWT.LEFT);
        label_2.setLayoutData(fd_label_2);

        keyFile = new Text(this, SWT.BORDER);
        fd_label_2.top = new FormAttachment(keyFile, 3, SWT.TOP);
        FormData fd_keyFile = new FormData();
        fd_keyFile.top = new FormAttachment(nicknameField, 23);
        fd_keyFile.right = new FormAttachment(cancel, 0, SWT.RIGHT);
        fd_keyFile.left = new FormAttachment(serverIP, 0, SWT.LEFT);
        keyFile.setLayoutData(fd_keyFile);

        Button generateKey = new Button(this, SWT.NONE);
        generateKey.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                KeyGenerationDialog.showDialog();
            }
        });
        FormData fd_generateKey = new FormData();
        fd_generateKey.left = new FormAttachment(keyFile, 6);
        fd_generateKey.top = new FormAttachment(label_2, -5, SWT.TOP);
        generateKey.setLayoutData(fd_generateKey);
        generateKey.setText("\u751F\u6210");

        Button browseKey = new Button(this, SWT.NONE);
        fd_generateKey.right = new FormAttachment(100, -88);
        FormData fd_browseKey = new FormData();
        fd_browseKey.left = new FormAttachment(generateKey, 6);
        fd_browseKey.top = new FormAttachment(label_2, -5, SWT.TOP);
        fd_browseKey.right = new FormAttachment(100, -10);
        browseKey.setLayoutData(fd_browseKey);
        browseKey.setText("\u6D4F\u89C8...");
        createContents();
    }

    public static StatusWrapper showDialog() {
        Display display = Display.getDefault();
        ClientConfigurationDialog shell = new ClientConfigurationDialog(display);
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        return shell.status;
    }

    /**
     * Create contents of the shell.
     */
    protected void createContents() {
        setText("Client Configuration");
        setSize(642, 367);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    class StatusWrapper {
        public final byte[] ip;
        public final int port;
        public final String nickname;
        public final String keyFile;

        public StatusWrapper(String ip, String port, String nickname, String keyFile) {
            super();

            String[] address = ip.split("[.]");

            byte[] addressByteArray = new byte[4];

            for (int i = 0; i < address.length; i++) {
                addressByteArray[i] = (byte) Integer.parseInt(address[i]);
            }

            this.ip = addressByteArray;
            this.port = Integer.parseInt(port);
            this.nickname = nickname;
            this.keyFile = keyFile;
        }
    }
}
