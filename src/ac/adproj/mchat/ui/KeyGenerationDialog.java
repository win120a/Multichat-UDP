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
import ac.adproj.mchat.crypto.key.SymmetricKeyService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class KeyGenerationDialog extends Shell {
    private final Text text;

    /**
     * Create the shell.
     *
     * @param display
     */
    public KeyGenerationDialog(Display display) {
        super(display, SWT.CLOSE | SWT.TITLE | SWT.APPLICATION_MODAL);
        setLayout(new FormLayout());

        Button ok = new Button(this, SWT.NONE);
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                SymmetricKeyService sks = new AESKeyServiceImpl();

                try {
                    sks.generateKeyAndStoreToFile(text.getText());
                } catch (IOException e1) {
                    CommonDialogs.errorDialog("I/O 错误");
                }
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

        Label label_2 = new Label(this, SWT.NONE);
        label_2.setText("\u5BC6\u94A5\u6587\u4EF6");
        label_2.setAlignment(SWT.CENTER);
        FormData fd_label_2 = new FormData();
        fd_label_2.left = new FormAttachment(0, 10);
        fd_label_2.top = new FormAttachment(0, 32);
        label_2.setLayoutData(fd_label_2);

        text = new Text(this, SWT.BORDER);
        fd_label_2.right = new FormAttachment(text, -6);
        FormData fd_text = new FormData();
        fd_text.top = new FormAttachment(label_2, -3, SWT.TOP);
        fd_text.left = new FormAttachment(0, 106);
        fd_text.right = new FormAttachment(100, -156);
        text.setLayoutData(fd_text);

        Button browseKey = new Button(this, SWT.NONE);
        FormData fd_browseKey = new FormData();
        fd_browseKey.left = new FormAttachment(text, 19);
        fd_browseKey.right = new FormAttachment(100, -10);
        fd_browseKey.top = new FormAttachment(label_2, -5, SWT.TOP);
        browseKey.setLayoutData(fd_browseKey);
        browseKey.setText("\u6D4F\u89C8...");
        createContents();
    }

    public static void showDialog() {
        Display display = Display.getDefault();
        KeyGenerationDialog shell = new KeyGenerationDialog(display);
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Create contents of the shell.
     */
    protected void createContents() {
        setText("Generate New Key");
        setSize(642, 200);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
