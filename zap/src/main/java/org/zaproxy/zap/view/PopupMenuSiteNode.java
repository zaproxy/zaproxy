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
package org.zaproxy.zap.view;

import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.view.popup.PopupMenuItemSiteNodeContainer;

/**
 * @deprecated (2.3.0) Superseded by {@link PopupMenuItemSiteNodeContainer}. It will be removed in a future release.
 */
@Deprecated
public abstract class PopupMenuSiteNode extends PopupMenuHistoryReference {

	public PopupMenuSiteNode(String label) {
		super(label);
	}

	public PopupMenuSiteNode(String label, boolean multiSelect) {
		super(label, multiSelect);
	}

	private static final long serialVersionUID = 1L;

    private SiteNode getSiteNode (HistoryReference href) {
    	if (href == null) {
    		return null;
    	}
		SiteNode sn = href.getSiteNode();
		if (sn == null) {
			sn = Model.getSingleton().getSession().getSiteTree().getSiteNode(href.getHistoryId());
		}
		return sn;
    }

    @Override
    public boolean isEnabledForHistoryReference (HistoryReference href) {
		SiteNode sn = this.getSiteNode(href);
		if (sn != null) {
			return this.isEnabledForSiteNode(sn);
		}
		return false;
    }
    
    /**
	 * @param sn
	 */
    public boolean isEnabledForSiteNode (SiteNode sn) {
    	// Can Override if required 
    	return true;
    }
    
    @Override
    public void performAction (HistoryReference href) throws Exception {
		SiteNode sn = this.getSiteNode(href);
		if (sn != null) {
			this.performAction(sn);
		}
    }

    public abstract void performAction (SiteNode sn) throws Exception;

}