package org.zaproxy.zap.extension.stdmenus;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;

import javax.swing.text.JTextComponent;
import java.awt.*;

public class PopupPasteMenu extends ExtensionPopupMenuItem {
    private static final long serialVersionUID = 1L;
    private JTextComponent lastInvoker = null;


    public JTextComponent getLastInvoker() {
        return lastInvoker;
    }


    public PopupPasteMenu() {
        super();
        initialize();
    }

    private void initialize() {
        this.setText(Constant.messages.getString("paste.paste.popup"));
    }

    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (invoker instanceof JTextComponent && !(invoker instanceof RSyntaxTextArea)) {
            this.lastInvoker = (JTextComponent) invoker;
            this.setEnabled(((JTextComponent) invoker).isEditable());

            return true;
        }

        this.lastInvoker = null;
        return false;
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}
