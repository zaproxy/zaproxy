


package org.zaproxy.zap.extension.history;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemSiteNodeContainer;

import javax.swing.JTree;
import java.awt.Component;

@SuppressWarnings("serial")
public class PopupMenuSiteNote extends PopupMenuItemSiteNodeContainer {

    private static final long serialVersionUID = -5692544221103745600L;

    private static final Logger LOGGER = LogManager.getLogger(PopupMenuNote.class);

    private final ExtensionHistory extension;

    public PopupMenuSiteNote(ExtensionHistory extension) {
        super(Constant.messages.getString("history.note.popup"));

        this.extension = extension;
    }


    @Override
    protected boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
        return invoker == Invoker.SITES_PANEL;
    }

    @Override
    public void performAction(SiteNode siteNode) {
        System.out.println(siteNode.getNodeName());
        try {
            extension.showSiteNotesAddDialog(siteNode);

        } catch (Error  e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    @Override
    public boolean isSafe() {
        return true;
    }
}

