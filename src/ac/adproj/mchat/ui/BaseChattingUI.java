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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 基本聊天用户界面。
 * 
 * @author Andy Cheung
 * @implNote 本用户界面使用 SWT 实现
 */
public abstract class BaseChattingUI extends Shell {
    protected Text messageToSend;
    protected Text messageDisplay;
    protected Button logoff;
    protected Button send;
    
    private boolean hasReceivedMessage;
    
    /**
     * 追加消息到消息显示框。
     * 
     * @param message 消息
     */
    public void appendMessageDisplay(String message) {
        if (hasReceivedMessage) {
            messageDisplay.setText(messageDisplay.getText() + "\r\n" + message);
        } else {
            messageDisplay.setText(message);
            hasReceivedMessage = true;
        }
    }

    /**
     * Create the shell.
     * @param display The display object.
     */
    public BaseChattingUI(Display display) {
        super(display, SWT.SHELL_TRIM);
        setLayout(new GridLayout(4, false));
        createContents();
    }
    
    public BaseChattingUI() {
        this(new Display());
    }

    /**
     * Create contents of the shell.
     */
    protected void createContents() {
        
        messageDisplay = new Text(this, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
        GridData gd_messageDisplay = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
        gd_messageDisplay.heightHint = 364;
        messageDisplay.setLayoutData(gd_messageDisplay);
        
        messageToSend = new Text(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
        GridData gd_messageToSend = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
        gd_messageToSend.heightHint = 103;
        messageToSend.setLayoutData(gd_messageToSend);
        
        logoff = new Button(this, SWT.NONE);
        logoff.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1));
        logoff.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleLogoff();
            }
        });
        logoff.setText("  \u6CE8\u9500  ");
        
        setText("\u591A\u7AEF\u804A\u5929\u7A0B\u5E8F");
        setSize(870, 647);
        
        send = new Button(this, SWT.NONE);
        send.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1));
        send.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (messageToSend.getText().trim().isEmpty()) {
                    return;
                }
                
                handleSendMessage(messageToSend.getText());
                messageToSend.setText("");
            }
        });
        send.setText("  \u53D1\u9001  ");
    }
    
    /**
     * 按下“发送”按键的处理方法
     * 
     * @param text 要发送的消息
     */
    protected abstract void handleSendMessage(String text);
    
    /**
     * 按下“注销”按键的处理方法
     */
    protected abstract void handleLogoff();

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
