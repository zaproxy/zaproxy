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
package org.zaproxy.zap.extension.alert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.ExtensionPopupMenuComponent;
import org.zaproxy.zap.view.popup.PopupMenuHistoryReferenceContainer;

public class PopupMenuShowAlerts extends PopupMenuHistoryReferenceContainer {

	private static final long serialVersionUID = 1L;

    /**
     * @param label
     */
    public PopupMenuShowAlerts(String label) {
        super(label);
        setProcessExtensionPopupChildren(false);
    }

	@Override
	public boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
		switch (invoker) {
		case SITES_PANEL:
		case SPIDER_PANEL:
		case HISTORY_PANEL:
			return true;
		default:
			return false;
		}
	}

	@Override
    public boolean isButtonEnabledForHistoryReference (HistoryReference href) {
		List<Alert> alerts;
		if (href.getSiteNode() != null) {
			alerts = href.getSiteNode().getAlerts();
		} else {
			alerts = href.getAlerts();
		}
		URI hrefURI = href.getURI();
		List<PopupMenuShowAlert> alertList = new ArrayList<>(alerts.size()); 
		for (Alert alert : alerts) {
			// Just show ones for this node
			if (hrefURI != null && ! alert.getUri().equals(hrefURI.toString())) {
				continue;
			}
			final PopupMenuShowAlert menuItem = new PopupMenuShowAlert(alert.getName(), alert);
			menuItem.setIcon(new ImageIcon(alert.getIconUrl()));
			
			alertList.add(menuItem);
		}
		Collections.sort(alertList);
		
		for (PopupMenuShowAlert pmsa : alertList) {
			this.add(pmsa);
		}
		
		return (alertList.size() > 0);
    }

	@Override
	public void dismissed(ExtensionPopupMenuComponent selectedMenuComponent) {
		if (getMenuComponentCount() > 0) {
			removeAll();
		}
	}

    @Override
    public boolean isSafe() {
        return true;
    }

}
