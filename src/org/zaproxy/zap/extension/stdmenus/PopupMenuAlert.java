/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.stdmenus;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemHistoryReferenceContainer;


public class PopupMenuAlert extends PopupMenuItemHistoryReferenceContainer {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(PopupMenuAlert.class);

    private ExtensionHistory extension = null;

    /**
     * @param label
     */
    public PopupMenuAlert(String label) {
        super(label);
    }
	
	@Override
	public void performAction(HistoryReference href) {
	    Invoker invoker = getInvoker();
	    if (invoker == Invoker.ACTIVE_SCANNER_PANEL) {
	        try {
	            getExtensionHistory().showAlertAddDialog(href.getHttpMessage(), HistoryReference.TYPE_SCANNER);
	        } catch (HttpMalformedHeaderException | DatabaseException e) {
	            logger.error(e.getMessage(), e);
	        }
	    } else if (invoker == Invoker.FUZZER_PANEL) {
	        try {
	            getExtensionHistory().showAlertAddDialog(href.getHttpMessage(), HistoryReference.TYPE_FUZZER);
    	    } catch (HttpMalformedHeaderException | DatabaseException e) {
                logger.error(e.getMessage(), e);
            }
	    } else {
	        getExtensionHistory().showAlertAddDialog(href);
	    }
	}

    private ExtensionHistory getExtensionHistory() {
    	if (extension == null) {
    		extension = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME);
    	}
    	return extension;
    }

	@Override
	public boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
		if (getExtensionHistory() == null) {
			return false;
		}
		switch (invoker) {
		case ALERTS_PANEL:
			return false;
		case SITES_PANEL:
		case HISTORY_PANEL:
		case ACTIVE_SCANNER_PANEL:
		case SEARCH_PANEL:
		case FUZZER_PANEL:
		case FORCED_BROWSE_PANEL:
		default:
			return true;
		}
	}

	@Override
    public boolean isButtonEnabledForHistoryReference (HistoryReference href) {
        if (href != null) {
            switch (getInvoker()) {
            case ACTIVE_SCANNER_PANEL:
            case FUZZER_PANEL:
                return true;
            default:
                return (href.getHistoryType() != HistoryReference.TYPE_TEMPORARY);
            }
        }
        return false;
    }
	
    @Override
    public boolean isSafe() {
    	return true;
    }
}
