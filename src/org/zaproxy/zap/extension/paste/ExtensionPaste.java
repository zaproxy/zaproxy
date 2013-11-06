package org.zaproxy.zap.extension.paste;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class ExtensionPaste extends ExtensionAdaptor implements ClipboardOwner {
    private org.zaproxy.zap.extension.stdmenus.PopupPasteMenu popupPaste = null;


    private static Logger log = Logger.getLogger(ExtensionPaste.class);

    public ExtensionPaste() {
        super();
        initialize();
    }


    public ExtensionPaste(String name) {
        super(name);
    }


    private void initialize() {
        this.setName("ExtensionPaste");
        this.setOrder(6);
    }


    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        if (getView() != null) {
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuPaste());
        }

    }


    private org.zaproxy.zap.extension.stdmenus.PopupPasteMenu getPopupMenuPaste() {
        if (popupPaste == null) {
            popupPaste = new org.zaproxy.zap.extension.stdmenus.PopupPasteMenu();
            popupPaste.setText(Constant.messages.getString("paste.paste.popup"));
            popupPaste.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    popupPaste.getLastInvoker().setText(popupPaste.getLastInvoker().getText() + getClipboardContents());


                }
            });
        }
        return popupPaste;
    }

    private String getClipboardContents() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);

        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                log.error("Unable to get data from clipboard");
            }
        }

        return "";
    }


    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("paste.desc");
    }

    @Override
    public URL getURL() {
        try {
            return new URL(Constant.ZAP_HOMEPAGE);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable transferable) {

    }
}
