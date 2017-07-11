package org.zaproxy.zap.extension.history;

import java.lang.*;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemHistoryReferenceContainer;

public class PopupMenuField extends PopupMenuItemHistoryReferenceContainer {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(PopupMenuField.class);

    private final ExtensionHistory extension;

    public PopupMenuField(ExtensionHistory extension) {
        super("Field Enumeration");

        this.extension = extension;
    }

    @Override
    public boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
        return (invoker == Invoker.HISTORY_PANEL);
    }

    @Override
    public void performAction(HistoryReference href) {
        try {
            extension.ShowFieldEnumeration(href);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

     public boolean isSafe() {
        return true;
    }
}

