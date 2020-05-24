package ac.adproj.mchat.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

public class ServerConfigurationDialog extends Shell {
    private Text text;

    /**
     * Launch the application.
     * @param args
     */
    public static void main(String args[]) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the shell.
     * @param display
     */
    public ServerConfigurationDialog(Display display) {
        super(display, SWT.CLOSE | SWT.TITLE);
        setLayout(new FormLayout());
        
        Label lblNewLabel = new Label(this, SWT.NONE);
        FormData fd_lblNewLabel = new FormData();
        fd_lblNewLabel.top = new FormAttachment(0, 37);
        fd_lblNewLabel.left = new FormAttachment(0, 10);
        lblNewLabel.setLayoutData(fd_lblNewLabel);
        lblNewLabel.setText("New Label");
        
        text = new Text(this, SWT.BORDER);
        FormData fd_text = new FormData();
        fd_text.top = new FormAttachment(lblNewLabel, -3, SWT.TOP);
        fd_text.right = new FormAttachment(100, -10);
        fd_text.left = new FormAttachment(0, 114);
        text.setLayoutData(fd_text);
        
        Button ok = new Button(this, SWT.NONE);
        FormData fd_ok = new FormData();
        fd_ok.bottom = new FormAttachment(100, -10);
        fd_ok.left = new FormAttachment(0, 161);
        ok.setLayoutData(fd_ok);
        ok.setText("\u786E\u5B9A");
        
        Button cancel = new Button(this, SWT.NONE);
        fd_ok.right = new FormAttachment(100, -354);
        FormData fd_cancel = new FormData();
        fd_cancel.top = new FormAttachment(ok, 0, SWT.TOP);
        fd_cancel.left = new FormAttachment(ok, 85);
        fd_cancel.right = new FormAttachment(100, -148);
        cancel.setLayoutData(fd_cancel);
        cancel.setText("\u53D6\u6D88");
        createContents();
    }

    /**
     * Create contents of the shell.
     */
    protected void createContents() {
        setText("SWT Application");
        setSize(642, 421);

    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

}
