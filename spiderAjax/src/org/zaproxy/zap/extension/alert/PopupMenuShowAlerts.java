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

import org.apache.commons.httpclient.URI;
import org.apache.commons.lang.StringEscapeUtils;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.PopupMenuHistoryReference;

public class PopupMenuShowAlerts extends PopupMenuHistoryReference {

	private static final long serialVersionUID = 1L;

    private List<PopupMenuShowAlert> subMenus = new ArrayList<PopupMenuShowAlert>();
    
    private String parentName = null;

    /**
     * @param label
     */
    public PopupMenuShowAlerts(String label) {
        super("No Alerts for this node"); // Note that this will never be shown in the UI, so doesnt need to be i18n
        this.parentName = label;
    }

	@Override
	public boolean isEnableForInvoker(Invoker invoker) {
		switch (invoker) {
		case sites:
		case history:
			return true;
		}
		return false;
	}

	@Override
	public void performAction(HistoryReference href) throws Exception {
		
	}

	@Override
    public boolean isEnabledForHistoryReference (HistoryReference href) {
    	// Delete all submenus - these (probably) refer to a previous node
		for (PopupMenuShowAlert menu : subMenus) {
			View.getSingleton().getPopupMenu().removeMenu(menu);
			
		}
		subMenus.clear();
    	 
		if (href != null) {
			URI hrefURI = null;
			List<Alert> alerts = href.getAlerts();
			if (href.getSiteNode() != null) {
				alerts = href.getSiteNode().getAlerts();
			}
			try {
				hrefURI = href.getHttpMessage().getRequestHeader().getURI();
			} catch (Exception e) {
				// Ignore
			}
			List<PopupMenuShowAlert> alertList = new ArrayList<PopupMenuShowAlert>(); 
			for (Alert alert : alerts) {
				// Just show ones for this node
				if (alert.getMessage() != null) {
					if ( ! alert.getMessage().getRequestHeader().getURI().equals(hrefURI)) {
						continue;
					} else if (hrefURI != null && ! alert.getUri().equals(hrefURI.toString())) {
						continue;
					}
				}
		    	StringBuilder sb = new StringBuilder();
		    	sb.append("<html><body>");
	    		sb.append("&nbsp;<img src=\"");
	    		sb.append(alert.getIconUrl());
	    		sb.append("\">&nbsp;");
		    	sb.append(StringEscapeUtils.escapeHtml(alert.getAlert()));
		    	sb.append("</body></html>");
		    	
		    	alertList.add(new PopupMenuShowAlert(sb.toString(), alert, this.parentName));
			}
			Collections.sort(alertList);
			
			for (PopupMenuShowAlert pmsa : alertList) {
				View.getSingleton().getPopupMenu().addMenu(pmsa);
				this.add(pmsa);
				this.subMenus.add(pmsa);
			}
		}
		return false;
    }

    @Override
    public String getParentMenuName() {
    	return this.parentName;
    }
   
    @Override
    public boolean isSubMenu() {
    	return true;
    }

    @Override
    public boolean isDummyItem () {
    	return true;
    }

}
