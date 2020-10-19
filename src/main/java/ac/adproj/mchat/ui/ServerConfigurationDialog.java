package ac.adproj.mchat.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 服务器配置窗口。
 * 
 * @author Andy Cheung
 */
public class ServerConfigurationDialog extends Shell {
    private Text keyFile;
    private StatusWrapper status;

    /**
     * 显示对话框。
     * 
     * @return 含有配置信息的包装类对象。
     */
    public static StatusWrapper showDialog() {
        try {
            Display display = Display.getDefault();
            ServerConfigurationDialog shell = new ServerConfigurationDialog(display);
            shell.open();
            shell.layout();
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
            
            return shell.status;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    class StatusWrapper {
        private String keyFile;

        public StatusWrapper(String keyFile) {
            super();
            this.keyFile = keyFile;
        }

        public String getKeyFile() {
            return keyFile;
        }
    }

    /**
     * Create the shell.
     * @param display The display object.
     */
    public ServerConfigurationDialog(Display display) {
        super(display, SWT.CLOSE | SWT.TITLE);
        setLayout(new FormLayout());
        
        Label lblNewLabel = new Label(this, SWT.NONE);
        lblNewLabel.setAlignment(SWT.CENTER);
        FormData fd_lblNewLabel = new FormData();
        fd_lblNewLabel.top = new FormAttachment(0, 37);
        lblNewLabel.setLayoutData(fd_lblNewLabel);
        lblNewLabel.setText("\u5BC6    \u94A5");
        
        keyFile = new Text(this, SWT.BORDER);
        fd_lblNewLabel.right = new FormAttachment(keyFile, -22);
        FormData fd_keyFile = new FormData();
        fd_keyFile.top = new FormAttachment(0, 34);
        fd_keyFile.right = new FormAttachment(100, -185);
        fd_keyFile.left = new FormAttachment(0, 114);
        keyFile.setLayoutData(fd_keyFile);
        
        Button ok = new Button(this, SWT.NONE);
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                status = new StatusWrapper(keyFile.getText());
                ServerConfigurationDialog.this.setVisible(false);
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
                status = new StatusWrapper(null);
                ServerConfigurationDialog.this.setVisible(false);
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
        
        Button btnNewButton = new Button(this, SWT.NONE);
        btnNewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                KeyGenerationDialog.showDialog();
            }
        });
        FormData fd_btnNewButton = new FormData();
        fd_btnNewButton.bottom = new FormAttachment(0, 68);
        fd_btnNewButton.left = new FormAttachment(keyFile, 6);
        fd_btnNewButton.top = new FormAttachment(0, 34);
        btnNewButton.setLayoutData(fd_btnNewButton);
        btnNewButton.setText("\u751F\u6210");
        
        Button browse = new Button(this, SWT.NONE);
        browse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String path = CommonDialogs.chooseFileDialog();
                keyFile.setText(path == null ? "" : path);
            }
        });
        fd_btnNewButton.right = new FormAttachment(100, -94);
        FormData fd_browse = new FormData();
        fd_browse.bottom = new FormAttachment(0, 68);
        fd_browse.left = new FormAttachment(btnNewButton, 6);
        fd_browse.top = new FormAttachment(0, 34);
        browse.setLayoutData(fd_browse);
        browse.setText("\u6D4F\u89C8\u2026\u2026");
        createContents();
    }

    /**
     * Create contents of the shell.
     */
    protected void createContents() {
        setText("Server Configuration");
        setSize(642, 216);

    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
